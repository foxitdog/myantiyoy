package yio.tro.antiyoy.gameplay.rules;

import yio.tro.antiyoy.gameplay.*;

public abstract class Ruleset {

    GameController gameController;


    public Ruleset(GameController gameController) {
        this.gameController = gameController;
    }


    /**
     * 是否可以长松树
     * @param hex
     * @return
     */
    public abstract boolean canSpawnPineOnHex(Hex hex);


    /**
     * 是否长棕榈树
     * @param hex
     * @return
     */
    public abstract boolean canSpawnPalmOnHex(Hex hex);


    /**
     * 添加单位
     * @param hex
     */
    public abstract void onUnitAdd(Hex hex);


    /**
     * 回合结束
     */
    public abstract void onTurnEnd();


    /**
     * 合并单位
     * @param unit1
     * @param unit2
     * @return
     */
    public abstract boolean canMergeUnits(Unit unit1, Unit unit2);


    /**
     * 地块收入
     * @param hex
     * @return
     */
    public abstract int getHexIncome(Hex hex);


    /**
     * 地块税收
     * @param hex
     * @return
     */
    public abstract int getHexTax(Hex hex);


    /**
     * 根据单位强度，花费多少税收
     * @param strength
     * @return
     */
    public abstract int getUnitTax(int strength);


    /**
     * 是否可以建造新的单位
     * @param province
     * @param strength
     * @return
     */
    public abstract boolean canBuildUnit(Province province, int strength);


    /**
     * 移动到下个点的行为
     * @param unit
     * @param hex
     */
    public abstract void onUnitMoveToHex(Unit unit, Hex hex);


    /**
     * 攻击地块
     * @param unitStrength
     * @param hex
     * @return
     */
    public abstract boolean canUnitAttackHex(int unitStrength, Hex hex);


    public abstract int getColorIndexWithOffset(int srcIndex);


    public int howManyTreesNearby(Hex hex) {
        if (!hex.active) return 0;
        int c = 0;
        for (int i = 0; i < 6; i++)
            if (hex.getAdjacentHex(i).containsTree()) c++;
        return c;
    }
}
