import java.util.ArrayList;

public class CollisionSystem {
    private final SurvivalGame game;
    private final SpatialGrid enemyGrid;
    private final SpatialGrid pickupGrid;
    private final double[] pushBuffer = new double[2];

    public CollisionSystem(SurvivalGame game) {
        this.game = game;
        this.enemyGrid = new SpatialGrid(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT, 200);
        this.pickupGrid = new SpatialGrid(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT, 200);
    }

    public void update(double dt) {
        Player player = game.player;
        if (player == null || !player.alive) return;

        enemyGrid.clear();
        pickupGrid.clear();

        for (Enemy e : game.enemies) {
            if (e.alive && !e.dying) enemyGrid.insert(e);
        }
        for (Pickup p : game.pickups) {
            if (p.alive) pickupGrid.insert(p);
        }

        playerVsEnemies(player);
        projectilesVsEnemies();
        hostileProjectilesVsPlayer(player);
        playerVsPickups(player);
        obstacleVsPlayer(player);
        obstacleVsEnemies();
        obstacleVsProjectiles();
    }

    private void playerVsEnemies(Player p) {
        ArrayList<Entity> nearby = enemyGrid.getNearby(p);
        for (Entity entity : nearby) {
            Enemy e = (Enemy)entity;
            if (!e.alive || e.dying) continue;
            double rr = p.radius + e.radius;
            double dx = p.x - e.x;
            double dy = p.y - e.y;
            if (dx * dx + dy * dy <= rr * rr) {
                p.takeDamage(e.getContactDamage(), e);
                double len = Math.sqrt(Math.max(0.001, dx * dx + dy * dy));
                p.x += (dx / len) * 6;
                p.y += (dy / len) * 6;
            }
        }
    }

    private void projectilesVsEnemies() {
        for (Projectile proj : game.projectiles) {
            if (!proj.alive || !proj.friendly) continue;
            ArrayList<Entity> nearby = enemyGrid.getNearby(proj);
            for (Entity entity : nearby) {
                Enemy e = (Enemy)entity;
                if (!e.alive || e.dying) continue;

                double rr = proj.radius + e.radius;
                double dx = proj.x - e.x;
                double dy = proj.y - e.y;
                if (dx * dx + dy * dy <= rr * rr) {
                    if (!proj.canHit(e)) continue;
                    double damage = proj.damage * game.player.damageMultiplier;
                    boolean isCrit = game.rand(1.0) < game.player.critChance;
                    if (isCrit) {
                        damage *= game.player.critDamage;
                    }
                    e.takeDamage(damage, proj);

                    if (damage > 0 && game.player.lifeSteal > 0 && game.player.alive) {
                        double heal = damage * game.player.lifeSteal;
                        game.player.health = Math.min(game.player.maxHealth, game.player.health + heal);
                    }

                    game.vfx.spawnDamageText(e.x, e.y - e.radius, damage, isCrit);
                    proj.markHit(e);

                    double knockback = 5.0;
                    e.x += Math.cos(proj.angleRad) * knockback;
                    e.y += Math.sin(proj.angleRad) * knockback;
                    game.triggerHitstop(isCrit ? 0.05 : 0.03);

                    if (proj.pierces > 0) {
                        proj.pierces--;
                    } else {
                        proj.alive = false;
                        break;
                    }
                }
            }
        }
    }

    private void playerVsPickups(Player p) {
        ArrayList<Entity> nearby = pickupGrid.getNearby(p);
        for (Entity entity : nearby) {
            Pickup pickup = (Pickup)entity;
            if (!pickup.alive) continue;
            double rr = p.radius + pickup.radius;
            double dx = p.x - pickup.x;
            double dy = p.y - pickup.y;
            if (dx * dx + dy * dy <= rr * rr) {
                if (pickup.pickupType == Pickup.TYPE_WEAPON) {
                    WeaponDrop wd = (WeaponDrop) pickup;
                    pickup.alive = false;
                    game.weaponManager.pickupWeapon(wd.weaponId, wd.rarity);
                } else {
                    pickup.alive = false;
                    game.gainXP(pickup.getXpValue());
                }
            }
        }
    }

    private void hostileProjectilesVsPlayer(Player p) {
        for (Projectile proj : game.projectiles) {
            if (!proj.alive || proj.friendly) continue;
            double rr = proj.radius + p.radius;
            double dx = proj.x - p.x;
            double dy = proj.y - p.y;
            if (dx * dx + dy * dy <= rr * rr) {
                p.takeDamage(proj.damage, proj);
                proj.alive = false;
            }
        }
    }

    private void obstacleVsPlayer(Player p) {
        if (game.gameMap == null) {
            return;
        }
        ArrayList<Obstacle> nearby = game.gameMap.getNearby(p.x, p.y, 240);
        for (Obstacle ob : nearby) {
            if (!ob.blocksPlayer()) continue;
            if (ob.resolveCircleCollision(p.x, p.y, p.radius, pushBuffer)) {
                p.x += pushBuffer[0];
                p.y += pushBuffer[1];
            }
        }
        p.clampToWorld();
    }

    private void obstacleVsEnemies() {
        if (game.gameMap == null) {
            return;
        }
        for (Enemy e : game.enemies) {
            if (!e.alive || e.dying) continue;
            ArrayList<Obstacle> nearby = game.gameMap.getNearby(e.x, e.y, 180);
            for (Obstacle ob : nearby) {
                if (!ob.blocksEnemies()) continue;
                if (!ob.resolveCircleCollision(e.x, e.y, e.radius, pushBuffer)) continue;

                e.x += pushBuffer[0];
                e.y += pushBuffer[1];

                double pushLen = Math.sqrt(pushBuffer[0] * pushBuffer[0] + pushBuffer[1] * pushBuffer[1]);
                if (pushLen > 0.0001) {
                    double nx = pushBuffer[0] / pushLen;
                    double ny = pushBuffer[1] / pushLen;
                    double tangentX = -ny;
                    double tangentY = nx;
                    double dot = e.vx * tangentX + e.vy * tangentY;
                    e.vx = tangentX * dot * 0.7;
                    e.vy = tangentY * dot * 0.7;
                }
            }
        }
    }

    private void obstacleVsProjectiles() {
        if (game.gameMap == null) {
            return;
        }
        for (Projectile proj : game.projectiles) {
            if (!proj.alive) continue;
            ArrayList<Obstacle> nearby = game.gameMap.getNearby(proj.x, proj.y, 120);
            for (Obstacle ob : nearby) {
                if (ob.collidesWith(proj.x, proj.y, proj.radius)) {
                    if (proj.pierces > 0) {
                        proj.pierces--;
                    }
                    proj.alive = false;
                    break;
                }
            }
        }
    }
}
