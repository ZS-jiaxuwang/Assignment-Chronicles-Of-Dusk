import java.io.File;

public final class AssetLibrary {
    private AssetLibrary() {}

    private static final String[] SPRITE_DIR_CANDIDATES = {
        "assets/sprites",
        "Game-test/assets/sprites",
        "Assignment-Chronicles-Of-Dusk/assets/sprites",
        "Assignment-Chronicles-Of-Dusk/Game-test/assets/sprites"
    };

    public static final String CHAR_WARRIOR = "character_warrior.png";
    public static final String CHAR_MAGE = "character_mage.png";
    public static final String CHAR_ASSASSIN = "character_assassin.png";

    public static final String ENEMY_SLIME = "enemy_slime.png";
    public static final String ENEMY_BAT = "enemy_bat.png";
    public static final String ENEMY_SKELETON = "enemy_skeleton.png";
    public static final String ENEMY_GIANT = "enemy_giant.png";
    public static final String ENEMY_GHOST = "enemy_ghost.png";
    public static final String ENEMY_BOSS = "enemy_boss.png";
    public static final String ENEMY_GOBLIN = "enemy_goblin.png";
    public static final String ENEMY_MUSHROOM = "enemy_mushroom.png";
    public static final String ENEMY_FLYING_EYE = "enemy_flyingeye.png";

    public static final String ORC_IDLE = "enemy_orc_idle.png";
    public static final String ORC_WALK = "enemy_orc_walk.png";
    public static final String ORC_HURT = "enemy_orc_hurt.png";
    public static final String ORC_DEATH = "enemy_orc_death.png";
    public static final String ORC_ATTACK = "enemy_orc_attack.png";

    public static final String OBSTACLE_BUSH = "obstacle_bush.png";

    private static final String[] TREE_SPRITES = {
        "obstacle_tree_0.png", "obstacle_tree_1.png", "obstacle_tree_2.png",
        "obstacle_tree_3.png", "obstacle_tree_4.png", "obstacle_tree_5.png"
    };

    private static final String[] ROCK_SPRITES = {
        "obstacle_rock_0.png", "obstacle_rock_1.png", "obstacle_rock_2.png",
        "obstacle_rock_3.png", "obstacle_rock_4.png", "obstacle_rock_5.png",
        "obstacle_rock_6.png", "obstacle_rock_7.png", "obstacle_rock_8.png",
        "obstacle_rock_9.png", "obstacle_rock_10.png", "obstacle_rock_11.png",
        "obstacle_rock_12.png", "obstacle_rock_13.png", "obstacle_rock_14.png",
        "obstacle_rock_15.png", "obstacle_rock_16.png", "obstacle_rock_17.png"
    };

    public static File resolveSpriteFile(String filename) {
        for (String dir : SPRITE_DIR_CANDIDATES) {
            File f = new File(dir, filename);
            if (f.exists()) return f;
        }
        return new File(SPRITE_DIR_CANDIDATES[0], filename);
    }

    public static String swordsmanAnimFile(int tier, int anim) {
        String prefix;
        if (tier >= 4) prefix = "char_swordsman_lvl3";
        else if (tier >= 3) prefix = "char_swordsman_lvl2";
        else prefix = "char_swordsman_lvl1";
        switch (anim) {
            case SpriteManager.ANIM_WALK: return prefix + "_run.png";
            case SpriteManager.ANIM_ATTACK: return prefix + "_attack.png";
            case SpriteManager.ANIM_HURT: return prefix + "_hurt.png";
            case SpriteManager.ANIM_DEATH: return prefix + "_death.png";
            default: return prefix + "_idle.png";
        }
    }

    public static String assassinAnimFile(int tier, int anim) {
        String prefix;
        if (tier >= 4) prefix = "char_assassin_lvl3";
        else if (tier >= 3) prefix = "char_assassin_lvl2";
        else prefix = "char_assassin_lvl1";
        switch (anim) {
            case SpriteManager.ANIM_WALK: return prefix + "_run.png";
            case SpriteManager.ANIM_ATTACK: return prefix + "_attack.png";
            case SpriteManager.ANIM_HURT: return prefix + "_hurt.png";
            case SpriteManager.ANIM_DEATH: return prefix + "_hurt.png";
            default: return prefix + "_idle.png";
        }
    }

    public static String treeSpriteBySeed(int seed) {
        return TREE_SPRITES[Math.floorMod(seed, TREE_SPRITES.length)];
    }

    public static String rockSpriteBySeed(int seed) {
        return ROCK_SPRITES[Math.floorMod(seed, ROCK_SPRITES.length)];
    }
}
