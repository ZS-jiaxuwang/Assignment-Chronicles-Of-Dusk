// Jiaxu Wang (24009377)  Jiaheng Liu (24009483)  Angze Song (24009333)   Xiao Wu (24009458)
public class EnemySpawner {
    private final SurvivalGame game;
    private double gameTimer;
    private double waveTimer;
    private double trickleTimer;
    private boolean bossSpawned;

    public EnemySpawner(SurvivalGame game) {
        this.game = game;
    }

    public void reset() {
        gameTimer = 0;
        waveTimer = 0;
        trickleTimer = 0;
        bossSpawned = false;
    }

    public void update(double dt) {
        gameTimer += dt;
        waveTimer += dt;
        trickleTimer += dt;

        if (!bossSpawned && gameTimer >= GameConfig.BOSS_TRIGGER_SECONDS) {
            bossSpawned = true;
            game.enterBossIntro();
            spawnBoss();
            return;
        }

        if (game.getState() != SurvivalGame.STATE_PLAYING) {
            return;
        }

        double trickleInterval = Math.max(0.45, 2.0 - gameTimer * 0.005);
        if (trickleTimer >= trickleInterval) {
            trickleTimer = 0;
            spawnEnemyByTime();
        }

        if (waveTimer >= 40.0) {
            waveTimer = 0;
            int count = 10 + (int)(gameTimer / 25.0);
            for (int i = 0; i < count; i++) {
                spawnEnemyByTime();
            }
        }
    }

    public int currentMaxEnemies() {
        int bonus = (int)(gameTimer / GameConfig.MAX_ENEMIES_GROWTH_INTERVAL) * GameConfig.MAX_ENEMIES_PER_GROWTH;
        return Math.min(GameConfig.MAX_ENEMIES_START + bonus, GameConfig.MAX_ENEMIES_CAP);
    }

    private void spawnEnemyByTime() {
        if (game.enemies.size() >= currentMaxEnemies()) return;
        int typeRoll = game.rand(100);
        int type;
        if (typeRoll < 30) type = Enemy.SLIME;
        else if (typeRoll < 48) type = Enemy.BAT;
        else if (typeRoll < 60) type = Enemy.SKELETON;
        else if (typeRoll < 72) type = Enemy.ORC;
        else if (typeRoll < 82) type = Enemy.GOBLIN;
        else if (typeRoll < 90) type = Enemy.MUSHROOM;
        else if (typeRoll < 96) type = Enemy.GHOST;
        else if (typeRoll < 99) type = Enemy.FLYING_EYE;
        else type = Enemy.GIANT;
        spawnAtEdge(type);
    }

    private void spawnAtEdge(int type) {
        Player player = game.player;
        if (player == null) return;
        Camera cam = game.camera;
        double margin = 80;
        for (int i = 0; i < 20; i++) {
            int side = game.rand(4);
            double x;
            double y;
            if (side == 0) {
                x = cam.x - margin + game.rand(cam.viewWidth + margin * 2);
                y = cam.y - margin;
            } else if (side == 1) {
                x = cam.x - margin + game.rand(cam.viewWidth + margin * 2);
                y = cam.y + cam.viewHeight + margin;
            } else if (side == 2) {
                x = cam.x - margin;
                y = cam.y - margin + game.rand(cam.viewHeight + margin * 2);
            } else {
                x = cam.x + cam.viewWidth + margin;
                y = cam.y - margin + game.rand(cam.viewHeight + margin * 2);
            }
            x = Math.max(20, Math.min(GameConfig.WORLD_WIDTH - 20, x));
            y = Math.max(20, Math.min(GameConfig.WORLD_HEIGHT - 20, y));
            double dx = x - player.x;
            double dy = y - player.y;
            if (dx * dx + dy * dy >= 400 * 400) {
                game.addEnemy(new Enemy(game, type, x, y));
                return;
            }
        }
    }

    private void spawnBoss() {
        double x = GameConfig.WORLD_WIDTH * 0.5;
        double y = 120;
        game.addEnemy(new Enemy(game, Enemy.BOSS, x, y));
        game.vfx.addShake(10);
    }
}
