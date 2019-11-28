package yio.tro.antiyoy.gameplay.fog_of_war;

import yio.tro.antiyoy.gameplay.Hex;
import yio.tro.antiyoy.stuff.GraphicsYio;
import yio.tro.antiyoy.stuff.PointYio;
import yio.tro.antiyoy.stuff.object_pool.ReusableYio;

public class FogPoint implements ReusableYio{

    FogOfWarManager fogOfWarManager;

    /*相对应的六边形块*/
    Hex hex;
    /**
     * 迷雾状态
     */
    public boolean status;
    /**
     * 位置点
     */
    public PointYio position;


    public FogPoint(FogOfWarManager fogOfWarManager) {
        this.fogOfWarManager = fogOfWarManager;

        position = new PointYio();
    }


    @Override
    public void reset() {
        hex = null;
        position.reset();
        status = true;
    }


    /**
     * 是否可见
     * @return
     */
    public boolean isVisible() {
        return status && fogOfWarManager.visibleArea.isPointInside(position, fogOfWarManager.fieldController.hexSize);
    }


    /**
     *
     * @param i
     * @param j
     */
    public void setHexByIndexes(int i, int j) {
        hex = fogOfWarManager.fieldController.getHex(i, j);

        fogOfWarManager.fieldController.updatePointByHexIndexes(position, i, j);
    }


    public void setStatus(boolean status) {
        this.status = status;
    }


    @Override
    public String toString() {
        return "[FogPoint: " +
                hex + " " + status +
                "]";
    }
}
