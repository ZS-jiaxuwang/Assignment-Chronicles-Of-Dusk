# 游戏素材需求规格书

## 通用规范

- **像素风格**：32×32 px 为一个标准单位格（角色/敌人）
- **精灵表格式**：PNG，透明背景
- **排列方式**：水平排列，同一动画的帧从左到右依次排列，不同动画按行排列
- **命名规范**：`{category}_{name}.png`，如 `player_warrior.png`
- **调色板限制**：每个精灵控制在 8-16 色以内，保持像素游戏一致的视觉风格

---

## 一、角色精灵 (3 个角色)

每个角色一张精灵表，**640×256 px**，包含 4 方向 × 5 动画 × N 帧。

### 精灵表布局（统一规格）

```
行0: Down  Idle    4帧  |□|□|□|□|  (32×32 each)
行1: Down  Walk    6帧  |□|□|□|□|□|□|
行2: Left  Idle    4帧  |□|□|□|□|
行3: Left  Walk    6帧  |□|□|□|□|□|□|
行4: Right Idle    4帧  |□|□|□|□|
行5: Right Walk    6帧  |□|□|□|□|□|□|
行6: Up    Idle    4帧  |□|□|□|□|
行7: Up    Walk    6帧  |□|□|□|□|□|□|
行8: Hurt  任意    2帧  |□|□|
行9: Death 任意    4帧  |□|□|□|□|
```

总尺寸：6帧×32=192宽 不够 → 每行用 6×32=192 宽，10行，192×320。统一用 **256×384**（留边距）。

### 角色 1：战士 Warrior

**关键词搜索**：`pixel art warrior knight sprite sheet 32x32 free`

**外观描述**：
- 身着重甲，手持剑/鞭
- 配色：蓝银色调（主盔甲 #5088D0，轮廓 #1A2A4A，高光 #88B8F0）
- 行走时铠甲有轻微起伏
- 死亡：单膝跪地后倒下

**Tier 视觉变化**（用于后续，素材可不区分）：
- Tier 1 (Lv1-24)：基础铁甲，无装饰
- Tier 2 (Lv25-49)：银边装饰，肩甲变大
- Tier 3 (Lv50-74)：金色镶边，武器带光效
- Tier 4 (Lv75+)：全身发光粒子环绕

### 角色 2：法师 Mage

**关键词搜索**：`pixel art mage wizard sprite sheet 32x32 free`

**外观描述**：
- 身着长袍，持法杖
- 配色：紫色调（主袍 #8040C0，轮廓 #201040，高光 #B870E0）
- 行走时长袍下摆飘动
- 死亡：化为紫色粒子消散

### 角色 3：刺客 Assassin

**关键词搜索**：`pixel art assassin rogue sprite sheet 32x32 free`

**外观描述**：
- 轻装兜帽，双持匕首
- 配色：暗银色调（主衣 #C0C0C0，轮廓 #202020，高光 #FF4040 点缀）
- 行走时有残影感（快速小步）
- 死亡：后空翻倒地

---

## 二、敌人精灵 (6 种敌人)

每种敌人一张精灵表，**128×160 px**（4 行 × 最多 4 帧，32×32/帧）。

布局（统一）：
```
行0: Idle  / Float  2-4帧
行1: Move  / Chase  4帧
行2: Hurt           2帧
行3: Death          3-4帧
```

### 敌人 1：史莱姆 Slime

**关键词搜索**：`pixel art slime sprite sheet 32x32 free`

**外观**：绿色半透明凝胶状，弹跳移动。Idle 时上下挤压呼吸，Move 时拉伸形变。
**指定配色**：绿 #50D250，暗绿轮廓 #1A601A

### 敌人 2：蝙蝠 Bat

**关键词搜索**：`pixel art bat sprite sheet 32x32 free`

**外观**：紫色小蝙蝠，翅膀快速扇动（4帧以上才有灵动感）。Frame 0/2 翅膀上，Frame 1/3 翅膀下。
**指定配色**：紫 #9A5AD2，翼膜暗紫 #402070

### 敌人 3：骷髅 Skeleton

**关键词搜索**：`pixel art skeleton sprite sheet 32x32 free`

**外观**：白色骨架人形，走路咔咔响的僵硬步态。手持小骨盾。
**指定配色**：骨白 #E6E6E6，暗面 #999999

### 敌人 4：巨人 Giant

**关键词搜索**：`pixel art giant ogre sprite sheet 48x48 free`

**外观**：大型红色恶魔/食人魔，**48×48 px**（比标准大 1.5 倍），沉重步伐，地面震动感。
**注意**：这个尺寸不同！精灵表改为 **192×192**
**指定配色**：暗红 #BE413C，轮廓 #4A1512

### 敌人 5：幽灵 Ghost

**关键词搜索**：`pixel art ghost spirit sprite sheet 32x32 free`

**外观**：半透明蓝白幽灵，漂浮移动（无腿部动画），带拖尾虚影。
**指定配色**：浅蓝 #C8C8FF，核心白 #FFFFFF，半透明效果用 Alpha

### 敌人 6：Boss 死神/魔王

**关键词搜索**：`pixel art boss demon reaper sprite sheet 64x64 free`

**外观**：大型 Boss，**64×64 px**（2 倍标准），双阶段形态。
- 阶段1 (HP>50%)：持镰刀，慢速威严移动
- 阶段2 (HP<50%)：眼睛发光变红，速度加快，周围有暗红色光环
**精灵表**：**256×256**，额外需要 Phase2 的行
**指定配色**：深红 #FF4646，黑 #1A0A0A，金 #FFC828

---

## 三、武器投射物精灵 (7 种武器，各 1 帧)

尺寸 **16×16 px** 单帧即可（代码中可旋转），做成一张合集精灵表 `weapons_projectiles.png` **160×16**（10 格 × 16px）。

| # | 武器 | 投射物外观 | 搜索关键词 |
|---|------|-----------|-----------|
| 0 | Whip 鞭子 | 弧形斩击波（月牙形） | `pixel art slash wave 16x16` |
| 1 | Magic Wand 魔法杖 | 蓝色魔法弹（圆+拖尾） | `pixel art magic bolt 16x16` |
| 2 | Axe 斧头 | 旋转飞斧（带残影） | `pixel art throwing axe 16x16` |
| 3 | Garlic 大蒜 | 绿色光环（用代码圆环画也可以） | — |
| 4 | Knife 飞刀 | 细长匕首 | `pixel art throwing knife 16x16` |
| 5 | Holy Wand 神圣杖 | 金色三连弹（进化武器） | `pixel art holy bolt 16x16` |
| 6 | Bloody Tear 血腥泪 | 红色大剑气（进化武器） | `pixel art blood slash 16x16` |

---

## 四、拾取物精灵 (3 种)

一张合集精灵表 `pickups.png`，**48×16**（3 格 × 16px）。

| # | 拾取物 | 外观 | 搜索关键词 |
|---|--------|------|-----------|
| 0 | XP 经验宝石 | 绿色小菱形晶体 | `pixel art gem crystal 16x16` |
| 1 | 武器宝箱 | 棕色小宝箱 | `pixel art treasure chest 16x16` |
| 2 | 血瓶 | 红心 | `pixel art heart pickup 16x16` |

---

## 五、障碍物精灵 (4 种)

不需要精灵表，每种一个 **PNG 图片**即可（代码中直接 `drawImage`）。

| # | 障碍物 | 尺寸 | 外观 | 搜索关键词 |
|---|--------|------|------|-----------|
| 0 | Rock 岩石 | 48×48 | 灰暗色不规则岩石 | `pixel art rock boulder 48x48` |
| 1 | Tree 树木 | 32×48 | 绿树冠+棕色树干 | `pixel art tree 32x48` |
| 2 | Wall 墙壁 | 64×32 | 灰色石砖墙段 | `pixel art stone wall tile 64x32` |
| 3 | Bush 灌木 | 32×24 | 矮绿灌木丛 | `pixel art bush 32x24` |

---

## 六、UI 素材

一张合集精灵表 `ui_elements.png`，**160×80**。

| # | 元素 | 尺寸 | 用途 | 搜索关键词 |
|---|------|------|------|-----------|
| 0 | 血条边框 | 96×16 | 像素风格条框 | `pixel art health bar ui` |
| 1 | 经验条边框 | 96×12 | 稍小的条框 | 同上 |
| 2 | 武器槽框 | 32×32 | 每个武器图标背景 | `pixel art item slot ui` |
| 3 | Tier 星标 | 16×16 | 进化等级指示 | `pixel art star icon 16x16` |
| 4 | 技能冷却框 | 32×32 | 大招 CD 外框 | `pixel art cooldown frame` |

---

## 七、特效精灵 (可选，代码可先兜底)

一张合集 `effects.png`，**128×64**（用于粒子系统）。

| # | 效果 | 尺寸 | 用途 |
|---|------|------|------|
| 0-3 | 爆炸碎片 | 8×8 ×4 | 死亡粒子（4 种不同碎片形状） |
| 4-7 | 命中火花 | 8×8 ×4 | 攻击命中闪光 |
| 8 | 升级光柱 | 16×32 | 升 Tier 时的光柱 |
| 9 | 武器光柱 | 8×24 | 武器掉落的地面光柱 |

---

## 八、优先级与兜底策略

### 必须有（搜到就用，搜不到用代码画）

| 优先级 | 素材 | 代码兜底方案 |
|--------|------|-------------|
| P0 必须 | 角色精灵表 (3 个) | 代码画几何形状 + 方向线（当前方案） |
| P0 必须 | 敌人精灵表 (6 个) | 代码画几何形状（当前方案） |
| P1 重要 | 投射物精灵 | 代码画带颜色的圆 + 拖尾圆 |
| P1 重要 | UI 边框 | 代码 `drawRectangle` 多层嵌套模拟像素框 |
| P2 加分 | 拾取物精灵 | 代码画菱形(宝石)、矩形(宝箱)、三角+方块(心) |
| P2 加分 | 障碍物精灵 | 代码多边形填充 |
| P3 可选 | 特效粒子 | 代码画小方块替代 |

### 代码兜底的像素风格技巧

用 `drawSolidRectangle` 也能画出像素味，关键在于：

```java
// ❌ 平滑圆（不像像素游戏）
g.drawSolidCircle(x, y, radius);

// ✅ 用方块拼出锯齿圆（像像素游戏）
for (int angle = 0; angle < 360; angle += 15) {
    int px = (int)(x + Math.cos(Math.toRadians(angle)) * radius);
    int py = (int)(y + Math.sin(Math.toRadians(angle)) * radius);
    g.drawSolidRectangle(px - 1, py - 1, 3, 3);  // 2x2 像素块
}
```

---

## 九、推荐搜索网站

按优先级排列：

1. **[itch.io Game Assets](https://itch.io/game-assets/free)** — 标签筛选：`pixel-art` `sprite-sheet` `32x32` `fantasy` `rpg`
2. **[OpenGameArt.org](https://opengameart.org/)** — 搜索：`32x32 character sprite sheet pixel`
3. **[CraftPix.net](https://craftpix.net/freebies/)** — 免费区有很多成套的像素 RPG 素材
4. **[Kenney.nl](https://kenney.nl/assets)** — 知名免费游戏素材，有像素分类

### 搜索技巧

- 搜套装（pack）比单个搜效率高，比如 `"top-down rpg sprite pack free"` 一套就包含角色+敌人+物品
- 注意看授权协议：**CC0 / Public Domain** 最自由，**CC-BY** 需要署名（在游戏里加一行字即可）
- 统一风格的素材优先（同一个作者的系列），否则不同类型敌人画风不统一会违和

---

## 十、推荐的"开箱即用"素材包

如果能找到以下任一完整包，基本覆盖 80% 需求：

1. **"Pixel Adventure"** 系列（itch.io 免费）— 角色+怪物+地形完整
2. **"Tiny Swords"** 系列 — 像素策略游戏素材，战斗特效多
3. **"0x72" 的 Dungeon Tileset**（itch.io）— 16×16 像素，角色+怪物+物品全
4. **"Pipoya" RPG 素材** — 日本作者，风格统一，大量免费 RPG 角色/怪物精灵表
