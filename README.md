# Chronicles of Dusk

> **A Pixel Medieval Survival Chronicle**

A top-down pixel-art medieval survival game. Choose your hero, survive waves of enemies, grow stronger, and defeat the Abyssal Lord.

| Resolution | Frame Rate | World Map | Engine |
|:------:|:----:|:--------:|:----:|
| 960×720 | 60 FPS | 3600×2400 | Custom GameEngine (pure `java.awt`) |

**Win Condition**: Defeat the Boss after ~5 minutes　|　**Lose Condition**: HP reaches zero

---

## Table of Contents

- [How to Run](#how-to-run)
- [Classes](#classes)
- [Weapon System](#weapon-system)
- [Upgrades & Progression](#upgrades--progression)
- [Enemies & Boss](#enemies--boss)
- [Controls](#controls)
- [Scoring](#scoring)
- [Game Settings](#game-settings)
- [Project Structure](#project-structure)

---

## How to Run

```bash
# Compile
javac -encoding UTF-8 -d out Game-test/src/*.java

# Run
java -cp out SurvivalGame
```

| Item | Description |
|------|------|
| Main Class | `SurvivalGame` |
| JDK | Java 8+ |
| Dependencies | No external dependencies |

> Audio files are located in `Game-test/audio/`. Ensure this directory exists before running.

---

## Classes

| Attribute | Warrior | Mage |
|------|:------------:|:---------:|
| Title | Crimson Vanguard | Arcane Scholar |
| HP | ×1.25 | ×0.95 |
| Speed | ×0.95 | ×1.0 |
| Damage | ×1.05 | ×1.2 |
| Starting Weapon | Sword | Fire Staff |
| Ultimate Skill | Whirlwind | Thunderstorm |
| Playstyle | Melee tank, sustained combat | Ranged burst, positioning & survival |

- **T2 (Lv.25)** — Unlock Ultimate Skill, 30s cooldown
- **T3 (Lv.40)** — Ultimate Skill cooldown reduced to 15s

---

## Weapon System

| Weapon | Cooldown | Damage | Attack Pattern |
|------|:----:|:----:|----------|
| Sword | 1.5s | 18 | Frontal arc |
| Fire Staff | 1.0s | 10 | Tracks nearest enemy |
| Arcane Circle | 30.0s | 80 | AoE around self |
| Boomerang | 1.8s | 4 | Random ricochet |
| Throwing Dagger | 0.35s | 7 | Tracks nearest enemy |

**Key Mechanics**

- Carry up to **6** weapons; killing enemies has a chance to drop weapon crates
- Rarity: Common / Rare / Epic / Legendary (damage multiplier increases; higher rarities grant bonus levels)
- Max level **5**; leveling up reduces cooldown, increases damage and projectile count

---

## Upgrades & Progression

### Tier System

| Tier | Unlock Level | HP | Damage | Speed | Cooldown | Ultimate |
|:----:|:--------:|:--:|:----:|:----:|:----:|:--------:|
| T1 | Start | ×1.0 | ×1.0 | ×1.0 | ×1.0 | — |
| T2 Elite | Lv.25 | ×1.6 | ×1.6 | ×1.15 | ×0.82 | Unlocked, CD 30s |
| T3 Legendary | Lv.40 | ×2.2 | ×2.2 | ×1.25 | ×0.70 | CD 15s |

### Level-Up: Pick One of Three

| Type | Probability | Description |
|------|:----:|------|
| New Weapon | 30% | If a slot is free and not already owned, randomly grants a new weapon |
| Upgrade Weapon | 40% | Randomly upgrades a non-max-level weapon (damage ↑, cooldown ↓) |
| Stat Blessing | Remainder | See table below |

**Stat Blessings**

| Name | Effect |
|------|------|
| Swift Boots | Move speed +10% |
| Power Core | Damage multiplier +15% |
| Magnet | Pickup range +20% |
| Healing Light | Restores 30% of max HP |
| Critical Eye | Crit chance +8% |
| Life Steal | Lifesteal +3% |

---

## Enemies & Boss

### Normal Enemies

| Name | HP | Speed | Damage | Rarity | Trait |
|------|:--:|:----:|:----:|:------:|------|
| Slime | 60 | 60 | 8 | Common | — |
| Bat | 20 | 140 | 5 | Common | Sine-wave flight, swarm separation |
| Goblin | 120 | 85 | 12 | Common | — |
| Skeleton | 280 | 70 | 15 | Rare | — |
| Ghost | 35 | 120 | 10 | Rare | Ranged attack |
| Flying Eye | 90 | 130 | 10 | Rare | Ranged attack |
| Orc | 500 | 55 | 20 | Epic | — |
| Mushroom | 750 | 40 | 20 | Epic | Ranged attack |
| Giant | 1400 | 40 | 30 | Legendary | High-HP elite |

> All enemies scale over time: HP **+0.016/s**, Speed **+0.004/s**, Damage **+0.005/s**

### Boss — Abyssal Lord

| Attribute | Value |
|------|------|
| HP | 38,000 |
| Speed | 55 |
| Contact Damage | 60 |
| Spawn Time | **300 seconds** (5 minutes) into the game |

**Three-Phase Mechanics**

| Phase | HP Range | Ring Barrage | Summon | Charge | Special |
|:----:|:-------:|:--------:|:----:|:----:|----------|
| P1 | > 66% | 10 shots / 3.5s | Goblin ×4 / 6s | — | — |
| P2 | 33% – 66% | 14 shots / 3.0s | Orc ×4 / 5s | Every 5s | Speed ×1.4 |
| P3 | < 33% | 20 shots / 2.5s | Skeleton ×5 / 4s | Every 4s | Fan shot 9 / 3s; Shockwave 24 / 5.5s; Speed ×1.8 |

---

## Controls

| Key | Action |
|------|------|
| `W` `A` `S` `D` | Move |
| `SPACE` | Use Ultimate Skill (T2+) |
| `P` | Pause / Resume |
| `ESC` | Back to menu / Quit / Cancel weapon swap |
| `1` `2` `3` | Select upgrade card / Weapon swap slot / Character |
| `ENTER` | Confirm / Start game |
| `H` | View controls guide |
| `L` | Debug: quickly gain massive XP |

---

## Scoring

| Source | Points |
|------|------|
| Survival time | 10 pts/sec |
| Enemy kills | Base score per enemy × rarity multiplier |
| Boss kill | +5000 |
| Full-HP clear | +2000 |
| Reach Lv.25 | Total score ×2.0 |
| Reach Lv.40 | Total score ×3.0 |

---

## Game Settings

| Setting | Options |
|--------|--------|
| Screen Shake | ON / OFF |
| Combat Pace | Casual (0.82×) / Adventure (1.0×) / Intense (1.22×) |

---

## Project Structure

```
Assignment2/
├── README.md
└── Game-test/
    ├── src/                          # Game source code
    │   ├── SurvivalGame.java         # Main class — rendering, input, state machine
    │   ├── GameEngine.java           # Custom engine base class
    │   ├── GameConfig.java           # Global constants & configuration
    │   ├── Player.java               # Player logic
    │   ├── Enemy.java                # Enemy + Boss AI
    │   ├── EnemySpawner.java         # Spawn control
    │   ├── WeaponDef.java            # Weapon definitions
    │   ├── WeaponManager.java        # Weapon management
    │   ├── WeaponDrop.java           # Weapon drops
    │   ├── WeaponInstance.java       # Weapon instances
    │   ├── WeaponRarity.java         # Weapon rarity
    │   ├── UpgradeSystem.java        # Upgrade selection
    │   ├── UpgradeDef.java           # Upgrade option definitions
    │   ├── CharacterProgression.java # Tier stat scaling
    │   ├── CharacterDef.java         # Character definitions
    │   ├── UltimateSkill.java        # Ultimate skills
    │   ├── CollisionSystem.java      # Collision detection
    │   ├── SpatialGrid.java          # Spatial grid
    │   ├── Projectile.java           # Projectiles
    │   ├── Pickup.java               # Pickup items
    │   ├── VfxManager.java           # Particle effects
    │   ├── Particle.java             # Particles
    │   ├── FloatingText.java         # Floating text
    │   ├── Camera.java               # Camera follow
    │   ├── GameMap.java              # Map generation
    │   ├── Obstacle.java             # Obstacles
    │   ├── AudioManager.java         # Audio management
    │   ├── SpriteManager.java        # Sprite rendering
    │   ├── AssetLibrary.java         # Asset library
    │   └── AssetLoader.java          # Asset loading
    ├── assets/                       # Image assets (sprites, icons, etc.)
    ├── audio/                        # Sound effects & BGM (.wav)
    └── docs/                         # Documentation (game intro PPT, etc.)
```