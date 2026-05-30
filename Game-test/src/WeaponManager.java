import java.util.ArrayList;
// Jiaxu Wang (24009377)  Jiaheng Liu (24009483)  Angze Song (24009333)   Xiao Wu (24009458)
public class WeaponManager {
    private final SurvivalGame game;
    private final ArrayList<WeaponInstance> weapons = new ArrayList<WeaponInstance>();
    double dropCooldown;
    private final double[] orbiterAngles = new double[3];
    private boolean orbitersActive;
    private final ArrayList<MagicCircle> circles = new ArrayList<MagicCircle>();
    public double shieldTimer;

    class MagicCircle {
        double x, y, timer, duration, radius, damage;
        int type; // 0=heal, 1=lightning
        boolean alive = true;
    }

    public WeaponManager(SurvivalGame game) {
        this.game = game;
    }

    public void reset(int firstWeaponId) {
        weapons.clear();
        circles.clear();
        dropCooldown = 0;
        orbitersActive = false;
        shieldTimer = 0;
        addWeapon(firstWeaponId);
    }

    public void update(double dt) {
        Player player = game.player;
        if (player == null || !player.alive) return;

        for (WeaponInstance w : weapons) {
            w.cooldownTimer -= dt;
            if (w.cooldownTimer <= 0) {
                fire(w);
                w.cooldownTimer += w.currentCooldown(player);
            }
        }

        updateFireOrbiters(dt, player);
        updateCircles(dt, player);
        if (shieldTimer > 0) shieldTimer -= dt;
        if (dropCooldown > 0) dropCooldown -= dt;
    }

    private void updateCircles(double dt, Player player) {
        for (int i = circles.size() - 1; i >= 0; i--) {
            MagicCircle c = circles.get(i);
            c.timer -= dt;
            if (c.timer <= 0) {
                circles.remove(i);
                continue;
            }
            if (c.type == 0) {
                // Healing: regen while player stands inside
                double dx = player.x - c.x;
                double dy = player.y - c.y;
                if (dx * dx + dy * dy <= c.radius * c.radius) {
                    player.health = Math.min(player.maxHealth,
                        player.health + player.maxHealth * 0.04 * dt);
                }
            }
        }
    }

    public void renderCircles(GameEngine g) {
        for (MagicCircle c : circles) {
            double alpha = Math.min(1.0, c.timer / 0.3) * (c.type == 0 ? 0.7 : 0.85);
            g.saveCurrentTransform();
            g.translate(c.x, c.y);

            if (c.type == 0) {
                // Healing: layered green circles with leaf particles + light pillars
                double pulse = 1.0 + Math.sin(c.timer * 3.5) * 0.05;
                double r = c.radius;
                // Ground aura - bright center fading outward
                for (int i = 4; i >= 0; i--) {
                    double rr = r * (0.3 + i * 0.15) * pulse;
                    int a = (int)(50 * alpha * (1.0 - i * 0.18));
                    g.changeColor(new java.awt.Color(80, 230, 110, a));
                    g.drawSolidCircle(0, 0, rr);
                }
                // Rotating runic ring segments
                int segs = 8;
                for (int i = 0; i < segs; i++) {
                    double segAngle = i * Math.PI * 2 / segs + c.timer * 0.8;
                    double sx = Math.cos(segAngle) * r * 0.9;
                    double sy = Math.sin(segAngle) * r * 0.9;
                    g.changeColor(new java.awt.Color(120, 250, 140, (int)(140 * alpha)));
                    double ss = 5 + Math.sin(c.timer * 6 + i) * 2;
                    g.drawSolidRectangle(sx - ss * 0.5, sy - ss * 0.5, ss, ss);
                }
                // Outer border with glow
                g.changeColor(new java.awt.Color(100, 240, 130, (int)(90 * alpha)));
                g.drawCircle(0, 0, r * pulse, 3.5);
                g.changeColor(new java.awt.Color(160, 255, 170, (int)(50 * alpha)));
                g.drawCircle(0, 0, r * pulse * 1.08, 1.5);
                // Central cross / medical symbol
                double cs = r * 0.18;
                g.changeColor(new java.awt.Color(200, 255, 200, (int)(200 * alpha)));
                g.drawSolidRectangle(-cs * 1.1, -cs * 0.25, cs * 2.2, cs * 0.5);
                g.drawSolidRectangle(-cs * 0.25, -cs * 1.1, cs * 0.5, cs * 2.2);
                // Light pillars rising from edges
                for (int i = 0; i < 6; i++) {
                    double pa = i * Math.PI * 2 / 6 + c.timer * 0.5;
                    double px = Math.cos(pa) * r * 0.75;
                    double py = -(20 + (c.timer * 60) % 80);
                    double pw = 3 + Math.abs(Math.sin(c.timer * 4 + i)) * 4;
                    g.changeColor(new java.awt.Color(180, 255, 190, (int)(100 * alpha)));
                    g.drawSolidRectangle(px - pw * 0.5, py - 20, pw, 40);
                    g.changeColor(new java.awt.Color(220, 255, 220, (int)(160 * alpha)));
                    g.drawSolidRectangle(px - 1, py - 15, 2, 30);
                }
                // Floating leaf/drop particles
                for (int i = 0; i < 10; i++) {
                    double pa = i * Math.PI * 2 / 10 + c.timer * 1.5;
                    double pr = r * (0.2 + (c.timer * 0.6 + i * 0.1) % 0.7);
                    double py = -10 - (c.timer * 25 + i * 7) % 55;
                    g.changeColor(new java.awt.Color(160, 255, 170, (int)(140 * alpha)));
                    double ps = 2.0 + Math.random() * 3;
                    g.drawSolidRectangle(
                        Math.cos(pa) * pr - ps * 0.5,
                        py - ps * 0.5, ps, ps);
                }
            } else {
                // Lightning: purple/gold circles with crackle
                double shake = (Math.random() - 0.5) * 4;
                // Outer lightning ring
                g.changeColor(new java.awt.Color(180, 130, 255, (int)(50 * alpha)));
                g.drawSolidCircle(shake, shake, c.radius * 1.05);
                // Inner bright ring
                g.changeColor(new java.awt.Color(240, 200, 80, (int)(90 * alpha)));
                g.drawCircle(0, 0, c.radius, 3.5);
                g.changeColor(new java.awt.Color(200, 160, 255, (int)(70 * alpha)));
                g.drawCircle(0, 0, c.radius * 0.75, 2.0);
                // Lightning bolts
                g.changeColor(new java.awt.Color(255, 240, 150, (int)(200 * alpha)));
                for (int i = 0; i < 6; i++) {
                    double la = i * Math.PI * 2 / 6;
                    double lr = c.radius * 0.25;
                    for (int s = 0; s < 3; s++) {
                        double sx = Math.cos(la) * lr + (Math.random() - 0.5) * c.radius * 0.5;
                        double sy = Math.sin(la) * lr + (Math.random() - 0.5) * c.radius * 0.5;
                        double ex = Math.cos(la) * c.radius * (0.7 + Math.random() * 0.3);
                        double ey = Math.sin(la) * c.radius * (0.7 + Math.random() * 0.3);
                        g.drawLine(sx, sy, ex, ey, 2.5);
                    }
                }
                // Center flash
                g.changeColor(new java.awt.Color(255, 255, 200, (int)(150 * alpha)));
                g.drawSolidCircle(0, 0, c.radius * 0.18);
            }
            g.restoreLastTransform();
        }
    }

    private void updateFireOrbiters(double dt, Player player) {
        WeaponInstance fireStaff = getWeapon(WeaponDef.MAGIC_WAND);
        boolean shouldOrbit = fireStaff != null && fireStaff.level >= 5;

        if (shouldOrbit && !orbitersActive) {
            orbitersActive = true;
            for (int i = 0; i < 3; i++) {
                orbiterAngles[i] = Math.PI * 2.0 * i / 3.0;
                Projectile orb = Projectile.createOrbiter(game, orbiterAngles[i], 90, 3.2,
                    fireStaff.currentDamage(player) * 0.08, 12,
                    new java.awt.Color(255, 160, 30));
                game.addProjectile(orb);
            }
        }

        if (!shouldOrbit && orbitersActive) {
            orbitersActive = false;
            for (int i = game.projectiles.size() - 1; i >= 0; i--) {
                Projectile p = game.projectiles.get(i);
                if (p.orbiting) p.alive = false;
            }
        }
    }

    public boolean canDropWeapon() {
        return dropCooldown <= 0;
    }

    public void markDrop() {
        dropCooldown = GameConfig.WEAPON_DROP_COOLDOWN;
    }

    private void fire(WeaponInstance w) {
        WeaponDef def = w.def();
        Player player = game.player;

        // Melee weapons trigger attack animation
        if (w.weaponId == WeaponDef.SWORD) {
            player.triggerAttackAnim();
        }

        // Arcane Circle: random ritual effect
        if (w.weaponId == WeaponDef.AXE) {
            int roll = game.rand(3);
            if (roll == 0) {
                // Healing circle
                MagicCircle c = new MagicCircle();
                c.x = player.x; c.y = player.y;
                c.type = 0; c.timer = 5.0; c.duration = 5.0;
                c.radius = 90; c.damage = 0;
                circles.add(c);
                game.vfx.addShake(2);
            } else if (roll == 1) {
                // Lightning circle at densest enemy cluster
                double cx = player.x, cy = player.y;
                int bestCount = 0;
                for (Enemy e : game.enemies) {
                    if (!e.alive) continue;
                    int count = 0;
                    for (Enemy o : game.enemies) {
                        if (!o.alive || o == e) continue;
                        double ddx = o.x - e.x, ddy = o.y - e.y;
                        if (ddx * ddx + ddy * ddy <= 150 * 150) count++;
                    }
                    if (count > bestCount) {
                        bestCount = count;
                        cx = e.x;
                        cy = e.y;
                    }
                }
                MagicCircle c = new MagicCircle();
                c.x = cx; c.y = cy;
                c.type = 1; c.timer = 0.7; c.duration = 0.7;
                c.radius = 200; c.damage = w.currentDamage(player);
                circles.add(c);
                // Instant damage to all enemies in radius
                double rr = c.radius * c.radius;
                for (Enemy e : game.enemies) {
                    if (!e.alive) continue;
                    double dx = e.x - c.x;
                    double dy = e.y - c.y;
                    if (dx * dx + dy * dy <= rr) {
                        e.takeDamage(c.damage, player);
                        game.vfx.spawnDamageText(e.x, e.y - e.radius, c.damage);
                    }
                }
                game.vfx.addShake(10);
            } else {
                // Shield: 3s invincibility
                player.setInvulnTimer(3.0);
                shieldTimer = 3.0;
            }
            return;
        }

        int count = w.currentProjectileCount();
        double spread = Math.toRadians(w.currentSpreadAngle());
        double baseAngle;

        if (def.targetingMode == WeaponDef.TARGET_NEAREST) {
            Enemy nearest = game.findNearestEnemy(player.x, player.y);
            if (nearest == null) return;
            baseAngle = Math.atan2(nearest.y - player.y, nearest.x - player.x);
        } else if (def.targetingMode == WeaponDef.TARGET_RANDOM) {
            baseAngle = Math.toRadians(game.rand(360.0));
        } else {
            baseAngle = player.facingAngleRad;
        }

        // Boomerang: special firing pattern
        if (w.weaponId == WeaponDef.GARLIC) {
            double[] angles;
            if (w.level >= 5) {
                angles = new double[] {baseAngle, baseAngle + Math.PI};
            } else {
                angles = new double[] {baseAngle};
            }
            for (double a : angles) {
                Projectile b = Projectile.createBoomerang(game, player.x, player.y,
                    def.projectileRadius, def.projectileSpeed, a,
                    w.currentDamage(player), def.lifetime, 500, def.projColor);
                game.addProjectile(b);
            }
            return;
        }

        for (int i = 0; i < count; i++) {
            double t = count <= 1 ? 0 : (i / (double)(count - 1)) * 2 - 1;
            double angle = baseAngle + t * spread;
            int pierce;
            if (w.weaponId == WeaponDef.KNIFE) {
                pierce = 1 + (w.level >= 3 ? 1 : 0) + (w.level >= 5 ? 1 : 0);
            } else {
                pierce = (w.level >= 5) ? 1 : 0;
            }
            int projType;
            if (w.weaponId == WeaponDef.SWORD) projType = Projectile.TYPE_SWORD_WAVE;
            else if (w.weaponId == WeaponDef.KNIFE) projType = Projectile.TYPE_KNIFE;
            else if (w.weaponId == WeaponDef.MAGIC_WAND) projType = Projectile.TYPE_FIRE_MAGIC;
            else projType = Projectile.TYPE_ARROW;
            Projectile p = new Projectile(game, player.x, player.y, def.projectileRadius, def.projectileSpeed,
                angle, w.currentDamage(player), def.lifetime, pierce, true, def.projColor, projType);
            if (w.weaponId == WeaponDef.SWORD) {
                p.x += Math.cos(angle) * 26;
                p.y += Math.sin(angle) * 26;
            }
            game.addProjectile(p);
        }
    }

    public void pickupWeapon(int weaponId, WeaponRarity rarity) {
        WeaponInstance existing = getWeapon(weaponId);
        if (existing != null) {
            if (existing.canLevelUp()) {
                existing.levelUp();
                System.out.println("[Pickup] Upgraded " + WeaponDef.ALL[weaponId].name + " to Lv" + existing.level);
            } else {
                System.out.println("[Pickup] " + WeaponDef.ALL[weaponId].name + " already MAX");
            }
            return;
        }
        if (weapons.size() < maxSlots()) {
            weapons.add(new WeaponInstance(weaponId, rarity, rarity.bonusLevels));
            System.out.println("[Pickup] Added " + rarity.name() + " " + WeaponDef.ALL[weaponId].name);
            return;
        }
        System.out.println("[Pickup] Slots full, opening swap for " + WeaponDef.ALL[weaponId].name);
        game.enterWeaponSwap(weaponId, rarity);
    }

    public boolean addWeapon(int weaponId) {
        if (hasWeapon(weaponId) || weapons.size() >= maxSlots()) return false;
        weapons.add(new WeaponInstance(weaponId));
        return true;
    }

    public boolean upgradeWeapon(int weaponId) {
        WeaponInstance w = getWeapon(weaponId);
        if (w == null || !w.canLevelUp()) return false;
        w.levelUp();
        return true;
    }

    public void replaceWeapon(int slotIndex, int newWeaponId, WeaponRarity rarity) {
        if (slotIndex < 0 || slotIndex >= weapons.size()) return;
        weapons.set(slotIndex, new WeaponInstance(newWeaponId, rarity, rarity.bonusLevels));
    }

    public int maxSlots() {
        return GameConfig.MAX_WEAPON_SLOTS;
    }

    public boolean hasWeapon(int weaponId) {
        return getWeapon(weaponId) != null;
    }

    public WeaponInstance getWeapon(int weaponId) {
        for (WeaponInstance w : weapons) {
            if (w.weaponId == weaponId) return w;
        }
        return null;
    }

    public ArrayList<WeaponInstance> list() {
        return weapons;
    }
}
