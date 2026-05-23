import java.awt.Color;

public class CharacterDef {
    public static final int WARRIOR = 0;
    public static final int MAGE = 1;
    public static final int ASSASSIN = 2;

    public final int id;
    public final String name;
    public final Color color;
    public final double hpMultiplier;
    public final double speedMultiplier;
    public final double damageMultiplier;
    public final int startingWeaponId;
    public final int ultimateType;

    public CharacterDef(int id, String name, Color color, double hpMultiplier, double speedMultiplier,
                        double damageMultiplier, int startingWeaponId, int ultimateType) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.hpMultiplier = hpMultiplier;
        this.speedMultiplier = speedMultiplier;
        this.damageMultiplier = damageMultiplier;
        this.startingWeaponId = startingWeaponId;
        this.ultimateType = ultimateType;
    }

    public int getId() {
        return id;
    }

    public static CharacterDef[] all() {
        return new CharacterDef[] {
            new CharacterDef(WARRIOR, "Warrior", new Color(80, 160, 250), 1.25, 0.95, 1.05,
                WeaponDef.SWORD, UltimateSkill.WHIRLWIND),
            new CharacterDef(MAGE, "Mage", new Color(150, 90, 250), 0.95, 1.0, 1.2,
                WeaponDef.MAGIC_WAND, UltimateSkill.THUNDER),
            new CharacterDef(ASSASSIN, "Assassin", new Color(220, 220, 220), 0.85, 1.2, 1.1,
                WeaponDef.KNIFE, UltimateSkill.SHADOW_CLONE)
        };
    }
}
