package yio.tro.antiyoy.menu.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import yio.tro.antiyoy.ai.Difficulty;
import yio.tro.antiyoy.gameplay.rules.GameRules;
import yio.tro.antiyoy.menu.Animation;
import yio.tro.antiyoy.menu.behaviors.Reaction;
import yio.tro.antiyoy.menu.ButtonYio;
import yio.tro.antiyoy.menu.MenuControllerYio;
import yio.tro.antiyoy.menu.slider.SliderBehavior;
import yio.tro.antiyoy.menu.slider.SliderYio;
import yio.tro.antiyoy.stuff.GraphicsYio;
import yio.tro.antiyoy.stuff.LanguagesManager;
import yio.tro.antiyoy.stuff.RectangleYio;

import java.util.ArrayList;

/**
 * 遭遇战
 */
public class  SceneSkirmishMenu extends AbstractScene {


    public ButtonYio startButton;
    private ButtonYio topLabel;
    private ButtonYio bottomLabel;
    ArrayList<SliderYio> sliders;
    public SliderYio difficultySlider;
    public SliderYio mapSizeSlider;
    public SliderYio playersSlider;
    public SliderYio colorsSlider;
    private ButtonYio moreButton;


    public SceneSkirmishMenu(MenuControllerYio menuControllerYio) {
        super(menuControllerYio);

        sliders = null;
    }


    public void saveValues() {
        Preferences prefs = Gdx.app.getPreferences("skirmish");

        prefs.putInteger("difficulty", difficultySlider.getValueIndex());
        prefs.putInteger("map_size", mapSizeSlider.getValueIndex());
        prefs.putInteger("player_number", playersSlider.getValueIndex());
        prefs.putInteger("color_number", colorsSlider.getValueIndex());

        prefs.flush();
    }


    public void loadValues() {
        Preferences prefs = Gdx.app.getPreferences("skirmish");

        difficultySlider.setValueIndex(prefs.getInteger("difficulty", 1));
        mapSizeSlider.setValueIndex(prefs.getInteger("map_size", 1));
        colorsSlider.setValueIndex(prefs.getInteger("color_number", 2));
        playersSlider.setValueIndex(prefs.getInteger("player_number", 1));
    }


    @Override
    public void create() {
        menuControllerYio.beginMenuCreation();

        menuControllerYio.getYioGdxGame().beginBackgroundChange(2, true, true);

        createLabels();
        createSliders();

        createBackButton();
        createStartButton();
        createMoreButton();

        loadValues();

        menuControllerYio.endMenuCreation();
    }


    private void createSliders() {
        if (sliders == null) {
            initSliders();
        }

        for (SliderYio slider : sliders) {
            slider.appear();
        }
    }


    private void initSliders() {
        sliders = new ArrayList<>();
        double sWidth = 0.6;
        RectangleYio pos = generateRectangle((1 - sWidth) / 2, 0, sWidth, 0);

        difficultySlider = new SliderYio(menuControllerYio, -1);
        difficultySlider.setValues(0.33, 1, 5, Animation.NONE);
        difficultySlider.setPosition(pos);
        difficultySlider.setParentElement(topLabel, 0.2);
        difficultySlider.setTitle("difficulty");
        difficultySlider.setBehavior(new SliderBehavior() {
            @Override
            public String getValueString(SliderYio sliderYio) {
                return getDifficultyStringBySliderIndex(sliderYio.getValueIndex());
            }
        });
        sliders.add(difficultySlider);

        mapSizeSlider = new SliderYio(menuControllerYio, -1);
        mapSizeSlider.setValues(0.5, 1, 4, Animation.NONE);
        mapSizeSlider.setPosition(pos);
        mapSizeSlider.setParentElement(topLabel, 0.05);
        mapSizeSlider.setTitle("map_size");
        mapSizeSlider.setBehavior(new SliderBehavior() {
            @Override
            public String getValueString(SliderYio sliderYio) {
                return getMapSizeStringBySliderIndex(sliderYio.getValueIndex());
            }
        });
        sliders.add(mapSizeSlider);

        playersSlider = new SliderYio(menuControllerYio, -1);
        playersSlider.setValues(0.2, 0, 5, Animation.NONE);
        playersSlider.setPosition(pos);
        playersSlider.setParentElement(bottomLabel, 0.24);
        playersSlider.setTitle("player_number");
        playersSlider.setBehavior(new SliderBehavior() {
            @Override
            public String getValueString(SliderYio sliderYio) {
                return getHumansString(sliderYio.getValueIndex());
            }


            @Override
            public void onAnotherSliderValueChanged(SliderYio sliderYio, SliderYio anotherSlider) {
                int currentRunnerIndex = sliderYio.getValueIndex();
                sliderYio.setNumberOfSegments(anotherSlider.getValueIndex() + anotherSlider.getMinNumber());
                sliderYio.setValueIndex(currentRunnerIndex);
            }
        });
        sliders.add(playersSlider);

        colorsSlider = new SliderYio(menuControllerYio, -1);
        colorsSlider.setValues(0.6, 2, 6, Animation.NONE);
        colorsSlider.setPosition(pos);
        colorsSlider.setParentElement(bottomLabel, 0.09);
        colorsSlider.setTitle("color_number");
        colorsSlider.setBehavior(new SliderBehavior() {
            @Override
            public String getValueString(SliderYio sliderYio) {
                int currentRunnerIndex = sliderYio.getValueIndex();
                if (currentRunnerIndex + sliderYio.getMinNumber() <= 4) {
                    return (currentRunnerIndex + sliderYio.getMinNumber()) + " " + LanguagesManager.getInstance().getString("color");
                } else {
                    return (currentRunnerIndex + sliderYio.getMinNumber()) + " " + LanguagesManager.getInstance().getString("colors");
                }
            }


            @Override
            public void onAnotherSliderValueChanged(SliderYio sliderYio, SliderYio anotherSlider) {
                int s = 3;
                if (anotherSlider.getValueIndex() == 1) {
                    s = GameRules.MAX_COLOR_NUMBER - 3;
                }
                if (anotherSlider.getValueIndex() >= 2) {
                    s = GameRules.MAX_COLOR_NUMBER - 2;
                }
                int currentRunnerIndex = sliderYio.getValueIndex();
                sliderYio.setNumberOfSegments(s);
                sliderYio.setValueIndex(currentRunnerIndex);
            }
        });
        sliders.add(colorsSlider);

        for (SliderYio slider : sliders) {
            menuControllerYio.addElementToScene(slider);
            slider.setVerticalTouchOffset(0.05f * GraphicsYio.height);
        }

        colorsSlider.addListener(playersSlider);
        mapSizeSlider.addListener(colorsSlider);
    }


    private void createLabels() {
        topLabel = buttonFactory.getButton(generateRectangle(0.1, 0.5, 0.8, 0.32), 88, " ");
        topLabel.setTouchable(false);
        topLabel.setAnimation(Animation.FIXED_UP);

        bottomLabel = buttonFactory.getButton(generateRectangle(0.1, 0.08, 0.8, 0.36), 87, " ");
        bottomLabel.setTouchable(false);
        bottomLabel.setAnimation(Animation.FIXED_DOWN);
    }


    private void createMoreButton() {
        moreButton = buttonFactory.getButton(generateRectangle(0.56, 0.08, 0.3, 0.04), 86, getString("more"));
        moreButton.setReaction(Reaction.rbMoreSkirmishOptions);
        moreButton.setAnimation(Animation.FIXED_DOWN);
        moreButton.setShadow(false);
        moreButton.setTouchOffset(0.1f * GraphicsYio.width);
        moreButton.disableTouchAnimation();
    }


    private void createStartButton() {
        startButton = buttonFactory.getButton(generateRectangle(0.55, 0.9, 0.4, 0.07), 83, getString("game_settings_start"));
        startButton.setReaction(Reaction.rbStartSkirmishGame);
        startButton.setAnimation(Animation.UP);
        startButton.disableTouchAnimation();
    }


    private void createBackButton() {
        ButtonYio backButton = menuControllerYio.spawnBackButton(80, Reaction.rbBackFromSkirmish);
        backButton.setTouchable(true);
    }


    public static String getHumansString(int n) {
        LanguagesManager instance = LanguagesManager.getInstance();

        if (n == 0) {
            return instance.getString("ai_only");
        } else if (n == 1) {
            return instance.getString("single_player");
        } else {
            return instance.getString("multiplayer") + " " + (n) + "x";
        }
    }


    public static String getColorStringBySliderIndex(int sliderIndex) {
        switch (sliderIndex) {
            default:
            case 0:
                return LanguagesManager.getInstance().getString("random");
            case 1:
                return LanguagesManager.getInstance().getString("green_menu");
            case 2:
                return LanguagesManager.getInstance().getString("red_menu");
            case 3:
                return LanguagesManager.getInstance().getString("magenta_menu");
            case 4:
                return LanguagesManager.getInstance().getString("cyan_menu");
            case 5:
                return LanguagesManager.getInstance().getString("yellow_menu");
            case 6:
                return LanguagesManager.getInstance().getString("red_menu") + "+";
            case 7:
                return LanguagesManager.getInstance().getString("green_menu") + "+";
        }
    }


    public static String getDifficultyStringBySliderIndex(int sliderIndex) {
        switch (sliderIndex) {
            default:
            case Difficulty.EASY:
                return LanguagesManager.getInstance().getString("easy");
            case Difficulty.NORMAL:
                return LanguagesManager.getInstance().getString("normal");
            case Difficulty.HARD:
                return LanguagesManager.getInstance().getString("hard");
            case Difficulty.EXPERT:
                return LanguagesManager.getInstance().getString("expert");
            case Difficulty.BALANCER:
                return LanguagesManager.getInstance().getString("balancer");
        }
    }


    public static String getMapSizeStringBySliderIndex(int sliderIndex) {
        switch (sliderIndex) {
            default:
            case 0:
                return LanguagesManager.getInstance().getString("small");
            case 1:
                return LanguagesManager.getInstance().getString("medium");
            case 2:
                return LanguagesManager.getInstance().getString("big");
            case 3:
                return LanguagesManager.getInstance().getString("huge");
        }
    }
}