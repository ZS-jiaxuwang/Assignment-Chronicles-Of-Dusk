import java.awt.Color;
// Jiaxu Wang (24009377)  Jiaheng Liu (24009483)  Angze Song (24009333)   Xiao Wu (24009458)
public class Particle {
    double x;
    double y;
    double vx;
    double vy;
    double radius;
    double age;
    double lifetime;
    Color color;
    boolean alive = true;

    public Particle(double x, double y, double vx, double vy, double radius, double lifetime, Color color) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.radius = radius;
        this.lifetime = lifetime;
        this.color = color;
    }

    public void update(double dt) {
        age += dt;
        if (age >= lifetime) {
            alive = false;
            return;
        }
        x += vx * dt;
        y += vy * dt;
        vx *= 0.96;
        vy *= 0.96;
    }

    public void render(GameEngine g) {
        if (!alive) return;
        g.changeColor(color);
        double t = 1.0 - age / lifetime;
        g.drawSolidCircle(x, y, Math.max(1.0, radius * t));
    }
}
