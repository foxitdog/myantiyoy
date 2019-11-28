package yio.tro.antiyoy.gameplay;

import yio.tro.antiyoy.stuff.GraphicsYio;
import yio.tro.antiyoy.stuff.PointYio;

/**
 * 各地图大小等级的对应的边缘宽高
 */
public class LevelSizeManager {

    GameController gameController;
    /**
     * 地图大小
     */
    public int levelSize;
    /**
     * 边界宽
     */
    public float boundWidth;
    /**
     * 边界高
     */
    public float boundHeight;


    public LevelSizeManager(GameController gameController) {
        this.gameController = gameController;
    }


    public void updateBounds() {
        switch (levelSize) {
            default:
                System.out.println("LevelSizeManager.updateBounds(): problem");
                return;
            case LevelSize.SMALL:
                boundWidth = GraphicsYio.width;
                boundHeight = GraphicsYio.height;
                break;
            case LevelSize.MEDIUM:
                boundWidth = 2 * GraphicsYio.width;
                boundHeight = GraphicsYio.height;
                break;
            case LevelSize.BIG:
                boundWidth = 2 * GraphicsYio.width;
                boundHeight = 2 * GraphicsYio.height;
                break;
            case LevelSize.HUGE:
                boundWidth = 3 * GraphicsYio.width;
                boundHeight = 3 * GraphicsYio.height;
                break;
        }

        getCameraController().setBounds(boundWidth, boundHeight);
    }


    public boolean isPointInsideLevelBoundsWithOffset(PointYio pointYio, float offset) {
        // bigger offset -> bigger bounds
        if (pointYio.x < getFieldPos().x + getHexSize() / 2 - offset) return false;
        if (pointYio.x > getFieldPos().x + boundWidth + offset) return false;
        if (pointYio.y < getHexSize() / 2 - offset) return false;
        if (pointYio.y > boundHeight + offset) return false;

        return true;
    }


    public boolean isPointInsideLevelBoundsHorizontally(PointYio pointYio) {
        if (pointYio.x < getFieldPos().x + getHexSize() / 2) return false;
        if (pointYio.x > getFieldPos().x + boundWidth) return false;

        return true;
    }


    private float getHexSize() {
        return getFieldController().hexSize;
    }


    private PointYio getFieldPos() {
        return getFieldController().fieldPos;
    }


    private FieldController getFieldController() {
        return gameController.fieldController;
    }


    public void setLevelSize(int levelSize) {
        this.levelSize = levelSize;
        getCameraController().init(levelSize);
        updateBounds();
        gameController.yioGdxGame.gameView.createLevelCacheTextures();
    }


    private CameraController getCameraController() {
        return gameController.cameraController;
    }
}
