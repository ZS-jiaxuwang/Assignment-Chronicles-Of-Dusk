import java.awt.Color;

public class WeaponDrop extends Pickup {
    final int weaponId;
    final WeaponRarity rarity;
    double beamTimer;

    public WeaponDrop(SurvivalGame game, double x, double y, int weaponId, WeaponRarity rarity) {
        super(game, x, y, TYPE_WEAPON, rarity.beamColor, 7);
        this.weaponId = weaponId;
        this.rarity = rarity;
        this.magnetRange = GameConfig.WEAPON_DROP_MAGNET_RANGE;
        this.magnetStrength = GameConfig.PICKUP_MAGNET_STRENGTH * 0.7;
        this.beamTimer = Math.random() * Math.PI * 2;
    }

    @Override
    public void onUpdate(double dt) {
        super.onUpdate(dt);
        beamTimer += dt * 3;
    }

    @Override
    public void render(GameEngine g) {
        if (!alive) return;

        double pulse = 1.0 + Math.sin(beamTimer * 2.0) * 0.15;
        double s = 7 * pulse;
        if (s > 10) s = 10;

        // Floating beam
        double beamH = 14 + Math.sin(beamTimer) * 3;
        int alpha = 120 + (int)(Math.sin(beamTimer * 1.7) * 30);
        Color beam = new Color(rarity.beamColor.getRed(), rarity.beamColor.getGreen(),
                               rarity.beamColor.getBlue(), Math.min(255, alpha));
        g.changeColor(beam);
        g.drawSolidRectangle(x - 1.5, y - 10 - beamH, 3, beamH);

        // Glow ring
        g.changeColor(new Color(rarity.beamColor.getRed(), rarity.beamColor.getGreen(),
                                rarity.beamColor.getBlue(), 70));
        g.drawSolidCircle(x, y, s * 1.5);

        // Weapon icon
        g.saveCurrentTransform();
        g.translate(x, y);
        WeaponDef def = WeaponDef.ALL[weaponId];
        Color iconColor = new Color(
            Math.min(255, def.projColor.getRed() + 40),
            Math.min(255, def.projColor.getGreen() + 40),
            Math.min(255, def.projColor.getBlue() + 40), 230);

        if (weaponId == WeaponDef.SWORD) {
            // Golden crescent
            g.changeColor(iconColor);
            g.drawSolidRectangle(-s * 0.35, -s * 0.6, s * 0.7, s * 1.2);
            g.drawSolidRectangle(s * 0.2, -s * 0.35, s * 0.15, s * 0.7);
            g.drawSolidRectangle(-s * 0.35, -s * 0.35, s * 0.15, s * 0.7);
            g.changeColor(new Color(255, 255, 200, 200));
            g.drawSolidRectangle(-s * 0.1, -s * 0.3, s * 0.2, s * 0.6);
        } else if (weaponId == WeaponDef.MAGIC_WAND) {
            // Fire flame
            g.changeColor(new Color(255, 140, 20, 140));
            g.drawSolidCircle(0, s * 0.1, s * 0.9);
            g.changeColor(iconColor);
            g.drawSolidCircle(0, s * 0.2, s * 0.6);
            g.changeColor(new Color(255, 240, 140, 220));
            g.drawSolidCircle(0, s * 0.15, s * 0.25);
        } else if (weaponId == WeaponDef.GARLIC) {
            // Green triangle star
            g.changeColor(iconColor);
            double or2 = s * 1.0, ir2 = s * 0.3;
            int[] xs2 = new int[6], ys2 = new int[6];
            for (int i = 0; i < 6; i++) {
                double a = Math.toRadians(i * 60 - 90);
                double r = (i % 2 == 0) ? or2 : ir2;
                xs2[i] = (int)(Math.cos(a) * r);
                ys2[i] = (int)(Math.sin(a) * r);
            }
            g.drawSolidPolygon(xs2, ys2, 6);
            g.changeColor(new Color(20, 50, 30, 180));
            g.drawSolidCircle(0, 0, s * 0.28);
        } else if (weaponId == WeaponDef.KNIFE) {
            // Silver dagger
            g.changeColor(iconColor);
            g.drawSolidRectangle(-s * 0.1, -s * 0.8, s * 0.2, s * 1.6);
            g.drawSolidRectangle(-s * 0.45, -s * 0.2, s * 0.9, s * 0.4);
            g.drawSolidRectangle(-s * 0.5, -s * 0.3, s * 1.0, s * 0.12);
            g.changeColor(new Color(255, 255, 255, 200));
            g.drawSolidRectangle(-s * 0.04, -s * 0.6, s * 0.08, s * 1.2);
        } else if (weaponId == WeaponDef.AXE) {
            // Purple arcane circle
            g.changeColor(new Color(160, 120, 240, 60));
            g.drawSolidCircle(0, 0, s * 1.0);
            g.changeColor(iconColor);
            g.drawCircle(0, 0, s * 0.9, 1.5);
            g.drawCircle(0, 0, s * 0.5, 1.0);
            g.changeColor(new Color(220, 200, 255, 200));
            g.drawSolidCircle(0, 0, s * 0.22);
        } else {
            // Generic
            g.changeColor(iconColor);
            g.drawSolidCircle(0, 0, s * 0.7);
        }

        g.restoreLastTransform();
    }
}
