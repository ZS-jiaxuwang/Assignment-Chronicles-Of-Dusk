import java.io.File;
import javax.sound.sampled.*;

public class AudioManager {
    private static final String[] AUDIO_DIRS = {
        "Game-test/audio",
        "audio",
        "Assignment-Chronicles-Of-Dusk/Game-test/audio",
        "Assignment-Chronicles-Of-Dusk/audio"
    };

    private final GameEngine engine;
    private GameEngine.AudioClip bgmMenu;
    private GameEngine.AudioClip bgmBattle;
    private GameEngine.AudioClip bgmBoss;
    private GameEngine.AudioClip sfxBossIntro;
    private GameEngine.AudioClip sfxVictory;
    private GameEngine.AudioClip sfxDefeat;

    private String currentBgm = null;
    private float masterVolume = 0.0f;
    private Clip currentSfxClip;

    public AudioManager(GameEngine engine) {
        this.engine = engine;
        loadAll();
    }

    private String resolvePath(String filename) {
        for (String dir : AUDIO_DIRS) {
            File f = new File(dir, filename);
            if (f.exists()) return f.getPath();
        }
        return new File(AUDIO_DIRS[0], filename).getPath();
    }

    private void loadAll() {
        bgmMenu = engine.loadAudio(resolvePath("bgm_menu.wav"));
        bgmBattle = engine.loadAudio(resolvePath("bgm_battle.wav"));

        String bossPath = resolvePath("bgm_boss.wav");
        if (new File(bossPath).exists()) {
            bgmBoss = engine.loadAudio(bossPath);
        }

        sfxBossIntro = engine.loadAudio(resolvePath("sfx_boss_intro.wav"));
        sfxVictory = engine.loadAudio(resolvePath("sfx_victory.wav"));
        sfxDefeat = engine.loadAudio(resolvePath("sfx_defeat.wav"));
    }

    private void stopSfx() {
        if (currentSfxClip != null) {
            if (currentSfxClip.isRunning()) {
                currentSfxClip.stop();
            }
            currentSfxClip.close();
            currentSfxClip = null;
        }
    }

    private void playOneShot(GameEngine.AudioClip audioClip) {
        stopSfx();
        if (audioClip == null) return;
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(audioClip.getAudioFormat(), audioClip.getData(), 0, (int)audioClip.getBufferSize());
            FloatControl control = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
            control.setValue(masterVolume);
            clip.start();
            currentSfxClip = clip;
        } catch(Exception e) {
            System.out.println("Error playing SFX\n");
        }
    }

    public void playMenuBgm() {
        if ("menu".equals(currentBgm)) return;
        stopSfx();
        stopCurrentBgm();
        if (bgmMenu != null) {
            engine.startAudioLoop(bgmMenu, masterVolume);
            currentBgm = "menu";
        }
    }

    public void playBattleBgm() {
        if ("battle".equals(currentBgm)) return;
        stopSfx();
        stopCurrentBgm();
        if (bgmBattle != null) {
            engine.startAudioLoop(bgmBattle, masterVolume);
            currentBgm = "battle";
        }
    }

    public void playBossBgm() {
        if ("boss".equals(currentBgm)) return;
        stopSfx();
        stopCurrentBgm();
        if (bgmBoss != null) {
            engine.startAudioLoop(bgmBoss, masterVolume);
            currentBgm = "boss";
        }
    }

    public void stopCurrentBgm() {
        if (currentBgm == null) return;
        GameEngine.AudioClip clip = null;
        switch (currentBgm) {
            case "menu":  clip = bgmMenu;  break;
            case "battle": clip = bgmBattle; break;
            case "boss":  clip = bgmBoss;  break;
        }
        if (clip != null) {
            engine.stopAudioLoop(clip);
        }
        currentBgm = null;
    }

    public void playBossIntro() {
        playOneShot(sfxBossIntro);
    }

    public void playVictory() {
        stopCurrentBgm();
        playOneShot(sfxVictory);
    }

    public void playDefeat() {
        stopCurrentBgm();
        playOneShot(sfxDefeat);
    }

    public void stopAll() {
        stopCurrentBgm();
        stopSfx();
    }

    public void setVolume(float volume) {
        masterVolume = volume;
    }
}
