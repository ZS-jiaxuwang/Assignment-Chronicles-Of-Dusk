import java.awt.Color;

// Jiaxu Wang (24009377)  Jiaheng Liu (24009483)  Angze Song (24009333)   Xiao Wu (24009458)
public abstract class Entity {
    double x;
    double y;
    double vx;
    double vy;
    double radius;
    double width;
    double height;
    double maxHealth;
    double health;
    boolean alive = true;
    boolean dying = false;
    double deathTimer = 0;
    int hitFlashTimer;
    Color baseColor;

    Entity(double x, double y, double radius, double maxHealth, Color baseColor) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.width = 0;
        this.height = 0;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.baseColor = baseColor;
    }

    public final void update(double dt) {
        if (dying) {
            deathTimer -= dt;
            if (deathTimer <= 0) {
                alive = false;
                dying = false;
            }
            return;
        }
        onUpdate(dt);
        x += vx * dt;
        y += vy * dt;
        if (hitFlashTimer > 0) {
            hitFlashTimer--;
        }
    }

    public abstract void onUpdate(double dt);

    public void render(GameEngine g) {
        if (!alive) {
            return;
        }
        if (hitFlashTimer > 0 && hitFlashTimer % 2 == 0) {
            g.changeColor(Color.WHITE);
        } else {
            g.changeColor(baseColor);
        }
        g.drawSolidCircle(x, y, radius);
    }

    public void takeDamage(double amount, Entity source) {
        if (!alive || dying || amount <= 0) {
            return;
        }
        health -= amount;
        hitFlashTimer = 6;
        if (health <= 0) {
            health = 0;
            dying = true;
            deathTimer = 0.3;
            onDeath(source);
        }
    }

    public void onDeath(Entity killer) {
    }

    public double distSqTo(Entity other) {
        double dx = x - other.x;
        double dy = y - other.y;
        return dx * dx + dy * dy;
    }
}
