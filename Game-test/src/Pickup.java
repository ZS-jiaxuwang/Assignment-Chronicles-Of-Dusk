import java.awt.Color;
// Jiaxu Wang (24009377)  Jiaheng Liu (24009483)  Angze Song (24009333)   Xiao Wu (24009458)
public class Pickup extends Entity {
    public static final int TYPE_XP = 0;
    public static final int TYPE_WEAPON = 1;

    final SurvivalGame game;
    final int pickupType;
    private final int xpValue;
    double magnetRange;
    double magnetStrength;

    public Pickup(SurvivalGame game, double x, double y, int xpValue) {
        super(x, y, GameConfig.PICKUP_RADIUS, 1, new Color(255, 210, 70));
        this.game = game;
        this.pickupType = TYPE_XP;
        this.xpValue = xpValue;
        this.magnetRange = 0;
        this.magnetStrength = GameConfig.PICKUP_MAGNET_STRENGTH;
    }

    protected Pickup(SurvivalGame game, double x, double y, int type, Color color, double radius) {
        super(x, y, radius, 1, color);
        this.game = game;
        this.pickupType = type;
        this.xpValue = 0;
        this.magnetRange = 0;
        this.magnetStrength = GameConfig.PICKUP_MAGNET_STRENGTH;
    }

    @Override
    public void onUpdate(double dt) {
        Player p = game.player;
        if (p == null || !p.alive) return;
        double dx = p.x - x;
        double dy = p.y - y;
        double distSq = dx * dx + dy * dy;
        double range = magnetRange > 0 ? magnetRange : game.getPlayerPickupRange();
        if (distSq <= range * range) {
            double dist = Math.sqrt(Math.max(0.0001, distSq));
            vx = (dx / dist) * magnetStrength;
            vy = (dy / dist) * magnetStrength;
        } else {
            vx = 0;
            vy = 0;
        }
    }

    @Override
    public void render(GameEngine g) {
        if (!alive) return;
        if (pickupType == TYPE_XP) {
            g.changeColor(baseColor);
            g.drawSolidRectangle(x - 4, y - 4, 8, 8);
        } else {
            g.changeColor(baseColor);
            g.drawSolidCircle(x, y, radius);
        }
    }

    public int getXpValue() {
        return xpValue;
    }
}
