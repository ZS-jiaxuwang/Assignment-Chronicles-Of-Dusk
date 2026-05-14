import java.awt.Color;

public class WeaponDef {
    public static final int WHIP = 0;
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
        ALL[WHIP] = new WeaponDef("Whip", 1.2, 15, 520, 9, 0.22, 1, 36, TARGET_FORWARD, new Color(230, 190, 110), 5, -1);
        ALL[MAGIC_WAND] = new WeaponDef("Magic Wand", 1.0, 10, 400, 6, 1.4, 1, 8, TARGET_NEAREST, new Color(130, 190, 255), 5, -1);
        ALL[AXE] = new WeaponDef("Axe", 2.0, 20, 260, 10, 2.0, 1, 18, TARGET_FORWARD_ARC, new Color(220, 220, 220), 5, -1);
        ALL[GARLIC] = new WeaponDef("Garlic", 0.5, 5, 0, 66, 0.05, 1, 0, TARGET_SELF, new Color(205, 255, 180), 5, -1);
        ALL[KNIFE] = new WeaponDef("Knife", 0.8, 12, 620, 4, 1.8, 1, 8, TARGET_FORWARD, new Color(250, 250, 250), 5, -1);
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

    public WeaponDef(String name, double baseCooldown, double baseDamage, double projectileSpeed,
                     double projectileRadius, double lifetime, int projectileCount, double spreadAngle,
                     int targetingMode, Color projColor, int maxLevel, int evolvesInto) {
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
    }
}
