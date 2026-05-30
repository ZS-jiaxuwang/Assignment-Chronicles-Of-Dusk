import java.awt.Color;
import java.util.ArrayList;
// Jiaxu Wang (24009377)  Jiaheng Liu (24009483)  Angze Song (24009333)   Xiao Wu (24009458)
public class GameMap {
    private final SurvivalGame game;
    private final ArrayList<Obstacle> obstacles = new ArrayList<Obstacle>();

    public GameMap(SurvivalGame game) {
        this.game = game;
    }

    public void generate() {
        obstacles.clear();

        double worldW = GameConfig.WORLD_WIDTH;
        double worldH = GameConfig.WORLD_HEIGHT;
        double safeX = worldW * 0.5;
        double safeY = worldH * 0.5;
        double safeRadius = 350;

        // Top edge: scattered trees and rocks
        placeObstacle(350, 120, 50, 50, Obstacle.TREE, safeX, safeY, safeRadius);
        placeObstacle(700, 100, 56, 38, Obstacle.ROCK, safeX, safeY, safeRadius);
        placeObstacle(1200, 140, 50, 50, Obstacle.TREE, safeX, safeY, safeRadius);
        placeObstacle(1800, 110, 60, 42, Obstacle.ROCK, safeX, safeY, safeRadius);
        placeObstacle(2300, 140, 50, 50, Obstacle.TREE, safeX, safeY, safeRadius);
        placeObstacle(2900, 100, 56, 40, Obstacle.ROCK, safeX, safeY, safeRadius);
        placeObstacle(3300, 130, 50, 50, Obstacle.TREE, safeX, safeY, safeRadius);

        // Bottom edge: scattered trees and rocks
        placeObstacle(400, 2250, 50, 50, Obstacle.TREE, safeX, safeY, safeRadius);
        placeObstacle(800, 2280, 60, 42, Obstacle.ROCK, safeX, safeY, safeRadius);
        placeObstacle(1300, 2240, 50, 50, Obstacle.TREE, safeX, safeY, safeRadius);
        placeObstacle(1900, 2270, 56, 38, Obstacle.ROCK, safeX, safeY, safeRadius);
        placeObstacle(2400, 2230, 50, 50, Obstacle.TREE, safeX, safeY, safeRadius);
        placeObstacle(3000, 2260, 60, 40, Obstacle.ROCK, safeX, safeY, safeRadius);
        placeObstacle(3400, 2240, 50, 50, Obstacle.TREE, safeX, safeY, safeRadius);

        // Left edge: sparse cover
        placeObstacle(160, 500, 50, 50, Obstacle.TREE, safeX, safeY, safeRadius);
        placeObstacle(140, 1000, 56, 42, Obstacle.ROCK, safeX, safeY, safeRadius);
        placeObstacle(170, 1500, 50, 50, Obstacle.TREE, safeX, safeY, safeRadius);
        placeObstacle(150, 1900, 58, 40, Obstacle.ROCK, safeX, safeY, safeRadius);

        // Right edge: sparse cover
        placeObstacle(3390, 600, 50, 50, Obstacle.TREE, safeX, safeY, safeRadius);
        placeObstacle(3400, 1100, 56, 42, Obstacle.ROCK, safeX, safeY, safeRadius);
        placeObstacle(3380, 1600, 50, 50, Obstacle.TREE, safeX, safeY, safeRadius);
        placeObstacle(3410, 2000, 58, 40, Obstacle.ROCK, safeX, safeY, safeRadius);

        // Corners: a few bushes (enemies can pass through, decorative)
        placeObstacle(250, 380, 40, 40, Obstacle.BUSH, safeX, safeY, safeRadius);
        placeObstacle(3250, 370, 40, 40, Obstacle.BUSH, safeX, safeY, safeRadius);
        placeObstacle(280, 2050, 40, 40, Obstacle.BUSH, safeX, safeY, safeRadius);
        placeObstacle(3280, 2070, 40, 40, Obstacle.BUSH, safeX, safeY, safeRadius);
    }

    private void placeObstacle(double x, double y, double w, double h, int type,
                                double safeX, double safeY, double safeRadius) {
        double cx = x + w * 0.5;
        double cy = y + h * 0.5;
        double dx = cx - safeX;
        double dy = cy - safeY;
        if (dx * dx + dy * dy < safeRadius * safeRadius) {
            return;
        }
        obstacles.add(new Obstacle(x, y, w, h, type));
    }

    public void render(GameEngine g, Camera camera) {
        for (Obstacle ob : obstacles) {
            if (camera == null || camera.isVisible(ob.x - 10, ob.y - 10, ob.width + 20)) {
                ob.render(g);
            }
        }
    }

    public ArrayList<Obstacle> getAllObstacles() {
        return obstacles;
    }

    public ArrayList<Obstacle> getNearby(double x, double y, double range) {
        ArrayList<Obstacle> result = new ArrayList<Obstacle>();
        double rr = range * range;
        for (Obstacle ob : obstacles) {
            double cx = ob.x + ob.width * 0.5;
            double cy = ob.y + ob.height * 0.5;
            double dx = cx - x;
            double dy = cy - y;
            if (dx * dx + dy * dy <= rr) {
                result.add(ob);
            }
        }
        return result;
    }
}
