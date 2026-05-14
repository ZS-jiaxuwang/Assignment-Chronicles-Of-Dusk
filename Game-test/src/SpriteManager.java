import java.awt.Color;
import java.awt.Image;

public class SpriteManager {
    public static final int ANIM_IDLE  = 0;
    public static final int ANIM_WALK  = 1;
    public static final int ANIM_HURT  = 2;
    public static final int ANIM_DEATH = 3;
    public static final int ANIM_ATTACK = 4;

    public static final int DIR_DOWN  = 0;
    public static final int DIR_LEFT  = 1;
    public static final int DIR_RIGHT = 2;
    public static final int DIR_UP    = 3;

    // Swordsman: 64x64 frames, 4 rows = 4 dirs, scale 2.0 = 128px visual
    private static final int S_FRAME = 64;
    private static final double S_SCALE = 2.0;

    // Orc: 100x100 frames
    private static final int O_FRAME = 100;
    private static final double O_SCALE = 3.2;

    private static GameEngine engine;

    public static void init(GameEngine g) {
        engine = g;
        AssetLoader.init(g);
    }

    // ── Swordsman (Warrior) ──

    public static void drawSwordsman(int tier, int anim, int dir, double animTimer,
                                      double cx, double cy) {
        String file = swordsmanFile(tier, anim);
        if (file == null || !AssetLoader.hasAsset(file)) {
            drawFallbackCharacter(cx, cy, 16, new Color(80, 160, 250), 0, anim, animTimer);
            return;
        }
        int cols = totalFrames(file, S_FRAME);
        double fps = animFps(anim);
        int col = ((int)(animTimer * fps)) % Math.max(1, cols);
        int row = Math.min(dir, 3);
        Image frame = AssetLoader.getFrame(file, col, row, S_FRAME, S_FRAME);
        if (frame == null) {
            drawFallbackCharacter(cx, cy, 16, new Color(80, 160, 250), 0, anim, animTimer);
            return;
        }
        double rw = S_FRAME * S_SCALE;
        double rh = S_FRAME * S_SCALE;
        engine.drawImage(frame, cx - rw * 0.5, cy - rh * 0.6, rw, rh);
    }

    private static String swordsmanFile(int tier, int anim) {
        return AssetLibrary.swordsmanAnimFile(tier, anim);
    }

    // ── Assassin (80x80 frames, scale 1.6 = 128px) ──
    private static final int A_FRAME = 80;
    private static final double A_SCALE = 1.6;

    public static void drawAssassin(int tier, int anim, int dir, double animTimer,
                                     double cx, double cy) {
        String file = assassinFile(tier, anim);
        if (file == null || !AssetLoader.hasAsset(file)) {
            drawFallbackCharacter(cx, cy, 16, new Color(220, 220, 220), dir, anim, animTimer);
            return;
        }
        int cols = totalFrames(file, A_FRAME);
        double fps = animFps(anim);
        int col = ((int)(animTimer * fps)) % Math.max(1, cols);
        int row = Math.min(dir, 3);
        Image frame = AssetLoader.getFrame(file, col, row, A_FRAME, A_FRAME);
        if (frame == null) {
            drawFallbackCharacter(cx, cy, 16, new Color(220, 220, 220), dir, anim, animTimer);
            return;
        }
        double rw = A_FRAME * A_SCALE;
        double rh = A_FRAME * A_SCALE;
        engine.drawImage(frame, cx - rw * 0.5, cy - rh * 0.6, rw, rh);
    }

    private static String assassinFile(int tier, int anim) {
        return AssetLibrary.assassinAnimFile(tier, anim);
    }

    // ── Orc ──

    public static void drawOrc(int anim, double animTimer,
                                double cx, double cy, double radius, Color color,
                                boolean flipX) {
        String file = animFile(AssetLibrary.ORC_IDLE, AssetLibrary.ORC_WALK,
            AssetLibrary.ORC_HURT, AssetLibrary.ORC_DEATH, AssetLibrary.ORC_ATTACK, anim);
        if (file == null || !AssetLoader.hasAsset(file)) {
            drawFallbackEnemy(cx, cy, radius, color, anim, animTimer);
            return;
        }
        int totalFrames = totalFrames(file, O_FRAME);
        double fps = (anim == ANIM_WALK) ? 10.0 : 6.0;
        int col = ((int)(animTimer * fps)) % Math.max(1, totalFrames);
        Image frame = AssetLoader.getFrame(file, col, O_FRAME, O_FRAME);
        if (frame == null) {
            drawFallbackEnemy(cx, cy, radius, color, anim, animTimer);
            return;
        }
        double rw = O_FRAME * O_SCALE;
        double rh = O_FRAME * O_SCALE;
        if (flipX) {
            engine.drawImage(frame, cx + rw * 0.5, cy - rh * ENEMY_Y_ANCHOR, -rw, rh);
        } else {
            engine.drawImage(frame, cx - rw * 0.5, cy - rh * ENEMY_Y_ANCHOR, rw, rh);
        }
    }

    // ── Single-sheet monsters (one sprite sheet for all animations, 150x150 frames) ──

    private static final int M_FRAME = 150;
    private static final double M_SCALE = 2.0;
    // Lower enemy sprite anchor so visual body aligns with circle hitbox.
    private static final double ENEMY_Y_ANCHOR = 0.55;

    public static void drawMonsterSingle(String sheetName, int anim, double animTimer,
                                          double cx, double cy, double radius, Color color,
                                          boolean flipX) {
        if (!AssetLoader.hasAsset(sheetName)) {
            drawFallbackEnemy(cx, cy, radius, color, anim, animTimer);
            return;
        }
        int totalFrames = totalFrames(sheetName, M_FRAME);
        int col;
        if (anim == ANIM_IDLE) {
            col = 0;
        } else if (anim == ANIM_ATTACK) {
            col = ((int)(animTimer * 12.0)) % Math.max(1, totalFrames);
        } else {
            col = ((int)(animTimer * 8.0)) % Math.max(1, totalFrames);
        }
        Image frame = AssetLoader.getFrame(sheetName, col, M_FRAME, M_FRAME);
        if (frame == null) {
            drawFallbackEnemy(cx, cy, radius, color, anim, animTimer);
            return;
        }
        double rw = M_FRAME * M_SCALE;
        double rh = M_FRAME * M_SCALE;
        if (flipX) {
            engine.drawImage(frame, cx + rw * 0.5, cy - rh * ENEMY_Y_ANCHOR, -rw, rh);
        } else {
            engine.drawImage(frame, cx - rw * 0.5, cy - rh * ENEMY_Y_ANCHOR, rw, rh);
        }
    }

    // ── Legacy (no sprites, always fallback) ──

    public static void drawCharacter(String assetKey, int anim, int dir,
                                      double animTimer, double cx, double cy,
                                      double radius, Color color) {
        drawFallbackCharacter(cx, cy, radius, color, dir, anim, animTimer);
    }

    public static void drawEnemy(String assetKey, int anim, double animTimer,
                                  double cx, double cy, double radius, Color color) {
        drawFallbackEnemy(cx, cy, radius, color, anim, animTimer);
    }

    public static void drawGenericEnemy(String prefix, int anim, double animTimer,
                                         double cx, double cy, double radius, Color color) {
        String file = animFile(prefix + "_idle.png", prefix + "_walk.png",
            prefix + "_hurt.png", prefix + "_death.png", prefix + "_attack.png", anim);
        if (file == null || !AssetLoader.hasAsset(file)) {
            drawFallbackEnemy(cx, cy, radius, color, anim, animTimer);
            return;
        }
        int totalFrames = totalFrames(file, 100);
        double fps = (anim == ANIM_WALK) ? 10.0 : 6.0;
        int col = ((int)(animTimer * fps)) % Math.max(1, totalFrames);
        Image frame = AssetLoader.getFrame(file, col, 100, 100);
        if (frame == null) {
            drawFallbackEnemy(cx, cy, radius, color, anim, animTimer);
            return;
        }
        double scale = 3.2;
        double rw = 100 * scale;
        double rh = 100 * scale;
        engine.drawImage(frame, cx - rw * 0.5, cy - rh * ENEMY_Y_ANCHOR, rw, rh);
    }

    // ── Helpers ──

    private static String animFile(String idle, String walk, String hurt,
                                    String death, String attack, int anim) {
        switch (anim) {
            case ANIM_WALK:  return walk;
            case ANIM_HURT:  return hurt;
            case ANIM_DEATH: return death;
            case ANIM_ATTACK: return attack;
            default:         return idle;
        }
    }

    private static double animFps(int anim) {
        switch (anim) {
            case ANIM_WALK:  return 10.0;
            case ANIM_ATTACK: return 8.0;
            case ANIM_HURT:
            case ANIM_DEATH: return 4.0;
            default:         return 6.0;
        }
    }

    private static int totalFrames(String file, int frameSize) {
        java.awt.image.BufferedImage img = AssetLoader.load(file);
        if (img == null) return 1;
        return Math.max(1, img.getWidth() / frameSize);
    }

    // ── Fallback rendering ──

    private static void drawFallbackCharacter(double cx, double cy, double r,
                                               Color c, int dir, int anim, double timer) {
        double bounce = (anim == ANIM_WALK) ? Math.sin(timer * 10.0) * 2.0 : 0;
        cy += bounce;
        drawPixelCircle(cx, cy, r, c, 3);
        engine.changeColor(Color.WHITE);
        engine.drawSolidRectangle(cx - 5, cy - 5, 4, 4);
        engine.drawSolidRectangle(cx + 1, cy - 5, 4, 4);
        double da = getDirAngle(dir);
        double ix = cx + Math.cos(da) * (r + 4);
        double iy = cy + Math.sin(da) * (r + 4);
        engine.changeColor(c.brighter());
        engine.drawSolidRectangle(ix - 3, iy - 3, 6, 6);
    }

    private static void drawFallbackEnemy(double cx, double cy, double r,
                                           Color c, int anim, double timer) {
        double bounce = (anim == ANIM_WALK) ? Math.sin(timer * 8.0) * 2.0 : 0;
        cy += bounce;
        drawPixelCircle(cx, cy, r, c, 3);
        if (r > 8) {
            engine.changeColor(Color.WHITE);
            engine.drawSolidRectangle(cx - 4, cy - 4, 3, 3);
            engine.drawSolidRectangle(cx + 1, cy - 4, 3, 3);
        }
    }

    private static void drawPixelCircle(double cx, double cy, double r, Color c, int bs) {
        engine.changeColor(c);
        int steps = Math.max(8, (int)(Math.PI * 2 * r / (bs * 2)));
        double step = Math.PI * 2 / steps;
        for (int i = 0; i < steps; i++) {
            double a = i * step;
            engine.drawSolidRectangle(cx + Math.cos(a) * r - bs * 0.5,
                                       cy + Math.sin(a) * r - bs * 0.5, bs, bs);
        }
        engine.drawSolidRectangle(cx - r * 0.5, cy - r * 0.5, r, r * 1.5);
    }

    private static double getDirAngle(int dir) {
        switch (dir) {
            case DIR_LEFT:  return Math.PI;
            case DIR_RIGHT: return 0;
            case DIR_UP:    return -Math.PI / 2;
            default:        return Math.PI / 2;
        }
    }
}
