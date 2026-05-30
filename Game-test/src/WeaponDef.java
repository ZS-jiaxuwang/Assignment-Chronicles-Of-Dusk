import java.awt.Color;
// Jiaxu Wang (24009377)  Jiaheng Liu (24009483)  Angze Song (24009333)   Xiao Wu (24009458)
public class WeaponDef {
    public static final int SWORD = 0;
    public static final int MAGIC_WAND = 1;
    public static final int AXE = 2;
    public static final int GARLIC = 3;
    public static final int KNIFE = 4;

    public static final int TARGET_NEAREST = 0;
    public static final int TARGET_FORWARD = 1;
    public static final int TARGET_SELF = 2;
    public static final int TARGET_RANDOM = 3;
    public static final int TARGET_FORWARD_ARC = 4;

    public static final WeaponDef[] ALL = new WeaponDef[20];

    static {
        ALL[SWORD] = new WeaponDef("Sword", 1.5, 18, 380, 14, 0.55, 3, 24, TARGET_FORWARD, new Color(255, 200, 40), 5, -1, 1, 9.0);
        ALL[MAGIC_WAND] = new WeaponDef("Fire Staff", 1.0, 10, 400, 8, 1.4, 1, 8, TARGET_NEAREST, new Color(255, 140, 20), 5, -1, 0, 0.0);
        ALL[AXE] = new WeaponDef("Arcane Circle", 30.0, 80, 0, 200, 0, 1, 0, TARGET_SELF, new Color(220, 220, 220), 5, -1, 0, 0.0);
        ALL[GARLIC] = new WeaponDef("Boomerang", 1.8, 4, 350, 24, 4.0, 1, 0, TARGET_RANDOM, new Color(140, 200, 160), 5, -1, 0, 0.0);
        ALL[KNIFE] = new WeaponDef("Throwing Dagger", 0.35, 7, 500, 5, 0.7, 1, 0, TARGET_NEAREST, new Color(220, 220, 240), 5, -1, 0, 0.0);
    }

    public final String name;
    public final double baseCooldown;
    public final double baseDamage;
    public final double projectileSpeed;
    public final double projectileRadius;
    public final double lifetime;
    public final int projectileCount;
    public final double spreadAngle;
    public final int targetingMode;
    public final Color projColor;
    public final int maxLevel;
    public final int evolvesInto;
    public final int projectilesPerLevel;
    public final double spreadPerLevel;

    public WeaponDef(String name, double baseCooldown, double baseDamage, double projectileSpeed,
                     double projectileRadius, double lifetime, int projectileCount, double spreadAngle,
                     int targetingMode, Color projColor, int maxLevel, int evolvesInto,
                     int projectilesPerLevel, double spreadPerLevel) {
        this.name = name;
        this.baseCooldown = baseCooldown;
        this.baseDamage = baseDamage;
        this.projectileSpeed = projectileSpeed;
        this.projectileRadius = projectileRadius;
        this.lifetime = lifetime;
        this.projectileCount = projectileCount;
        this.spreadAngle = spreadAngle;
        this.targetingMode = targetingMode;
        this.projColor = projColor;
        this.maxLevel = maxLevel;
        this.evolvesInto = evolvesInto;
        this.projectilesPerLevel = projectilesPerLevel;
        this.spreadPerLevel = spreadPerLevel;
    }
}
