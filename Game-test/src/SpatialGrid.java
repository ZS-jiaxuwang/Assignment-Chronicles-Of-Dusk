import java.util.ArrayList;
import java.util.HashMap;
// Jiaxu Wang (24009377)  Jiaheng Liu (24009483)  Angze Song (24009333)   Xiao Wu (24009458)
public class SpatialGrid {
    private final double worldWidth;
    private final double worldHeight;
    private final int cellSize;
    private final int cols;
    private final int rows;
    private final HashMap<Integer, ArrayList<Entity>> cells = new HashMap<Integer, ArrayList<Entity>>();

    public SpatialGrid(double worldWidth, double worldHeight, int cellSize) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.cellSize = cellSize;
        this.cols = Math.max(1, (int)Math.ceil(worldWidth / cellSize));
        this.rows = Math.max(1, (int)Math.ceil(worldHeight / cellSize));
    }

    public void clear() {
        cells.clear();
    }

    public void insert(Entity e) {
        int cx = clampX((int)(e.x / cellSize));
        int cy = clampY((int)(e.y / cellSize));
        int key = cy * cols + cx;
        ArrayList<Entity> list = cells.get(key);
        if (list == null) {
            list = new ArrayList<Entity>();
            cells.put(key, list);
        }
        list.add(e);
    }

    public ArrayList<Entity> getNearby(Entity e) {
        int cx = clampX((int)(e.x / cellSize));
        int cy = clampY((int)(e.y / cellSize));
        ArrayList<Entity> result = new ArrayList<Entity>();
        for (int yy = cy - 1; yy <= cy + 1; yy++) {
            for (int xx = cx - 1; xx <= cx + 1; xx++) {
                if (xx < 0 || xx >= cols || yy < 0 || yy >= rows) continue;
                int key = yy * cols + xx;
                ArrayList<Entity> list = cells.get(key);
                if (list != null) {
                    result.addAll(list);
                }
            }
        }
        return result;
    }

    private int clampX(int x) {
        if (x < 0) return 0;
        if (x >= cols) return cols - 1;
        return x;
    }

    private int clampY(int y) {
        if (y < 0) return 0;
        if (y >= rows) return rows - 1;
        return y;
    }
}
