import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class SurvivalGame extends GameEngine {
    private static final String FONT_TITLE = "Serif";
    private static final String FONT_BODY = "Dialog";
    private static final String FONT_NUM = "Georgia";
    public static final int STATE_MENU = 0;
    public static final int STATE_CHAR_SELECT = 1;
    public static final int STATE_PLAYING = 2;
    public static final int STATE_UPGRADE_PAUSE = 3;
    public static final int STATE_BOSS_INTRO = 4;
    public static final int STATE_VICTORY = 5;
    public static final int STATE_DEFEAT = 6;
    public static final int STATE_WEAPON_SWAP = 7;
    public static final int STATE_TIER_UP = 8;
    public static final int STATE_INTRO = 9;

    int gameState = STATE_MENU;
    double introTimer;
    int introPhase;
    double introScrollOffset;
    int swapPendingWeaponId;
    WeaponRarity swapPendingRarity;
    double tierUpTimer;
    int pendingTier;

    final boolean[] keys = new boolean[512];
    int mouseX;
    int mouseY;
    int menuHoverButton = -1;
    int charHoverIndex = -1;
    final double[] charCardLift = new double[] {0, 0, 0};
    boolean menuSettingsOpen;
    boolean screenShakeEnabled = true;
    int combatPaceLevel = 1; // 0: relaxed, 1: normal, 2: intense

    public Player player;
    public final ArrayList<Enemy> enemies = new ArrayList<Enemy>();
    public final ArrayList<Projectile> projectiles = new ArrayList<Projectile>();
    public final ArrayList<Pickup> pickups = new ArrayList<Pickup>();

    public VfxManager vfx;
    public Camera camera;
    public GameMap gameMap;
    public EnemySpawner spawner;
    public CollisionSystem collisionSystem;
    public WeaponManager weaponManager;
    public UpgradeSystem upgradeSystem;

    private CharacterDef[] characters;
    private double runTimeSeconds;
    private double bossIntroTimer;
    private double playerAuraTrailTimer;
    private int levelMilestoneNotice = -1;
    private double levelMilestoneNoticeTimer;

    private int killCount;
    private int totalKillScore;
    private boolean bossKilled;
    private boolean fullHealthBonusEarned;
    private int highestLevelReached;

    public static void main(String[] args) {
        createGame(new SurvivalGame(), 60);
    }

    @Override
    public void init() {
        while (mFrame == null) {
            sleep(10);
        }
        setWindowSize(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);
        mFrame.setTitle("Survival Game");
        characters = CharacterDef.all();

        SpriteManager.init(this);
        vfx = new VfxManager(this);
        camera = new Camera(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT,
            GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);
        gameMap = new GameMap(this);
        spawner = new EnemySpawner(this);
        collisionSystem = new CollisionSystem(this);
        weaponManager = new WeaponManager(this);
        upgradeSystem = new UpgradeSystem(this);
        introTimer = 0;
        introPhase = 0;
        introScrollOffset = 0;
    }

    @Override
    public void update(double dt) {
        if (levelMilestoneNoticeTimer > 0) {
            levelMilestoneNoticeTimer -= dt;
            if (levelMilestoneNoticeTimer <= 0) {
                levelMilestoneNoticeTimer = 0;
                levelMilestoneNotice = -1;
            }
        }

        if (gameState == STATE_PLAYING) {
            updatePlaying(dt);
        } else if (gameState == STATE_UPGRADE_PAUSE) {
            vfx.update(dt);
            updateUpgradeHover();
        } else if (gameState == STATE_BOSS_INTRO) {
            bossIntroTimer -= dt;
            vfx.update(dt);
            if (bossIntroTimer <= 0) {
                gameState = STATE_PLAYING;
            }
        } else if (gameState == STATE_TIER_UP) {
            tierUpTimer -= dt;
            vfx.update(dt);
            if (player != null && player.ultimate != null) {
                player.ultimate.update(dt);
            }
            if (tierUpTimer <= 0) {
                gameState = STATE_UPGRADE_PAUSE;
            }
        } else if (gameState == STATE_WEAPON_SWAP) {
            vfx.update(dt);
        } else if (gameState == STATE_INTRO) {
            updateIntro(dt);
            vfx.update(dt);
        } else if (gameState == STATE_MENU || gameState == STATE_CHAR_SELECT
                || gameState == STATE_VICTORY || gameState == STATE_DEFEAT) {
            vfx.update(dt);
            if (gameState == STATE_CHAR_SELECT) {
                updateCharCardAnimation(dt);
            }
        }
    }

    private void updatePlaying(double dt) {
        if (player == null || !player.alive) {
            gameState = STATE_DEFEAT;
            return;
        }

        runTimeSeconds += dt;
        player.update(dt);
        updatePlayerAuraVfx(dt);
        player.clampToWorld();
        camera.follow(player.x, player.y, dt);

        if (upgradeSystem.level() > highestLevelReached) {
            highestLevelReached = upgradeSystem.level();
        }

        spawner.update(dt * combatPaceMultiplier());
        weaponManager.update(dt);

        for (Enemy e : enemies) e.update(dt);
        for (Projectile p : projectiles) p.update(dt);
        for (Pickup p : pickups) p.update(dt);

        collisionSystem.update(dt);
        cleanupDead();
        vfx.update(dt);

        if (!player.alive) {
            gameState = STATE_DEFEAT;
        }
    }

    private void cleanupDead() {
        for (int i = enemies.size() - 1; i >= 0; i--) {
            if (!enemies.get(i).alive) enemies.remove(i);
        }
        for (int i = projectiles.size() - 1; i >= 0; i--) {
            if (!projectiles.get(i).alive) projectiles.remove(i);
        }
        for (int i = pickups.size() - 1; i >= 0; i--) {
            if (!pickups.get(i).alive) pickups.remove(i);
        }
    }

    @Override
    public void paintComponent() {
        changeBackgroundColor(GameConfig.BG_DARK);
        clearBackground(width(), height());

        if (gameState == STATE_INTRO) {
            renderIntro();
        } else if (gameState == STATE_MENU) {
            renderMenu();
        } else if (gameState == STATE_CHAR_SELECT) {
            renderCharSelect();
        } else if (gameState == STATE_PLAYING || gameState == STATE_UPGRADE_PAUSE
                || gameState == STATE_BOSS_INTRO || gameState == STATE_WEAPON_SWAP
                || gameState == STATE_TIER_UP) {
            renderWorld();
            if (gameState == STATE_UPGRADE_PAUSE) {
                renderUpgradeOverlay();
            } else if (gameState == STATE_BOSS_INTRO) {
                renderBossIntro();
            } else if (gameState == STATE_WEAPON_SWAP) {
                renderWeaponSwapOverlay();
            } else if (gameState == STATE_TIER_UP) {
                renderTierUpOverlay();
            }
        } else if (gameState == STATE_VICTORY) {
            renderEndScreen(true);
        } else if (gameState == STATE_DEFEAT) {
            renderEndScreen(false);
        }
    }

    private void renderWorld() {
        vfx.beginCamera(this, camera);

        changeColor(GameConfig.WORLD_GROUND);
        drawSolidRectangle(0, 0, GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT);
        changeColor(GameConfig.WORLD_BORDER);
        drawRectangle(0, 0, GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT, 4);

        if (gameMap != null) {
            gameMap.render(this, camera);
        }

        for (Pickup p : pickups) {
            if (camera.isVisible(p.x, p.y, 30)) p.render(this);
        }
        for (Projectile p : projectiles) {
            if (camera.isVisible(p.x, p.y, 30)) p.render(this);
        }
        for (Enemy e : enemies) {
            if (camera.isVisible(e.x, e.y, 60)) e.render(this);
        }
        if (player != null) player.render(this);
        vfx.render(this);
        vfx.endCamera(this);

        renderHUD();
    }

    private void renderHUD() {
        if (player == null) return;

        double hpPct = player.health / player.maxHealth;
        changeColor(new Color(16, 14, 20, 190));
        drawSolidRectangle(12, 10, 436, 156);
        changeColor(new Color(112, 95, 68));
        drawRectangle(12, 10, 436, 156, 2.5);

        drawEmbossedText(24, 30, "ADVENTURER STATUS", 16, new Color(220, 205, 165), new Color(68, 56, 38));

        drawFramedBar(24, 38, 260, 18, hpPct, new Color(220, 80, 80), "HP " + (int)Math.ceil(player.health) + " / " + (int)Math.ceil(player.maxHealth));

        double xpPct = upgradeSystem.xp() / upgradeSystem.xpForNextLevel();
        drawFramedBar(24, 64, 260, 14, xpPct, new Color(90, 180, 255), "XP " + (int)upgradeSystem.xp() + " / " + (int)upgradeSystem.xpForNextLevel());

        String tierLabel = "";
        Color tierColor = Color.WHITE;
        if (player.tier >= 4) { tierLabel = " T4"; tierColor = new Color(255, 200, 40); }
        else if (player.tier >= 3) { tierLabel = " T3"; tierColor = new Color(180, 80, 255); }
        else if (player.tier >= 2) { tierLabel = " T2"; tierColor = new Color(80, 180, 255); }
        changeColor(tierColor);
        drawText(300, 49, "Lv " + upgradeSystem.level() + tierLabel, "Arial", 16);
        changeColor(Color.WHITE);
        drawText(300, 72, "Time " + formatTime(runTimeSeconds), "Arial", 14);

        // Ultimate cooldown
        if (player.tier >= 3 && player.ultimate != null) {
            double uPct = player.ultimate.cooldownPct();
            int ux = 300;
            int uy = 82;
            changeColor(new Color(27, 25, 35));
            drawSolidRectangle(ux, uy - 2, 134, 34);
            changeColor(new Color(99, 82, 132));
            drawRectangle(ux, uy - 2, 134, 34, 2);
            drawText(ux + 8, uy + 11, "ULTIMATE", "Arial", 11);

            changeColor(new Color(35, 35, 55));
            drawSolidRectangle(ux + 8, uy + 16, 118, 8);
            if (uPct >= 1.0) {
                changeColor(new Color(255, 200, 40));
                drawSolidRectangle(ux + 8, uy + 16, 118 * uPct, 8);
                drawText(ux + 82, uy + 11, "READY", "Arial", 10);
            } else {
                changeColor(new Color(120, 120, 200));
                drawSolidRectangle(ux + 8, uy + 16, 118 * uPct, 8);
                drawText(ux + 70, uy + 11, (int)(player.ultimate.cooldownPct() * 100) + "%", "Arial", 10);
            }
            changeColor(Color.WHITE);
            drawRectangle(ux + 8, uy + 16, 118, 8, 1);
        }

        renderLevelMilestoneTrack(300, 122);

        int wx = 22;
        int wy = 102;
        ArrayList<WeaponInstance> weapons = weaponManager.list();
        for (int i = 0; i < weapons.size(); i++) {
            WeaponInstance w = weapons.get(i);
            int col = i % 3;
            int row = i / 3;
            int cx = wx + col * 140;
            int cy = wy + row * 28;
            changeColor(new Color(24, 24, 34, 230));
            drawSolidRectangle(cx, cy, 132, 24);
            Color accent = w.rarity != WeaponRarity.COMMON ? w.rarity.beamColor : WeaponDef.ALL[w.weaponId].projColor;
            changeColor(accent);
            drawSolidRectangle(cx + 2, cy + 2, 4, 20);
            drawRectangle(cx, cy, 132, 24, 1.5);

            changeColor(accent);
            String label = shortWeaponName(WeaponDef.ALL[w.weaponId].name) + " Lv" + w.level;
            if (w.rarity != WeaponRarity.COMMON) {
                label += " " + w.rarity.name().charAt(0);
            }
            drawText(cx + 10, cy + 16, label, "Arial", 11);
        }

        Enemy boss = findBoss();
        if (boss != null) {
            double pct = boss.health / boss.maxHealth;
            changeColor(new Color(40, 20, 20));
            drawSolidRectangle(220, 14, 520, 16);
            changeColor(new Color(220, 45, 45));
            drawSolidRectangle(220, 14, 520 * pct, 16);
            changeColor(Color.WHITE);
            drawRectangle(220, 14, 520, 16, 2);
            drawEmbossedText(445, 31, "BOSS", 13, new Color(255, 236, 236), new Color(75, 20, 20));
        }

        if (levelMilestoneNoticeTimer > 0 && levelMilestoneNotice > 0 && gameState != STATE_TIER_UP) {
            renderLevelMilestoneBanner(levelMilestoneNotice, levelMilestoneNoticeTimer / 2.2);
        }

        int sbx = 728;
        int sby = 570;
        int sbw = 218;
        int sbh = 82;
        changeColor(new Color(16, 14, 20, 190));
        drawSolidRectangle(sbx, sby, sbw, sbh);
        changeColor(new Color(112, 95, 68));
        drawRectangle(sbx, sby, sbw, sbh, 2);
        drawEmbossedText(sbx + 8, sby + 14, "SCORE", 14, new Color(220, 205, 165), new Color(68, 56, 38));
        int currentScore = totalKillScore + (int)Math.floor(runTimeSeconds * GameConfig.SCORE_SURVIVAL_PER_SECOND);
        changeColor(new Color(255, 220, 80));
        drawBoldText(sbx + 8, sby + 38, String.valueOf(currentScore), "Georgia", 26);
        changeColor(new Color(180, 170, 150));
        drawText(sbx + 8, sby + 58, "Kills: " + killCount, "Arial", 12);
        drawText(sbx + 108, sby + 58, "Time: " + formatTime(runTimeSeconds), "Arial", 12);
    }

    private void renderMenu() {
        drawMenuBackdrop();
        double pulse = 1.0 + Math.sin(uiTime() * 2.2) * 0.02;
        int titleY = 138 + (int)(Math.sin(uiTime() * 2.0) * 3);

        drawEmbossedText(256, titleY, "CHRONICLES OF DUSK", 46, new Color(235, 220, 170), new Color(66, 50, 33));
        changeColor(new Color(188, 165, 112));
        drawText(337, 178, "A PIXEL MEDIEVAL SURVIVAL CHRONICLE", "Arial", 15);

        drawMenuButton(320, 236, 320, 62, "START ADVENTURE", menuHoverButton == 0, new Color(130, 86, 42));
        drawMenuButton(320, 316, 320, 62, "SETTINGS", menuHoverButton == 1, new Color(86, 96, 128));
        drawMenuButton(320, 396, 320, 62, "EXIT KINGDOM", menuHoverButton == 2, new Color(126, 64, 64));

        changeColor(new Color(214, 201, 165));
        drawText(338, 500, "KEYS: [1/ENTER] START   [S] SETTINGS   [ESC] EXIT", "Arial", 14);
        if (!menuSettingsOpen) {
            changeColor(new Color(186, 172, 133, 120));
            drawSolidRectangle(320, 236, 320 * pulse, 4);
        }



        
        if (menuSettingsOpen) {
            renderSettingsPanel();
        }
    }

    private void renderCharSelect() {
        drawMenuBackdrop();
        int panelX = 48;
        int panelY = 54;
        int panelW = width() - 96;
        int panelH = height() - 94;
        int cardW = 250;
        int cardH = 308;
        int gap = (panelW - cardW * 3) / 4;
        if (gap < 24) gap = 24;
        int cardY = panelY + 142;

        changeColor(new Color(28, 24, 30, 170));
        drawSolidRectangle(panelX, panelY, panelW, panelH);
        changeColor(new Color(122, 104, 76));
        drawRectangle(panelX, panelY, panelW, panelH, 3);

        int floatY = (int)(Math.sin(uiTime() * 1.7) * 3);
        String title = "CHOOSE YOUR CHAMPION";
        int titleSize = 42;
        int titleX = panelX + (panelW - estimateTextWidth(title, titleSize, true)) / 2;
        drawEmbossedText(titleX, panelY + 50 + floatY, title, titleSize, new Color(230, 216, 175), new Color(70, 54, 34));
        changeColor(new Color(182, 168, 128));
        String subtitle = "Each vocation grants a unique path to victory";
        int subtitleX = panelX + (panelW - estimateTextWidth(subtitle, 16, false)) / 2;
        drawText(subtitleX, panelY + 86, subtitle, "Arial", 16);

        for (int i = 0; i < characters.length; i++) {
            CharacterDef def = characters[i];
            int x = panelX + gap + i * (cardW + gap);
            boolean hover = charHoverIndex == i;
            int y = cardY - (int)Math.round(charCardLift[i]);

            Color accent = classAccent(def.getId());
            changeColor(hover ? new Color(54, 49, 61) : new Color(36, 32, 43));
            drawSolidRectangle(x, y, cardW, cardH);
            changeColor(new Color(84, 73, 56));
            drawRectangle(x, y, cardW, cardH, hover ? 4 : 2);
            changeColor(accent);
            drawRectangle(x + 6, y + 6, cardW - 12, 84, 2);

            changeColor(new Color(24, 21, 28));
            drawSolidRectangle(x + 18, y + 104, cardW - 36, 110);
            changeColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 120));
            drawSolidRectangle(x + 18, y + 104, cardW - 36, 18);

            // Portrait medallion
            changeColor(new Color(24, 22, 18));
            drawSolidCircle(x + 62, y + 52, 30);
            changeColor(accent);
            drawSolidCircle(x + 62, y + 52, 24);
            drawClassGlyph(def.getId(), x + 62, y + 52);

            drawEmbossedText(x + 100, y + 44, def.name.toUpperCase(), 22, new Color(236, 228, 198), new Color(70, 62, 52));
            changeColor(new Color(200, 183, 140));
            drawText(x + 100, y + 66, classTitle(def.getId()).toUpperCase(), "Arial", 12);

            changeColor(new Color(216, 206, 182));
            drawStatGlyph(x + 27, y + 136, 0);
            drawBoldText(x + 28, y + 140, "HP", "Arial", 13);
            drawText(x + 68, y + 140, "x" + def.hpMultiplier, "Arial", 13);
            drawStatGlyph(x + 27, y + 158, 1);
            drawBoldText(x + 28, y + 162, "SPD", "Arial", 13);
            drawText(x + 68, y + 162, "x" + def.speedMultiplier, "Arial", 13);
            drawStatGlyph(x + 27, y + 180, 2);
            drawBoldText(x + 28, y + 184, "DMG", "Arial", 13);
            drawText(x + 68, y + 184, "x" + def.damageMultiplier, "Arial", 13);

            changeColor(new Color(187, 177, 153));
            drawText(x + 28, y + 228, classTagline(def.getId()), "Arial", 12);
            drawText(x + 28, y + 248, classHint(def.getId()), "Arial", 12);

            drawMenuButton(x + 28, y + 266, cardW - 56, 32, "SELECT [" + (i + 1) + "]", hover, accent);
        }

        changeColor(new Color(205, 190, 150));
        drawText(panelX + panelW / 2 - 245, panelY + panelH - 24, "[ESC] BACK TO MAIN MENU   |   CLICK A CARD TO START", "Arial", 15);
    }

    private void renderUpgradeOverlay() {
        changeColor(new Color(0, 0, 0, 180));
        drawSolidRectangle(70, 92, 820, 490);
        changeColor(new Color(113, 94, 66));
        drawRectangle(70, 92, 820, 490, 3);
        drawEmbossedText(336, 150, "LEVEL UP!", 46, FONT_TITLE, new Color(242, 222, 170), new Color(72, 55, 34));
        changeColor(new Color(198, 183, 150));
        drawText(248, 210, "Select one blessing to shape your build", FONT_BODY, 17);

        UpgradeDef[] cards = upgradeSystem.offered();
        for (int i = 0; i < cards.length; i++) {
            int x = 116 + i * 258;
            int y = 244;
            int w = 228;
            int h = 270;
            boolean hover = upgradeSystem.hoveredCard() == i;
            Color card = hover ? new Color(76, 90, 118) : new Color(45, 56, 82);
            changeColor(card);
            drawSolidRectangle(x, y, w, h);
            Color accent = hover ? new Color(212, 190, 132) : new Color(118, 129, 162);
            changeColor(accent);
            drawRectangle(x, y, w, h, hover ? 4 : 2);
            changeColor(new Color(255, 255, 255, hover ? 40 : 18));
            drawSolidRectangle(x + 4, y + 4, w - 8, 10);
            if (cards[i] != null) {
                drawEmbossedText(x + 12, y + 50, "CHOICE " + (i + 1), 12, FONT_BODY, new Color(229, 216, 182), new Color(58, 44, 29));
                int titleBottomY = drawAdaptiveUpgradeTitle(x + 12, y + 86, w - 24, cards[i].title.toUpperCase());
                changeColor(new Color(214, 207, 185));
                int descStartY = titleBottomY + 16;
                int buttonY = y + 226;
                int descLines = Math.max(3, (buttonY - descStartY - 8) / 14);
                drawWrappedText(x + 12, descStartY, cards[i].description, 28, 14, descLines, FONT_BODY);
                drawMenuButton(x + 14, y + 226, w - 28, 32, "[ " + (i + 1) + " ] SELECT", hover, new Color(120, 150, 176));
            }
        }
    }

    private void renderWeaponSwapOverlay() {
        changeColor(new Color(0, 0, 0, 170));
        drawSolidRectangle(100, 140, 760, 440);
        changeColor(Color.WHITE);
        drawBoldText(280, 200, "WEAPON CHEST FOUND!", "Arial", 32);

        WeaponDef newDef = WeaponDef.ALL[swapPendingWeaponId];
        String rarityLabel = swapPendingRarity.name();
        changeColor(swapPendingRarity.beamColor);
        drawBoldText(300, 240, newDef.name + "  [" + rarityLabel + "]", "Arial", 22);
        drawText(280, 270, "Choose a slot to replace (or ESC to discard)", "Arial", 16);

        ArrayList<WeaponInstance> list = weaponManager.list();
        for (int i = 0; i < list.size(); i++) {
            int x = 130 + i * 120;
            int y = 310;
            boolean hover = mouseX >= x && mouseX <= x + 100 && mouseY >= y && mouseY <= y + 120;
            changeColor(hover ? new Color(85, 105, 140) : new Color(45, 60, 90));
            drawSolidRectangle(x, y, 100, 120);
            changeColor(Color.WHITE);
            drawRectangle(x, y, 100, 120, hover ? 4 : 2);
            WeaponInstance w = list.get(i);
            changeColor(WeaponDef.ALL[w.weaponId].projColor);
            drawBoldText(x + 8, y + 30, WeaponDef.ALL[w.weaponId].name, "Arial", 14);
            changeColor(Color.WHITE);
            drawText(x + 8, y + 55, "Lv " + w.level, "Arial", 12);
            if (w.rarity != WeaponRarity.COMMON) {
                changeColor(w.rarity.beamColor);
                drawText(x + 8, y + 80, w.rarity.name(), "Arial", 10);
            }
            drawBoldText(x + 8, y + 100, (i + 1) + "", "Arial", 16);
        }
    }

    private void renderTierUpOverlay() {
        int x = 140;
        int y = 188;
        int w = 680;
        int h = 286;

        changeColor(new Color(0, 0, 0, 178));
        drawSolidRectangle(0, 0, width(), height());

        changeColor(new Color(14, 12, 19, 226));
        drawSolidRectangle(x, y, w, h);
        Color tierColor = tierThemeColor();
        changeColor(new Color(126, 108, 76));
        drawRectangle(x, y, w, h, 3);
        changeColor(new Color(tierColor.getRed(), tierColor.getGreen(), tierColor.getBlue(), 155));
        drawRectangle(x + 6, y + 6, w - 12, 84, 2);

        drawCenteredEmbossedText(x, y + 54, w, "TIER UP!", 52, FONT_TITLE, tierColor, new Color(64, 45, 20));
        drawCenteredEmbossedText(x, y + 102, w, tierDisplayName(), 36, FONT_TITLE, new Color(242, 236, 218), new Color(62, 56, 42));
        changeColor(new Color(198, 187, 154));
        drawCenteredText(x, y + 134, w, "Your class ascends to a new combat discipline", FONT_BODY, 16);

        int oldTier = Math.max(1, pendingTier - 1);
        drawTierStatCard(x + 34, y + 160, 190, 102, "HP", statGainPct(CharacterProgression.hpMultiplier(oldTier), CharacterProgression.hpMultiplier(pendingTier)), new Color(228, 112, 112));
        drawTierStatCard(x + 246, y + 160, 190, 102, "DMG", statGainPct(CharacterProgression.damageMultiplier(oldTier), CharacterProgression.damageMultiplier(pendingTier)), new Color(238, 184, 112));
        drawTierStatCard(x + 458, y + 160, 190, 102, "SPD", statGainPct(CharacterProgression.speedMultiplier(oldTier), CharacterProgression.speedMultiplier(pendingTier)), new Color(126, 198, 255));

        changeColor(new Color(225, 214, 182));
        drawCenteredText(x, y + 282, w, pendingTier >= 3
            ? "Ultimate skill unlocked! Press SPACE to cast."
            : "Core stats improved. Keep pushing your build.", FONT_BODY, 16);
    }

    private void renderBossIntro() {
        changeColor(new Color(0, 0, 0, 160));
        drawSolidRectangle(220, 270, 520, 120);
        changeColor(new Color(255, 80, 80));
        drawBoldText(370, 345, "BOSS APPROACHING", "Arial", 34);
    }

    private void renderEndScreen(boolean victory) {
        changeColor(victory ? new Color(100, 230, 120) : new Color(230, 100, 100));
        drawBoldText(380, 220, victory ? "VICTORY" : "DEFEAT", "Arial", 64);

        int survivalScore = (int)Math.floor(runTimeSeconds * GameConfig.SCORE_SURVIVAL_PER_SECOND);
        int bossBonus = bossKilled ? GameConfig.SCORE_BOSS_KILL : 0;
        int fullHpBonus = fullHealthBonusEarned ? GameConfig.SCORE_FULL_HP_BONUS : 0;
        int finalScore = calculateFinalScore();

        changeColor(new Color(255, 220, 80));
        drawBoldText(320, 280, "TOTAL SCORE: " + finalScore, "Arial", 32);

        int y = 318;
        changeColor(new Color(200, 200, 200));
        drawText(280, y, "Kills: " + killCount + " enemies  —  " + totalKillScore + " pts", "Arial", 16);
        y += 22;
        drawText(280, y, "Survival Time: " + formatTime(runTimeSeconds) + "  —  +" + survivalScore + " pts", "Arial", 16);
        y += 22;
        if (bossKilled) {
            changeColor(new Color(255, 180, 80));
            drawText(280, y, "Boss Kill  —  +" + bossBonus + " pts", "Arial", 16);
            y += 22;
        }
        if (fullHealthBonusEarned) {
            changeColor(new Color(120, 255, 120));
            drawText(280, y, "Full HP Completion  —  +" + fullHpBonus + " pts", "Arial", 16);
            y += 22;
        }
        double milestoneMult = 1.0;
        if (highestLevelReached >= 75) milestoneMult = GameConfig.SCORE_MILESTONE_LV75;
        else if (highestLevelReached >= 50) milestoneMult = GameConfig.SCORE_MILESTONE_LV50;
        else if (highestLevelReached >= 25) milestoneMult = GameConfig.SCORE_MILESTONE_LV25;
        if (milestoneMult > 1.0) {
            changeColor(new Color(180, 160, 255));
            drawText(280, y, "Level Milestone x" + String.format("%.1f", milestoneMult) + "  (reached Lv " + highestLevelReached + ")", "Arial", 16);
            y += 22;
        }

        changeColor(Color.WHITE);
        drawText(300, y + 16, "Press SPACE to restart", "Arial", 28);
    }

    private String formatTime(double sec) {
        int total = (int)Math.floor(sec);
        int mm = total / 60;
        int ss = total % 60;
        return String.format("%02d:%02d", mm, ss);
    }

    private void startIntro() {
        introTimer = 0;
        introPhase = 0;
        introScrollOffset = height();
        gameState = STATE_INTRO;
    }

    private void updateIntro(double dt) {
        introTimer += dt;
        double scrollSpeed = 38;
        introScrollOffset -= scrollSpeed * dt;

        if (introTimer > 1.5 && introPhase == 0) {
            introPhase = 1;
        }
        if (introTimer > 7.0 && introPhase == 1) {
            introPhase = 2;
        }
        if (introTimer > 14.5 && introPhase == 2) {
            introPhase = 3;
        }
        if (introTimer > 22.0) {
            gameState = STATE_CHAR_SELECT;
        }
    }

    private void renderIntro() {
        int W = width();
        int H = height();
        double t = introTimer;

        for (int i = 0; i < H; i++) {
            double ratio = (double)i / H;
            int r = (int)(8 + ratio * 18);
            int g = (int)(6 + ratio * 20);
            int b = (int)(16 + ratio * 32);
            changeColor(r, g, b);
            drawSolidRectangle(0, i, W, 1);
        }

        int[] starSeeds = new int[] {17, 42, 73, 101, 138, 179, 211, 259, 301, 344, 389, 423, 467, 512, 556, 601, 47, 88, 124, 166, 198, 243, 288, 327, 371, 415, 458, 503, 548, 586};
        for (int i = 0; i < starSeeds.length; i++) {
            int sx = (starSeeds[i] * 37 + 131) % W;
            int sy = (starSeeds[i] * 53 + 271) % (H / 2);
            double flicker = 0.5 + 0.5 * Math.sin(t * 3.0 + starSeeds[i] * 1.7);
            int alpha = (int)(130 + flicker * 110);
            changeColor(new Color(220, 225, 255, alpha));
            int size = (i % 5 == 0) ? 2 : 1;
            drawSolidRectangle(sx, sy, size, size);
        }

        int moonX = W - 130;
        int moonY = 75;
        changeColor(new Color(235, 228, 200, 210));
        drawSolidCircle(moonX, moonY, 36);
        changeColor(new Color(16, 14, 26));
        drawSolidCircle(moonX + 10, moonY - 4, 31);

        changeColor(new Color(18, 14, 24, 220));
        int[] mtnX = new int[] {0, 60, 140, 200, 280, 370, 440, 520, 620, 710, 790, 880, 960};
        int[] mtnH = new int[] {90, 130, 85, 150, 100, 170, 110, 140, 95, 165, 105, 145, 90};
        for (int i = 0; i < mtnX.length - 2; i++) {
            changeColor(new Color(18, 14, 24, 220));
            int cx = (mtnX[i] + mtnX[i + 1]) / 2;
            int peakY = H - 80 - mtnH[i];
            int[] px = new int[] {mtnX[i], cx, mtnX[i + 1]};
            int[] py = new int[] {H - 70, peakY, H - 70};
            fillTriangle(px[0], py[0], px[1], py[1], px[2], py[2]);
        }

        changeColor(new Color(22, 16, 28));
        drawSolidRectangle(0, H - 68, W, 68);

        int cxBase = W / 2;
        int cyBase = H - 68;
        Color castleColor = new Color(24, 18, 30);
        changeColor(castleColor);

        drawSolidRectangle(cxBase - 50, cyBase - 140, 100, 140);
        drawSolidRectangle(cxBase - 95, cyBase - 110, 40, 110);
        drawSolidRectangle(cxBase + 55, cyBase - 110, 40, 110);
        drawSolidRectangle(cxBase - 145, cyBase - 75, 30, 75);
        drawSolidRectangle(cxBase + 115, cyBase - 75, 30, 75);

        for (int i = 0; i < 5; i++) {
            drawSolidRectangle(cxBase - 46 + i * 19, cyBase - 152, 12, 14);
        }
        for (int i = 0; i < 3; i++) {
            drawSolidRectangle(cxBase - 93 + i * 14, cyBase - 122, 9, 12);
        }
        for (int i = 0; i < 3; i++) {
            drawSolidRectangle(cxBase + 57 + i * 14, cyBase - 122, 9, 12);
        }
        for (int i = 0; i < 2; i++) {
            drawSolidRectangle(cxBase - 143 + i * 16, cyBase - 87, 8, 12);
            drawSolidRectangle(cxBase + 117 + i * 16, cyBase - 87, 8, 12);
        }

        changeColor(new Color(34, 24, 20));
        drawSolidRectangle(cxBase - 16, cyBase - 52, 32, 52);
        int gateArchY = cyBase - 52;
        drawSolidRectangle(cxBase - 16, gateArchY, 32, 10);
        changeColor(new Color(56, 42, 20));
        drawSolidRectangle(cxBase - 16, gateArchY, 32, 2);

        Color windowGlow = new Color(255, 200, 100, 180);
        changeColor(windowGlow);
        drawSolidRectangle(cxBase - 28, cyBase - 90, 10, 14);
        drawSolidRectangle(cxBase + 18, cyBase - 90, 10, 14);
        drawSolidRectangle(cxBase - 6, cyBase - 110, 10, 14);
        drawSolidRectangle(cxBase - 82, cyBase - 70, 8, 12);
        drawSolidRectangle(cxBase + 74, cyBase - 70, 8, 12);

        int torchLX = cxBase - 24;
        int torchLY = cyBase - 58;
        changeColor(new Color(46, 36, 22));
        drawSolidRectangle(torchLX - 1, torchLY, 3, 10);
        double flickerA = Math.sin(t * 12.0) * 0.2 + Math.sin(t * 17.3) * 0.15;
        int flameAlpha = (int)(180 + flickerA * 60);
        changeColor(new Color(255, 160, 40, flameAlpha));
        drawSolidCircle(torchLX, torchLY - 6, 5);
        changeColor(new Color(255, 210, 80, 140));
        drawSolidCircle(torchLX, torchLY - 5, 3);

        int torchRX = cxBase + 24;
        int torchRY = cyBase - 58;
        changeColor(new Color(46, 36, 22));
        drawSolidRectangle(torchRX - 1, torchRY, 3, 10);
        double flickerB = Math.sin(t * 11.3 + 1.7) * 0.2 + Math.sin(t * 19.1) * 0.15;
        int flameAlphaB = (int)(180 + flickerB * 60);
        changeColor(new Color(255, 160, 40, flameAlphaB));
        drawSolidCircle(torchRX, torchRY - 6, 5);
        changeColor(new Color(255, 210, 80, 140));
        drawSolidCircle(torchRX, torchRY - 5, 3);

        int swordX = 110;
        int swordY = cyBase;
        changeColor(new Color(80, 78, 90));
        drawSolidRectangle(swordX - 3, swordY + 10, 6, 24);
        changeColor(new Color(180, 185, 200));
        drawSolidRectangle(swordX - 2, swordY - 34, 4, 48);
        changeColor(new Color(210, 215, 230));
        drawSolidRectangle(swordX - 1, swordY - 34, 2, 46);
        changeColor(new Color(160, 140, 90));
        drawSolidRectangle(swordX - 12, swordY + 8, 24, 5);
        changeColor(new Color(180, 155, 90));
        drawSolidCircle(swordX, swordY + 7, 5);
        double bladeGlow = 0.55 + 0.45 * Math.sin(t * 2.4);
        changeColor(new Color(160, 200, 255, (int)(80 * bladeGlow)));
        drawSolidRectangle(swordX - 1, swordY - 30, 2, 38);

        for (int i = 0; i < 6; i++) {
            double ex = torchLX - 6 + (i * 17 + 37) % 16;
            double ey = torchLY - 8 - (t * 30 + i * 53) % 60;
            double efade = 1.0 - ((t * 30 + i * 53) % 60) / 60.0;
            if (efade > 0) {
                changeColor(new Color(255, 150, 40, (int)(140 * efade)));
                drawSolidRectangle((int)ex, (int)ey, 2, 2);
            }
        }

        int textCenterX = W / 2;
        int textY = H / 2 + 30;

        if (introPhase == 0) {
            double fadeIn = Math.min(1.0, introTimer / 1.2);
            int alpha = (int)(fadeIn * 240);
            drawCenteredEmbossedText(0, textY - 20, W, "CHRONICLES OF DUSK", 44, FONT_TITLE,
                new Color(235, 220, 170, alpha), new Color(66, 50, 33, alpha));
            changeColor(new Color(188, 165, 112, alpha));
            drawCenteredText(0, textY + 28, W, "A tale of darkness, courage, and redemption", FONT_BODY, 15);
        } else if (introPhase == 1) {
            double scrollY = introScrollOffset;
            String[] lines = new String[] {
                "Long ago, in the war-torn lands of Dusk...",
                "",
                "The Kingdom of Aethelgard stood as a beacon",
                "of hope and prosperity for all who dwelt",
                "within its ancient walls.",
                "",
                "But peace is fragile, and shadows grow",
                "in the hearts of the envious...",
                "",
                "From the Abyssal Rift, a tide of darkness",
                "poured forth — orcs, wraiths, and things",
                "far older than memory itself.",
                "",
                "One by one, the outer provinces fell.",
                "The King's armies were shattered.",
                "The great mages vanished into silence.",
                "",
                "Now, only a single bastion remains...",
            };
            int lineHeight = 22;
            for (int i = 0; i < lines.length; i++) {
                int ly = (int)(scrollY + i * lineHeight);
                if (ly > H / 5 && ly < H - 40) {
                    double lineFade = 1.0;
                    if (ly < H / 5 + 40) lineFade = (ly - H / 5) / 40.0;
                    if (ly > H - 120) lineFade = (H - 40 - ly) / 80.0;
                    if (lineFade > 0.01 && !lines[i].isEmpty()) {
                        int la = (int)(200 * Math.min(1.0, lineFade));
                        changeColor(new Color(210, 198, 172, la));
                        int tx = textCenterX - estimateTextWidth(lines[i], 16, false, FONT_BODY) / 2;
                        drawText(tx, ly, lines[i], FONT_BODY, 16);
                    }
                }
            }
        } else if (introPhase == 2) {
            double fadeIn = Math.min(1.0, (introTimer - 7.0) / 1.5);
            int alpha = (int)(fadeIn * 220);
            String[] lines2 = new String[] {
                "But hope has not yet perished...",
                "",
                "Three champions stand ready to answer",
                "the call of destiny.",
                "",
                "A warrior of unyielding courage.",
                "A mage of boundless arcane power.",
                "An assassin who walks in shadow.",
                "",
                "One of them shall rise...",
                "and turn the tide of darkness.",
            };
            for (int i = 0; i < lines2.length; i++) {
                int ly = textY - 80 + i * 24;
                if (!lines2[i].isEmpty()) {
                    int la = alpha;
                    if (i < 2) la = (int)(alpha * 0.7);
                    changeColor(new Color(210, 198, 172, la));
                    int tx = textCenterX - estimateTextWidth(lines2[i], 16, false, FONT_BODY) / 2;
                    drawText(tx, ly, lines2[i], FONT_BODY, 16);
                }
            }
        } else if (introPhase == 3) {
            double phaseTime = introTimer - 14.5;
            double fadeIn = Math.min(1.0, phaseTime / 1.5);
            int alpha = (int)(fadeIn * 240);

            double flash = Math.sin(phaseTime * 4.0) * 0.5 + 0.5;
            if (flash > 0.7) {
                changeColor(new Color(255, 240, 200, (int)(60 * (flash - 0.7) / 0.3)));
                drawSolidRectangle(0, 0, W, H);
            }

            drawCenteredEmbossedText(0, textY - 40, W, "THE TIME HAS COME", 40, FONT_TITLE,
                new Color(242, 222, 170, alpha), new Color(72, 55, 34, alpha));
            changeColor(new Color(210, 190, 150, alpha));
            drawCenteredText(0, textY + 4, W, "Choose your champion and reclaim the fallen kingdom", FONT_BODY, 16);

            if (phaseTime > 5.5) {
                double endFade = (phaseTime - 5.5) / 2.0;
                changeColor(new Color(255, 252, 245, (int)(endFade * 240)));
                drawSolidRectangle(0, 0, W, H);
            }
        }

        if (introTimer > 1.0) {
            double hintAlpha = 110 + Math.sin(t * 2.5) * 30;
            changeColor(new Color(160, 150, 130, (int)hintAlpha));
            drawCenteredText(0, H - 22, W, "Press ENTER or SPACE to skip", FONT_BODY, 13);
        }
    }

    private void fillTriangle(int x1, int y1, int x2, int y2, int x3, int y3) {
        int[] xs = new int[] {x1, x2, x3};
        int[] ys = new int[] {y1, y2, y3};
        mGraphics.fillPolygon(xs, ys, 3);
    }

    public void startRun(CharacterDef def) {
        runTimeSeconds = 0;
        bossIntroTimer = 0;
        playerAuraTrailTimer = 0;
        levelMilestoneNotice = -1;
        levelMilestoneNoticeTimer = 0;
        killCount = 0;
        totalKillScore = 0;
        bossKilled = false;
        fullHealthBonusEarned = false;
        highestLevelReached = 0;
        enemies.clear();
        pickups.clear();
        projectiles.clear();
        player = new Player(this, def, GameConfig.WORLD_WIDTH * 0.5,
            GameConfig.WORLD_HEIGHT * 0.5);
        if (gameMap != null) {
            gameMap.generate();
        }
        spawner.reset();
        upgradeSystem.reset();
        weaponManager.reset(def.startingWeaponId);
        gameState = STATE_PLAYING;
    }

    private void updatePlayerAuraVfx(double dt) {
        if (player == null || !player.alive) return;
        playerAuraTrailTimer += dt;
        double moveMag = Math.abs(player.vx) + Math.abs(player.vy);
        boolean moving = moveMag > 16;
        boolean empowered = player.tier >= 3 || (player.ultimate != null && player.ultimate.active);
        double interval = empowered ? 0.03 : 0.06;
        if (!moving && !empowered) interval = 0.12;
        while (playerAuraTrailTimer >= interval) {
            playerAuraTrailTimer -= interval;
            Color c = player.getCharacter().color;
            if (player.ultimate != null && player.ultimate.active) {
                c = (player.tier >= 4) ? new Color(255, 214, 112) : new Color(178, 162, 255);
            } else if (player.tier >= 4) {
                c = new Color(242, 182, 86);
            } else if (player.tier >= 3) {
                c = new Color(175, 134, 232);
            }
            double ox = rand(18.0) - 9.0;
            double oy = rand(18.0) - 9.0;
            vfx.spawnAuraSpark(player.x + ox, player.y + oy, c, empowered);
        }
    }

    @Override
    public void keyPressed(KeyEvent event) {
        int code = event.getKeyCode();
        if (code >= 0 && code < keys.length) keys[code] = true;

        if (code == KeyEvent.VK_ESCAPE) {
            if (gameState == STATE_PLAYING || gameState == STATE_UPGRADE_PAUSE || gameState == STATE_BOSS_INTRO) {
                gameState = STATE_MENU;
            } else if (gameState == STATE_MENU) {
                if (menuSettingsOpen) menuSettingsOpen = false;
                else System.exit(0);
            }
        }

        if (gameState == STATE_PLAYING && code == KeyEvent.VK_SPACE) {
            if (player != null && player.tier >= 3 && player.ultimate != null && player.ultimate.isReady()) {
                player.ultimate.activate(this);
                addScreenShake(4);
                System.out.println("[Ultimate] " + player.ultimate.name + " activated!");
            }
        }
        // DEBUG: press L to gain XP for testing tier ups
        if (code == KeyEvent.VK_L && gameState == STATE_PLAYING) {
            for (int i = 0; i < 8; i++) upgradeSystem.gainXP(upgradeSystem.xpForNextLevel() * 2);
        }
        if (gameState == STATE_INTRO) {
            if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE || code == KeyEvent.VK_ESCAPE) {
                gameState = STATE_CHAR_SELECT;
            }
        } else if (gameState == STATE_MENU) {
            if (menuSettingsOpen) {
                if (code == KeyEvent.VK_S || code == KeyEvent.VK_ESCAPE) {
                    menuSettingsOpen = false;
                } else if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) {
                    combatPaceLevel = Math.max(0, combatPaceLevel - 1);
                } else if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) {
                    combatPaceLevel = Math.min(2, combatPaceLevel + 1);
                } else if (code == KeyEvent.VK_SPACE || code == KeyEvent.VK_ENTER) {
                    screenShakeEnabled = !screenShakeEnabled;
                }
            } else {
                if (code == KeyEvent.VK_1 || code == KeyEvent.VK_ENTER) startIntro();
                if (code == KeyEvent.VK_S) menuSettingsOpen = true;
            }
        } else if (gameState == STATE_CHAR_SELECT) {
            if (code == KeyEvent.VK_ESCAPE) {
                gameState = STATE_MENU;
                return;
            }
            if (code == KeyEvent.VK_1) startRun(characters[0]);
            if (code == KeyEvent.VK_2) startRun(characters[1]);
            if (code == KeyEvent.VK_3) startRun(characters[2]);
        } else if (gameState == STATE_UPGRADE_PAUSE) {
            if (code == KeyEvent.VK_1) upgradeSystem.choose(0);
            if (code == KeyEvent.VK_2) upgradeSystem.choose(1);
            if (code == KeyEvent.VK_3) upgradeSystem.choose(2);
        } else if (gameState == STATE_WEAPON_SWAP) {
            if (code == KeyEvent.VK_ESCAPE) cancelWeaponSwap();
            if (code == KeyEvent.VK_1) resolveWeaponSwap(0);
            if (code == KeyEvent.VK_2) resolveWeaponSwap(1);
            if (code == KeyEvent.VK_3) resolveWeaponSwap(2);
            if (code == KeyEvent.VK_4) resolveWeaponSwap(3);
            if (code == KeyEvent.VK_5) resolveWeaponSwap(4);
            if (code == KeyEvent.VK_6) resolveWeaponSwap(5);
        } else if ((gameState == STATE_VICTORY || gameState == STATE_DEFEAT) && code == KeyEvent.VK_SPACE) {
            gameState = STATE_CHAR_SELECT;
        }
    }

    @Override
    public void keyReleased(KeyEvent event) {
        int code = event.getKeyCode();
        if (code >= 0 && code < keys.length) keys[code] = false;
    }

    @Override
    public void mouseMoved(MouseEvent event) {
        mouseX = event.getX();
        mouseY = event.getY();
        if (gameState == STATE_MENU) {
            updateMenuHover();
        } else if (gameState == STATE_CHAR_SELECT) {
            updateCharHover();
        } else if (gameState == STATE_UPGRADE_PAUSE) {
            updateUpgradeHover();
        }
    }

    @Override
    public void mouseClicked(MouseEvent event) {
        if (gameState == STATE_MENU) {
            handleMenuClick();
        } else if (gameState == STATE_CHAR_SELECT) {
            handleCharSelectClick();
        } else if (gameState == STATE_UPGRADE_PAUSE) {
            int h = upgradeSystem.hoveredCard();
            if (h >= 0) {
                upgradeSystem.choose(h);
            }
        } else if (gameState == STATE_WEAPON_SWAP) {
            ArrayList<WeaponInstance> list = weaponManager.list();
            for (int i = 0; i < list.size(); i++) {
                int x = 130 + i * 120;
                int y = 310;
                if (mouseX >= x && mouseX <= x + 100 && mouseY >= y && mouseY <= y + 120) {
                    resolveWeaponSwap(i);
                    return;
                }
            }
        }
    }

    private void drawMenuBackdrop() {
        changeColor(new Color(18, 16, 24));
        drawSolidRectangle(0, 0, width(), height());
        changeColor(new Color(28, 24, 36));
        for (int i = 0; i < width(); i += 34) {
            drawSolidRectangle(i, 0, 2, height());
        }
        changeColor(new Color(34, 28, 24, 140));
        drawSolidRectangle(84, 70, 812, 500);
        changeColor(new Color(100, 86, 63));
        drawRectangle(84, 70, 812, 500, 3);
    }

    private void drawMenuButton(int x, int y, int w, int h, String text, boolean hover, Color accent) {
        Color base = hover ? new Color(77, 66, 56) : new Color(47, 40, 35);
        changeColor(base);
        drawSolidRectangle(x, y, w, h);
        changeColor(new Color(255, 255, 255, hover ? 30 : 12));
        drawSolidRectangle(x + 3, y + 3, w - 6, 9);
        changeColor(hover ? accent.brighter() : accent);
        drawRectangle(x, y, w, h, hover ? 4 : 3);
        int size = (h <= 34) ? 13 : (h <= 48 ? 18 : 24);
        int textX = x + Math.max(10, (int)(w * 0.5 - text.length() * size * 0.28));
        int textY = y + h / 2 + (size <= 13 ? 5 : (size <= 18 ? 6 : 8));
        drawEmbossedText(textX, textY, text, size, new Color(245, 236, 208), new Color(60, 46, 30));
    }

    private void renderSettingsPanel() {
        int x = 240;
        int y = 190;
        int w = 480;
        int h = 300;
        changeColor(new Color(8, 8, 12, 195));
        drawSolidRectangle(x, y, w, h);
        changeColor(new Color(180, 152, 100));
        drawRectangle(x, y, w, h, 3);

        drawEmbossedText(x + 148, y + 50, "SETTINGS", 34, new Color(240, 224, 178), new Color(70, 52, 30));
        changeColor(new Color(170, 154, 117));
        drawText(x + 115, y + 70, "Tune your adventure experience", "Arial", 14);

        drawSettingRow(x + 40, y + 92, 400, "Screen Shake", screenShakeEnabled ? "ON" : "OFF");
        drawSettingRow(x + 40, y + 148, 400, "Combat Pace", paceLabel());

        drawMenuButton(x + 40, y + 220, 180, 46, "TOGGLE SHAKE", inRect(mouseX, mouseY, x + 40, y + 220, 180, 46), new Color(120, 92, 150));
        drawMenuButton(x + 260, y + 220, 180, 46, "PACE  < >", inRect(mouseX, mouseY, x + 260, y + 220, 180, 46), new Color(80, 120, 145));
        changeColor(new Color(206, 194, 166));
        drawText(x + 95, y + 286, "ESC / S to close", "Arial", 15);
    }

    private void drawSettingRow(int x, int y, int w, String key, String value) {
        changeColor(new Color(36, 31, 40));
        drawSolidRectangle(x, y, w, 44);
        changeColor(new Color(95, 84, 104));
        drawRectangle(x, y, w, 44, 2);
        changeColor(new Color(220, 212, 190));
        drawBoldText(x + 14, y + 29, key.toUpperCase(), "Arial", 16);
        drawEmbossedText(x + w - 150, y + 30, value, 18, new Color(150, 210, 220), new Color(45, 72, 84));
    }

    private void drawClassGlyph(int id, int cx, int cy) {
        if (id == CharacterDef.WARRIOR) {
            changeColor(new Color(245, 235, 210));
            drawSolidRectangle(cx - 2, cy - 16, 4, 22);
            drawSolidRectangle(cx - 10, cy - 12, 20, 4);
            drawSolidRectangle(cx - 6, cy + 8, 12, 4);
        } else if (id == CharacterDef.MAGE) {
            changeColor(new Color(240, 230, 255));
            drawSolidRectangle(cx - 2, cy - 14, 4, 20);
            drawSolidCircle(cx, cy - 18, 6);
            drawSolidRectangle(cx - 10, cy + 8, 20, 3);
        } else {
            changeColor(new Color(235, 235, 235));
            drawSolidRectangle(cx - 1, cy - 15, 3, 20);
            drawSolidRectangle(cx - 8, cy + 8, 16, 3);
            drawSolidRectangle(cx + 6, cy - 14, 3, 12);
        }
    }

    private void drawStatGlyph(int x, int y, int type) {
        if (type == 0) {
            changeColor(new Color(226, 102, 102));
            drawSolidRectangle(x - 2, y - 2, 6, 2);
            drawSolidRectangle(x, y - 4, 2, 6);
        } else if (type == 1) {
            changeColor(new Color(126, 196, 238));
            drawSolidRectangle(x - 2, y - 2, 6, 2);
            drawSolidRectangle(x + 2, y - 4, 2, 6);
        } else {
            changeColor(new Color(245, 184, 104));
            drawSolidRectangle(x - 2, y - 2, 5, 2);
            drawSolidRectangle(x + 1, y - 4, 2, 6);
        }
    }

    private Color classAccent(int id) {
        if (id == CharacterDef.WARRIOR) return new Color(201, 143, 76);
        if (id == CharacterDef.MAGE) return new Color(124, 113, 206);
        return new Color(174, 177, 191);
    }

    private String classTitle(int id) {
        if (id == CharacterDef.WARRIOR) return "Crimson Vanguard";
        if (id == CharacterDef.MAGE) return "Arcane Scholar";
        return "Shadow Duelist";
    }

    private String classTagline(int id) {
        if (id == CharacterDef.WARRIOR) return "Frontline bruiser, stable sustain.";
        if (id == CharacterDef.MAGE) return "Burst caster, strong ranged pressure.";
        return "Fast assassin, lethal hit-and-run.";
    }

    private String classHint(int id) {
        if (id == CharacterDef.WARRIOR) return "Great for beginners and long fights.";
        if (id == CharacterDef.MAGE) return "Master spacing to survive.";
        return "High tempo, high reward.";
    }

    private Color tierThemeColor() {
        if (pendingTier >= 4) return new Color(255, 205, 76);
        if (pendingTier >= 3) return new Color(193, 116, 255);
        return new Color(102, 196, 255);
    }

    private String tierDisplayName() {
        if (pendingTier >= 4) return "LEGENDARY";
        if (pendingTier >= 3) return "ELITE";
        return "ADVANCED";
    }

    private int statGainPct(double oldMul, double newMul) {
        if (oldMul <= 0.0001) return 0;
        return (int)Math.round((newMul / oldMul - 1.0) * 100.0);
    }

    private void drawTierStatCard(int x, int y, int w, int h, String label, int gainPct, Color accent) {
        changeColor(new Color(30, 27, 39));
        drawSolidRectangle(x, y, w, h);
        changeColor(new Color(98, 88, 114));
        drawRectangle(x, y, w, h, 2);
        changeColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 65));
        drawSolidRectangle(x + 4, y + 4, w - 8, 16);
        drawEmbossedText(x + 12, y + 30, label, 17, FONT_BODY, new Color(236, 226, 204), new Color(64, 57, 42));
        drawCenteredEmbossedText(x, y + 68, w, "+" + gainPct + "%", 33, FONT_NUM, accent, new Color(46, 34, 22));
        changeColor(new Color(182, 175, 154));
        drawCenteredText(x, y + 88, w, "Tier bonus applied", FONT_BODY, 12);
    }

    private int drawAdaptiveUpgradeTitle(int x, int y, int maxWidth, String title) {
        int size = 20;
        while (size > 15 && estimateTextWidth(title, size, true, FONT_TITLE) > maxWidth * 2) {
            size--;
        }

        String[] lines = splitUpgradeTitle(title, maxWidth, size);
        drawEmbossedText(x, y, lines[0], size, FONT_TITLE, new Color(238, 232, 210), new Color(62, 56, 76));
        if (lines[1] != null && !lines[1].isEmpty()) {
            drawEmbossedText(x, y + size + 2, lines[1], size, FONT_TITLE, new Color(238, 232, 210), new Color(62, 56, 76));
            return y + size + 2 + size;
        }
        return y + size;
    }

    private String[] splitUpgradeTitle(String title, int maxWidth, int size) {
        if (estimateTextWidth(title, size, true, FONT_TITLE) <= maxWidth) {
            return new String[] {title, null};
        }
        String[] words = title.split(" ");
        if (words.length <= 1) {
            return new String[] {trimToWidth(title, maxWidth, size, true, FONT_TITLE), null};
        }

        String line1 = "";
        String line2 = "";
        for (int i = 0; i < words.length; i++) {
            String candidate = line1.isEmpty() ? words[i] : (line1 + " " + words[i]);
            if (estimateTextWidth(candidate, size, true, FONT_TITLE) <= maxWidth || line1.isEmpty()) {
                line1 = candidate;
            } else {
                for (int j = i; j < words.length; j++) {
                    line2 = line2.isEmpty() ? words[j] : (line2 + " " + words[j]);
                }
                break;
            }
        }

        line1 = trimToWidth(line1, maxWidth, size, true, FONT_TITLE);
        line2 = trimToWidth(line2, maxWidth, size, true, FONT_TITLE);
        return new String[] {line1, line2.isEmpty() ? null : line2};
    }

    private String trimToWidth(String text, int maxWidth, int size, boolean bold, String font) {
        if (text == null) return "";
        if (estimateTextWidth(text, size, bold, font) <= maxWidth) return text;
        String out = text;
        while (out.length() > 1 && estimateTextWidth(out + "...", size, bold, font) > maxWidth) {
            out = out.substring(0, out.length() - 1);
        }
        return out + "...";
    }

    private void renderLevelMilestoneTrack(int x, int y) {
        int[] marks = new int[] {25, 50, 75};
        changeColor(new Color(202, 188, 152));
        drawText(x, y, "MILESTONES", "Arial", 11);
        for (int i = 0; i < marks.length; i++) {
            int cx = x + i * 44;
            int cy = y + 8;
            boolean reached = upgradeSystem.level() >= marks[i];
            changeColor(reached ? new Color(245, 198, 88) : new Color(68, 62, 74));
            drawSolidCircle(cx + 12, cy + 10, 9);
            changeColor(reached ? new Color(255, 234, 188) : new Color(156, 146, 168));
            drawText(cx + 4, cy + 14, Integer.toString(marks[i]), "Arial", 9);
        }
    }

    private void renderLevelMilestoneBanner(int levelMark, double timeRatio) {
        int w = 420;
        int h = 88;
        int x = width() / 2 - w / 2;
        int y = 94 + (int)(Math.sin(uiTime() * 9) * 2);
        int alpha = 120 + (int)(Math.max(0, Math.min(1, timeRatio)) * 70);
        changeColor(new Color(12, 10, 18, alpha));
        drawSolidRectangle(x, y, w, h);
        changeColor(new Color(186, 154, 90, 230));
        drawRectangle(x, y, w, h, 3);
        drawEmbossedText(x + 78, y + 34, "LEVEL " + levelMark + " MILESTONE!", 24, new Color(250, 226, 164), new Color(76, 55, 31));
        changeColor(new Color(216, 206, 182));
        drawText(x + 82, y + 58, "Power surge: your legend grows stronger", "Arial", 14);
    }

    private void updateCharCardAnimation(double dt) {
        for (int i = 0; i < charCardLift.length; i++) {
            double target = (i == charHoverIndex) ? 7.0 : 0.0;
            double speed = 14.0;
            charCardLift[i] += (target - charCardLift[i]) * Math.min(1.0, dt * speed);
        }
    }

    private void updateMenuHover() {
        if (menuSettingsOpen) {
            menuHoverButton = -1;
            return;
        }
        menuHoverButton = -1;
        if (inRect(mouseX, mouseY, 320, 236, 320, 62)) menuHoverButton = 0;
        else if (inRect(mouseX, mouseY, 320, 316, 320, 62)) menuHoverButton = 1;
        else if (inRect(mouseX, mouseY, 320, 396, 320, 62)) menuHoverButton = 2;
    }

    private void handleMenuClick() {
        if (menuSettingsOpen) {
            if (inRect(mouseX, mouseY, 280, 410, 180, 46)) {
                screenShakeEnabled = !screenShakeEnabled;
            } else if (inRect(mouseX, mouseY, 500, 410, 180, 46)) {
                combatPaceLevel = (combatPaceLevel + 1) % 3;
            }
            return;
        }
        if (menuHoverButton == 0) startIntro();
        else if (menuHoverButton == 1) menuSettingsOpen = true;
        else if (menuHoverButton == 2) System.exit(0);
    }

    private void updateCharHover() {
        int panelX = 48;
        int panelY = 54;
        int panelW = width() - 96;
        int cardW = 250;
        int cardH = 308;
        int gap = (panelW - cardW * 3) / 4;
        if (gap < 24) gap = 24;
        int cardY = panelY + 142;

        charHoverIndex = -1;
        for (int i = 0; i < characters.length; i++) {
            int x = panelX + gap + i * (cardW + gap);
            if (inRect(mouseX, mouseY, x, cardY, cardW, cardH)) {
                charHoverIndex = i;
                break;
            }
        }
    }

    private void handleCharSelectClick() {
        if (charHoverIndex >= 0 && charHoverIndex < characters.length) {
            startRun(characters[charHoverIndex]);
        }
    }

    private boolean inRect(int px, int py, int x, int y, int w, int h) {
        return px >= x && px <= x + w && py >= y && py <= y + h;
    }

    private String paceLabel() {
        if (combatPaceLevel <= 0) return "RELAXED";
        if (combatPaceLevel >= 2) return "INTENSE";
        return "ADVENTURER";
    }

    private double combatPaceMultiplier() {
        if (combatPaceLevel <= 0) return 0.82;
        if (combatPaceLevel >= 2) return 1.22;
        return 1.0;
    }

    public void onLevelMilestone(int levelMark) {
        levelMilestoneNotice = levelMark;
        levelMilestoneNoticeTimer = 2.2;
        addScreenShake(4);
        if (player != null) {
            Color c = (levelMark >= 75) ? new Color(255, 210, 88)
                : (levelMark >= 50) ? new Color(206, 152, 255)
                : new Color(120, 210, 255);
            for (int i = 0; i < 3; i++) {
                vfx.spawnDeathBurst(player.x, player.y, c);
            }
        }
    }

    private double uiTime() {
        return System.currentTimeMillis() * 0.001;
    }

    private void drawEmbossedText(int x, int y, String text, int size, Color front, Color shadow) {
        drawEmbossedText(x, y, text, size, FONT_BODY, front, shadow);
    }

    private void drawEmbossedText(int x, int y, String text, int size, String font, Color front, Color shadow) {
        changeColor(shadow);
        drawBoldText(x + 2, y + 2, text, font, size);
        changeColor(front);
        drawBoldText(x, y, text, font, size);
    }

    private int estimateTextWidth(String text, int size, boolean bold) {
        return estimateTextWidth(text, size, bold, FONT_BODY);
    }

    private int estimateTextWidth(String text, int size, boolean bold, String font) {
        double factor = bold ? 0.62 : 0.54;
        if (FONT_TITLE.equals(font)) factor += 0.05;
        if (FONT_NUM.equals(font)) factor += 0.02;
        return (int)Math.round(text.length() * size * factor);
    }

    private void drawCenteredText(int x, int y, int w, String text, String font, int size) {
        int tx = x + (w - estimateTextWidth(text, size, false, font)) / 2;
        drawText(tx, y, text, font, size);
    }

    private void drawCenteredEmbossedText(int x, int y, int w, String text, int size, String font, Color front, Color shadow) {
        int tx = x + (w - estimateTextWidth(text, size, true, font)) / 2;
        drawEmbossedText(tx, y, text, size, font, front, shadow);
    }

    private void drawFramedBar(int x, int y, int w, int h, double pct, Color fill, String label) {
        pct = Math.max(0.0, Math.min(1.0, pct));
        changeColor(new Color(32, 30, 40));
        drawSolidRectangle(x, y, w, h);
        changeColor(fill);
        drawSolidRectangle(x, y, w * pct, h);
        changeColor(new Color(255, 255, 255, 45));
        drawSolidRectangle(x + 2, y + 2, (w - 4) * pct, Math.max(2, h * 0.28));
        changeColor(new Color(210, 198, 170));
        drawRectangle(x, y, w, h, 1.7);
        changeColor(new Color(233, 225, 202));
        drawText(x + 6, y + h - 3, label, "Arial", 11);
    }

    private String shortWeaponName(String name) {
        if (name.length() <= 9) return name;
        return name.substring(0, 8) + ".";
    }

    private void drawWrappedText(int x, int y, String text, int maxCharsPerLine, int lineHeight, int maxLines, String font) {
        if (text == null || text.isEmpty()) return;
        String[] words = text.split(" ");
        String line = "";
        int lines = 0;
        for (int i = 0; i < words.length; i++) {
            String next = line.isEmpty() ? words[i] : line + " " + words[i];
            if (next.length() > maxCharsPerLine && !line.isEmpty()) {
                drawText(x, y + lines * lineHeight, line, font, 14);
                lines++;
                if (lines >= maxLines) return;
                line = words[i];
            } else {
                line = next;
            }
        }
        if (!line.isEmpty() && lines < maxLines) {
            drawText(x, y + lines * lineHeight, line, font, 14);
        }
    }

    private void addScreenShake(int amount) {
        if (screenShakeEnabled) vfx.addShake(amount);
    }

    private void updateUpgradeHover() {
        int hover = -1;
        for (int i = 0; i < 3; i++) {
            int x = 116 + i * 258;
            int y = 228;
            if (mouseX >= x && mouseX <= x + 228 && mouseY >= y && mouseY <= y + 270) {
                hover = i;
                break;
            }
        }
        upgradeSystem.setHoveredCard(hover);
    }

    public boolean isKeyDown(char c) {
        int keyCode = KeyEvent.getExtendedKeyCodeForChar(Character.toUpperCase(c));
        if (keyCode < 0 || keyCode >= keys.length) return false;
        return keys[keyCode];
    }

    public void addEnemy(Enemy e) {
        if (enemies.size() < GameConfig.MAX_ENEMIES) enemies.add(e);
    }

    public void addProjectile(Projectile p) {
        if (projectiles.size() < GameConfig.MAX_PROJECTILES) projectiles.add(p);
    }

    public void addPickup(Pickup pickup) {
        if (pickups.size() < GameConfig.MAX_PICKUPS) pickups.add(pickup);
    }

    public void addWeaponDrop(WeaponDrop wd) {
        if (pickups.size() < GameConfig.MAX_PICKUPS) pickups.add(wd);
    }

    public void enterWeaponSwap(int weaponId, WeaponRarity rarity) {
        if (gameState == STATE_PLAYING) {
            swapPendingWeaponId = weaponId;
            swapPendingRarity = rarity;
            gameState = STATE_WEAPON_SWAP;
        }
    }

    public void resolveWeaponSwap(int slotIndex) {
        weaponManager.replaceWeapon(slotIndex, swapPendingWeaponId, swapPendingRarity);
        gameState = STATE_PLAYING;
    }

    public void cancelWeaponSwap() {
        gameState = STATE_PLAYING;
    }

    public Enemy findNearestEnemy(double x, double y) {
        Enemy best = null;
        double bestDist = Double.MAX_VALUE;
        for (Enemy e : enemies) {
            if (!e.alive) continue;
            double dx = e.x - x;
            double dy = e.y - y;
            double d = dx * dx + dy * dy;
            if (d < bestDist) {
                bestDist = d;
                best = e;
            }
        }
        return best;
    }

    public double getPlayerPickupRange() {
        return 130 * player.pickupRangeMultiplier;
    }

    public void gainXP(double amount) {
        upgradeSystem.gainXP(amount);
    }

    public void addKillScore(Enemy enemy) {
        if (enemy.getType() == Enemy.BOSS) return;
        killCount++;
        double rarityMult;
        switch (enemy.getRarity()) {
            case Enemy.RARITY_RARE:   rarityMult = GameConfig.SCORE_RARITY_RARE; break;
            case Enemy.RARITY_EPIC:   rarityMult = GameConfig.SCORE_RARITY_EPIC; break;
            case Enemy.RARITY_LEGENDARY: rarityMult = GameConfig.SCORE_RARITY_LEGENDARY; break;
            default:                  rarityMult = GameConfig.SCORE_RARITY_NORMAL; break;
        }
        totalKillScore += (int)Math.round(enemy.getBaseKillScore() * rarityMult);
    }

    public int calculateFinalScore() {
        int survivalScore = (int)Math.floor(runTimeSeconds * GameConfig.SCORE_SURVIVAL_PER_SECOND);
        int bossBonus = bossKilled ? GameConfig.SCORE_BOSS_KILL : 0;
        int fullHpBonus = fullHealthBonusEarned ? GameConfig.SCORE_FULL_HP_BONUS : 0;
        int subtotal = totalKillScore + survivalScore + bossBonus + fullHpBonus;
        double milestoneMult;
        if (highestLevelReached >= 75) milestoneMult = GameConfig.SCORE_MILESTONE_LV75;
        else if (highestLevelReached >= 50) milestoneMult = GameConfig.SCORE_MILESTONE_LV50;
        else if (highestLevelReached >= 25) milestoneMult = GameConfig.SCORE_MILESTONE_LV25;
        else milestoneMult = 1.0;
        return (int)Math.round(subtotal * milestoneMult);
    }

    public void spawnEnemyNear(double x, double y, int type, double radius) {
        double angle = Math.toRadians(rand(360.0));
        double sx = x + Math.cos(angle) * radius;
        double sy = y + Math.sin(angle) * radius;
        sx = Math.max(12, Math.min(GameConfig.WORLD_WIDTH - 12, sx));
        sy = Math.max(12, Math.min(GameConfig.WORLD_HEIGHT - 12, sy));
        addEnemy(new Enemy(this, type, sx, sy));
    }

    public void spawnBossRadial(Enemy boss) {
        addScreenShake(8);
        for (int i = 0; i < 8; i++) {
            double angle = Math.toRadians(i * 45);
            Projectile p = new Projectile(this, boss.x, boss.y, 6, 180, angle, 20, 3.5, 0, false, new Color(255, 110, 110));
            addProjectile(p);
        }
    }

    public void onBossKilled() {
        bossKilled = true;
        if (player != null && player.health >= player.maxHealth) {
            fullHealthBonusEarned = true;
        }
        gameState = STATE_VICTORY;
    }

    public void enterBossIntro() {
        gameState = STATE_BOSS_INTRO;
        bossIntroTimer = GameConfig.BOSS_INTRO_SECONDS;
    }

    public void enterTierUp(int newTier) {
        if (gameState != STATE_PLAYING) return;
        pendingTier = newTier;
        tierUpTimer = GameConfig.TIER_UP_INTRO_SECONDS;
        gameState = STATE_TIER_UP;
        if (player != null) {
            player.applyTierBonuses(newTier);
        }
        addScreenShake(6);
        // Spawn celebration particles around player
        if (player != null) {
            for (int i = 0; i < 30; i++) {
                vfx.spawnDeathBurst(player.x, player.y,
                    newTier >= 4 ? new Color(255, 200, 40) :
                    newTier >= 3 ? new Color(180, 80, 255) :
                    new Color(80, 180, 255));
            }
        }
        System.out.println("[Tier] Player reached Tier " + newTier + "!");
        // Generate upgrade choices after tier animation
        upgradeSystem.generateChoices();
    }

    public void enterUpgradePause() {
        if (gameState == STATE_PLAYING) {
            gameState = STATE_UPGRADE_PAUSE;
        }
    }

    public void resumePlayingFromUpgrade() {
        gameState = STATE_PLAYING;
    }

    public Enemy findBoss() {
        for (Enemy e : enemies) {
            if (e.alive && e.getType() == Enemy.BOSS) return e;
        }
        return null;
    }

    public int getState() {
        return gameState;
    }

    public void playerBoostSpeed(double multiplier) {
        if (player != null) {
            player.boostMoveSpeed(multiplier);
        }
    }
}
