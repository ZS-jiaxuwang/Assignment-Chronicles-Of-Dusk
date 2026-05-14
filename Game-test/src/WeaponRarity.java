import java.awt.Color;

public enum WeaponRarity {
    COMMON(1.0, 0, new Color(200, 200, 200), 50),
    RARE(1.2, 1, new Color(80, 160, 255), 30),
    EPIC(1.4, 2, new Color(180, 80, 255), 15),
    LEGENDARY(1.6, 3, new Color(255, 200, 50), 5);

    public final double damageMultiplier;
    public final int bonusLevels;
    public final Color beamColor;
    public final int weight;

    WeaponRarity(double dmgMul, int bonusLv, Color beam, int w) {
        this.damageMultiplier = dmgMul;
        this.bonusLevels = bonusLv;
        this.beamColor = beam;
        this.weight = w;
    }

    public static WeaponRarity roll() {
        int total = 0;
        for (WeaponRarity r : values()) total += r.weight;
        int roll = (int)(Math.random() * total);
        int cumulative = 0;
        for (WeaponRarity r : values()) {
            cumulative += r.weight;
            if (roll < cumulative) return r;
        }
        return COMMON;
    }
}
