import java.awt.Color;

public class Projectile extends Entity {
    final SurvivalGame game;
    double damage;
    double lifetime;
    double age;
    int pierces;
    boolean friendly;
    double gravityY;
    double angleRad;

    public Projectile(SurvivalGame game, double x, double y, double radius, double speed, double angleRad,
                      double damage, double lifetime, int pierces, boolean friendly, Color color) {
        super(x, y, radius, 1, color);
        this.game = game;
        this.vx = Math.cos(angleRad) * speed;
        this.vy = Math.sin(angleRad) * speed;
        this.angleRad = angleRad;
        this.damage = damage;
        this.lifetime = lifetime;
        this.pierces = pierces;
        this.friendly = friendly;
        this.gravityY = 0;
    }

    @Override
    public void onUpdate(double dt) {
        age += dt;
        if (age >= lifetime) {
            alive = false;
            return;
        }
        if (gravityY != 0) {
            vy += gravityY * dt;
        }
        if (vx != 0 || vy != 0) {
            angleRad = Math.atan2(vy, vx);
        }
    }

    @Override
    public void render(GameEngine g) {
        if (!alive) return;
        double px = Math.max(2.0, radius * 0.5);
        double deg = Math.toDegrees(angleRad);

        g.saveCurrentTransform();
        g.translate(x, y);
        g.rotate(deg);
        g.changeColor(baseColor);

        // shaft
        g.drawSolidRectangle(-px * 2.5, -px * 0.5, px * 5, px);
        // arrowhead top
        g.drawSolidRectangle(px * 0.5, -px * 1.5, px * 2, px);
        // arrowhead bottom
        g.drawSolidRectangle(px * 0.5, px * 0.5, px * 2, px);
        // tip point
        g.drawSolidRectangle(px * 2, -px * 0.5, px, px);

        g.restoreLastTransform();
    }
}
