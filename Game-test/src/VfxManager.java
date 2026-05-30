import java.awt.Color;
import java.util.ArrayList;
// Jiaxu Wang (24009377)  Jiaheng Liu (24009483)  Angze Song (24009333)   Xiao Wu (24009458)
public class VfxManager {
    private final SurvivalGame game;
    private final ArrayList<Particle> particles = new ArrayList<Particle>();
    private final ArrayList<FloatingText> texts = new ArrayList<FloatingText>();

    private double shakeIntensity = 0.0;

    public VfxManager(SurvivalGame game) {
        this.game = game;
    }

    public void spawnDeathBurst(double x, double y, Color color) {
        int count = 8 + game.rand(8);
        for (int i = 0; i < count; i++) {
            double angle = game.rand(360.0);
            double speed = 100 + game.rand(100.0);
            double vx = Math.cos(Math.toRadians(angle)) * speed;
            double vy = Math.sin(Math.toRadians(angle)) * speed;
            double radius = 2 + game.rand(4.0);
            double life = 0.3 + game.rand(0.5);
            particles.add(new Particle(x, y, vx, vy, radius, life, color));
        }
    }

    public void spawnDamageText(double x, double y, double damage) {
        spawnDamageText(x, y, damage, false);
    }

    public void spawnDamageText(double x, double y, double damage, boolean isCrit) {
        Color c = isCrit ? new Color(255, 220, 40)
            : (damage > 20 ? new Color(255, 180, 80) : Color.WHITE);
        texts.add(new FloatingText(x, y, Integer.toString((int)Math.round(damage)), c));
    }

    public void spawnAuraSpark(double x, double y, Color color, boolean empowered) {
        int count = empowered ? 3 : 2;
        for (int i = 0; i < count; i++) {
            double angle = game.rand(360.0);
            double speed = 14 + game.rand(empowered ? 26.0 : 16.0);
            double vx = Math.cos(Math.toRadians(angle)) * speed;
            double vy = Math.sin(Math.toRadians(angle)) * speed - (empowered ? 8 : 3);
            double radius = empowered ? (2.2 + game.rand(1.8)) : (1.4 + game.rand(1.2));
            double life = empowered ? (0.28 + game.rand(0.24)) : (0.18 + game.rand(0.18));
            int alpha = empowered ? 180 : 130;
            Color tint = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
            particles.add(new Particle(x, y, vx, vy, radius, life, tint));
        }
    }

    public void addShake(double amount) {
        shakeIntensity = Math.max(shakeIntensity, amount);
    }

    public void update(double dt) {
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.update(dt);
            if (!p.alive) particles.remove(i);
        }
        for (int i = texts.size() - 1; i >= 0; i--) {
            FloatingText t = texts.get(i);
            t.update(dt);
            if (!t.alive) texts.remove(i);
        }
        if (shakeIntensity > 0) {
            shakeIntensity -= 15 * dt;
            if (shakeIntensity < 0) shakeIntensity = 0;
        }
    }

    public void beginCamera(GameEngine g, Camera camera) {
        g.saveCurrentTransform();
        if (camera != null) {
            g.translate(-camera.x, -camera.y);
        }
        if (shakeIntensity > 0.01) {
            double ox = (game.rand(2.0) - 1.0) * shakeIntensity;
            double oy = (game.rand(2.0) - 1.0) * shakeIntensity;
            g.translate(ox, oy);
        }
    }

    public void endCamera(GameEngine g) {
        g.restoreLastTransform();
    }

    public void render(GameEngine g) {
        for (Particle p : particles) p.render(g);
        for (FloatingText t : texts) t.render(g);
    }
}
