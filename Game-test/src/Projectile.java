import java.awt.Color;

public class Projectile extends Entity {
    final SurvivalGame game;
    double damage;
    double lifetime;
    double age;
    int pierces;
    boolean friendly;
    double gravityY;

    public Projectile(SurvivalGame game, double x, double y, double radius, double speed, double angleRad,
                      double damage, double lifetime, int pierces, boolean friendly, Color color) {
        super(x, y, radius, 1, color);
        this.game = game;
        this.vx = Math.cos(angleRad) * speed;
        this.vy = Math.sin(angleRad) * speed;
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
    }

    @Override
    public void render(GameEngine g) {
        if (!alive) return;
        g.changeColor(baseColor);
        g.drawSolidCircle(x, y, radius);
    }
}
