package yio.tro.antiyoy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class SettingsManager {

    static SettingsManager instance = null;
    YioGdxGame yioGdxGame;
    /**
     * 询问结束
     */
    public static boolean askToEndTurn = false;
    /**
     * 自动保存
     */
    public static boolean autosave;
    /**
     * 长按移动，所有单位进行移动
     */
    public static boolean longTapToMove;
    /**
     * 音效
     */
    public static boolean soundEnabled = true;
    public static float sensitivity;
    /**
     * 水纹理
     */
    public static boolean waterTextureEnabled;
    /**
     * 皮肤序号
     */
    public static int skinIndex;
    /**
     * 回放
     */
    public static boolean replaysEnabled;
    /**
     * 快速构造
     */
    public static boolean fastConstructionEnabled;
    /**
     * 音乐
     */
    public static boolean musicEnabled;
    /**
     * 左手模式
     */
    public static boolean leftHandMode;
    /**
     * 展示继续按钮
     */
    public static boolean resumeButtonEnabled;
    /**
     * 展示城市名字
     */
    public static boolean cityNamesEnabled;
    /**
     * 全屏模式
     */
    public static boolean fullScreenMode;


    public static void initialize() {
        instance = null;
    }


    public static SettingsManager getInstance() {
        if (instance == null) {
            instance = new SettingsManager();
        }

        return instance;
    }


    public void setYioGdxGame(YioGdxGame yioGdxGame) {
        this.yioGdxGame = yioGdxGame;
    }


    public void loadAllSettings() {
        loadMainSettings();
        loadMoreSettings();
    }


    private void loadMoreSettings() {
        Preferences prefs = getPrefs();

        skinIndex = prefs.getInteger("skin", 0);

        setSensitivity(prefs.getInteger("sensitivity", 6));

        longTapToMove = prefs.getBoolean("long_tap_to_move", true);
        waterTextureEnabled = prefs.getBoolean("water_texture", false);
        replaysEnabled = prefs.getBoolean("replays_enabled", true);
        fastConstructionEnabled = prefs.getBoolean("fast_construction", false);
        leftHandMode = prefs.getBoolean("left_hand_mode", false);
        resumeButtonEnabled = prefs.getBoolean("resume_button", getResumeButtonDefaultValue());
        fullScreenMode = prefs.getBoolean("full_screen", false);
    }


    public void setSensitivity(int sliderIndex) {
        sensitivity = Math.max(0.1f, sliderIndex / 6f);
    }


    private void loadMainSettings() {
        Preferences prefs = getPrefs();

        autosave = convertToBoolean(prefs.getInteger("autosave", 1));
        musicEnabled = prefs.getBoolean("music", false);
        askToEndTurn = convertToBoolean(prefs.getInteger("ask_to_end_turn", 0));
        cityNamesEnabled = convertToBoolean(prefs.getInteger("city_names", 0));
        soundEnabled = convertToBoolean(prefs.getInteger("sound", 0));

        MusicManager.getInstance().onMusicStatusChanged();
    }


    private Preferences getPrefs() {
        return Gdx.app.getPreferences("settings");
    }


    private boolean convertToBoolean(int value) {
        return value == 1;
    }


    private boolean getResumeButtonDefaultValue() {
        return YioGdxGame.IOS;

    }


    public boolean saveMainSettings() {
        Preferences prefs = getPrefs();

        prefs.putInteger("sound", convertToInteger(soundEnabled));
        prefs.putInteger("autosave", convertToInteger(autosave));
        prefs.putInteger("ask_to_end_turn", convertToInteger(askToEndTurn));
        prefs.putInteger("city_names", convertToInteger(cityNamesEnabled));
        prefs.putBoolean("music", musicEnabled);

        prefs.flush();

        return false;
    }


    public void saveMoreSettings() {
        Preferences prefs = getPrefs();

        prefs.putInteger("skin", skinIndex);
        prefs.putInteger("sensitivity", (int) (sensitivity * 6));
        prefs.putBoolean("water_texture", waterTextureEnabled);
        prefs.putBoolean("long_tap_to_move", longTapToMove);
        prefs.putBoolean("replays_enabled", replaysEnabled);
        prefs.putBoolean("fast_construction", fastConstructionEnabled);
        prefs.putBoolean("left_hand_mode", leftHandMode);
        prefs.putBoolean("resume_button", resumeButtonEnabled);
        prefs.putBoolean("full_screen", fullScreenMode);

        prefs.flush();
    }


    public void setSkin(int index) {
        if (index == skinIndex) return;

        skinIndex = index;
        yioGdxGame.skinManager.onSkinChanged();
    }


    private int convertToInteger(boolean value) {
        if (value) {
            return 1;
        }
        return 0;
    }
}
