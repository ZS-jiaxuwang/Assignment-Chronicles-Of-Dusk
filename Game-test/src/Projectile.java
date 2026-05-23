import java.awt.Color;
import java.util.HashSet;

public class Projectile extends Entity {
    public static final int TYPE_ARROW = 0;
    public static final int TYPE_SWORD_WAVE = 1;
    public static final int TYPE_KNIFE = 2;
    public static final int TYPE_FIRE_MAGIC = 3;
    public static final int TYPE_BOOMERANG = 4;

    final SurvivalGame game;
    double damage;
    double lifetime;
    double age;
    int pierces;
    boolean friendly;
    double gravityY;
    double angleRad;
    int projectileType;
    boolean orbiting;
    double orbitAngle;
    double orbitRadius;
    double orbitSpeed;
    Player orbitTarget;
    boolean boomerang;
    int boomerangPhase;
    double boomerangMaxDist;
    double boomerangSpeed;
    double boomerangOriginX;
    double boomerangOriginY;
    double boomerangLaunchAngle;
    HashSet<Enemy> hitRecent = new HashSet<Enemy>();
    double hitClearTimer;

    public Projectile(SurvivalGame game, double x, double y, double radius, double speed, double angleRad,
                      double damage, double lifetime, int pierces, boolean friendly, Color color,
                      int projectileType) {
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
        this.projectileType = projectileType;
    }

    public static Projectile createOrbiter(SurvivalGame game, double startAngle, double orbitRadius,
                                            double orbitSpeed, double damage, double radius, Color color) {
        Player p = game.player;
        double sx = p.x + Math.cos(startAngle) * orbitRadius;
        double sy = p.y + Math.sin(startAngle) * orbitRadius;
        Projectile proj = new Projectile(game, sx, sy, radius, 0, startAngle,
            damage, 999.0, 9999, true, color, TYPE_FIRE_MAGIC);
        proj.orbiting = true;
        proj.orbitAngle = startAngle;
        proj.orbitRadius = orbitRadius;
        proj.orbitSpeed = orbitSpeed;
        proj.orbitTarget = p;
        return proj;
    }

    public static Projectile createBoomerang(SurvivalGame game, double x, double y, double radius,
                                              double speed, double angle, double damage, double lifetime,
                                              double maxDist, Color color) {
        Projectile proj = new Projectile(game, x, y, radius, speed, angle,
            damage, lifetime, 999, true, color, TYPE_BOOMERANG);
        proj.boomerang = true;
        proj.boomerangPhase = 0;
        proj.boomerangMaxDist = maxDist;
        proj.boomerangSpeed = speed;
        proj.boomerangOriginX = x;
        proj.boomerangOriginY = y;
        proj.boomerangLaunchAngle = angle;
        return proj;
    }

    @Override
    public void onUpdate(double dt) {
        age += dt;
        if (age >= lifetime) {
            alive = false;
            return;
        }
        if (orbiting) {
            if (orbitTarget == null || !orbitTarget.alive) {
                alive = false;
                return;
            }
            orbitAngle += orbitSpeed * dt;
            x = orbitTarget.x + Math.cos(orbitAngle) * orbitRadius;
            y = orbitTarget.y + Math.sin(orbitAngle) * orbitRadius;
            angleRad = orbitAngle + Math.PI / 2;
            return;
        }
        if (boomerang) {
            if (boomerangPhase == 0) {
                double dist = Math.sqrt(
                    (x - boomerangOriginX) * (x - boomerangOriginX) +
                    (y - boomerangOriginY) * (y - boomerangOriginY));
                if (dist >= boomerangMaxDist) {
                    boomerangPhase = 1;
                }
            }
            if (boomerangPhase == 1) {
                Player p = game.player;
                if (p == null || !p.alive) {
                    alive = false;
                    return;
                }
                double dx = p.x - x;
                double dy = p.y - y;
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist < 30.0) {
                    alive = false;
                    return;
                }
                angleRad = Math.atan2(dy, dx);
                vx = Math.cos(angleRad) * boomerangSpeed * 1.3;
                vy = Math.sin(angleRad) * boomerangSpeed * 1.3;
            }
            return;
        }
        if (gravityY != 0) {
            vy += gravityY * dt;
        }
        if (vx != 0 || vy != 0) {
            angleRad = Math.atan2(vy, vx);
        }
        hitClearTimer -= dt;
        if (hitClearTimer <= 0) {
            hitRecent.clear();
            hitClearTimer = 0.3;
        }
    }

    public boolean canHit(Enemy e) {
        return !hitRecent.contains(e);
    }

    public void markHit(Enemy e) {
        hitRecent.add(e);
    }

    @Override
    public void render(GameEngine g) {
        if (!alive) return;

        if (!friendly) {
            g.changeColor(baseColor);
            g.drawSolidCircle(x, y, radius);
            g.changeColor(new Color(255, 255, 255, 140));
            g.drawSolidCircle(x, y, radius * 0.55);
            return;
        }

        if (projectileType == TYPE_SWORD_WAVE) {
            renderSwordWave(g);
            return;
        }
        if (projectileType == TYPE_KNIFE) {
            renderKnife(g);
            return;
        }
        if (projectileType == TYPE_FIRE_MAGIC) {
            renderFireball(g);
            return;
        }
        if (projectileType == TYPE_BOOMERANG) {
            renderBoomerang(g);
            return;
        }

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

    private void renderSwordWave(GameEngine g) {
        double lifeRatio = age / lifetime;
        double alpha = 1.0 - lifeRatio * 0.4;
        double scale = 1.0 + lifeRatio * 0.3;

        g.saveCurrentTransform();
        g.translate(x, y);
        g.rotate(Math.toDegrees(angleRad));

        double r = radius * scale;

        // Layer 1: wide outer glow
        drawCrescent(g, r * 1.5,
            new Color(255, 180, 30, (int)(30 * alpha)));

        // Layer 2: mid glow
        drawCrescent(g, r * 1.2,
            new Color(255, 200, 50, (int)(90 * alpha)));

        // Layer 3: main body
        drawCrescent(g, r * 0.9,
            new Color(255, 215, 60, (int)(200 * alpha)));

        // Layer 4: bright core
        drawCrescent(g, r * 0.55,
            new Color(255, 240, 160, (int)(240 * alpha)));

        // Layer 5: white-hot edge
        double edgeR = r * 1.05;
        int[] edgeXs = new int[21];
        int[] edgeYs = new int[21];
        int n = 21;
        for (int i = 0; i < n; i++) {
            double t = -1.0 + (i / (double)(n - 1)) * 2.0;
            double a = t * Math.toRadians(65);
            edgeXs[i] = (int)(Math.cos(a) * edgeR);
            edgeYs[i] = (int)(Math.sin(a) * edgeR);
        }
        g.changeColor(new Color(255, 250, 220, (int)(160 * alpha)));
        for (int i = 0; i < n - 1; i++) {
            g.drawLine(edgeXs[i], edgeYs[i], edgeXs[i + 1], edgeYs[i + 1], 2.0);
        }

        g.restoreLastTransform();
    }

    private void renderBoomerang(GameEngine g) {
        double lifeRatio = age / lifetime;
        double alpha = 1.0 - lifeRatio * 0.3;
        double spin = age * 12.0;
        double s = radius * 2.2;

        g.saveCurrentTransform();
        g.translate(x, y);
        g.rotate(Math.toDegrees(angleRad) + Math.toDegrees(spin));

        // Wind trail rings
        for (int i = 1; i <= 3; i++) {
            double ringR = s * (1.1 + i * 0.45);
            int ringA = (int)(25 * alpha / i);
            g.changeColor(new Color(160, 230, 180, ringA));
            g.drawCircle(0, 0, ringR, 1.5);
        }

        // Outer glow
        int glowA = (int)(45 * alpha);
        g.changeColor(new Color(100, 210, 140, glowA));
        g.drawSolidCircle(0, 0, s * 1.5);

        // 3-pointed star polygon
        double outerR = s * 1.55;
        double innerR = s * 0.5;
        int[] xs = new int[6];
        int[] ys = new int[6];
        for (int i = 0; i < 6; i++) {
            double a = Math.toRadians(i * 60 - 90);
            double r = (i % 2 == 0) ? outerR : innerR;
            xs[i] = (int)(Math.cos(a) * r);
            ys[i] = (int)(Math.sin(a) * r);
        }

        // Star shadow
        int shadowA = (int)(120 * alpha);
        g.changeColor(new Color(50, 120, 70, shadowA));
        g.drawSolidPolygon(xs, ys, 6);

        // Star body (slightly smaller)
        double bodyOuter = outerR * 0.88;
        double bodyInner = innerR * 0.88;
        for (int i = 0; i < 6; i++) {
            double a = Math.toRadians(i * 60 - 90);
            double r = (i % 2 == 0) ? bodyOuter : bodyInner;
            xs[i] = (int)(Math.cos(a) * r);
            ys[i] = (int)(Math.sin(a) * r);
        }
        int bodyA = (int)(220 * alpha);
        g.changeColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), bodyA));
        g.drawSolidPolygon(xs, ys, 6);

        // Blade edge highlights
        int edgeA = (int)(180 * alpha);
        g.changeColor(new Color(200, 250, 210, edgeA));
        for (int i = 0; i < 3; i++) {
            double a = Math.toRadians(i * 120 - 90);
            double bx = Math.cos(a) * bodyOuter * 0.5;
            double by = Math.sin(a) * bodyOuter * 0.5;
            double ex = Math.cos(a) * bodyOuter;
            double ey = Math.sin(a) * bodyOuter;
            g.drawLine(bx, by, ex, ey, 2.5);
        }

        // Hollow center
        double holeR = s * 0.35;
        g.changeColor(new Color(30, 60, 40, (int)(200 * alpha)));
        g.drawSolidCircle(0, 0, holeR);
        // Bright inner ring
        g.changeColor(new Color(180, 240, 190, (int)(160 * alpha)));
        g.drawCircle(0, 0, holeR, 2.0);
        // Tiny center dot
        g.changeColor(new Color(220, 255, 230, (int)(140 * alpha)));
        g.drawSolidCircle(0, 0, holeR * 0.25);

        g.restoreLastTransform();
    }

    private void renderFireball(GameEngine g) {
        double lifeRatio = age / lifetime;
        double alpha = 1.0 - lifeRatio * 0.3;
        double s = radius * 1.4;
        double flicker = 1.0 + Math.sin(age * 22.0) * 0.15 + Math.sin(age * 37.0) * 0.1;

        g.saveCurrentTransform();
        g.translate(x, y);

        // Outer glow
        int glowA = (int)(50 * alpha);
        g.changeColor(new Color(255, 100, 20, glowA));
        g.drawSolidCircle(0, 0, s * flicker * 2.2);
        g.changeColor(new Color(255, 160, 40, (int)(35 * alpha)));
        g.drawSolidCircle(0, 0, s * flicker * 1.7);

        // Fire aura
        int auraA = (int)(140 * alpha);
        g.changeColor(new Color(255, 80, 10, auraA));
        g.drawSolidCircle(0, 0, s * flicker * 1.25);

        // Main fire body
        int bodyA = (int)(220 * alpha);
        g.changeColor(new Color(255, 180, 30, bodyA));
        g.drawSolidCircle(0, -s * 0.1, s * flicker);

        // Hot core
        int coreA = (int)(240 * alpha);
        g.changeColor(new Color(255, 240, 140, coreA));
        g.drawSolidCircle(0, -s * 0.05, s * flicker * 0.55);

        // White-hot center
        g.changeColor(new Color(255, 255, 220, (int)(200 * alpha)));
        g.drawSolidCircle(0, 0, s * flicker * 0.22);

        // Flame sparks (flying particles)
        for (int i = 0; i < 5; i++) {
            double sparkAngle = age * 14.0 + i * Math.PI * 2.0 / 5.0;
            double sparkDist = s * 0.7 + Math.sin(age * 12.0 + i) * s * 0.4;
            double sx = Math.cos(sparkAngle) * sparkDist;
            double sy = Math.sin(sparkAngle) * sparkDist - s * 0.2;
            double sparkSize = 2.0 + Math.abs(Math.sin(age * 8.0 + i * 1.7)) * 2.5;
            g.changeColor(new Color(255, 220, 60, (int)(180 * alpha)));
            g.drawSolidRectangle(sx - sparkSize * 0.5, sy - sparkSize * 0.5, sparkSize, sparkSize);
        }

        // Trail flame behind
        g.rotate(Math.toDegrees(angleRad));
        double trailLen = s * 0.6;
        g.changeColor(new Color(255, 120, 20, (int)(100 * alpha)));
        g.drawSolidRectangle(-s * 0.35, -s * 0.25, s * 0.7, s * 0.5);
        g.changeColor(new Color(255, 200, 60, (int)(150 * alpha)));
        g.drawSolidRectangle(-s * 0.25, -s * 0.18, s * 0.5, s * 0.36);

        g.restoreLastTransform();
    }

    private void renderKnife(GameEngine g) {
        double lifeRatio = age / lifetime;
        double alpha = 1.0 - lifeRatio * 0.3;
        double spinAngle = age * 18.0; // fast spinning
        double s = radius * 1.6;

        g.saveCurrentTransform();
        g.translate(x, y);
        g.rotate(Math.toDegrees(angleRad) + spinAngle);

        // Glow trail
        int glowAlpha = (int)(40 * alpha);
        g.changeColor(new Color(180, 200, 240, glowAlpha));
        g.drawSolidCircle(0, 0, s * 1.4);

        // Blade body - diamond shape
        int bodyAlpha = (int)(230 * alpha);
        g.changeColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), bodyAlpha));
        g.drawSolidRectangle(-s * 0.15, -s * 0.9, s * 0.3, s * 1.8);
        g.drawSolidRectangle(-s * 0.6, -s * 0.25, s * 1.2, s * 0.5);

        // Cross-guard
        g.changeColor(new Color(180, 180, 200, bodyAlpha));
        g.drawSolidRectangle(-s * 0.7, -s * 0.35, s * 1.4, s * 0.12);

        // Bright center line
        int coreAlpha = (int)(180 * alpha);
        g.changeColor(new Color(255, 255, 255, coreAlpha));
        g.drawSolidRectangle(-s * 0.06, -s * 0.7, s * 0.12, s * 1.4);

        g.restoreLastTransform();
    }

    private void drawCrescent(GameEngine g, double size, Color color) {
        g.changeColor(color);
        int segments = 24;
        double outerAngle = Math.toRadians(65);
        int total = segments * 2 + 2;
        int[] xs = new int[total];
        int[] ys = new int[total];
        int idx = 0;

        // Outer arc: center (0,0), radius size, from -65° to +65°
        for (int i = 0; i <= segments; i++) {
            double t = (i / (double)segments) * 2.0 - 1.0;
            double a = t * outerAngle;
            xs[idx] = (int)(Math.cos(a) * size);
            ys[idx] = (int)(Math.sin(a) * size);
            idx++;
        }

        // Inner arc: center offset left, radius smaller, from +65° to -65°
        double innerCx = -size * 0.42;
        double innerR = size * 0.55;
        for (int i = segments; i >= 0; i--) {
            double t = (i / (double)segments) * 2.0 - 1.0;
            double a = t * outerAngle;
            xs[idx] = (int)(innerCx + Math.cos(a) * innerR);
            ys[idx] = (int)(Math.sin(a) * innerR);
            idx++;
        }

        g.drawSolidPolygon(xs, ys, total);
    }
}
