import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;

public class AssetLoader {
    private static final HashMap<String, BufferedImage> cache = new HashMap<String, BufferedImage>();
    private static final HashMap<String, Boolean> missing = new HashMap<String, Boolean>();
    private static GameEngine engine;

    public static void init(GameEngine g) {
        engine = g;
        cache.clear();
        missing.clear();
    }

    public static boolean hasAsset(String filename) {
        if (cache.containsKey(filename)) return true;
        if (missing.containsKey(filename)) return false;
        File f = AssetLibrary.resolveSpriteFile(filename);
        if (f.exists()) return true;
        missing.put(filename, true);
        System.out.println("[AssetLoader] Missing: " + filename + " (cwd=" + new File(".").getAbsolutePath() + ")");
        return false;
    }

    public static BufferedImage load(String filename) {
        if (cache.containsKey(filename)) return cache.get(filename);
        if (missing.containsKey(filename)) return null;
        File f = AssetLibrary.resolveSpriteFile(filename);
        if (!f.exists()) {
            missing.put(filename, true);
            System.out.println("[AssetLoader] Missing: " + filename + " (cwd=" + new File(".").getAbsolutePath() + ")");
            return null;
        }
        try {
            BufferedImage img = (BufferedImage) engine.loadImage(f.getPath());
            cache.put(filename, img);
            System.out.println("[AssetLoader] Loaded: " + filename + " <- " + f.getPath());
            return img;
        } catch (Exception e) {
            missing.put(filename, true);
            return null;
        }
    }

    public static BufferedImage getFrame(String filename, int col, int frameW, int frameH) {
        return getFrame(filename, col, 0, frameW, frameH);
    }

    public static BufferedImage getFrame(String filename, int col, int row, int frameW, int frameH) {
        BufferedImage sheet = load(filename);
        if (sheet == null) return null;
        int x = col * frameW;
        int y = row * frameH;
        if (x + frameW > sheet.getWidth() || y + frameH > sheet.getHeight()) return null;
        return (BufferedImage) engine.subImage(sheet, x, y, frameW, frameH);
    }
}
