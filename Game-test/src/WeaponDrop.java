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

        // Beam of light rising from the drop
        double beamH = 20 + Math.sin(beamTimer) * 4;
        int alpha = 140 + (int)(Math.sin(beamTimer * 1.7) * 40);
        Color beam = new Color(rarity.beamColor.getRed(), rarity.beamColor.getGreen(),
                               rarity.beamColor.getBlue(), Math.min(255, alpha));
        g.changeColor(beam);
        g.drawSolidRectangle(x - 2, y - 12 - beamH, 4, beamH);

        // Chest body
        g.changeColor(new Color(120, 70, 30));
        g.drawSolidRectangle(x - 6, y - 5, 12, 10);
        g.changeColor(new Color(160, 110, 50));
        g.drawSolidRectangle(x - 5, y - 4, 10, 4);
        g.changeColor(rarity.beamColor);
        g.drawSolidRectangle(x - 1, y - 2, 3, 3);

        // Glow ring
        double glowR = 8 + Math.sin(beamTimer * 0.7) * 1.5;
        g.changeColor(new Color(rarity.beamColor.getRed(), rarity.beamColor.getGreen(),
                                rarity.beamColor.getBlue(), 80));
        g.drawSolidCircle(x, y, glowR);
    }
}
