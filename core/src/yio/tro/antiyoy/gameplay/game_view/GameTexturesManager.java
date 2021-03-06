package yio.tro.antiyoy.gameplay.game_view;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import yio.tro.antiyoy.SettingsManager;
import yio.tro.antiyoy.Storage3xTexture;
import yio.tro.antiyoy.gameplay.FieldController;
import yio.tro.antiyoy.gameplay.GameController;
import yio.tro.antiyoy.gameplay.Unit;
import yio.tro.antiyoy.gameplay.skins.SkinManager;
import yio.tro.antiyoy.menu.ButtonYio;
import yio.tro.antiyoy.stuff.AtlasLoader;
import yio.tro.antiyoy.stuff.GraphicsYio;

public class GameTexturesManager {

    GameView gameView;
    TextureRegion backgroundRegion;
    public TextureRegion blackCircleTexture, exclamationMarkTexture, forefingerTexture;
    TextureRegion animationTextureRegion, blackBorderTexture;
    TextureRegion hexGreen, hexRed, hexBlue, hexYellow, hexCyan, hexColor1, hexColor2, hexColor3;
    public TextureRegion blackPixel, grayPixel, selectionPixel, shadowHexTexture, gradientShadow, transCircle1, transCircle2, selUnitShadow;
    TextureRegion sideShadow, responseAnimHexTexture, selectionBorder, defenseIcon;
    public Storage3xTexture manTextures[], palmTexture, houseTexture, towerTexture, graveTexture, pineTexture;
    public Storage3xTexture castleTexture, strongTowerTexture, farmTexture[];
    public AtlasLoader atlasLoader;


    public GameTexturesManager(GameView gameView) {
        this.gameView = gameView;
    }


    public void loadTextures() {
        loadSkin();
        blackCircleTexture = GraphicsYio.loadTextureRegion("black_circle.png", true);
        shadowHexTexture = GraphicsYio.loadTextureRegion("shadow_hex.png", true);
        gradientShadow = GraphicsYio.loadTextureRegion("gradient_shadow.png", false);
        blackPixel = GraphicsYio.loadTextureRegion("black_pixel.png", false);
        transCircle1 = GraphicsYio.loadTextureRegion("transition_circle_1.png", false);
        transCircle2 = GraphicsYio.loadTextureRegion("transition_circle_2.png", false);
        loadFieldTextures();
        selUnitShadow = GraphicsYio.loadTextureRegion("sel_shadow.png", true);
        sideShadow = GraphicsYio.loadTextureRegion("money_shadow.png", true);
        responseAnimHexTexture = GraphicsYio.loadTextureRegion("response_anim_hex.png", false);
        loadExclamationMark();
        forefingerTexture = GraphicsYio.loadTextureRegion("forefinger.png", true);
        defenseIcon = GraphicsYio.loadTextureRegion("defense_icon.png", true);
        blackBorderTexture = GraphicsYio.loadTextureRegion("pixels/black_border.png", true);
        grayPixel = GraphicsYio.loadTextureRegion("pixels/gray_pixel.png", false);
    }


    private void loadHexTextures() {
        SkinManager skinManager = getSkinManager();
        hexGreen = skinManager.loadHexTexture("green");
        hexRed = skinManager.loadHexTexture("red");
        hexBlue = skinManager.loadHexTexture("blue");
        hexCyan = skinManager.loadHexTexture("cyan");
        hexYellow = skinManager.loadHexTexture("yellow");
        hexColor1 = skinManager.loadHexTexture("color1");
        hexColor2 = skinManager.loadHexTexture("color2");
        hexColor3 = skinManager.loadHexTexture("color3");
    }


    private void loadFieldTextures() {
        atlasLoader = getSkinManager().createAtlasLoader();
        selectionPixel = atlasLoader.getTexture("selection_pixel_lowest.png");
        manTextures = new Storage3xTexture[4];
        for (int i = 0; i < 4; i++) {
            manTextures[i] = new Storage3xTexture(atlasLoader, "man" + i + ".png");
        }
        graveTexture = new Storage3xTexture(atlasLoader, "grave.png");
        houseTexture = new Storage3xTexture(atlasLoader, "house.png");
        palmTexture = new Storage3xTexture(atlasLoader, "palm.png");
        pineTexture = new Storage3xTexture(atlasLoader, "pine.png");
        towerTexture = new Storage3xTexture(atlasLoader, "tower.png");
        castleTexture = new Storage3xTexture(atlasLoader, "castle.png");
        farmTexture = new Storage3xTexture[3];
        farmTexture[0] = new Storage3xTexture(atlasLoader, "farm1.png");
        farmTexture[1] = new Storage3xTexture(atlasLoader, "farm2.png");
        farmTexture[2] = new Storage3xTexture(atlasLoader, "farm3.png");
        strongTowerTexture = new Storage3xTexture(atlasLoader, "strong_tower.png");
    }


    public void loadBackgroundTexture() {
        if (SettingsManager.waterTextureEnabled || getSkinManager().currentSkinRequiresWater()) {
            backgroundRegion = GraphicsYio.loadTextureRegion(getSkinManager().getWaterTexturePath(), true);
        } else {
            backgroundRegion = GraphicsYio.loadTextureRegion("game_background.png", true);
        }
    }


    private void loadExclamationMark() {
        exclamationMarkTexture = getSkinManager().loadExclamationMark();
    }


    public void loadSkin() {
        loadBackgroundTexture();
        loadHexTextures();
        reloadTextures();
        gameView.rList.loadTextures();
        selectionBorder = GraphicsYio.loadTextureRegion(getSkinManager().getSelectionBorderTexturePath(), false);
    }


    private void reloadTextures() {
        loadFieldTextures();
        loadExclamationMark();

        resetButtonTexture(38); // tower (build)
        resetButtonTexture(39); // unit (build)
    }


    public void onMoreSettingsChanged() {
        loadBackgroundTexture();
        loadSkin();
    }


    private void resetButtonTexture(int id) {
        ButtonYio button = gameView.yioGdxGame.menuControllerYio.getButtonById(id);
        if (button != null) {
            button.resetTexture();
        }
    }


    TextureRegion getTransitionTexture() {
        if (animationTextureRegion == null) {
            return backgroundRegion;
        }

        return animationTextureRegion;
    }


    TextureRegion getHexTextureByColor(int colorIndex) {
        if (getGameController().colorIndexViewOffset > 0 && colorIndex != FieldController.NEUTRAL_LANDS_INDEX) {
            colorIndex = getGameController().getColorIndexWithOffset(colorIndex);
        }
        switch (colorIndex) {
            default:
            case 0:
                return hexGreen;
            case 1:
                return hexRed;
            case 2:
                return hexBlue;
            case 3:
                return hexCyan;
            case 4:
                return hexYellow;
            case 5:
                return hexColor1;
            case 6:
                return hexColor2;
            case 7:
                return hexColor3;
        }
    }


    private SkinManager getSkinManager() {
        return gameView.getSkinManager();
    }


    TextureRegion getUnitTexture(Unit unit) {
        if (!getGameController().isPlayerTurn() && unit.moveFactor.get() < 1 && unit.moveFactor.get() > 0.1) {
            return manTextures[unit.strength - 1].getLowest();
        }
        return manTextures[unit.strength - 1].getTexture(gameView.currentZoomQuality);
    }


    private GameController getGameController() {
        return gameView.yioGdxGame.gameController;
    }


    void disposeTextures() {
        backgroundRegion.getTexture().dispose();

        blackCircleTexture.getTexture().dispose();
        shadowHexTexture.getTexture().dispose();
        gradientShadow.getTexture().dispose();
        blackPixel.getTexture().dispose();
        transCircle1.getTexture().dispose();
        transCircle2.getTexture().dispose();
        selUnitShadow.getTexture().dispose();
        sideShadow.getTexture().dispose();
        responseAnimHexTexture.getTexture().dispose();
        selectionBorder.getTexture().dispose();
        forefingerTexture.getTexture().dispose();
        defenseIcon.getTexture().dispose();
        blackBorderTexture.getTexture().dispose();

        atlasLoader.disposeAtlasRegion();
        gameView.rList.disposeTextures();
    }
}
