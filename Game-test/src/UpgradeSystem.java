import java.util.ArrayList;

public class UpgradeSystem {
    private final SurvivalGame game;
    private double xp;
    private int level;
    private final UpgradeDef[] offered = new UpgradeDef[3];
    private int hoveredCard = -1;

    public UpgradeSystem(SurvivalGame game) {
        this.game = game;
    }

    public void reset() {
        xp = 0;
        level = 1;
        hoveredCard = -1;
        clearOffered();
    }

    public void gainXP(double amount) {
        xp += amount;
        checkLevelUp();
    }

    private void checkLevelUp() {
        while (xp >= xpForNextLevel()) {
            int oldLevel = level;
            xp -= xpForNextLevel();
            level++;
            if (level == 25 || level == 50 || level == 75) {
                game.onLevelMilestone(level);
            }
            if (CharacterProgression.justCrossedTier(oldLevel, level)) {
                int newTier = CharacterProgression.tierForLevel(level);
                game.enterTierUp(newTier);
            } else {
                generateChoices();
                game.enterUpgradePause();
            }
            break;
        }
    }

    public double xpForNextLevel() {
        return GameConfig.XP_BASE_REQUIREMENT + level * GameConfig.XP_LEVEL_SCALAR
            + level * level * GameConfig.XP_QUADRATIC_FACTOR;
    }

    public void generateChoices() {
        for (int i = 0; i < 3; i++) {
            offered[i] = makeOneChoice();
        }
    }

    private UpgradeDef makeOneChoice() {
        int roll = game.rand(100);

        if (roll < 30) {
            ArrayList<Integer> newWeaponPool = new ArrayList<Integer>();
            for (int wid = WeaponDef.WHIP; wid <= WeaponDef.KNIFE; wid++) {
                if (!game.weaponManager.hasWeapon(wid) && game.weaponManager.list().size() < 6) {
                    newWeaponPool.add(wid);
                }
            }
            if (!newWeaponPool.isEmpty()) {
                int wid = newWeaponPool.get(game.rand(newWeaponPool.size()));
                final int id = wid;
                return new UpgradeDef("New Weapon: " + WeaponDef.ALL[id].name,
                    "Gain a new weapon slot.",
                    new UpgradeDef.UpgradeAction() {
                        @Override
                        public void apply(SurvivalGame g) {
                            g.weaponManager.addWeapon(id);
                        }
                    });
            }
        }

        if (roll < 70) {
            ArrayList<WeaponInstance> upgradable = new ArrayList<WeaponInstance>();
            for (WeaponInstance w : game.weaponManager.list()) {
                if (w.canLevelUp()) upgradable.add(w);
            }
            if (!upgradable.isEmpty()) {
                WeaponInstance picked = upgradable.get(game.rand(upgradable.size()));
                final int id = picked.weaponId;
                return new UpgradeDef("Upgrade " + picked.def().name,
                    "Damage up, cooldown down.",
                    new UpgradeDef.UpgradeAction() {
                        @Override
                        public void apply(SurvivalGame g) {
                            g.weaponManager.upgradeWeapon(id);
                        }
                    });
            }
        }

        int stat = game.rand(4);
        if (stat == 0) {
            return new UpgradeDef("Swift Boots", "Move speed +10%",
                new UpgradeDef.UpgradeAction() {
                    @Override
                    public void apply(SurvivalGame g) {
                        g.playerBoostSpeed(1.10);
                    }
                });
        } else if (stat == 1) {
            return new UpgradeDef("Power Core", "Damage multiplier +15%",
                new UpgradeDef.UpgradeAction() {
                    @Override
                    public void apply(SurvivalGame g) {
                        g.player.damageMultiplier *= 1.15;
                    }
                });
        } else if (stat == 2) {
            return new UpgradeDef("Magnet", "Pickup range +20%",
                new UpgradeDef.UpgradeAction() {
                    @Override
                    public void apply(SurvivalGame g) {
                        g.player.pickupRangeMultiplier *= 1.2;
                    }
                });
        } else {
            return new UpgradeDef("Healing Light", "Restore 30% of max HP",
                new UpgradeDef.UpgradeAction() {
                    @Override
                    public void apply(SurvivalGame g) {
                        double healAmount = g.player.maxHealth * 0.30;
                        g.player.health = Math.min(g.player.maxHealth, g.player.health + healAmount);
                    }
                });
        }
    }

    public void choose(int index) {
        if (index < 0 || index >= offered.length || offered[index] == null) return;
        offered[index].apply(game);
        clearOffered();
        game.resumePlayingFromUpgrade();
    }

    public void clearOffered() {
        for (int i = 0; i < offered.length; i++) offered[i] = null;
    }

    public UpgradeDef[] offered() {
        return offered;
    }

    public int level() {
        return level;
    }

    public double xp() {
        return xp;
    }

    public int hoveredCard() {
        return hoveredCard;
    }

    public void setHoveredCard(int hoveredCard) {
        this.hoveredCard = hoveredCard;
    }
}
