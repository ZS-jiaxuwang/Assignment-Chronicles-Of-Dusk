import java.awt.Color;
import java.awt.Image;
// Jiaxu Wang (24009377)  Jiaheng Liu (24009483)  Angze Song (24009333)   Xiao Wu (24009458)
public class Obstacle extends Entity {
    public static final int ROCK = 0;
    public static final int TREE = 1;
    public static final int WALL = 2;
    public static final int BUSH = 3;

    private final int type;

    public Obstacle(double x, double y, double width, double height, int type) {
        super(x + width * 0.5, y + height * 0.5, Math.max(width, height) * 0.5, 1, colorByType(type));
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.type = type;
    }

    @Override
    public void onUpdate(double dt) {
        // Static obstacle, no movement.
    }

    public boolean blocksPlayer() {
        return true;
    }

    public boolean blocksEnemies() {
        return type != BUSH;
    }

    public int getType() {
        return type;
    }

    public boolean collidesWith(double cx, double cy, double cr) {
        double closestX = Math.max(x, Math.min(cx, x + width));
        double closestY = Math.max(y, Math.min(cy, y + height));
        double dx = cx - closestX;
        double dy = cy - closestY;
        return (dx * dx + dy * dy) < (cr * cr);
    }

    public boolean resolveCircleCollision(double cx, double cy, double cr, double[] outPush) {
        if (outPush == null || outPush.length < 2) {
            return false;
        }
        outPush[0] = 0;
        outPush[1] = 0;

        double closestX = Math.max(x, Math.min(cx, x + width));
        double closestY = Math.max(y, Math.min(cy, y + height));
        double dx = cx - closestX;
        double dy = cy - closestY;
        double distSq = dx * dx + dy * dy;
        double rr = cr * cr;
        if (distSq >= rr) {
            return false;
        }

        if (distSq > 0.000001) {
            double dist = Math.sqrt(distSq);
            double overlap = cr - dist;
            outPush[0] = (dx / dist) * overlap;
            outPush[1] = (dy / dist) * overlap;
            return true;
        }

        double left = cx - x;
        double right = (x + width) - cx;
        double top = cy - y;
        double bottom = (y + height) - cy;
        double minPen = left;
        int axis = 0;
        if (right < minPen) {
            minPen = right;
            axis = 1;
        }
        if (top < minPen) {
            minPen = top;
            axis = 2;
        }
        if (bottom < minPen) {
            minPen = bottom;
            axis = 3;
        }

        if (axis == 0) {
            outPush[0] = -(cr + left);
        } else if (axis == 1) {
            outPush[0] = cr + right;
        } else if (axis == 2) {
            outPush[1] = -(cr + top);
        } else {
            outPush[1] = cr + bottom;
        }
        return true;
    }

    @Override
    public void render(GameEngine g) {
        if (!alive) {
            return;
        }
        if (type == TREE) {
            drawTree(g);
            return;
        }
        if (type == ROCK) {
            drawRock(g);
            return;
        }
        if (type == BUSH) {
            drawBush(g);
            return;
        }
        // WALL: keep procedural
        g.changeColor(new Color(120, 92, 78));
        g.drawSolidRectangle(x, y, width, height);
        g.changeColor(new Color(146, 116, 96));
        g.drawRectangle(x, y, width, height, 2);
    }

    private void drawTree(GameEngine g) {
        String asset = treeSprite();
        if (!AssetLoader.hasAsset(asset)) {
            drawFallback(g);
            return;
        }
        Image img = AssetLoader.load(asset);
        if (img == null) {
            drawFallback(g);
            return;
        }
        double size = 150;
        double cx = x + width * 0.5;
        double cy = y + height * 0.5;
        g.drawImage(img, cx - size * 0.5, cy - size * 0.7, size, size);
    }

    private void drawRock(GameEngine g) {
        int seed = ((int)(x * 31 + y * 17) & 0x7fffffff);
        String asset = AssetLibrary.rockSpriteBySeed(seed);
        Image img = AssetLoader.load(asset);
        if (img == null) {
            drawFallback(g);
            return;
        }
        double size = Math.max(width, height) * 1.4;
        double cx = x + width * 0.5;
        double cy = y + height * 0.5;
        g.drawImage(img, cx - size * 0.5, cy - size * 0.5, size, size);
    }

    private void drawBush(GameEngine g) {
        String asset = AssetLibrary.OBSTACLE_BUSH;
        if (!AssetLoader.hasAsset(asset)) {
            drawFallback(g);
            return;
        }
        Image img = AssetLoader.load(asset);
        if (img == null) {
            drawFallback(g);
            return;
        }
        double size = Math.max(width, height) * 1.6;
        double cx = x + width * 0.5;
        double cy = y + height * 0.5;
        g.drawImage(img, cx - size * 0.5, cy - size * 0.5, size, size);
    }

    private String treeSprite() {
        int seed = ((int)(x * 31 + y * 17) & 0x7fffffff);
        return AssetLibrary.treeSpriteBySeed(seed);
    }

    private void drawFallback(GameEngine g) {
        if (type == TREE) {
            g.changeColor(new Color(90, 60, 35));
            g.drawSolidRectangle(x + width * 0.38, y + height * 0.45, width * 0.24, height * 0.55);
            g.changeColor(new Color(45, 145, 70));
            g.drawSolidCircle(x + width * 0.5, y + height * 0.36, Math.min(width, height) * 0.42);
        } else if (type == ROCK) {
            g.changeColor(new Color(115, 120, 132));
            g.drawSolidRectangle(x, y, width, height);
        } else if (type == BUSH) {
            g.changeColor(new Color(48, 122, 54));
            g.drawSolidRectangle(x, y, width, height);
        }
    }

    private static Color colorByType(int t) {
        if (t == TREE) return new Color(50, 140, 70);
        if (t == WALL) return new Color(118, 95, 78);
        if (t == BUSH) return new Color(60, 132, 60);
        return new Color(120, 120, 130);
    }
}
