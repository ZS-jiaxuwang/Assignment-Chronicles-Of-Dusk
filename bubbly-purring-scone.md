# Vampire Survivors 风格生存割草游戏 — 完整实施计划

## Context

基于 `GameEngine.java` 框架（Java2D 封装），制作一款让人「眼前一亮」的课程作业游戏。框架只提供游戏循环 + 基础 2D 绘图 + 键盘鼠标输入 + 音频，其余所有系统需从零构建。选择 Vampire Survivors 风格是因为：几何图形即可表现（无需美工资源）、机制深度足够展示技术能力、视觉冲击力强、市面上有大量成功案例可参考。

---

## 1. 架构总览

### 文件结构（19 个文件，全部在 `src/` 平铺）

```
src/
  SurvivalGame.java         -- 主类，继承 GameEngine，状态机，游戏循环桥接
  GameConfig.java           -- 所有可调常量（arena大小、数值平衡、难度曲线）
  Entity.java               -- 所有游戏对象的基类
  Player.java               -- 玩家角色
  Enemy.java                -- 敌人实体（含AI寻路、Boss行为）
  Projectile.java           -- 武器投射物
  Pickup.java               -- 经验宝石/生命掉落
  WeaponDef.java            -- 武器数据表（纯数据类）
  WeaponInstance.java       -- 单个武器槽位运行时状态
  WeaponManager.java        -- 管理所有武器，冷却计时，开火
  EnemySpawner.java         -- 刷怪调度、波次、难度曲线、Boss触发
  CollisionSystem.java      -- 碰撞检测（空间网格 + 圆-圆检测）
  SpatialGrid.java          -- 均匀网格空间分区（碰撞宽相位）
  UpgradeSystem.java        -- 经验曲线、升级触发、3选1生成
  UpgradeDef.java           -- 升级选项数据定义
  VfxManager.java           -- 粒子特效、浮动文字、屏幕震动
  Particle.java             -- 单个粒子（死亡/受击特效）
  FloatingText.java         -- 浮动伤害数字
  CharacterDef.java         -- 角色职业数据定义
```

### 核心循环流程

```
GameEngine.update(dt)
  ├─ 根据 gameState 分发
  ├─ PLAYING 状态:
  │   ├─ Player.onUpdate(dt)          — 读取 WASD，计算速度，边界限制
  │   ├─ EnemySpawner.update(dt)      — 波次计时，生成敌人
  │   ├─ for each Enemy.onUpdate(dt)  — AI 寻路向玩家移动
  │   ├─ WeaponManager.update(dt)     — 冷却计时，自动开火生成投射物
  │   ├─ for each Projectile.onUpdate(dt) — 移动，生命周期计时
  │   ├─ for each Pickup.onUpdate(dt) — 磁铁效果飞向玩家
  │   ├─ CollisionSystem.update()     — 空间网格宽相位 + 距离平方窄相位
  │   │   ├─ 玩家 vs 敌人  → 扣血 + 无敌帧 + 击退
  │   │   ├─ 投射物 vs 敌人 → 扣血 + 死亡判定 + 经验掉落
  │   │   └─ 玩家 vs 拾取物 → 收集经验
  │   ├─ UpgradeSystem.checkLevelUp() — 经验达标触发 UPGRADE_PAUSE
  │   ├─ VfxManager.update(dt)        — 粒子物理 + 浮动文字上升
  │   └─ 清理 alive==false 的实体
  └─ GameEngine.paintComponent()
      ├─ 绘制竞技场背景 + 边界
      ├─ 绘制所有实体（按颜色分组，减少 setColor 调用）
      ├─ VfxManager 粒子 + 浮动文字
      └─ HUD（血条、经验条、计时器、武器图标）
```

---

## 2. 实体系统

### Entity.java（基类）

```java
public abstract class Entity {
    double x, y;           // 世界坐标（圆心）
    double vx, vy;         // 速度（像素/秒），与 dt 配合实现帧率无关
    double radius;         // 碰撞和渲染半径
    double maxHealth, health;
    boolean alive = true;  // false = 标记待清理（避免 ConcurrentModification）
    int hitFlashTimer;     // 受击闪烁剩余帧数
    Color baseColor;

    void update(double dt) {
        x += vx * dt;
        y += vy * dt;
        if (hitFlashTimer > 0) hitFlashTimer--;
    }
    abstract void onUpdate(double dt);
    void takeDamage(double amount, Entity source) { ... }
    void onDeath(Entity killer) {}
    double distTo(Entity other) { ... }  // sqrt(dx² + dy²)，调用框架的 sqrt
}
```

### 关键设计决策
- **vx/vy 是速度而非位移**：乘以 dt 使移动与帧率无关
- **alive 标志而非立即 remove**：遍历时反向扫描跳过 dead 实体，避免 ConcurrentModificationException
- **hitFlashTimer**：每个实体自带受击闪烁，渲染时检测 `hitFlashTimer % 2 == 0` 切换为白色

### Player.java

- 半径 16px，颜色取决于职业
- WASD 移动（通过 boolean[] keys 数组连续读取，非单次按键事件）
- 对角线移动归一化（除以 sqrt(2) 防止斜向加速）
- 限制在竞技场边界内
- 受伤后无敌帧 0.3 秒
- 朝向计算：`atan2(vy, vx)` 得出角度，画一条指示线

### Enemy.java

- 基础类型：Slime(慢/绿)、Bat(快/紫/正弦摆动)、Skeleton(中/白)、Giant(大/红/慢)、Ghost(快/半透明)
- AI：每帧计算 toPlayer 方向向量，归一化后乘 moveSpeed
- Bat 额外：`wobbleAngle = sin(age * 4.0) * 30°` 叠加在寻路方向上
- Boss：阶段切换（100%-50% HP 慢速近战，50%-0% 加速+召唤小兵+环形弹幕）

### Projectile.java

- 有 damage、lifetime（存活时间）、age（已存活时间）、pierces（是否穿透）
- age >= lifetime 时 alive = false
- 渲染：小圆 + 可选拖尾效果

### Pickup.java（经验宝石）

- 小黄圆（半径 5px）
- magnetRange 内加速飞向玩家（吸引力 400px/s）
- 被玩家碰撞后调用 `SurvivalGame.gainXP(xpValue)` 并标记 dead

### 集合管理

```java
Player player;                           // 单例
ArrayList<Enemy> enemies;               // 活跃敌人（最多200）
ArrayList<Projectile> projectiles;      // 活跃投射物（最多300）
ArrayList<Pickup> pickups;             // 活跃拾取物（最多100）
```

每帧流程：onUpdate → collision → update → 反向遍历移除 !alive

---

## 3. 碰撞系统

### 空间网格（SpatialGrid.java）

- 将 800×600 竞技场划分为 cellSize=120px 的网格（约 7×5=35 个格子）
- `insert(Entity e)`：根据 (x,y) 放入对应格子
- `getNearby(Entity e)`：返回 e 所在格子 + 8 邻格的实体列表
- 每帧 `clear()` 后重新插入所有实体

### 碰撞检测（CollisionSystem.java）

**宽相位**：空间网格将 O(N×M) 降到 ~O(N+M)
**窄相位**：`dx² + dy² < (r1+r2)²` — 只用乘法和加法，无需开方

检查对：
- 玩家 vs 敌人：掉血 + 无敌帧 + 击退
- 投射物 vs 敌人：敌人掉血 + 投射物自毁（除非穿透）
- 玩家 vs 拾取物：收集经验

---

## 4. 武器系统

### WeaponDef.java（静态数据表）

```java
static final int WHIP=0, MAGIC_WAND=1, AXE=2, GARLIC=3, KNIFE=4;
static WeaponDef[] ALL = new WeaponDef[20];

class WeaponDef {
    String name;
    double baseCooldown, baseDamage, projectileSpeed;
    double projectileRadius, lifetime;
    int projectileCount;
    double spreadAngle;
    int targetingMode;   // NEAREST, FORWARD, SELF, RANDOM, FORWARD_ARC
    Color projColor;
    int maxLevel;
    int evolvesInto;     // 进化目标武器ID，-1表示无进化
}
```

### 5 种初始武器

| 武器 | 冷却 | 伤害 | 机制 |
|------|------|------|------|
| **鞭子(Whip)** | 1.2s | 15 | 玩家前方生成宽弧形投射物，短寿命 |
| **魔法杖(Wand)** | 1.0s | 10 | 瞄准最近敌人发射快速弹 |
| **斧头(Axe)** | 2.0s | 20 | 向上投掷后受重力下坠（vy -= 200*dt） |
| **大蒜(Garlic)** | 0.5s | 5 | 自身周围 AOE，每帧对范围内敌人造成 damage*dt |
| **飞刀(Knife)** | 0.8s | 12 | 向面朝方向高速射出，长寿命 |

### 瞄准模式

- `NEAREST`：扫描所有敌人找最近，atan2 计算方向
- `FORWARD`：使用玩家朝向角度
- `SELF`：AOE，不生成投射物，直接在碰撞系统中处理
- `FORWARD_ARC`：初速向上，施加重力加速度

### WeaponInstance.java & WeaponManager.java

- 每个武器实例维护 level（1~maxLevel）和 cooldownTimer
- 升级降低冷却、增加伤害、增加弹丸数
- WeaponManager 持有最多 6 个武器槽位
- 每帧 `for each weapon: cooldownTimer -= dt; if(canFire) fire()`

---

## 5. 敌人 AI 与刷怪

### 刷怪调度（EnemySpawner.java）

- 每 30 秒一波，难度递增
- 波间持续少量刷怪（trickle spawn）
- 在竞技场边缘随机位置生成，保证离玩家至少 300px
- **5 分钟时（300s）Boss 出现**

### 敌人类型数据

| ID | 名称 | 半径 | HP | 速度 | 伤害 | XP |
|----|------|------|-----|------|------|-----|
| 0 | Slime | 14 | 20 | 60 | 8 | 5 |
| 1 | Bat | 10 | 8 | 140 | 5 | 8 |
| 2 | Skeleton | 16 | 50 | 70 | 15 | 12 |
| 3 | Giant | 24 | 200 | 40 | 30 | 50 |
| 4 | Ghost | 10 | 12 | 120 | 10 | 10 |

### Boss 设计

- 半径 40px，HP 500+，出现时屏幕震动 + BOSS_INTRO 状态暂停 2 秒
- **阶段 1 (100%-50%)**：慢速追踪，每 3 秒近战攻击（碰撞即高伤害）
- **阶段 2 (50%-0%)**：加速 50%，每 5 秒向 8 方向发射环形弹幕，每 8 秒召唤 3 只 Slime
- Boss 血条显示在屏幕顶部，红色粗条

---

## 6. 升级系统

### 经验曲线（UpgradeSystem.java）

```java
xpForNextLevel() = 5 + playerLevel * 3;
// Level 1→2: 8 XP, 2→3: 11 XP, 3→4: 14 XP...
// 溢出经验保留到下一级
```

### 3 选 1 升级界面

触发升级时 `gameState = UPGRADE_PAUSE`，在屏幕中央渲染 3 张卡片：
- 每张卡片用 `drawRectangle` + `drawBoldText` 显示名称和描述
- 鼠标悬停高亮（`mouseMoved` 检测边界框）
- 鼠标点击或按 1/2/3 键选择

### 升级池

| 类别 | 概率 | 示例 |
|------|------|------|
| 新武器 | 30%（武器槽 < 6 时） | 获得鞭子/魔法杖/斧头/大蒜/飞刀 |
| 升级已有武器 | 40% | 鞭子 Lv1→Lv2（冷却-8%，伤害+15%，弹丸数+1） |
| 属性提升 | 30% | 移速+10%、伤害倍率+15%、冷却-8%、拾取范围+20% |

### 武器进化

当武器达到最大等级 + 满足特定条件时触发进化：
- 鞭子 Lv5 → 血腥之泪（范围更大，可穿透）
- 魔法杖 Lv5 → 神圣之杖（弹丸数+3）
- 斧头 Lv5 → 死亡螺旋（4 方向同时投掷）

---

## 7. 游戏状态机

```
MENU ──[1]──> CHAR_SELECT ──[1/2/3]──> PLAYING
                                          │
                          ┌───────────────┤
                          ▼               ▼
                    UPGRADE_PAUSE     BOSS_INTRO
                    (升级触发)       (300s触发)
                          │               │
                          ▼               ▼
                      PLAYING          PLAYING
                          │               │
                ┌─────────┤       ┌───────┴──────┐
                ▼         ▼       ▼              ▼
            VICTORY    DEFEAT  VICTORY         DEFEAT
```

### 各状态输入处理

- **MENU**：1=进入角色选择，ESC=退出
- **CHAR_SELECT**：1=战士，2=法师，3=刺客
- **PLAYING**：WASD 移动（boolean[] 连续读取），ESC=回主菜单
- **UPGRADE_PAUSE**：1/2/3 选升级或鼠标点击卡片
- **BOSS_INTRO**：2 秒后自动进入 PLAYING
- **VICTORY/DEFEAT**：SPACE=重新开始

---

## 8. 视觉特效

### 受击闪烁
Entity.render() 中 `hitFlashTimer % 2 == 0` 时切换为白色

### 死亡粒子
- 敌人死亡时在位置生成 8-15 个粒子
- 每个粒子：随机方向、速度 100-200、半径 2-6、生命 0.3-0.8s
- 生命衰减中 alpha 降低、半径缩小

### 浮动伤害数字
- 受击位置生成，vy=-60（上浮），生命 0.8s
- 颜色：>20 伤害为橙色，否则白色
- 使用框架 `drawText()` 渲染，字号 14

### 屏幕震动
- Boss 攻击时触发 intensity=8
- 每帧 offsetX/offsetY 随机偏移，intensity 以 15/s 衰减
- 使用框架 `saveCurrentTransform()/translate(ox,oy)/restoreLastTransform()`

---

## 9. 性能策略

### 对象池
- 敌人池 300、投射物池 300、拾取物池 100
- 数组而非 ArrayList，遍历时跳过 null/!alive
- 完全避免帧内 GC

### 绘制优化
- 按颜色分组渲染（同色所有敌人在一次 setColor 后批量 drawSolidCircle）
- 竞技场背景只用一次 fillRect

### 空间分区
- 120px 网格，35 个格子
- 碰撞检查仅遍历 3×3 邻域

### 帧预算分析（60fps = 16.6ms/帧）
- 实体更新：~4ms（200 敌人 + 100 投射物，简单数学）
- 碰撞宽相位：~2ms（网格插入 + 邻域查询）
- 碰撞窄相位：~2ms（距离平方比较）
- 渲染：~5ms（Graphics2D fill）
- 开销：~3ms
- **总计 ~16ms**，在预算内

---

## 10. 实施阶段

### Phase 1：骨架 — 窗口、循环、状态机
**文件**：`SurvivalGame.java`, `GameConfig.java`

- 继承 GameEngine，createGame(game, 60)
- init() 设置 800×600 窗口，初始化 keys 数组
- update(dt) / paintComponent() 按 gameState 分发
- 状态切换：MENU → CHAR_SELECT → PLAYING（键盘 1/2/3）
- WASD 按键追踪（keyPressed 置 true，keyReleased 置 false）

**可测试**：看到标题界面，按键切换状态

### Phase 2：玩家与竞技场
**文件**：`Player.java`

- 800×600 竞技场（深色背景 + 边框）
- Player 类：蓝色圆半径 16，WASD 移动，对角线归一化
- 边界夹持，朝向指示线
- HUD：计时器 + 经验条

**可测试**：WASD 移动玩家，看到边界限制

### Phase 3：敌人 — 刷怪 + AI + 死亡
**文件**：`Enemy.java`, `EnemySpawner.java`

- Enemy 实体：atan2 寻路，Slime + Bat 两种
- EnemySpawner：每 30s 一波，难度递增，边缘生成
- 死亡标记 + 清理

**可测试**：敌人追逐玩家，波次递增

### Phase 4：碰撞与死亡效果
**文件**：`CollisionSystem.java`, `SpatialGrid.java`, `Pickup.java`, `VfxManager.java`, `Particle.java`

- 空间网格 + 圆碰撞
- 玩家受击 + 无敌帧 + 死亡 → DEFEAT
- 敌人死亡 → 经验宝石 + 粒子爆发
- 经验宝石磁铁效果

**可测试**：玩家会死，敌人死亡掉宝石有粒子

### Phase 5：武器系统
**文件**：`Projectile.java`, `WeaponDef.java`, `WeaponInstance.java`, `WeaponManager.java`

- 5 种武器数据表
- 冷却计时 + 自动开火 + 瞄准模式
- 投射物-敌人碰撞
- 投射物渲染 + 穿透/超时销毁

**可测试**：武器自动攻击，敌人被弹幕消灭

### Phase 6：经验 + 升级 UI
**文件**：`UpgradeSystem.java`, `UpgradeDef.java`, `FloatingText.java`

- XP 曲线 + 升级触发
- UPGRADE_PAUSE 状态 + 3 卡片 UI
- 鼠标悬停高亮 + 点击选择
- 属性加成、新武器、武器升级
- 浮动伤害数字

**可测试**：击杀敌人→升级→选卡→效果生效

### Phase 7：Boss 战
**修改**：`EnemySpawner.java`, `Enemy.java`

- 300s 触发 BOSS_INTRO
- Boss 实体的两阶段行为
- 环形弹幕攻击
- Boss 血条 + 屏幕震动
- VICTORY / DEFEAT 判定

**可测试**：撑 5 分钟 → Boss 出现 → 击败 → 胜利

### Phase 8：打磨 — 音频、平衡、角色选择、特效
**文件**：`CharacterDef.java`

- 3 职业（战士/法师/刺客）+ 角色选择界面
- BGM 循环 + 受击/拾取/升级音效
- 武器进化实现
- 数值平衡调整
- 标题画面 + 胜利/失败画面统计

**可测试**：完整游戏流程，音效正常，难度合理

---

## 11. 依赖关系图

```
Phase 1 (骨架)
  └── Phase 2 (玩家+竞技场)
        └── Phase 3 (敌人+刷怪)
              └── Phase 4 (碰撞+死亡+VFX)
                    └── Phase 5 (武器)
                          └── Phase 6 (经验+升级)
                                └── Phase 7 (Boss)
                                      └── Phase 8 (打磨)
```

---

## 12. 借鉴的市面优秀割草游戏经验

| 来源 | 借鉴要点 |
|------|----------|
| **Vampire Survivors** | 武器自动攻击降低操作门槛；升级 3 选 1 增加策略深度；武器进化系统提供长期目标 |
| **Brotato** | 波次制而非连续出怪（每波有喘息空间）；角色职业差异化明显 |
| **20 Minutes Till Dawn** | 主动瞄准 + 被动武器的混合设计；暗黑美术风格用简单几何也能体现 |
| **通用经验** | 前期怪少让玩家感觉强大（power fantasy），后期怪多制造紧张感；升级频率前期快后期慢保持节奏；视觉反馈（屏幕震动、粒子、闪光）放大打击感 |

---

## 13. 验证方式

1. **Phase 2 后**：运行游戏，确认 WASD 移动流畅、边界限制生效
2. **Phase 4 后**：确认敌人追逐/击杀/掉落宝石/玩家死亡流程完整
3. **Phase 5 后**：确认 5 种武器各自独特行为正确
4. **Phase 6 后**：确认升级循环（击杀→XP→升级→变强→更多击杀）work
5. **Phase 7 后**：确认 Boss 两阶段切换 + 弹幕模式
6. **Phase 8 后**：完整通关测试，确认帧率稳定 60fps，无内存泄漏
