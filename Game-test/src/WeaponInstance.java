public class WeaponInstance {
    public final int weaponId;
    public int level = 1;
    public double cooldownTimer = 0;
    public WeaponRarity rarity = WeaponRarity.COMMON;

    public WeaponInstance(int weaponId) {
        this.weaponId = weaponId;
    }

    public WeaponInstance(int weaponId, WeaponRarity rarity, int bonusLevels) {
        this.weaponId = weaponId;
        this.rarity = rarity;
        this.level = Math.min(def().maxLevel, 1 + bonusLevels);
    }

    public WeaponDef def() {
        return WeaponDef.ALL[weaponId];
    }

    public boolean canLevelUp() {
        return level < def().maxLevel;
    }

    public void levelUp() {
        if (canLevelUp()) level++;
    }

    public double currentCooldown(Player player) {
        double cd = def().baseCooldown * (1.0 - 0.08 * (level - 1));
        cd *= player.cooldownMultiplier;
        return Math.max(0.1, cd);
    }

    public double currentDamage(Player player) {
        return def().baseDamage * (1.0 + 0.15 * (level - 1))
               * player.damageMultiplier * rarity.damageMultiplier;
    }

    public int currentProjectileCount() {
        return def().projectileCount + (level >= 3 ? 1 : 0) + (level >= 5 ? 1 : 0);
    }
}
