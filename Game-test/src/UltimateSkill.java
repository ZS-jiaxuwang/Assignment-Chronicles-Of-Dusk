public class UltimateSkill {
    public static final int WHIRLWIND    = 0;
    public static final int THUNDER      = 1;
    public static final int SHADOW_CLONE = 2;

    public final int type;
    public final String name;
    double cooldown;
    double cooldownTimer;
    boolean active;
    double activeTimer;

    public UltimateSkill(int type, double cooldown) {
        this.type = type;
        this.cooldown = cooldown;
        this.cooldownTimer = 0;
        this.active = false;
        this.activeTimer = 0;
        switch (type) {
            case WHIRLWIND:    name = "Whirlwind"; break;
            case THUNDER:      name = "Thunderstorm"; break;
            case SHADOW_CLONE: name = "Shadow Clone"; break;
            default:           name = "Unknown";
        }
    }

    public void setCooldown(double cd) {
        this.cooldown = cd;
    }

    public boolean isReady() {
        return cooldownTimer <= 0 && !active;
    }

    public double cooldownPct() {
        if (cooldown <= 0) return 1.0;
        return Math.min(1.0, 1.0 - cooldownTimer / cooldown);
    }

    public void update(double dt) {
        if (active) {
            activeTimer -= dt;
            if (activeTimer <= 0) {
                active = false;
                activeTimer = 0;
            }
        }
        if (!active && cooldownTimer > 0) {
            cooldownTimer -= dt;
            if (cooldownTimer < 0) cooldownTimer = 0;
        }
    }

    public void activate(SurvivalGame game) {
        if (!isReady()) return;
        cooldownTimer = cooldown;
        active = true;
        Player p = game.player;
        if (p == null) return;

        switch (type) {
            case WHIRLWIND:
                activeTimer = 3.0;
                break;
            case THUNDER:
                activeTimer = 0.1; // instant effect
                thunder(game);
                break;
            case SHADOW_CLONE:
                activeTimer = 5.0;
                break;
        }
    }

    public void onActiveUpdate(SurvivalGame game, double dt) {
        if (!active) return;
        Player p = game.player;
        if (p == null) return;

        switch (type) {
            case WHIRLWIND:
                // Fire a ring of golden sword waves every 0.3s
                if (((int)((activeTimer + 0.01) / 0.3)) != ((int)((activeTimer + dt + 0.01) / 0.3))) {
                    for (int i = 0; i < 10; i++) {
                        double angle = Math.toRadians(i * 36 + activeTimer * 150);
                        Projectile proj = new Projectile(game, p.x, p.y, 10, 300, angle,
                            50 * p.damageMultiplier, 0.9, 2, true, new java.awt.Color(255, 200, 40), Projectile.TYPE_SWORD_WAVE);
                        game.addProjectile(proj);
                    }
                }
                break;
            case SHADOW_CLONE:
                // Rapid-fire knives toward nearest enemy every 0.15s
                if (((int)((activeTimer + 0.01) / 0.15)) != ((int)((activeTimer + dt + 0.01) / 0.15))) {
                    Enemy nearest = game.findNearestEnemy(p.x, p.y);
                    if (nearest != null) {
                        double angle = Math.atan2(nearest.y - p.y, nearest.x - p.x);
                        for (int i = -1; i <= 1; i++) {
                            double a = angle + Math.toRadians(i * 10);
                            Projectile proj = new Projectile(game, p.x, p.y, 3, 550, a,
                                25 * p.damageMultiplier, 1.5, 0, true, new java.awt.Color(220, 220, 220), Projectile.TYPE_ARROW);
                            game.addProjectile(proj);
                        }
                    }
                }
                break;
        }
    }

    private void thunder(SurvivalGame game) {
        Player p = game.player;
        int hits = 0;
        for (Enemy e : game.enemies) {
            if (!e.alive) continue;
            if (hits >= 15) break;
            double dx = e.x - p.x;
            double dy = e.y - p.y;
            if (dx * dx + dy * dy <= 500 * 500) {
                e.takeDamage(80 * p.damageMultiplier, p);
                e.vx *= 0.3;
                e.vy *= 0.3;
                game.vfx.spawnDamageText(e.x, e.y - e.radius, 80 * p.damageMultiplier);
                game.vfx.addShake(3);
                hits++;
            }
        }
    }
}
