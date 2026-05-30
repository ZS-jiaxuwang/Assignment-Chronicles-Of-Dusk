import java.awt.Color;
// Jiaxu Wang (24009377)  Jiaheng Liu (24009483)  Angze Song (24009333)   Xiao Wu (24009458)
public class Enemy extends Entity {
    public static final int SLIME = 0;
    public static final int BAT = 1;
    public static final int SKELETON = 2;
    public static final int GIANT = 3;
    public static final int GHOST = 4;
    public static final int BOSS = 5;
    public static final int ORC = 6;
    public static final int GOBLIN = 7;
    public static final int MUSHROOM = 8;
    public static final int FLYING_EYE = 9;

    public static final int RARITY_NORMAL = 0;
    public static final int RARITY_RARE = 1;
    public static final int RARITY_EPIC = 2;
    public static final int RARITY_LEGENDARY = 3;
    public static final int RARITY_BOSS = 4;

    private final SurvivalGame game;
    private final int type;
    private final double baseSpeed;
    private final double contactDamage;
    private final int xpValue;
    @Override
    public void render(GameEngine g) {
        if (!alive) return;
        if (dying) {
            double t = deathTimer / 0.3;
            double fadeAlpha = Math.max(0, t);
            double expand = 1.0 + (1.0 - t) * 0.6;
            Color bodyColor = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), (int)(140 * fadeAlpha));
            g.changeColor(bodyColor);
            g.drawSolidCircle(x, y, radius * expand);
            Color glowColor = new Color(255, 255, 255, (int)(60 * fadeAlpha));
            g.changeColor(glowColor);
            g.drawSolidCircle(x, y, radius * expand * 0.6);
            return;
        }
        int anim;
        Player p = game.player;
        boolean nearPlayer;
        if (isRangedAttacker()) {
            nearPlayer = p != null && p.alive && distSqTo(p) < (rangedStandoffFar() + p.radius) * (rangedStandoffFar() + p.radius);
        } else {
            nearPlayer = p != null && p.alive && distSqTo(p) < (radius + p.radius + 20) * (radius + p.radius + 20);
        }
        if (nearPlayer) {
            anim = SpriteManager.ANIM_ATTACK;
        } else if (Math.abs(vx) > 0.5 || Math.abs(vy) > 0.5) {
            anim = SpriteManager.ANIM_WALK;
        } else {
            anim = SpriteManager.ANIM_IDLE;
        }
        Color c = (hitFlashTimer > 0 && hitFlashTimer % 2 == 0) ? Color.WHITE : baseColor;
        if (type == ORC) {
            boolean flip = p != null && x > p.x;
            SpriteManager.drawOrc(anim, age, x, y, radius, c, flip);
        } else if (type == SLIME) {
            SpriteManager.drawSlime(anim, age, x, y, radius, c);
        } else if (type == BAT) {
            SpriteManager.drawBat(anim, age, x, y, radius, c);
        } else if (type == GIANT) {
            SpriteManager.drawGiant(anim, age, x, y, radius, c);
        } else if (type == GHOST) {
            SpriteManager.drawGhost(anim, age, x, y, radius, c);
        } else if (type == BOSS) {
            SpriteManager.drawBoss(anim, age, x, y, radius, c);
        } else {
            boolean flip = p != null && x > p.x;
            double scale = visualScale();
            SpriteManager.drawMonsterSingle(getAssetKey(), anim, age, x, y, radius, c, flip, scale);
        }
    }

    private String getAssetKey() {
        switch (type) {
            case BAT:         return AssetLibrary.ENEMY_BAT;
            case SKELETON:    return AssetLibrary.ENEMY_SKELETON;
            case GIANT:       return AssetLibrary.ENEMY_GIANT;
            case GHOST:       return AssetLibrary.ENEMY_GHOST;
            case BOSS:        return AssetLibrary.ENEMY_BOSS;
            case ORC:         return "enemy_orc";
            case GOBLIN:      return AssetLibrary.ENEMY_GOBLIN;
            case MUSHROOM:    return AssetLibrary.ENEMY_MUSHROOM;
            case FLYING_EYE:  return AssetLibrary.ENEMY_FLYING_EYE;
            default:          return AssetLibrary.ENEMY_SLIME;
        }
    }

    private double age;
    private double bossProjectileTimer;
    private double bossSummonTimer;
    private double bossChargeTimer;
    private double bossDashTimer;
    private double bossBurstTimer;
    private double bossShockwaveTimer;
    private double rangedAttackTimer;

    public Enemy(SurvivalGame game, int type, double x, double y) {
        super(x, y, radiusByType(type), hpByType(type) * (0.55 + game.getRunTimeSeconds() * 0.016), colorByType(type));
        this.game = game;
        this.type = type;
        double t = game.getRunTimeSeconds();
        this.baseSpeed = speedByType(type) * (1.0 + t * 0.004);
        this.contactDamage = contactDamageByType(type) * (1.0 + t * 0.005);
        this.xpValue = xpByType(type);
    }

    @Override
    public void onUpdate(double dt) {
        if (dying) return;
        age += dt;
        Player p = game.player;
        if (p == null || !p.alive) {
            vx = 0;
            vy = 0;
            return;
        }

        double dx = p.x - x;
        double dy = p.y - y;
        double len = Math.sqrt(dx * dx + dy * dy);
        if (len < 0.001) len = 0.001;

        double nx = dx / len;
        double ny = dy / len;
        double speed = baseSpeed;

        if (type == BAT) {
            double wobble = Math.sin(age * 4.0) * Math.toRadians(30);
            double c = Math.cos(wobble);
            double s = Math.sin(wobble);
            double wx = nx * c - ny * s;
            double wy = nx * s + ny * c;
            nx = wx;
            ny = wy;
        }

        if (isRangedAttacker()) {
            double standoffNear = rangedStandoffNear();
            double standoffFar = rangedStandoffFar();
            if (len < standoffNear) {
                nx = -nx;
                ny = -ny;
                speed = baseSpeed * 0.85;
            } else if (len > standoffFar) {
                speed = baseSpeed;
            } else {
                double strafeDir = Math.sin(age * 1.8) > 0 ? 1 : -1;
                double snx = -ny * strafeDir;
                double sny = nx * strafeDir;
                nx = snx * 0.5 + nx * 0.1;
                ny = sny * 0.5 + ny * 0.1;
                double nLen = Math.sqrt(nx * nx + ny * ny);
                nx /= nLen;
                ny /= nLen;
                speed = baseSpeed * 0.45;
                rangedAttackTimer += dt;
                double interval = rangedFireInterval();
                if (rangedAttackTimer >= interval) {
                    rangedAttackTimer = 0.0;
                    fireAtPlayer(p, rangedProjSpeed(), rangedProjDamage(), rangedProjLifetime(), rangedProjRadius(), rangedProjColor());
                }
            }
            vx = nx * speed;
            vy = ny * speed;
            return;
        }

        if (type == BAT) {
            double sepX = 0, sepY = 0;
            int sepCount = 0;
            for (int i = 0; i < game.enemies.size(); i++) {
                Enemy other = game.enemies.get(i);
                if (other == this || !other.alive || other.type != BAT) continue;
                double sx = x - other.x;
                double sy = y - other.y;
                double sd = sx * sx + sy * sy;
                if (sd < 2500 && sd > 0.01) {
                    double sLen = Math.sqrt(sd);
                    sepX += sx / sLen;
                    sepY += sy / sLen;
                    sepCount++;
                }
            }
            if (sepCount > 0) {
                sepX /= sepCount;
                sepY /= sepCount;
                nx = nx * 0.55 + sepX * 0.45;
                ny = ny * 0.55 + sepY * 0.45;
                double nLen = Math.sqrt(nx * nx + ny * ny);
                if (nLen > 0.001) {
                    nx /= nLen;
                    ny /= nLen;
                }
            }
        }

        if (type == BOSS) {
            int phase = getBossPhase();
            double speedMult = (phase == 3) ? 1.8 : (phase == 2) ? 1.4 : 1.0;
            speed *= speedMult;

            bossProjectileTimer += dt;
            double radialInterval = (phase == 3) ? 2.5 : (phase == 2) ? 3.0 : 3.5;
            int radialCount = (phase == 3) ? 20 : (phase == 2) ? 14 : 10;
            if (bossProjectileTimer >= radialInterval) {
                bossProjectileTimer = 0.0;
                game.spawnBossRadial(this, radialCount);
            }

            bossSummonTimer += dt;
            double summonInterval = (phase == 3) ? 4.0 : (phase == 2) ? 5.0 : 6.0;
            if (bossSummonTimer >= summonInterval) {
                bossSummonTimer = 0.0;
                int summonType = (phase == 3) ? Enemy.SKELETON : (phase == 2) ? Enemy.ORC : Enemy.GOBLIN;
                int summonCount = (phase == 3) ? 5 : 4;
                for (int i = 0; i < summonCount; i++) {
                    game.spawnEnemyNear(this.x, this.y, summonType, 140);
                }
            }

            bossChargeTimer += dt;
            double chargeInterval = (phase == 3) ? 4.0 : (phase == 2) ? 5.0 : 99.0;
            if (phase >= 2 && bossChargeTimer >= chargeInterval) {
                bossChargeTimer = 0.0;
                performChargeDash();
                vx = nx * speed;
                vy = ny * speed;
                return;
            }

            if (bossDashTimer > 0) {
                bossDashTimer -= dt;
                if (bossDashTimer <= 0) {
                    bossDashTimer = 0;
                    vx = 0;
                    vy = 0;
                }
                return;
            }

            bossBurstTimer += dt;
            if (phase >= 3 && bossBurstTimer >= 3.0) {
                bossBurstTimer = 0.0;
                performBurstShot();
            }

            bossShockwaveTimer += dt;
            if (phase >= 3 && bossShockwaveTimer >= 5.5) {
                bossShockwaveTimer = 0.0;
                game.spawnBossShockwave(this);
            }
        }

        vx = nx * speed;
        vy = ny * speed;
    }

    @Override
    public void onDeath(Entity killer) {
        game.vfx.spawnDeathBurst(x, y, baseColor);

        double dropChance = (type == BOSS) ? GameConfig.WEAPON_DROP_CHANCE_BOSS
            : (type == GIANT) ? GameConfig.WEAPON_DROP_CHANCE_ELITE
            : GameConfig.WEAPON_DROP_CHANCE;
        if (game.weaponManager.canDropWeapon() && game.rand(1.0) < dropChance) {
            game.weaponManager.markDrop();
            WeaponRarity rarity = WeaponRarity.roll();
            int wid = randomWeaponId();
            System.out.println("[Drop] " + rarity.name() + " " + WeaponDef.ALL[wid].name + " at (" + (int)x + "," + (int)y + ")");
            game.addWeaponDrop(new WeaponDrop(game, x, y, wid, rarity));
        } else {
            game.addPickup(new Pickup(game, x, y, xpValue));
        }

        game.addKillScore(this);

        if (type == BOSS) {
            game.onBossKilled();
        }
    }

    private int randomWeaponId() {
        int[] pool = {WeaponDef.SWORD, WeaponDef.MAGIC_WAND, WeaponDef.AXE, WeaponDef.GARLIC, WeaponDef.KNIFE};
        return pool[game.rand(pool.length)];
    }

    public int getType() {
        return type;
    }

    public double getContactDamage() {
        return contactDamage;
    }

    public int getXpValue() {
        return xpValue;
    }

    public int getRarity() {
        switch (type) {
            case SKELETON: case GHOST: case FLYING_EYE: return RARITY_RARE;
            case ORC: case MUSHROOM: return RARITY_EPIC;
            case GIANT: return RARITY_LEGENDARY;
            case BOSS: return RARITY_BOSS;
            default: return RARITY_NORMAL;
        }
    }

    public int getBaseKillScore() {
        switch (type) {
            case BAT: return GameConfig.SCORE_BASE_BAT;
            case SKELETON: return GameConfig.SCORE_BASE_SKELETON;
            case GIANT: return GameConfig.SCORE_BASE_GIANT;
            case GHOST: return GameConfig.SCORE_BASE_GHOST;
            case ORC: return GameConfig.SCORE_BASE_ORC;
            case GOBLIN: return GameConfig.SCORE_BASE_GOBLIN;
            case MUSHROOM: return GameConfig.SCORE_BASE_MUSHROOM;
            case FLYING_EYE: return GameConfig.SCORE_BASE_FLYING_EYE;
            default: return GameConfig.SCORE_BASE_SLIME;
        }
    }

    private boolean isRangedAttacker() {
        return type == GHOST || type == FLYING_EYE || type == MUSHROOM;
    }

    private double rangedStandoffNear() {
        switch (type) {
            case GHOST: return 140;
            case FLYING_EYE: return 180;
            case MUSHROOM: return 100;
            default: return 0;
        }
    }

    private double rangedStandoffFar() {
        switch (type) {
            case GHOST: return 260;
            case FLYING_EYE: return 320;
            case MUSHROOM: return 200;
            default: return 0;
        }
    }

    private double rangedFireInterval() {
        switch (type) {
            case GHOST: return 2.5;
            case FLYING_EYE: return 3.0;
            case MUSHROOM: return 3.5;
            default: return 99;
        }
    }

    private double rangedProjSpeed() {
        switch (type) {
            case GHOST: return 150;
            case FLYING_EYE: return 200;
            case MUSHROOM: return 110;
            default: return 0;
        }
    }

    private double rangedProjDamage() {
        switch (type) {
            case GHOST: return 10;
            case FLYING_EYE: return 12;
            case MUSHROOM: return 15;
            default: return 0;
        }
    }

    private double rangedProjLifetime() {
        switch (type) {
            case GHOST: return 3.0;
            case FLYING_EYE: return 2.5;
            case MUSHROOM: return 3.5;
            default: return 0;
        }
    }

    private double rangedProjRadius() {
        switch (type) {
            case GHOST: return 6;
            case FLYING_EYE: return 5;
            case MUSHROOM: return 7;
            default: return 0;
        }
    }

    private Color rangedProjColor() {
        switch (type) {
            case GHOST: return new Color(180, 210, 255);
            case FLYING_EYE: return new Color(210, 100, 240);
            case MUSHROOM: return new Color(180, 140, 70);
            default: return Color.WHITE;
        }
    }

    private int getBossPhase() {
        if (health <= maxHealth * 0.33) return 3;
        if (health <= maxHealth * 0.66) return 2;
        return 1;
    }

    private void performChargeDash() {
        Player p = game.player;
        if (p == null || !p.alive) return;
        double dx = p.x - x;
        double dy = p.y - y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist < 0.001) dist = 0.001;
        vx = (dx / dist) * baseSpeed * 4.5;
        vy = (dy / dist) * baseSpeed * 4.5;
        bossDashTimer = 0.5;
    }

    private void performBurstShot() {
        Player p = game.player;
        if (p == null || !p.alive) return;
        double baseAngle = Math.atan2(p.y - y, p.x - x);
        for (int i = -4; i <= 4; i++) {
            double angle = baseAngle + i * Math.toRadians(12);
            double sx = x + Math.cos(angle) * (radius + 10);
            double sy = y + Math.sin(angle) * (radius + 10);
            Projectile proj = new Projectile(game, sx, sy, 7, 260, angle, 26, 2.8, 0, false,
                new Color(255, 50, 50), Projectile.TYPE_ARROW);
            game.addProjectile(proj);
        }
    }

    private void fireAtPlayer(Player p, double projSpeed, double damage, double lifetime, double radius, Color color) {
        double dx = p.x - x;
        double dy = p.y - y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist < 0.001) dist = 0.001;
        double angle = Math.atan2(dy, dx);
        double spawnX = x + Math.cos(angle) * (this.radius + radius + 2);
        double spawnY = y + Math.sin(angle) * (this.radius + radius + 2);
        Projectile proj = new Projectile(game, spawnX, spawnY, radius, projSpeed, angle, damage, lifetime, 0, false, color, Projectile.TYPE_ARROW);
        game.addProjectile(proj);
    }

    private static double radiusByType(int t) {
        switch (t) {
            case BAT: return 18;
            case SKELETON: return 28;
            case GIANT: return 44;
            case GHOST: return 20;
            case BOSS: return 52;
            case ORC: return 30;
            case GOBLIN: return 24;
            case MUSHROOM: return 36;
            case FLYING_EYE: return 22;
            default: return 24;
        }
    }

    private double visualScale() {
        return 1.5 + radius * 0.025;
    }

    private static double hpByType(int t) {
        switch (t) {
            case BAT: return 20;
            case SKELETON: return 280;
            case GIANT: return 1400;
            case GHOST: return 35;
            case BOSS: return 38000;
            case ORC: return 500;
            case GOBLIN: return 120;
            case MUSHROOM: return 750;
            case FLYING_EYE: return 90;
            default: return 60;
        }
    }

    private static double speedByType(int t) {
        switch (t) {
            case BAT: return 140;
            case SKELETON: return 70;
            case GIANT: return 40;
            case GHOST: return 120;
            case BOSS: return 55;
            case ORC: return 55;
            case GOBLIN: return 85;
            case MUSHROOM: return 40;
            case FLYING_EYE: return 130;
            default: return 60;
        }
    }

    private static double contactDamageByType(int t) {
        switch (t) {
            case BAT: return 5;
            case SKELETON: return 15;
            case GIANT: return 30;
            case GHOST: return 10;
            case BOSS: return 60;
            case ORC: return 20;
            case GOBLIN: return 12;
            case MUSHROOM: return 20;
            case FLYING_EYE: return 10;
            default: return 8;
        }
    }

    private static int xpByType(int t) {
        switch (t) {
            case BAT: return 8;
            case SKELETON: return 12;
            case GIANT: return 50;
            case GHOST: return 10;
            case BOSS: return 300;
            case ORC: return 25;
            case GOBLIN: return 15;
            case MUSHROOM: return 30;
            case FLYING_EYE: return 15;
            default: return 5;
        }
    }

    private static Color colorByType(int t) {
        switch (t) {
            case BAT: return new Color(155, 90, 210);
            case SKELETON: return new Color(230, 230, 230);
            case GIANT: return new Color(190, 65, 60);
            case GHOST: return new Color(200, 200, 255);
            case BOSS: return new Color(255, 70, 70);
            case ORC: return new Color(120, 160, 80);
            case GOBLIN: return new Color(100, 180, 80);
            case MUSHROOM: return new Color(200, 120, 80);
            case FLYING_EYE: return new Color(200, 100, 220);
            default: return new Color(80, 210, 80);
        }
    }
}
