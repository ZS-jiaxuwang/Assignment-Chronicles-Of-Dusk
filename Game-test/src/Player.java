import java.awt.Color;

public class Player extends Entity {
    private final SurvivalGame game;
    private final CharacterDef character;
    private double moveSpeed;
    private double invulnTimer;

    public double facingAngleRad = 0.0;
    public double pickupRangeMultiplier = 1.0;
    public double damageMultiplier = 1.0;
    public double cooldownMultiplier = 1.0;
    double animTimer;

    public int tier = 1;
    public UltimateSkill ultimate;
    double attackAnimTimer;
    double hurtAnimTimer;
    double deathTimer;
    boolean dying;

    public Player(SurvivalGame game, CharacterDef character, double x, double y) {
        super(x, y, GameConfig.PLAYER_RADIUS, GameConfig.PLAYER_MAX_HP * character.hpMultiplier, character.color);
        this.game = game;
        this.character = character;
        this.moveSpeed = GameConfig.PLAYER_BASE_SPEED * character.speedMultiplier;
        this.health = this.maxHealth;
        this.damageMultiplier = character.damageMultiplier;
        this.ultimate = new UltimateSkill(character.ultimateType, GameConfig.ULTIMATE_COOLDOWN);
    }

    @Override
    public void onUpdate(double dt) {
        double dx = 0;
        double dy = 0;

        if (game.isKeyDown('W')) dy -= 1;
        if (game.isKeyDown('S')) dy += 1;
        if (game.isKeyDown('A')) dx -= 1;
        if (game.isKeyDown('D')) dx += 1;

        if (dx != 0 || dy != 0) {
            double len = Math.sqrt(dx * dx + dy * dy);
            dx /= len;
            dy /= len;
            facingAngleRad = Math.atan2(dy, dx);
        }

        vx = dx * moveSpeed;
        vy = dy * moveSpeed;

        animTimer += dt;

        if (invulnTimer > 0) {
            invulnTimer -= dt;
            if (invulnTimer < 0) invulnTimer = 0;
        }

        if (attackAnimTimer > 0) attackAnimTimer -= dt;
        if (hurtAnimTimer > 0) hurtAnimTimer -= dt;
        if (dying) {
            deathTimer -= dt;
            vx = 0;
            vy = 0;
            if (deathTimer <= 0) {
                alive = false;
            }
            return;
        }
        ultimate.update(dt);
        if (ultimate.active) {
            ultimate.onActiveUpdate(game, dt);
        }
    }

    public void applyTierBonuses(int newTier) {
        int oldTier = this.tier;
        this.tier = newTier;

        double oldMax = maxHealth;
        maxHealth = GameConfig.PLAYER_MAX_HP * character.hpMultiplier
                    * CharacterProgression.hpMultiplier(newTier);
        health += (maxHealth - oldMax);

        damageMultiplier = character.damageMultiplier
                           * CharacterProgression.damageMultiplier(newTier);
        cooldownMultiplier = CharacterProgression.cooldownMultiplier(newTier);
        moveSpeed = GameConfig.PLAYER_BASE_SPEED * character.speedMultiplier
                    * CharacterProgression.speedMultiplier(newTier);

        if (newTier == 3 && oldTier < 3) {
            ultimate.cooldownTimer = 0;
        }
        if (newTier >= 4) {
            ultimate.setCooldown(GameConfig.ULTIMATE_COOLDOWN_TIER4);
        }
    }

    @Override
    public void takeDamage(double amount, Entity source) {
        if (invulnTimer > 0 || !alive || dying) {
            return;
        }
        super.takeDamage(amount, source);
        if (!alive) {
            dying = true;
            deathTimer = 0.8;
            alive = true; // keep alive during death anim
            return;
        }
        hurtAnimTimer = 0.35;
        invulnTimer = GameConfig.PLAYER_INVULN_SECONDS;
    }

    public void setInvulnTimer(double t) {
        invulnTimer = t;
    }

    public double getInvulnTimer() {
        return invulnTimer;
    }

    @Override
    public void render(GameEngine g) {
        if (!alive) return;
        int anim;
        if (dying) anim = SpriteManager.ANIM_DEATH;
        else if (hurtAnimTimer > 0) anim = SpriteManager.ANIM_HURT;
        else if (attackAnimTimer > 0) anim = SpriteManager.ANIM_ATTACK;
        else if (Math.abs(vx) > 1 || Math.abs(vy) > 1) anim = SpriteManager.ANIM_WALK;
        else anim = SpriteManager.ANIM_IDLE;
        int dir = getFacingDirection();

        // Ultimate active glow
        if (ultimate.active) {
            Color glow = (tier >= 4) ? new Color(255, 220, 40, 60 + (int)(Math.sin(animTimer * 8) * 40))
                                     : new Color(200, 200, 255, 50 + (int)(Math.sin(animTimer * 6) * 30));
            g.changeColor(glow);
            g.drawSolidCircle(x, y, radius * 1.3);
        }

        // Tier aura
        if (tier >= 3) {
            Color aura = (tier >= 4) ? new Color(255, 200, 40, 25 + (int)(Math.sin(animTimer * 3) * 15))
                                     : new Color(180, 150, 255, 20 + (int)(Math.sin(animTimer * 3) * 10));
            g.changeColor(aura);
            g.drawSolidCircle(x, y, radius * 1.1);
        }

        if (character.getId() == CharacterDef.WARRIOR) {
            SpriteManager.drawSwordsman(tier, anim, dir, animTimer, x, y);
        } else {
            Color c = (hitFlashTimer > 0 && hitFlashTimer % 2 == 0) ? Color.WHITE : baseColor;
            SpriteManager.drawCharacter(getAssetKey(), anim, dir, animTimer, x, y, radius, c);
        }
    }

    public void triggerAttackAnim() {
        attackAnimTimer = 0.35;
    }

    private String getAssetKey() {
        int id = character.getId();
        if (id == CharacterDef.MAGE) return AssetLibrary.CHAR_MAGE;
        return AssetLibrary.CHAR_WARRIOR;
    }

    private int getFacingDirection() {
        double a = facingAngleRad;
        if (a >= -Math.PI / 4 && a < Math.PI / 4) return SpriteManager.DIR_RIGHT;
        if (a >= Math.PI / 4 && a < 3 * Math.PI / 4) return SpriteManager.DIR_DOWN;
        if (a >= -3 * Math.PI / 4 && a < -Math.PI / 4) return SpriteManager.DIR_UP;
        return SpriteManager.DIR_LEFT;
    }

    public void clampToWorld() {
        x = Math.max(radius, Math.min(GameConfig.WORLD_WIDTH - radius, x));
        y = Math.max(radius, Math.min(GameConfig.WORLD_HEIGHT - radius, y));
    }

    public CharacterDef getCharacter() {
        return character;
    }

    public void boostMoveSpeed(double multiplier) {
        moveSpeed *= multiplier;
    }
}
