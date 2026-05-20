import java.awt.Color;

public final class GameConfig {
    private GameConfig() {}

    public static final int WINDOW_WIDTH = 960;
    public static final int WINDOW_HEIGHT = 720;

    public static final int WORLD_WIDTH = 3600;
    public static final int WORLD_HEIGHT = 2400;

    public static final double CAMERA_LERP_SPEED = 5.0;

    public static final double PLAYER_RADIUS = 36.0;
    public static final double PLAYER_BASE_SPEED = 210.0;
    public static final double PLAYER_MAX_HP = 100.0;
    public static final double PLAYER_INVULN_SECONDS = 0.3;

    public static final double PICKUP_RADIUS = 5.0;
    public static final double PICKUP_MAGNET_STRENGTH = 400.0;

    public static final double XP_BASE_REQUIREMENT = 5.0;
    public static final double XP_LEVEL_SCALAR = 3.0;

    public static final int MAX_ENEMIES = 240;
    public static final int MAX_PROJECTILES = 320;
    public static final int MAX_PICKUPS = 180;

    public static final double BOSS_TRIGGER_SECONDS = 300.0;
    public static final double BOSS_INTRO_SECONDS = 2.0;

    public static final double WEAPON_DROP_CHANCE = 0.15;
    public static final double WEAPON_DROP_CHANCE_ELITE = 0.30;
    public static final double WEAPON_DROP_CHANCE_BOSS = 1.0;
    public static final double WEAPON_DROP_COOLDOWN = 3.0;
    public static final double WEAPON_DROP_MAGNET_RANGE = 180.0;
    public static final int MAX_WEAPON_SLOTS = 6;

    public static final int TIER2_LEVEL = 25;
    public static final int TIER3_LEVEL = 50;
    public static final int TIER4_LEVEL = 75;
    public static final double TIER_UP_INTRO_SECONDS = 2.5;
    public static final double ULTIMATE_COOLDOWN = 30.0;
    public static final double ULTIMATE_COOLDOWN_TIER4 = 15.0;

    // Score system
    public static final int SCORE_BOSS_KILL = 5000;
    public static final int SCORE_FULL_HP_BONUS = 2000;
    public static final double SCORE_SURVIVAL_PER_SECOND = 10.0;
    public static final int SCORE_BASE_SLIME = 100;
    public static final int SCORE_BASE_BAT = 150;
    public static final int SCORE_BASE_GOBLIN = 200;
    public static final int SCORE_BASE_SKELETON = 300;
    public static final int SCORE_BASE_GHOST = 300;
    public static final int SCORE_BASE_FLYING_EYE = 350;
    public static final int SCORE_BASE_ORC = 500;
    public static final int SCORE_BASE_MUSHROOM = 600;
    public static final int SCORE_BASE_GIANT = 1000;
    public static final double SCORE_RARITY_NORMAL = 1.0;
    public static final double SCORE_RARITY_RARE = 1.5;
    public static final double SCORE_RARITY_EPIC = 2.0;
    public static final double SCORE_RARITY_LEGENDARY = 3.0;
    public static final double SCORE_MILESTONE_LV25 = 1.5;
    public static final double SCORE_MILESTONE_LV50 = 2.0;
    public static final double SCORE_MILESTONE_LV75 = 3.0;

    public static final Color BG_DARK = new Color(12, 16, 24);
    public static final Color WORLD_GROUND = new Color(18, 24, 36);
    public static final Color WORLD_BORDER = new Color(75, 95, 130);
}
