import java.util.ArrayList;

public class WeaponManager {
    private final SurvivalGame game;
    private final ArrayList<WeaponInstance> weapons = new ArrayList<WeaponInstance>();
    private double garlicTickAccumulator = 0;
    double dropCooldown;

    public WeaponManager(SurvivalGame game) {
        this.game = game;
    }

    public void reset(int firstWeaponId) {
        weapons.clear();
        dropCooldown = 0;
        addWeapon(firstWeaponId);
    }

    public void update(double dt) {
        Player player = game.player;
        if (player == null || !player.alive) return;

        for (WeaponInstance w : weapons) {
            w.cooldownTimer -= dt;
            if (w.cooldownTimer <= 0) {
                fire(w);
                w.cooldownTimer += w.currentCooldown(player);
            }
        }

        garlicTickAccumulator += dt;
        if (garlicTickAccumulator >= 0.1) {
            garlicTickAccumulator = 0;
            applyGarlicAura();
        }

        if (dropCooldown > 0) dropCooldown -= dt;
    }

    public boolean canDropWeapon() {
        return dropCooldown <= 0;
    }

    public void markDrop() {
        dropCooldown = GameConfig.WEAPON_DROP_COOLDOWN;
    }

    private void applyGarlicAura() {
        WeaponInstance garlic = getWeapon(WeaponDef.GARLIC);
        if (garlic == null) return;
        double radius = 66 + (garlic.level - 1) * 12;
        double damage = garlic.currentDamage(game.player) * 0.1;
        double rr = radius * radius;
        for (Enemy e : game.enemies) {
            if (!e.alive) continue;
            double dx = e.x - game.player.x;
            double dy = e.y - game.player.y;
            if (dx * dx + dy * dy <= rr) {
                e.takeDamage(damage, game.player);
            }
        }
    }

    private void fire(WeaponInstance w) {
        WeaponDef def = w.def();
        Player player = game.player;

        // Melee / short-range weapons trigger attack animation
        if (w.weaponId == WeaponDef.WHIP || w.weaponId == WeaponDef.KNIFE) {
            player.triggerAttackAnim();
        }

        int count = w.currentProjectileCount();
        double spread = Math.toRadians(def.spreadAngle);
        double baseAngle;

        if (def.targetingMode == WeaponDef.TARGET_NEAREST) {
            Enemy nearest = game.findNearestEnemy(player.x, player.y);
            if (nearest == null) return;
            baseAngle = Math.atan2(nearest.y - player.y, nearest.x - player.x);
        } else if (def.targetingMode == WeaponDef.TARGET_RANDOM) {
            baseAngle = Math.toRadians(game.rand(360.0));
        } else if (def.targetingMode == WeaponDef.TARGET_FORWARD_ARC) {
            baseAngle = -Math.PI / 2;
        } else if (def.targetingMode == WeaponDef.TARGET_SELF) {
            return;
        } else {
            baseAngle = player.facingAngleRad;
        }

        for (int i = 0; i < count; i++) {
            double t = count <= 1 ? 0 : (i / (double)(count - 1)) * 2 - 1;
            double angle = baseAngle + t * spread;
            Projectile p = new Projectile(game, player.x, player.y, def.projectileRadius, def.projectileSpeed,
                angle, w.currentDamage(player), def.lifetime, w.level >= 5 ? 1 : 0, true, def.projColor);
            if (w.weaponId == WeaponDef.AXE) {
                p.gravityY = 200;
            }
            if (w.weaponId == WeaponDef.WHIP) {
                p.x += Math.cos(angle) * 26;
                p.y += Math.sin(angle) * 26;
            }
            game.addProjectile(p);
        }
    }

    public void pickupWeapon(int weaponId, WeaponRarity rarity) {
        WeaponInstance existing = getWeapon(weaponId);
        if (existing != null) {
            if (existing.canLevelUp()) {
                existing.levelUp();
                System.out.println("[Pickup] Upgraded " + WeaponDef.ALL[weaponId].name + " to Lv" + existing.level);
            } else {
                System.out.println("[Pickup] " + WeaponDef.ALL[weaponId].name + " already MAX");
            }
            return;
        }
        if (weapons.size() < maxSlots()) {
            weapons.add(new WeaponInstance(weaponId, rarity, rarity.bonusLevels));
            System.out.println("[Pickup] Added " + rarity.name() + " " + WeaponDef.ALL[weaponId].name);
            return;
        }
        System.out.println("[Pickup] Slots full, opening swap for " + WeaponDef.ALL[weaponId].name);
        game.enterWeaponSwap(weaponId, rarity);
    }

    public boolean addWeapon(int weaponId) {
        if (hasWeapon(weaponId) || weapons.size() >= maxSlots()) return false;
        weapons.add(new WeaponInstance(weaponId));
        return true;
    }

    public boolean upgradeWeapon(int weaponId) {
        WeaponInstance w = getWeapon(weaponId);
        if (w == null || !w.canLevelUp()) return false;
        w.levelUp();
        return true;
    }

    public void replaceWeapon(int slotIndex, int newWeaponId, WeaponRarity rarity) {
        if (slotIndex < 0 || slotIndex >= weapons.size()) return;
        weapons.set(slotIndex, new WeaponInstance(newWeaponId, rarity, rarity.bonusLevels));
    }

    public int maxSlots() {
        return GameConfig.MAX_WEAPON_SLOTS;
    }

    public boolean hasWeapon(int weaponId) {
        return getWeapon(weaponId) != null;
    }

    public WeaponInstance getWeapon(int weaponId) {
        for (WeaponInstance w : weapons) {
            if (w.weaponId == weaponId) return w;
        }
        return null;
    }

    public ArrayList<WeaponInstance> list() {
        return weapons;
    }
}
