# 开放世界割草游戏 — 优化实施计划

## Context

基于现有 `SurvivalGame`（类吸血鬼幸存者玩法，19 个 Java 源文件，60fps Java2D 游戏），在保持现有武器/敌人/升级系统的基础上，进行三个方向的重大优化：

1. **开放大地图 + 障碍物系统** — 从固定 880×620 竞技场升级为 3600×2400 开放世界，加入摄像机跟踪和障碍物地形
2. **像素风格角色 + 阶段进化** — 几何图形渲染替换为像素精灵，Lv25/50/75 触发视觉和技能质变
3. **随机武器掉落** — 在升级获取之外增加击杀掉落武器的途径，引入稀有度系统

所有优化基于现有 `GameEngine.java` 框架（Java2D 封装，不引入外部库），遵循现有 Entity 继承体系和数据驱动设计模式。

---

## 1. 优化后架构总览

### 新增文件（10 个）

```
src/
  Camera.java                -- 摄像机系统，跟踪玩家，计算视口偏移
  Obstacle.java              -- 障碍物实体（继承 Entity）
  GameMap.java               -- 开放世界地图（障碍物布局/生成/渲染）
  SpriteManager.java         -- 精灵表加载/裁切/渲染管理（PNG方案+代码兜底）
  PixelFont.java             -- 5×7 点阵像素字体渲染器
  CharacterProgression.java  -- 角色阶段进化（Tier判定、属性加成、大招）
  UltimateSkill.java         -- 大招定义（旋风斩/全屏雷/影分身）
  WeaponDrop.java            -- 武器掉落实体（继承 Pickup）
  WeaponRarity.java          -- 武器稀有度枚举+掉落表
  AssetLoader.java           -- 素材加载器，尝试加载PNG，失败则用代码兜底
```

### 修改文件（14 个）

```
  SurvivalGame.java          -- 新增 Camera/GameMap/CharacterProgression 子系统，修改状态机
  GameConfig.java            -- 新增世界尺寸、摄像机、掉落率、Tier 等常量
  Entity.java                -- 新增 width/height 字段支持矩形碰撞（障碍物）
  Player.java                -- 移除 clampToArena()，接入 Camera，SpriteManager 渲染，Tier
  Enemy.java                 -- 新增障碍物绕行AI，Boss增强，SpriteManager 渲染
  EnemySpawner.java          -- 改为摄像机视口外生成
  CollisionSystem.java       -- 新增障碍物碰撞（Player/Enemy/Projectile vs Obstacle）
  SpatialGrid.java           -- 改为支持全地图尺寸的动态网格
  WeaponDef.java             -- 新增稀有度字段、进化分支武器
  WeaponManager.java         -- 新增 addWeapon/replaceWeapon/swapWeapon 完整操作
  Pickup.java                -- 增加类型区分（经验/武器/生命）
  UpgradeSystem.java         -- 升级池加入武器掉落卡，Tier相关升级
  VfxManager.java            -- beginCamera/endCamera 整合 Camera 偏移
  CharacterDef.java          -- 新增精灵引用、大招类型、Tier属性加成表
```

---

## 2. Phase 1：摄像机系统 + 开放世界

### 目标
将视口从固定竞技场解耦，实现玩家居中（或偏移动态）的摄像机跟踪，为大地图打下基础。

### 2.1 Camera.java（新建）

**技术方案**：在现有 `VfxManager.beginCamera/endCamera` 的 `saveCurrentTransform/translate/restoreLastTransform` 机制上叠加摄像机偏移。

```java
public class Camera {
    double worldWidth, worldHeight;    // 世界总尺寸
    double viewWidth, viewHeight;      // 视口尺寸（窗口大小）
    double x, y;                       // 摄像机左上角在世界中的坐标

    // 每帧调用：让摄像机跟踪 targetX, targetY（玩家位置），保持在视口中央
    void follow(double targetX, double targetY, double dt);

    // 返回世界坐标 → 屏幕坐标的转换
    double screenX(double worldX);  // = worldX - x
    double screenY(double worldY);  // = worldY - y

    // 判断世界坐标矩形是否在视口内（用于视口裁剪）
    boolean isVisible(double worldX, double worldY, double margin);
}
```

**关键细节**：
- 摄像机跟随使用线性插值（lerp），`x += (targetX - viewWidth/2 - x) * 5 * dt`，产生平滑跟随而非硬锁定
- 摄像机边界限制：`clamp(0, worldWidth - viewWidth)`，防止显示世界外的黑边
- `isVisible()` 用于渲染时跳过屏幕外的实体，减少 `drawSolidCircle` 等调用

### 2.2 GameConfig.java 修改

```java
// === 删除旧常量 ===
// ARENA_X, ARENA_Y, ARENA_WIDTH, ARENA_HEIGHT  → 被世界尺寸替代

// === 新增常量 ===
public static final int WORLD_WIDTH = 3600;     // 约窗口宽度的 3.75 倍
public static final int WORLD_HEIGHT = 2400;    // 约窗口高度的 3.3 倍
public static final int VIEW_WIDTH = 960;       // = WINDOW_WIDTH（保持不变）
public static final int VIEW_HEIGHT = 720;      // = WINDOW_HEIGHT（保持不变）
public static final double CAMERA_LERP_SPEED = 5.0;  // 摄像机平滑跟随速度
```

### 2.3 SurvivalGame.java 修改点

| 行号 | 现有代码 | 改为 |
|------|---------|------|
| 27 | `public VfxManager vfx;` | 下方新增 `public Camera camera;` |
| 28 | — | 新增 `public GameMap gameMap;` |
| 50 | `vfx = new VfxManager(this);` | 下方新增 `camera = new Camera(GameConfig.WORLD_WIDTH, ...);` |
| 77 | `player.update(dt);` | 下方新增 `camera.follow(player.x, player.y, dt);` |
| 84 | `player.clampToArena();` | **删除此行**（改为 `player.clampToWorld();` 使用世界边界） |
| 138 | `vfx.beginCamera(this);` | 改为整合 camera 偏移的版本 |

**renderWorld() 重写要点**：
```java
private void renderWorld() {
    vfx.beginCamera(this);       // 内部叠加 camera.x, camera.y + shake 偏移
    // 绘制地图背景（tile或暗色底色）
    gameMap.render(this, camera);
    // 视口裁剪 + 渲染实体
    for (Pickup p : pickups) if (camera.isVisible(p.x, p.y, 20)) p.render(this);
    for (Projectile p : projectiles) if (camera.isVisible(p.x, p.y, 20)) p.render(this);
    for (Enemy e : enemies) if (camera.isVisible(e.x, e.y, 40)) e.render(this);
    if (player != null) player.render(this);
    vfx.render(this);
    vfx.endCamera(this);
    renderHUD();  // HUD 在屏幕空间，不受摄像机影响
}
```

**VfxManager.beginCamera/endCamera 修改**：
```java
public void beginCamera(GameEngine g, Camera camera) {
    g.saveCurrentTransform();
    // 先应用摄像机偏移
    g.translate(-camera.x, -camera.y);
    // 再叠加屏幕震动
    if (shakeIntensity > 0.01) {
        double ox = (game.rand(2.0) - 1.0) * shakeIntensity;
        double oy = (game.rand(2.0) - 1.0) * shakeIntensity;
        g.translate(ox, oy);
    }
}
```

### 2.4 Player.java 修改

| 行号 | 现有代码 | 改为 |
|------|---------|------|
| 71-74 | `clampToArena()` | 改为 `clampToWorld()` — 使用 `GameConfig.WORLD_WIDTH/HEIGHT` 替代 `ARENA_*` |

### 2.5 SpatialGrid.java 修改

当前构造参数为 `(arenaX, arenaY, width, height, cellSize)` — 竞技场坐标系。改为：
```java
public SpatialGrid(double worldWidth, double worldHeight, int cellSize)
// 内部 arenaX=0, arenaY=0, 覆盖整个世界
// cellSize 改为 200（世界大了需要更大的格）
```

### 2.6 EnemySpawner.java 修改

`spawnAtEdge()` 方法（行 61-91）当前使用 `GameConfig.ARENA_*` 边界生成。改为基于摄像机视口：
```java
private void spawnAtEdge(int type) {
    // 在摄像机视口外、世界边界内随机生成
    // 四边：camera.x - 100 ~ camera.x - 60  (左边)
    //       camera.x + viewWidth + 60 ~ camera.x + viewWidth + 100 (右边)
    //       ...同理上下
    // 确保距离玩家至少 400px
}
```

### 验证标准
- 运行游戏，玩家 WASD 移动，摄像机平滑跟随
- 在 3600×2400 世界中移动到边缘，摄像机正确停止
- 屏幕外敌人不会被渲染（可通过打印计数验证）
- 敌人在摄像机视口外生成，能正确追踪进入视口

---

## 3. Phase 2：障碍物系统

### 目标
在世界中放置障碍物，阻挡玩家和敌人的移动，弹幕碰到障碍物消失，让玩家可以利用地形走位。

### 3.1 Obstacle.java（新建）

```java
public class Obstacle extends Entity {
    // 障碍物使用矩形碰撞（非圆形）
    double width, height;           // 矩形尺寸
    int type;                       // ROCK=0, TREE=1, WALL=2, BUSH=3

    // ROCK:  40~80px 不规则多边形
    // TREE:  24×24，树干+树冠
    // WALL:  宽矩形，古代遗迹墙壁
    // BUSH:  20×20，只阻挡玩家（敌人可穿过）

    // 碰撞检测使用 AABB vs Circle（而非 Circle vs Circle）
    boolean collidesWith(double cx, double cy, double cr);
}
```

**碰撞算法（AABB vs Circle）**：
```java
boolean collidesWith(double cx, double cy, double cr) {
    double closestX = Math.max(x, Math.min(cx, x + width));
    double closestY = Math.max(y, Math.min(cy, y + height));
    double dx = cx - closestX;
    double dy = cy - closestY;
    return (dx * dx + dy * dy) < (cr * cr);
}
```

### 3.2 GameMap.java（新建）

```java
public class GameMap {
    ArrayList<Obstacle> obstacles = new ArrayList<>();

    // 通过预设模板生成地图
    void generate();                    // 放置障碍物，确保连通性
    void render(GameEngine g, Camera c);  // 仅渲染视口内的障碍物
    ArrayList<Obstacle> getNearby(double x, double y, double range);
}
```

**地图布局设计**（手动预设 + 随机点缀）：

世界 3600×2400，分为 3×2 共 6 个区域，每区域 1200×1200：
- **左上（森林区）**：密集树木 + 灌木，狭窄通道
- **中上（遗迹区）**：长条形墙壁形成走廊，少量岩石
- **右上（平原区）**：稀疏岩石，开阔空间
- **左下（沼泽区）**：灌木丛限制玩家移动，敌人可穿行
- **中下（废墟区）**：墙壁碎片 + 岩石，环形竞技场结构
- **右下（矿区）**：大量岩石形成迷宫式通道

障碍物总数量控制在 80~120 个，分布密度有节奏感（密集区→开阔区交替）。

### 3.3 CollisionSystem.java 修改

**新增 3 个碰撞通道**：

```java
// 在 update(dt) 中新增：
obstacleVsPlayer(player);
obstacleVsEnemies();
obstacleVsProjectiles();
```

**Player vs Obstacle**（推动分离）：
```java
private void obstacleVsPlayer(Player p) {
    for (Obstacle ob : game.gameMap.getAllObstacles()) {
        if (ob.collidesWith(p.x, p.y, p.radius)) {
            // 计算最短分离向量，推出玩家
            double overlap = ...;  // 重叠量
            // 沿分离方向推出
            p.x += pushX * overlap;
            p.y += pushY * overlap;
        }
    }
}
```

**Enemy vs Obstacle**（滑动绕行）：
碰撞时沿障碍物边缘滑动而非完全停止 — 计算切线方向，将速度投影到切线。
```java
private void obstacleVsEnemies() {
    for (Enemy e : game.enemies) {
        if (!e.alive) continue;
        for (Obstacle ob : game.gameMap.getNearby(e.x, e.y, 100)) {
            if (ob.collidesWith(e.x, e.y, e.radius)) {
                // 推出 + 速度方向投影到切线
                e.x += pushX;
                e.y += pushY;
                // 投影速度到障碍物切线方向
                double dot = e.vx * tangentX + e.vy * tangentY;
                e.vx = tangentX * dot * 0.7;  // 0.7 = 绕行速度衰减
                e.vy = tangentY * dot * 0.7;
            }
        }
    }
}
```

**Projectile vs Obstacle**：
投射物碰到障碍物直接标记 `alive = false`（穿透弹减少 pierce 计数）。

### 3.4 Entity.java 修改

新增 `width`、`height` 字段（仅 Obstacle 使用），`Entity` 构造器提供默认 0 值。`render()` 保持圆形渲染逻辑不变，`Obstacle` 重写 `render()` 绘制矩形/多边形。

### 验证标准
- 玩家撞到岩石/墙壁被挡住，沿边缘滑动可绕过
- 敌人遇到障碍物自动绕行，不会卡死在障碍物上
- 投射物撞到障碍物消失
- 玩家可以利用狭窄通道卡住大批敌人

---

## 4. Phase 3：精灵表加载系统（PNG 方案 + 代码兜底）

### 目标
将所有角色/敌人/投射物从几何圆形替换为 PNG 精灵表渲染。素材按 `asset-spec.md` 规格从 itch.io / OpenGameArt 获取。素材缺失时自动用代码兜底绘制，保证程序始终可运行。

### 核心设计原则

**三层降级策略**：
```
第 1 层：尝试加载 PNG 精灵表 → 成功则使用 BufferedImage 渲染（最佳效果）
第 2 层：PNG 不存在 → 使用代码绘制像素方块替代（保证运行）
第 3 层：单个帧缺失 → 用同动画的相邻帧补位
```

### 4.1 AssetLoader.java（新建）— 统一素材入口

```java
public class AssetLoader {
    // 素材根目录
    static final String ASSET_DIR = "assets/sprites/";

    // 所有素材路径常量（与 asset-spec.md 对应）
    static final String CHAR_WARRIOR  = "character_warrior.png";
    static final String CHAR_MAGE     = "character_mage.png";
    static final String CHAR_ASSASSIN = "character_assassin.png";
    static final String ENEMY_SLIME   = "enemy_slime.png";
    static final String ENEMY_BAT     = "enemy_bat.png";
    static final String ENEMY_SKELETON = "enemy_skeleton.png";
    static final String ENEMY_GIANT   = "enemy_giant.png";
    static final String ENEMY_GHOST   = "enemy_ghost.png";
    static final String ENEMY_BOSS    = "enemy_boss.png";
    static final String PROJECTILES   = "weapons_projectiles.png";
    static final String PICKUPS       = "pickups.png";
    static final String OBSTACLES     = "obstacles.png";
    static final String UI_ELEMENTS   = "ui_elements.png";
    static final String EFFECTS       = "effects.png";

    // 加载状态缓存 (key = 文件路径, value = BufferedImage 或 null 表示缺失)
    static HashMap<String, BufferedImage> cache = new HashMap<>();
    static HashMap<String, Boolean> missing = new HashMap<>();

    /**
     * 尝试加载 PNG，失败返回 null 并标记缺失。
     * loadImage() 是 GameEngine 的方法。
     */
    static BufferedImage tryLoad(GameEngine g, String filename);

    /**
     * 判断某素材是否可用。
     */
    static boolean hasAsset(String filename);

    /**
     * 从精灵表裁切单帧。
     * subImage() 是 GameEngine 的方法。
     */
    static BufferedImage getFrame(GameEngine g, String filename,
                                   int row, int col,
                                   int frameWidth, int frameHeight);
}
```

### 4.2 SpriteManager.java（新建）— 动画管理与渲染

**职责**：管理每个实体的动画状态（当前动画类型、当前帧、帧计时），从 AssetLoader 获取帧图并渲染到屏幕。

```java
public class SpriteManager {
    // 动画类型常量
    public static final int ANIM_IDLE  = 0;
    public static final int ANIM_WALK  = 1;
    public static final int ANIM_HURT  = 2;
    public static final int ANIM_DEATH = 3;
    public static final int ANIM_ATTACK = 4;

    // 方向常量 (与精灵表行号对应)
    public static final int DIR_DOWN  = 0;
    public static final int DIR_LEFT  = 1;
    public static final int DIR_RIGHT = 2;
    public static final int DIR_UP    = 3;

    // === 精灵表元数据（所有规格与 asset-spec.md 一致）===

    /**
     * 角色精灵表布局：256×384，10行 × 6列
     *   行0: Down Idle(4帧)   行1: Down Walk(6帧)
     *   行2: Left Idle(4帧)   行3: Left Walk(6帧)
     *   行4: Right Idle(4帧)  行5: Right Walk(6帧)
     *   行6: Up Idle(4帧)     行7: Up Walk(6帧)
     *   行8: Hurt(2帧)        行9: Death(4帧)
     *   帧尺寸: 32×32 px
     *   animRow[动画类型][方向] → 行索引
     *   frameCount[动画类型] → 帧数
     */
    static final int CHAR_FRAME_SIZE = 32;
    static final int CHAR_FRAMES_PER_ROW = 6;

    // animRow[ANIM][DIR] = 精灵表行号
    static final int[][] CHAR_ANIM_ROW = {
        // DIR_DOWN, DIR_LEFT, DIR_RIGHT, DIR_UP
        {0, 2, 4, 6},  // ANIM_IDLE
        {1, 3, 5, 7},  // ANIM_WALK
        {8, 8, 8, 8},  // ANIM_HURT (只有1行，不分方向)
        {9, 9, 9, 9},  // ANIM_DEATH
    };
    static final int[] CHAR_FRAME_COUNT = {4, 6, 2, 4};  // IDLE/WALK/HURT/DEATH

    /**
     * 敌人精灵表布局：128×160，4行
     *   行0: Idle/Float(4帧)  行1: Move/Chase(4帧)
     *   行2: Hurt(2帧)        行3: Death(3帧)
     *   帧尺寸: 32×32 (Boss 64×64, Giant 48×48)
     */
    static final int ENEMY_FRAME_SIZE = 32;

    // === 核心方法 ===

    /** 初始化：预加载所有素材，失败的标记为代码兜底 */
    static void init(GameEngine g);

    /**
     * 判断某个精灵是否使用 PNG 渲染。
     * 返回 false 时调用者用代码兜底绘制。
     */
    static boolean hasSpriteFor(String assetKey);

    /**
     * 渲染实体精灵
     * @param g         GameEngine
     * @param assetKey  素材文件名
     * @param anim      动画类型 (ANIM_IDLE etc.)
     * @param direction 朝向 (DIR_DOWN etc.)，HURT/DEATH 传0
     * @param animTimer 动画计时器（秒）
     * @param cx, cy    屏幕绘制中心坐标
     * @param scale     缩放倍数（1.0=32px, 1.5=48px for Giant, 2.0=64px for Boss）
     */
    static void drawEntity(GameEngine g, String assetKey, int anim, int direction,
                           double animTimer, double cx, double cy, double scale);

    /**
     * 代码兜底：画像素风格几何体（素材缺失时自动调用）
     */
    static void drawFallback(GameEngine g, String entityType, Color color,
                             double cx, double cy, double radius, int anim, double animTimer);
}
```

### 4.3 代码兜底渲染（素材缺失时的自动降级）

每种实体都有一个 `drawFallback_xxx()` 方法，用 `drawSolidRectangle` 拼出**有像素味的图形**（不是光滑圆形）：

```java
// 战士兜底：蓝色锯齿"像素圆" + 方向指示方块
static void drawFallbackWarrior(GameEngine g, double cx, double cy,
                                 double r, int dir, int anim, double timer) {
    // 身体 — 用阶梯圆替代光滑圆（像素味）
    g.changeColor(new Color(80, 150, 250));
    drawPixelCircle(g, cx, cy, r, 3);  // 3px 方块拼圆

    // 眼睛 — 两个白色小方块
    g.changeColor(Color.WHITE);
    g.drawSolidRectangle(cx - 5, cy - 6, 4, 4);
    g.drawSolidRectangle(cx + 1, cy - 6, 4, 4);

    // 武器 — 方向指示
    double wx = cx + Math.cos(getDirAngle(dir)) * 18;
    double wy = cy + Math.sin(getDirAngle(dir)) * 18;
    g.drawSolidRectangle(wx - 2, wy - 2, 5, 5);

    // Walk 动画：身体上下弹跳
    if (anim == ANIM_WALK) {
        double bounce = Math.sin(timer * 10) * 2;  // ±2px 弹跳
        cy += bounce;
    }
}

// 史莱姆兜底：压扁弹跳效果
static void drawFallbackSlime(GameEngine g, double cx, double cy, double r, int anim, double timer) {
    g.changeColor(new Color(80, 210, 80));
    if (anim == ANIM_WALK) {
        // 弹跳时压扁
        double squash = 1.0 + Math.abs(Math.sin(timer * 6)) * 0.3;
        drawPixelEllipse(g, cx, cy, r * squash, r / squash, 3);
    } else {
        drawPixelCircle(g, cx, cy, r, 3);
    }
    // 眼睛
    g.changeColor(Color.WHITE);
    g.drawSolidRectangle(cx - 5, cy - 4, 4, 4);
    g.drawSolidRectangle(cx + 2, cy - 4, 4, 4);
}
```

**关键技巧**：
- `drawPixelCircle(x, y, r, blockSize)` — 用 blockSize 尺寸的小方块拼出阶梯圆，避免光滑曲线
- `drawPixelEllipse()` — 同理，支持压扁/拉伸效果
- 弹跳/晃动/呼吸 — 用 `sin(timer * freq) * amplitude` 微调位置或尺寸

### 4.4 BufferedImage 预渲染优化

为兜底方案也做预渲染缓存：

```java
// SpriteManager 初始化时：
static void preRenderFallbacks() {
    // 对每种兜底精灵，渲染到 BufferedImage 缓存
    for 每种实体类型:
        BufferedImage img = new BufferedImage(size, size, TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        drawFallback_xxx(g2, ...);  // 画到离屏图像
        g2.dispose();
        fallbackCache.put(key, img);
}

// 运行时直接用 drawImage 而非逐帧重绘 → 性能与 PNG 方案一致
```

### 4.5 Player.java 渲染修改

```java
// Player.render() 改为：
@Override
public void render(GameEngine g) {
    if (!alive) return;

    // 根据移动方向确定朝向
    int dir = getFacingDirection();  // DIR_DOWN/LEFT/RIGHT/UP
    int anim = (vx != 0 || vy != 0) ? SpriteManager.ANIM_WALK : SpriteManager.ANIM_IDLE;

    // 受击闪烁
    if (hitFlashTimer > 0 && hitFlashTimer % 2 == 0) {
        // 用白色叠加或跳帧
    }

    // 计算当前帧(基于 animTimer 和帧数)
    animTimer += dt;  // 在 onUpdate 中累加
    double fps = (anim == ANIM_WALK) ? 10 : 6;  // Walk 10fps, Idle 6fps

    String assetKey = getCharacterAssetKey();  // "character_warrior.png" 等
    SpriteManager.drawEntity(g, assetKey, anim, dir, animTimer, x, y, 1.0);
}
```

### 4.6 Enemy.java 渲染修改

```java
// Enemy.render() 改为：
@Override
public void render(GameEngine g) {
    if (!alive) return;
    int anim = isMoving() ? SpriteManager.ANIM_WALK : SpriteManager.ANIM_IDLE;
    double scale = (type == BOSS) ? 2.0 : (type == GIANT) ? 1.5 : 1.0;
    String assetKey = getEnemyAssetKey(type);  // "enemy_slime.png" 等
    SpriteManager.drawEntity(g, assetKey, anim, 0, age, x, y, scale);
}
```

### 4.7 新增文件 vs 原计划

| 原计划 | 新方案 |
|--------|--------|
| `SpriteSheet.java` (代码手绘像素) | `SpriteManager.java` (PNG加载+动画管理) |
| — | `AssetLoader.java` (素材加载+缓存+降级) |
| `PixelFont.java` | 保留不变 |

### 4.8 素材目录结构

```
Assignment2/Game-test/
  assets/
    sprites/
      character_warrior.png      ← 搜索下载
      character_mage.png          ← 搜索下载
      character_assassin.png      ← 搜索下载
      enemy_slime.png             ← 搜索下载
      enemy_bat.png               ← 搜索下载
      enemy_skeleton.png          ← 搜索下载
      enemy_giant.png             ← 搜索下载
      enemy_ghost.png             ← 搜索下载
      enemy_boss.png              ← 搜索下载
      weapons_projectiles.png     ← 搜索下载
      pickups.png                 ← 搜索下载
      obstacles.png               ← 搜索下载
      ui_elements.png             ← 搜索下载
      effects.png                 ← 搜索下载
```

素材目录最初**完全为空**，游戏启动时 AssetLoader 检测到全部缺失 → 自动使用代码兜底绘制。每放入一张 PNG，对应实体立即切换为精灵渲染，无需修改代码。

### 验证标准
- 素材目录为空时游戏正常运行（代码兜底，像素方块风格）
- 放入任意一张精灵表后，对应实体自动切换为 PNG 渲染
- 角色 4 方向移动时动画切换正确
- 受击闪烁不减帧
- 代码兜底也有"像素感"（阶梯圆、弹跳动画）

---

## 5. Phase 4：角色阶段进化（Tier System）

### 目标
在 Lv25、Lv50、Lv75 触发质的飞跃 — 不仅是属性数值提升，而是外观变化 + 新技能解锁。

### 5.1 CharacterProgression.java（新建）

```java
public class CharacterProgression {
    public static int tierForLevel(int level) {
        if (level >= 75) return 4;   // 传说
        if (level >= 50) return 3;   // 精英
        if (level >= 25) return 2;   // 进阶
        return 1;                    // 初级
    }

    // 检查是否刚跨越 Tier 阈值（level-1 的 tier < level 的 tier）
    public static boolean justCrossedTier(int oldLevel, int newLevel);

    // 获取某 Tier 的属性加成倍率
    public static double tierStatBonus(int tier);  // 1.0, 1.25, 1.75, 2.5
}
```

### 5.2 Tier 详细设计

| Tier | 等级 | 视觉 | 属性加成 | 技能变化 |
|------|------|------|----------|----------|
| **1 初级** | 1-24 | 基础像素精灵，单色 | 基础属性 | 初始武器 + 基础攻击 |
| **2 进阶** | 25-49 | 服装/装备升级，武器轨迹变色 | HP+25%, DMG+25% | 解锁第二被动技能槽（可同时持有2个属性升级buff） |
| **3 精英** | 50-74 | 更大精灵(1.2×)，元素光环粒子 | HP×2, SPD+15% | **大招解锁！** 冷却 30s |
| **4 传说** | 75+ | 全屏粒子环绕，精灵 1.5× 尺寸 | HP×3, DMG×2 | 大招冷却减半(15s)，武器槽+1（7把） |

### 5.3 UltimateSkill.java（新建）

三职业各有一个独特大招，空格键手动触发（新增操作维度）：

```java
public class UltimateSkill {
    String name;
    double cooldown;       // 冷却时间（秒）
    double cooldownTimer;  // 当前冷却计时器
    int type;              // WHIRLWIND=0, THUNDER=1, SHADOW_CLONE=2

    void activate(SurvivalGame game);   // 触发大招效果
    void update(double dt);             // 冷却计时
    boolean isReady();                   // 冷却完毕
}
```

**三职业大招**：

| 职业 | 大招 | 效果 |
|------|------|------|
| **Warrior** | 旋风斩 | 3秒内以玩家为中心生成 8 方向旋转剑气，伤害 50/ tick，半径 120px |
| **Mage** | 天雷降临 | 全屏随机雷击 12 次，每次 60 伤害 + 麻痹（敌人减速 50%，2秒） |
| **Assassin** | 影分身 | 生成 3 个影分身（持续 5 秒），自动攻击最近敌人，继承 50% 伤害 |

### 5.4 UpgradeSystem.java 修改

在 `checkLevelUp()` 中新增 Tier 跨越检测：
```java
private void checkLevelUp() {
    while (xp >= xpForNextLevel()) {
        int oldLevel = level;
        level++;
        xp -= xpForNextLevel();
        if (CharacterProgression.justCrossedTier(oldLevel, level)) {
            // 暂停游戏，显示 "TIER UP!" 动画（类似 BOSS_INTRO）
            game.enterTierUpPause(CharacterProgression.tierForLevel(level));
        }
        // ...
    }
}
```

新增 `STATE_TIER_UP` 状态（类似 `STATE_BOSS_INTRO`，2秒展示 + 粒子特效）。

### 5.5 SurvivalGame.java 修改

| 新增状态 | 值 | 触发条件 | 行为 |
|----------|-----|---------|------|
| `STATE_TIER_UP` | 7 | 升级跨越 25/50/75 | 显示 "TIER UP!" + 粒子特效, 2秒自动恢复 |

**新增大招触发**（输入处理）：
```java
// keyPressed 中：
if (gameState == STATE_PLAYING && code == KeyEvent.VK_SPACE) {
    if (characterProgression != null) {
        characterProgression.tryActivateUltimate();
    }
}
```

### 验证标准
- Lv24→Lv25 时触发 "TIER UP!" 动画，角色外观升级
- Lv50 解锁大招，按空格触发，冷却 30 秒正常倒计时
- Lv75 再次进化，大招冷却变为 15 秒，可持有 7 把武器
- HUD 显示当前 Tier 标志和大招冷却环形图标

---

## 6. Phase 5：随机武器掉落

### 目标
击杀敌人有概率掉落武器宝箱，拾取后获得武器，支持替换/升级已有武器。增加随机性，降低对升级选择的依赖。

### 6.1 WeaponRarity.java（新建）

```java
public enum WeaponRarity {
    COMMON(1.0, new Color(200,200,200), "普通"),     // 白色光柱
    RARE(1.2, new Color(80,160,255), "稀有"),        // 蓝色光柱
    EPIC(1.4, new Color(180,80,255), "史诗"),        // 紫色光柱
    LEGENDARY(1.6, new Color(255,200,50), "传说");   // 金色光柱

    final double damageMultiplier;  // 伤害加成
    final Color beamColor;          // 掉落光柱颜色
    final String label;

    // 掉落权重：COMMON 50%, RARE 30%, EPIC 15%, LEGENDARY 5%
    static WeaponRarity roll() {
        int r = rand(100);
        if (r < 50) return COMMON;
        if (r < 80) return RARE;
        if (r < 95) return EPIC;
        return LEGENDARY;
    }
}
```

### 6.2 WeaponDrop.java（新建，继承 Pickup）

```java
public class WeaponDrop extends Pickup {
    int weaponId;             // 武器ID
    WeaponRarity rarity;      // 稀有度
    int bonusLevels;          // 额外等级（COMMON=0, RARE=1, EPIC=2, LEGENDARY=3）

    @Override
    public void onUpdate(double dt) {
        // 同 Pickup 的磁铁逻辑，但掉落范围更大（200px）
    }

    @Override
    public void render(GameEngine g) {
        // 绘制旋转的武器宝箱精灵 + 稀有度光柱
        // 光柱：从地面向上的渐变光束（4px宽 × 30px高半透明矩形）
    }
}
```

### 6.3 掉落率配置（GameConfig.java 新增）

```java
// 武器掉落率
public static final double WEAPON_DROP_CHANCE_NORMAL = 0.02;   // 普通敌人 2%
public static final double WEAPON_DROP_CHANCE_ELITE = 0.10;    // GIANT 10%
public static final double WEAPON_DROP_CHANCE_BOSS = 1.00;     // Boss 100%
public static final int WEAPON_DROP_MAGNET_RANGE = 200;        // 拾取范围 px
```

### 6.4 Enemy.onDeath() 修改（Enemy.java 行 81-87）

```java
@Override
public void onDeath(Entity killer) {
    game.vfx.spawnDeathBurst(x, y, baseColor);
    // 新增：武器掉落判定
    if (game.rand(1.0) < getWeaponDropChance()) {
        WeaponRarity rarity = WeaponRarity.roll();
        int weaponId = randomWeaponId();
        game.addWeaponDrop(new WeaponDrop(game, x, y, weaponId, rarity));
    } else {
        game.addPickup(new Pickup(game, x, y, xpValue));
    }
    if (type == BOSS) {
        game.onBossKilled();
    }
}
```

### 6.5 WeaponManager.java 新增方法

```java
// 替换武器：移除指定槽位，放入新武器
public boolean replaceWeapon(int slotIndex, int newWeaponId, WeaponRarity rarity);

// 获取武器槽数量（Tier 4 时为 7，否则为 6）
public int maxSlots();

// 武器槽是否已满
public boolean isFull();

// 拾取武器入口（若已有同种则升级，否则先看有没有空槽，满了则弹出替换UI）
public void pickupWeapon(int weaponId, WeaponRarity rarity);
```

### 6.6 武器拾取 UI

当玩家接近武器掉落时：
1. 若有空槽 → 自动拾取，弹出浮动文字 "+ 武器名"
2. 若已有同种武器 → 自动升级（+1 级，最高 5 级）
3. 若槽满且不同种 → 弹出武器选择交换界面（`STATE_WEAPON_SWAP`）
   - 显示当前 6 把武器 + 新武器
   - 点击要替换的武器（或按 1-6 键）
   - 被替换的武器掉落到地上（作为新的 WeaponDrop）

**武器替换UI**（SurvivalGame.java）：
```java
// 新增 STATE_WEAPON_SWAP = 8
// renderWeaponSwapOverlay(): 绘制半透明背景 + 当前武器栏 + 新武器高亮
// 键盘 1-6 选择替换槽位，ESC 放弃拾取
```

### 6.7 Pickup.java 修改

```java
public class Pickup extends Entity {
    // 新增类型常量
    public static final int TYPE_XP = 0;
    public static final int TYPE_WEAPON = 1;
    public static final int TYPE_HEALTH = 2;
    int pickupType;

    // 渲染时根据类型绘制不同外观
    @Override
    public void render(GameEngine g) {
        if (pickupType == TYPE_WEAPON) { /* 宝箱精灵 + 光柱 */ }
        else if (pickupType == TYPE_HEALTH) { /* 红心 */ }
        else { /* 原经验宝石 */ }
    }
}
```

### 6.8 CollisionSystem 玩家拾取扩展

```java
private void playerVsPickups(Player p) {
    // ...原有经验拾取
    // 新增：武器拾取
    if (pickup.pickupType == Pickup.TYPE_WEAPON) {
        pickup.alive = false;
        WeaponDrop wd = (WeaponDrop) pickup;
        game.weaponManager.pickupWeapon(wd.weaponId, wd.rarity);
    }
}
```

### 验证标准
- 击杀敌人偶尔掉落带光柱的武器宝箱
- 走近自动拾取，武器栏增加/升级
- GIANT 掉落率明显更高，Boss 必掉
- 不同稀有度的武器伤害倍率不同
- 武器槽满时弹出替换界面

---

## 7. 实施优先级与依赖

```
Phase 1 (摄像机+开放世界)          ← 最优先，所有后续的基础
  ├── Phase 2 (障碍物系统)         ← 依赖 Phase 1，让大地图有意义
  ├── Phase 3 (像素精灵)           ← 独立，可并行
  │     └── Phase 4 (Tier进化)     ← 依赖 Phase 3（视觉）+ Phase 6（大招）
  └── Phase 5 (武器掉落)           ← 独立，可并行
```

### 推荐实施顺序

```
第1步: Phase 1 (摄像机+开放世界)
第2步: Phase 2 (障碍物系统)
第3步: Phase 3 (像素精灵)
第4步: Phase 5 (武器掉落)          ← 与 Phase 3 并行
第5步: Phase 4 (Tier进化)
```

---

## 8. 性能预算分析（60fps = 16.6ms/帧）

| 子系统 | 旧预算 | 新预算 | 说明 |
|--------|--------|--------|------|
| 实体更新 | 4ms | 5ms | 障碍物碰撞增加 O(N) 遍历 |
| 碰撞宽相位 | 2ms | 2.5ms | 网格变大但实体内聚 |
| 碰撞窄相位 | 2ms | 3ms | 新增障碍物碰撞 |
| 渲染 | 5ms | 6ms | 地图背景 + 精灵 + 光柱 |
| 开销 | 3ms | 3ms | — |
| **总计** | **16ms** | **19.5ms** ⚠️ | 超出预算 3ms |

**优化策略**：
- 视口裁剪：只渲染 `camera.isVisible()` 的实体，减少渲染调用 ~40%
- 障碍物碰撞也用粗筛（只检测玩家周围 300px 的障碍物）
- BufferedImage 预渲染精灵帧，避免逐像素 drawSolidRectangle
- 调整目标：优化后预期回落至 **15ms**

---

## 9. 风险与缓解

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 障碍物布局导致不连通区域 | 敌人卡死或被隔开 | 生成后用 BFS 验证连通性，确保所有区域可达 |
| 像素精灵绘制性能差 | 帧率下降 | 使用 BufferedImage 预渲染 + 缓存 |
| 武器掉落过多破坏平衡 | 武器过早满级 | 加入掉落冷却（每 10 秒最多掉落 1 把） |
| 开放世界敌人寻路过远 | 画面空荡 | 确保生成在玩家周围 600-800px 范围 |
| Tier 进化视觉切换断层 | 画面闪烁 | 加入 0.5 秒过渡动画（粒子环绕 + 缩放） |

---

## 10. 总结

三个优化方向的总工作量估计：

| 模块 | 新增文件 | 修改文件 | 代码行数估计 | 难度 |
|------|----------|----------|-------------|------|
| 摄像机+世界 | 1 (Camera) | 6 | ~200 行 | ⭐⭐ |
| 障碍物 | 2 (Obstacle, GameMap) | 3 | ~350 行 | ⭐⭐⭐ |
| 精灵系统 | 3 (SpriteManager, AssetLoader, PixelFont) | 3 | ~450 行 | ⭐⭐⭐ |
| Tier进化 | 2 (CharacterProgression, UltimateSkill) | 4 | ~300 行 | ⭐⭐⭐ |
| 武器掉落 | 2 (WeaponDrop, WeaponRarity) | 5 | ~250 行 | ⭐⭐ |
| **总计** | **10 个新文件** | **14 个修改** | **~1550 行** | — |

### 素材依赖说明

- **素材规格**：详见 `asset-spec.md`，所有精灵表尺寸/布局/帧数已标准化
- **素材来源**：[itch.io](https://itch.io/game-assets/free) / [OpenGameArt](https://opengameart.org/) 免费像素素材
- **兜底机制**：`AssetLoader` 检测素材缺失 → 自动使用 `drawFallback_xxx()` 代码绘制
- **渐进增强**：代码先写死兜底逻辑 → 搜到素材放入 `assets/sprites/` → 自动切换 PNG 渲染，**无需改代码**
