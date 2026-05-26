import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Generates pixel-art mage sprite sheets.
 * Format: 64x64 frames, 4 rows (DOWN/LEFT/RIGHT/UP), horizontal strips.
 * Matches the swordsman sprite layout used by SpriteManager.
 */
public class GenerateMageSprites {

    static final int FRAME = 64;
    static final int PIX = 3;

    // ── Tier 1 colors ──
    static final Color H1 = new Color(128, 64, 192);   // hat main
    static final Color HD1 = new Color(72, 32, 120);   // hat dark
    static final Color HL1 = new Color(168, 104, 216); // hat light
    static final Color R1 = new Color(120, 56, 184);   // robe main
    static final Color RD1 = new Color(64, 24, 112);   // robe dark
    static final Color RL1 = new Color(176, 104, 220); // robe light
    static final Color O1 = new Color(120, 200, 255);  // orb main
    static final Color OL1 = new Color(180, 230, 255); // orb light

    // Tier 2
    static final Color H2 = new Color(152, 88, 208);
    static final Color HD2 = new Color(96, 48, 144);
    static final Color HL2 = new Color(192, 128, 232);
    static final Color R2 = new Color(144, 80, 200);
    static final Color RD2 = new Color(88, 40, 136);
    static final Color RL2 = new Color(200, 128, 236);
    static final Color O2 = new Color(80, 220, 255);
    static final Color OL2 = new Color(160, 240, 255);
    static final Color TRIM2 = new Color(200, 180, 100);

    // Tier 3
    static final Color H3 = new Color(168, 104, 220);
    static final Color HD3 = new Color(112, 56, 160);
    static final Color HL3 = new Color(208, 144, 240);
    static final Color R3 = new Color(160, 96, 216);
    static final Color RD3 = new Color(104, 48, 152);
    static final Color RL3 = new Color(216, 144, 244);
    static final Color O3 = new Color(60, 240, 255);
    static final Color OL3 = new Color(140, 255, 255);
    static final Color TRIM3 = new Color(255, 210, 60);

    // Shared
    static final Color SKIN = new Color(255, 215, 175);
    static final Color SKIND = new Color(200, 160, 120);
    static final Color STAFF = new Color(130, 90, 50);
    static final Color STAFFD = new Color(90, 60, 30);
    static final Color EYE = new Color(255, 255, 255);
    static final Color PUPIL = new Color(40, 20, 60);

    static BufferedImage canvas() {
        return new BufferedImage(FRAME, FRAME, BufferedImage.TYPE_INT_ARGB);
    }

    /** Draw a PIX*PIX block at logical coordinates (lx, ly). */
    static void put(BufferedImage img, int lx, int ly, Color c) {
        int bx = 10 * PIX + lx * PIX;
        int by = 2 * PIX + ly * PIX;
        int rgb = c.getRGB();
        for (int dy = 0; dy < PIX; dy++) {
            for (int dx = 0; dx < PIX; dx++) {
                int x = bx + dx, y = by + dy;
                if (x >= 0 && x < FRAME && y >= 0 && y < FRAME) {
                    img.setRGB(x, y, rgb);
                }
            }
        }
    }

    /** Flip image horizontally. */
    static BufferedImage mirror(BufferedImage src) {
        BufferedImage out = canvas();
        for (int y = 0; y < FRAME; y++) {
            for (int x = 0; x < FRAME; x++) {
                int rgb = src.getRGB(x, y);
                if ((rgb & 0xFF000000) != 0) {
                    out.setRGB(FRAME - 1 - x, y, rgb);
                }
            }
        }
        return out;
    }

    // ═══════════════════════════════════════════════════════
    //  Body part drawers (draw directly onto BufferedImage)
    // ═══════════════════════════════════════════════════════

    static Color H(int tier) { return tier==1?H1 : tier==2?H2 : H3; }
    static Color HD(int tier) { return tier==1?HD1 : tier==2?HD2 : HD3; }
    static Color HL(int tier) { return tier==1?HL1 : tier==2?HL2 : HL3; }
    static Color R(int tier) { return tier==1?R1 : tier==2?R2 : R3; }
    static Color RD(int tier) { return tier==1?RD1 : tier==2?RD2 : RD3; }
    static Color RL(int tier) { return tier==1?RL1 : tier==2?RL2 : RL3; }
    static Color O(int tier) { return tier==1?O1 : tier==2?O2 : O3; }
    static Color OL(int tier) { return tier==1?OL1 : tier==2?OL2 : OL3; }

    static void drawHat(BufferedImage img, int tier, int by) {
        Color h = H(tier), hd = HD(tier), hl = HL(tier);
        // tip
        put(img, 5, by, hd); put(img, 6, by, hd);
        put(img, 4, by+1, hd); put(img, 5, by+1, hl); put(img, 6, by+1, hl); put(img, 7, by+1, hd);
        put(img, 3, by+2, hd); put(img, 4, by+2, h); put(img, 5, by+2, h); put(img, 6, by+2, h); put(img, 7, by+2, h); put(img, 8, by+2, hd);
        put(img, 2, by+3, hd); put(img, 3, by+3, h); put(img, 4, by+3, h); put(img, 5, by+3, hl); put(img, 6, by+3, h); put(img, 7, by+3, h); put(img, 8, by+3, h); put(img, 9, by+3, hd);
        put(img, 2, by+4, hd); put(img, 3, by+4, h); put(img, 4, by+4, h); put(img, 5, by+4, h); put(img, 6, by+4, h); put(img, 7, by+4, h); put(img, 8, by+4, h); put(img, 9, by+4, hd);
        // brim
        put(img, 1, by+5, hd); put(img, 2, by+5, hl); put(img, 3, by+5, hl); put(img, 4, by+5, h); put(img, 5, by+5, h); put(img, 6, by+5, h); put(img, 7, by+5, hl); put(img, 8, by+5, hl); put(img, 9, by+5, hl); put(img, 10, by+5, hd);
        put(img, 1, by+6, hd); put(img, 2, by+6, hd); put(img, 3, by+6, hd); put(img, 4, by+6, h); put(img, 5, by+6, h); put(img, 6, by+6, h); put(img, 7, by+6, hd); put(img, 8, by+6, hd); put(img, 9, by+6, hd); put(img, 10, by+6, hd);
    }

    static void drawFace(BufferedImage img, int by) {
        put(img, 4, by, SKIN); put(img, 5, by, SKIN); put(img, 6, by, SKIN); put(img, 7, by, SKIN);
        put(img, 3, by+1, SKIND); put(img, 4, by+1, SKIN); put(img, 5, by+1, SKIN); put(img, 6, by+1, SKIN); put(img, 7, by+1, SKIN); put(img, 8, by+1, SKIND);
        // eyes
        put(img, 4, by+1, EYE); put(img, 6, by+1, EYE);
        put(img, 4, by+1, PUPIL); put(img, 6, by+1, PUPIL);
    }

    static void drawRobe(BufferedImage img, int tier, int by) {
        Color r = R(tier), rd = RD(tier), rl = RL(tier);
        // shoulders
        put(img, 2, by, rd); put(img, 3, by, r); put(img, 4, by, r); put(img, 5, by, r); put(img, 6, by, r); put(img, 7, by, r); put(img, 8, by, r); put(img, 9, by, rd);
        put(img, 1, by+1, rd); put(img, 2, by+1, r); put(img, 3, by+1, rl); put(img, 4, by+1, r); put(img, 5, by+1, r); put(img, 6, by+1, r); put(img, 7, by+1, rl); put(img, 8, by+1, r); put(img, 9, by+1, r); put(img, 10, by+1, rd);
        put(img, 1, by+2, rd); put(img, 2, by+2, r); put(img, 3, by+2, r); put(img, 4, by+2, r); put(img, 5, by+2, r); put(img, 6, by+2, r); put(img, 7, by+2, r); put(img, 8, by+2, r); put(img, 9, by+2, r); put(img, 10, by+2, rd);
        put(img, 2, by+3, rd); put(img, 3, by+3, r); put(img, 4, by+3, r); put(img, 5, by+3, r); put(img, 6, by+3, r); put(img, 7, by+3, r); put(img, 8, by+3, r); put(img, 9, by+3, rd);
        put(img, 2, by+4, rd); put(img, 3, by+4, r); put(img, 4, by+4, r); put(img, 5, by+4, r); put(img, 6, by+4, r); put(img, 7, by+4, r); put(img, 8, by+4, r); put(img, 9, by+4, rd);
        put(img, 2, by+5, rd); put(img, 3, by+5, r); put(img, 4, by+5, r); put(img, 5, by+5, r); put(img, 6, by+5, r); put(img, 7, by+5, r); put(img, 8, by+5, rd);
        put(img, 2, by+6, rd); put(img, 3, by+6, r); put(img, 4, by+6, r); put(img, 5, by+6, r); put(img, 6, by+6, r); put(img, 7, by+6, r); put(img, 8, by+6, rd);
        put(img, 1, by+7, rd); put(img, 2, by+7, r); put(img, 3, by+7, r); put(img, 4, by+7, r); put(img, 5, by+7, r); put(img, 6, by+7, r); put(img, 7, by+7, r); put(img, 8, by+7, r); put(img, 9, by+7, r); put(img, 10, by+7, rd);
        put(img, 1, by+8, rd); put(img, 2, by+8, r); put(img, 3, by+8, r); put(img, 4, by+8, r); put(img, 5, by+8, r); put(img, 6, by+8, r); put(img, 7, by+8, r); put(img, 8, by+8, r); put(img, 9, by+8, r); put(img, 10, by+8, rd);
        put(img, 2, by+9, rd); put(img, 3, by+9, r); put(img, 4, by+9, r); put(img, 5, by+9, r); put(img, 6, by+9, r); put(img, 7, by+9, r); put(img, 8, by+9, r); put(img, 9, by+9, rd);
    }

    static void drawStaff(BufferedImage img, int tier, int by) {
        Color o = O(tier), ol = OL(tier);
        // orb at top
        put(img, 9, by, ol); put(img, 10, by, ol);
        put(img, 9, by+1, o); put(img, 10, by+1, o);
        // shaft
        for (int r = 2; r <= 10; r++) {
            put(img, 9, by+r, STAFFD); put(img, 10, by+r, STAFF);
        }
        // base
        put(img, 8, by+11, STAFFD); put(img, 9, by+11, STAFF); put(img, 10, by+11, STAFF); put(img, 11, by+11, STAFFD);
    }

    static void drawFeet(BufferedImage img, int by) {
        put(img, 3, by, RD1); put(img, 4, by, R1); put(img, 5, by, R1); put(img, 6, by, R1); put(img, 7, by, R1); put(img, 8, by, RD1);
        put(img, 3, by+1, RD1); put(img, 5, by+1, SKIND); put(img, 6, by+1, SKIND); put(img, 8, by+1, RD1);
    }

    static void drawTrim(BufferedImage img, int tier) {
        if (tier == 2) {
            put(img, 1, 19, TRIM2); put(img, 10, 19, TRIM2);
            put(img, 2, 24, TRIM2); put(img, 9, 24, TRIM2);
        } else if (tier == 3) {
            put(img, 1, 19, TRIM3); put(img, 10, 19, TRIM3);
            put(img, 2, 24, TRIM3); put(img, 9, 24, TRIM3);
            put(img, 5, 8, TRIM3); put(img, 6, 8, TRIM3);
        }
    }

    // ═══════════════════════════════════════════════════════
    //  Full frame assemblers
    // ═══════════════════════════════════════════════════════

    /** Facing DOWN frame. */
    static BufferedImage frameDown(int tier, int bob) {
        BufferedImage img = canvas();
        drawHat(img, tier, bob);
        drawFace(img, 7 + bob);
        drawRobe(img, tier, 9 + bob);
        drawStaff(img, tier, 7 + bob);
        drawFeet(img, 19 + bob);
        drawTrim(img, tier);
        return img;
    }

    /** Facing LEFT frame (side view facing left). */
    static BufferedImage frameLeft(int tier, int bob) {
        BufferedImage img = canvas();
        Color h = H(tier), hd = HD(tier), hl = HL(tier);
        Color r = R(tier), rd = RD(tier), rl = RL(tier);
        Color o = O(tier), ol = OL(tier);
        int by = bob;

        // Hat (side profile)
        put(img, 4, by, hd); put(img, 5, by, hd);
        put(img, 4, by+1, hd); put(img, 5, by+1, h); put(img, 6, by+1, hd);
        put(img, 3, by+2, hd); put(img, 4, by+2, h); put(img, 5, by+2, h); put(img, 6, by+2, h); put(img, 7, by+2, hd);
        put(img, 3, by+3, hd); put(img, 4, by+3, h); put(img, 5, by+3, hl); put(img, 6, by+3, h); put(img, 7, by+3, h); put(img, 8, by+3, hd);
        put(img, 2, by+4, hd); put(img, 3, by+4, h); put(img, 4, by+4, h); put(img, 5, by+4, h); put(img, 6, by+4, h); put(img, 7, by+4, h); put(img, 8, by+4, hd);
        put(img, 2, by+5, hd); put(img, 3, by+5, hl); put(img, 4, by+5, h); put(img, 5, by+5, h); put(img, 6, by+5, h); put(img, 7, by+5, hl); put(img, 8, by+5, hl); put(img, 9, by+5, hd);
        put(img, 1, by+6, hd); put(img, 2, by+6, hd); put(img, 3, by+6, hd); put(img, 4, by+6, h); put(img, 5, by+6, h); put(img, 6, by+6, h); put(img, 7, by+6, hd); put(img, 8, by+6, hd); put(img, 9, by+6, hd);

        // Face (side)
        put(img, 3, by+7, SKIND); put(img, 4, by+7, SKIN); put(img, 5, by+7, SKIN); put(img, 6, by+7, SKIND);
        put(img, 3, by+8, SKIND); put(img, 4, by+8, SKIN); put(img, 5, by+8, EYE); put(img, 6, by+8, PUPIL); put(img, 7, by+8, SKIND);

        // Robe (side - narrower)
        put(img, 2, by+9, rd); put(img, 3, by+9, r); put(img, 4, by+9, r); put(img, 5, by+9, r); put(img, 6, by+9, r); put(img, 7, by+9, rd);
        put(img, 2, by+10, rd); put(img, 3, by+10, r); put(img, 4, by+10, rl); put(img, 5, by+10, r); put(img, 6, by+10, r); put(img, 7, by+10, rl); put(img, 8, by+10, rd);
        put(img, 1, by+11, rd); put(img, 2, by+11, r); put(img, 3, by+11, r); put(img, 4, by+11, r); put(img, 5, by+11, r); put(img, 6, by+11, r); put(img, 7, by+11, r); put(img, 8, by+11, rd);
        put(img, 1, by+12, rd); put(img, 2, by+12, r); put(img, 3, by+12, r); put(img, 4, by+12, r); put(img, 5, by+12, r); put(img, 6, by+12, r); put(img, 7, by+12, r); put(img, 8, by+12, rd);
        put(img, 2, by+13, rd); put(img, 3, by+13, r); put(img, 4, by+13, r); put(img, 5, by+13, r); put(img, 6, by+13, r); put(img, 7, by+13, r); put(img, 8, by+13, rd);
        put(img, 2, by+14, rd); put(img, 3, by+14, r); put(img, 4, by+14, r); put(img, 5, by+14, r); put(img, 6, by+14, r); put(img, 7, by+14, r); put(img, 8, by+14, rd);
        put(img, 2, by+15, rd); put(img, 3, by+15, r); put(img, 4, by+15, r); put(img, 5, by+15, r); put(img, 6, by+15, r); put(img, 7, by+15, r); put(img, 8, by+15, rd);
        put(img, 1, by+16, rd); put(img, 2, by+16, r); put(img, 3, by+16, r); put(img, 4, by+16, r); put(img, 5, by+16, r); put(img, 6, by+16, r); put(img, 7, by+16, r); put(img, 8, by+16, r); put(img, 9, by+16, rd);
        put(img, 2, by+17, rd); put(img, 3, by+17, r); put(img, 4, by+17, r); put(img, 5, by+17, r); put(img, 6, by+17, r); put(img, 7, by+17, r); put(img, 8, by+17, rd);

        // Staff (held forward in left hand - left side)
        put(img, 2, by+3, STAFFD); put(img, 3, by+3, STAFF);
        put(img, 2, by+4, STAFFD); put(img, 3, by+4, STAFF);
        put(img, 1, by+5, o); put(img, 2, by+5, ol);
        for (int row = 6; row <= 16; row++) {
            put(img, 1, by+row, STAFFD); put(img, 2, by+row, STAFF);
        }
        put(img, 0, by+17, STAFFD); put(img, 1, by+17, STAFF); put(img, 2, by+17, STAFF); put(img, 3, by+17, STAFFD);

        // Feet
        put(img, 3, by+18, RD1); put(img, 4, by+18, R1); put(img, 5, by+18, SKIND); put(img, 6, by+18, R1); put(img, 7, by+18, RD1);

        // Mirror so character faces LEFT (originally drawn facing right)
        return mirror(img);
    }

    /** Facing RIGHT frame (side view facing right). */
    static BufferedImage frameRight(int tier, int bob) {
        // Build facing right, then DON'T mirror
        BufferedImage img = canvas();
        Color h = H(tier), hd = HD(tier), hl = HL(tier);
        Color r = R(tier), rd = RD(tier), rl = RL(tier);
        Color o = O(tier), ol = OL(tier);
        int by = bob;

        // Hat (side profile)
        put(img, 4, by, hd); put(img, 5, by, hd);
        put(img, 4, by+1, hd); put(img, 5, by+1, h); put(img, 6, by+1, hd);
        put(img, 3, by+2, hd); put(img, 4, by+2, h); put(img, 5, by+2, h); put(img, 6, by+2, h); put(img, 7, by+2, hd);
        put(img, 3, by+3, hd); put(img, 4, by+3, h); put(img, 5, by+3, hl); put(img, 6, by+3, h); put(img, 7, by+3, h); put(img, 8, by+3, hd);
        put(img, 2, by+4, hd); put(img, 3, by+4, h); put(img, 4, by+4, h); put(img, 5, by+4, h); put(img, 6, by+4, h); put(img, 7, by+4, h); put(img, 8, by+4, hd);
        put(img, 2, by+5, hd); put(img, 3, by+5, hl); put(img, 4, by+5, h); put(img, 5, by+5, h); put(img, 6, by+5, h); put(img, 7, by+5, hl); put(img, 8, by+5, hl); put(img, 9, by+5, hd);
        put(img, 1, by+6, hd); put(img, 2, by+6, hd); put(img, 3, by+6, hd); put(img, 4, by+6, h); put(img, 5, by+6, h); put(img, 6, by+6, h); put(img, 7, by+6, hd); put(img, 8, by+6, hd); put(img, 9, by+6, hd);

        // Face (side profile, facing right)
        put(img, 3, by+7, SKIND); put(img, 4, by+7, SKIN); put(img, 5, by+7, SKIN); put(img, 6, by+7, SKIND);
        put(img, 3, by+8, SKIND); put(img, 4, by+8, SKIN); put(img, 5, by+8, EYE); put(img, 6, by+8, PUPIL); put(img, 7, by+8, SKIND);

        // Robe (side)
        put(img, 2, by+9, rd); put(img, 3, by+9, r); put(img, 4, by+9, r); put(img, 5, by+9, r); put(img, 6, by+9, r); put(img, 7, by+9, rd);
        put(img, 2, by+10, rd); put(img, 3, by+10, r); put(img, 4, by+10, rl); put(img, 5, by+10, r); put(img, 6, by+10, r); put(img, 7, by+10, rl); put(img, 8, by+10, rd);
        put(img, 1, by+11, rd); put(img, 2, by+11, r); put(img, 3, by+11, r); put(img, 4, by+11, r); put(img, 5, by+11, r); put(img, 6, by+11, r); put(img, 7, by+11, r); put(img, 8, by+11, rd);
        put(img, 1, by+12, rd); put(img, 2, by+12, r); put(img, 3, by+12, r); put(img, 4, by+12, r); put(img, 5, by+12, r); put(img, 6, by+12, r); put(img, 7, by+12, r); put(img, 8, by+12, rd);
        put(img, 2, by+13, rd); put(img, 3, by+13, r); put(img, 4, by+13, r); put(img, 5, by+13, r); put(img, 6, by+13, r); put(img, 7, by+13, r); put(img, 8, by+13, rd);
        put(img, 2, by+14, rd); put(img, 3, by+14, r); put(img, 4, by+14, r); put(img, 5, by+14, r); put(img, 6, by+14, r); put(img, 7, by+14, r); put(img, 8, by+14, rd);
        put(img, 2, by+15, rd); put(img, 3, by+15, r); put(img, 4, by+15, r); put(img, 5, by+15, r); put(img, 6, by+15, r); put(img, 7, by+15, r); put(img, 8, by+15, rd);
        put(img, 1, by+16, rd); put(img, 2, by+16, r); put(img, 3, by+16, r); put(img, 4, by+16, r); put(img, 5, by+16, r); put(img, 6, by+16, r); put(img, 7, by+16, r); put(img, 8, by+16, r); put(img, 9, by+16, rd);
        put(img, 2, by+17, rd); put(img, 3, by+17, r); put(img, 4, by+17, r); put(img, 5, by+17, r); put(img, 6, by+17, r); put(img, 7, by+17, r); put(img, 8, by+17, rd);

        // Staff (held in right hand - right side)
        put(img, 8, by+3, STAFFD); put(img, 9, by+3, STAFF);
        put(img, 8, by+4, STAFFD); put(img, 9, by+4, STAFF);
        put(img, 8, by+5, o); put(img, 9, by+5, ol);
        for (int row = 6; row <= 16; row++) {
            put(img, 8, by+row, STAFFD); put(img, 9, by+row, STAFF);
        }
        put(img, 7, by+17, STAFFD); put(img, 8, by+17, STAFF); put(img, 9, by+17, STAFF); put(img, 10, by+17, STAFFD);

        // Feet
        put(img, 3, by+18, RD1); put(img, 4, by+18, R1); put(img, 5, by+18, SKIND); put(img, 6, by+18, R1); put(img, 7, by+18, RD1);

        return img;
    }

    /** Facing UP frame (rear view). */
    static BufferedImage frameUp(int tier, int bob) {
        BufferedImage img = canvas();
        Color h = H(tier), hd = HD(tier), hl = HL(tier);
        Color r = R(tier), rd = RD(tier), rl = RL(tier);
        int by = bob;

        // Hat (same shape as front)
        drawHat(img, tier, by);

        // Back of head (no face)
        put(img, 3, by+7, rd); put(img, 4, by+7, r); put(img, 5, by+7, r); put(img, 6, by+7, r); put(img, 7, by+7, r); put(img, 8, by+7, rd);

        // Back of robe
        put(img, 2, by+8, rd); put(img, 3, by+8, r); put(img, 4, by+8, rl); put(img, 5, by+8, r); put(img, 6, by+8, r); put(img, 7, by+8, rl); put(img, 8, by+8, r); put(img, 9, by+8, rd);
        put(img, 2, by+9, rd); put(img, 3, by+9, r); put(img, 4, by+9, r); put(img, 5, by+9, r); put(img, 6, by+9, r); put(img, 7, by+9, r); put(img, 8, by+9, r); put(img, 9, by+9, rd);
        put(img, 1, by+10, rd); put(img, 2, by+10, r); put(img, 3, by+10, rl); put(img, 4, by+10, r); put(img, 5, by+10, r); put(img, 6, by+10, r); put(img, 7, by+10, rl); put(img, 8, by+10, r); put(img, 9, by+10, r); put(img, 10, by+10, rd);
        put(img, 1, by+11, rd); put(img, 2, by+11, r); put(img, 3, by+11, r); put(img, 4, by+11, r); put(img, 5, by+11, r); put(img, 6, by+11, r); put(img, 7, by+11, r); put(img, 8, by+11, r); put(img, 9, by+11, r); put(img, 10, by+11, rd);
        put(img, 2, by+12, rd); put(img, 3, by+12, r); put(img, 4, by+12, r); put(img, 5, by+12, r); put(img, 6, by+12, r); put(img, 7, by+12, r); put(img, 8, by+12, r); put(img, 9, by+12, rd);
        put(img, 2, by+13, rd); put(img, 3, by+13, r); put(img, 4, by+13, r); put(img, 5, by+13, r); put(img, 6, by+13, r); put(img, 7, by+13, r); put(img, 8, by+13, rd);
        put(img, 2, by+14, rd); put(img, 3, by+14, r); put(img, 4, by+14, r); put(img, 5, by+14, r); put(img, 6, by+14, r); put(img, 7, by+14, r); put(img, 8, by+14, rd);
        put(img, 2, by+15, rd); put(img, 3, by+15, r); put(img, 4, by+15, r); put(img, 5, by+15, r); put(img, 6, by+15, r); put(img, 7, by+15, r); put(img, 8, by+15, r); put(img, 9, by+15, rd);
        put(img, 1, by+16, rd); put(img, 2, by+16, r); put(img, 3, by+16, r); put(img, 4, by+16, r); put(img, 5, by+16, r); put(img, 6, by+16, r); put(img, 7, by+16, r); put(img, 8, by+16, r); put(img, 9, by+16, r); put(img, 10, by+16, rd);

        // Staff (behind robe, right side)
        for (int row = 0; row <= 15; row++) {
            put(img, 9, by+row, STAFFD); put(img, 10, by+row, STAFF);
        }
        put(img, 8, by+16, STAFFD); put(img, 9, by+16, STAFF); put(img, 10, by+16, STAFF); put(img, 11, by+16, STAFFD);

        // Feet
        put(img, 3, by+17, RD1); put(img, 4, by+17, R1); put(img, 5, by+17, R1); put(img, 6, by+17, R1); put(img, 7, by+17, R1); put(img, 8, by+17, RD1);

        return img;
    }

    // ═══════════════════════════════════════════════════════
    //  Animation frame arrays
    // ═══════════════════════════════════════════════════════

    static BufferedImage[] idleFrames(int tier) {
        return new BufferedImage[]{
            frameDown(tier, 0), frameDown(tier, 1),
            frameDown(tier, 0), frameDown(tier, 1),
        };
    }

    static BufferedImage[] runFrames(int tier) {
        int[] bounce = {0, 2, 0, -1, 0, 2};
        BufferedImage[] f = new BufferedImage[6];
        for (int i = 0; i < 6; i++) f[i] = frameDown(tier, bounce[i]);
        return f;
    }

    static BufferedImage[] attackFrames(int tier) {
        int[] bob = {0, 1, 2, 1, 0, -1};
        BufferedImage[] f = new BufferedImage[6];
        for (int i = 0; i < 6; i++) f[i] = frameDown(tier, bob[i]);
        return f;
    }

    static BufferedImage[] hurtFrames(int tier) {
        return new BufferedImage[]{
            frameDown(tier, -1), frameDown(tier, -2),
        };
    }

    static BufferedImage[] deathFrames(int tier) {
        BufferedImage[] f = new BufferedImage[4];
        for (int i = 0; i < 4; i++) {
            f[i] = frameDown(tier, i * 3 + i);
            if (i >= 2) {
                float alpha = (i == 2) ? 0.55f : 0.25f;
                for (int y = 0; y < FRAME; y++) {
                    for (int x = 0; x < FRAME; x++) {
                        int argb = f[i].getRGB(x, y);
                        if ((argb & 0xFF000000) != 0) {
                            int a = (int)(((argb >> 24) & 0xFF) * alpha);
                            f[i].setRGB(x, y, (argb & 0x00FFFFFF) | (a << 24));
                        }
                    }
                }
            }
        }
        return f;
    }

    // ═══════════════════════════════════════════════════════
    //  Sprite strip builder
    // ═══════════════════════════════════════════════════════

    static BufferedImage buildStrip(int tier, String anim) {
        BufferedImage[] down;
        int cols;
        switch (anim) {
            case "idle": down = idleFrames(tier); break;
            case "run": down = runFrames(tier); break;
            case "attack": down = attackFrames(tier); break;
            case "hurt": down = hurtFrames(tier); break;
            case "death": down = deathFrames(tier); break;
            default: return null;
        }
        cols = down.length;

        int[] bounces;
        if (anim.equals("idle")) bounces = new int[]{0,1,0,1};
        else if (anim.equals("run")) bounces = new int[]{0,2,0,-1,0,2};
        else if (anim.equals("attack")) bounces = new int[]{0,1,2,1,0,-1};
        else if (anim.equals("hurt")) bounces = new int[]{-1,-2};
        else bounces = new int[]{0,3,6,9}; // death

        int totalW = FRAME * cols;
        int totalH = FRAME * 4;
        BufferedImage sheet = new BufferedImage(totalW, totalH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D sg = sheet.createGraphics();

        // Row 0: DOWN
        for (int c = 0; c < cols; c++) {
            int bob = (c < bounces.length) ? bounces[c] : 0;
            sg.drawImage(frameDown(tier, bob), c * FRAME, 0, null);
        }
        // Row 1: LEFT
        for (int c = 0; c < cols; c++) {
            int bob = (c < bounces.length) ? bounces[c] : 0;
            sg.drawImage(frameLeft(tier, bob), c * FRAME, FRAME, null);
        }
        // Row 2: RIGHT
        for (int c = 0; c < cols; c++) {
            int bob = (c < bounces.length) ? bounces[c] : 0;
            sg.drawImage(frameRight(tier, bob), c * FRAME, 2 * FRAME, null);
        }
        // Row 3: UP
        for (int c = 0; c < cols; c++) {
            int bob = (c < bounces.length) ? bounces[c] : 0;
            sg.drawImage(frameUp(tier, bob), c * FRAME, 3 * FRAME, null);
        }
        sg.dispose();
        return sheet;
    }

    // ═══════════════════════════════════════════════════════
    //  Main
    // ═══════════════════════════════════════════════════════

    static final String DST = "Game-test/assets/sprites";

    public static void main(String[] args) throws IOException {
        File dstDir = new File(DST);
        dstDir.mkdirs();

        String[] anims = {"idle", "run", "attack", "hurt", "death"};
        for (int tier = 1; tier <= 3; tier++) {
            for (String anim : anims) {
                String fn = "char_mage_lvl" + tier + "_" + anim + ".png";
                BufferedImage strip = buildStrip(tier, anim);
                ImageIO.write(strip, "PNG", new File(dstDir, fn));
                System.out.println("Saved " + fn + " (" + strip.getWidth() + "x" + strip.getHeight() + ")");
            }
        }
        System.out.println("Done!");
    }
}
