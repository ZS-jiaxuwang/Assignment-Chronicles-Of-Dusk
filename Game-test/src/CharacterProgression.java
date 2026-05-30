// Jiaxu Wang (24009377)  Jiaheng Liu (24009483)  Angze Song (24009333)   Xiao Wu (24009458)
public class CharacterProgression {

    public static int tierForLevel(int level) {
        if (level >= GameConfig.TIER3_LEVEL) return 3;
        if (level >= GameConfig.TIER2_LEVEL) return 2;
        return 1;
    }

    public static boolean justCrossedTier(int oldLevel, int newLevel) {
        return tierForLevel(oldLevel) < tierForLevel(newLevel);
    }

    public static double hpMultiplier(int tier) {
        switch (tier) {
            case 2: return 1.6;
            case 3: return 2.2;
            default: return 1.0;
        }
    }

    public static double damageMultiplier(int tier) {
        switch (tier) {
            case 2: return 1.6;
            case 3: return 2.2;
            default: return 1.0;
        }
    }

    public static double speedMultiplier(int tier) {
        switch (tier) {
            case 2: return 1.15;
            case 3: return 1.25;
            default: return 1.0;
        }
    }

    public static double cooldownMultiplier(int tier) {
        switch (tier) {
            case 2: return 0.82;
            case 3: return 0.70;
            default: return 1.0;
        }
    }
}
