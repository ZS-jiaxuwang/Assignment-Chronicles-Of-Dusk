import java.awt.Color;

public class FloatingText {
    double x;
    double y;
    double vy;
    double age;
    double lifetime;
    String text;
    Color color;
    boolean alive = true;

    public FloatingText(double x, double y, String text, Color color) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.color = color;
        this.vy = -60;
        this.lifetime = 0.8;
    }

    public void update(double dt) {
        age += dt;
        if (age >= lifetime) {
            alive = false;
            return;
        }
        y += vy * dt;
    }

    public void render(GameEngine g) {
        if (!alive) return;
        g.changeColor(color);
        g.drawBoldText(x, y, text, "Arial", 14);
    }
}
