package yio.tro.antiyoy.gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import yio.tro.antiyoy.*;
import yio.tro.antiyoy.factor_yio.FactorYio;
import yio.tro.antiyoy.gameplay.diplomacy.DiplomacyManager;
import yio.tro.antiyoy.gameplay.fog_of_war.FogOfWarManager;
import yio.tro.antiyoy.gameplay.game_view.GameView;
import yio.tro.antiyoy.gameplay.rules.GameRules;
import yio.tro.antiyoy.menu.scenes.Scenes;
import yio.tro.antiyoy.stuff.GraphicsYio;
import yio.tro.antiyoy.stuff.PointYio;
import yio.tro.antiyoy.stuff.Yio;

import java.util.ArrayList;
import java.util.ListIterator;

public class FieldController {

    public final GameController gameController;
    public static int NEUTRAL_LANDS_INDEX = 7;
    public boolean letsCheckAnimHexes;
    /**
     * 六边形的边长
     */
    public float hexSize;

    /**
     * 六边形的高
     */
    public float hexhight;

    /**
     * 相邻六边形间距离
     */
    public float hexStep2;
    public Hex field[][];
    public ArrayList<Hex> activeHexes;
    public ArrayList<Hex> selectedHexes;
    public ArrayList<Hex> animHexes;
    /**
     * field的宽度，也就是x轴的最大数
     */
    public int fWidth;
    /**
     * field的高度，也就是最大y的数值
     */
    public int fHeight;
    public PointYio fieldPos;
    public float cos60;
    public float sin60;
    public Hex focusedHex;
    public Hex nullHex;
    public Hex responseAnimHex;
    public Hex defTipHex;
    public ArrayList<Hex> solidObjects;
    public ArrayList<Hex> defenseTips;
    public FactorYio responseAnimFactor;
    public FactorYio defenseTipFactor;
    public ArrayList<Province> provinces;
    public Province selectedProvince;
    public int selectedProvinceMoney;
    public long timeToCheckAnimHexes;
    public int[] playerHexCount;
    public float compensatoryOffset; // fix for widescreen
    public FogOfWarManager fogOfWarManager;
    public DiplomacyManager diplomacyManager;
    public String initialLevelString;
    public MoveZoneManager moveZoneManager;
    private ArrayList<Hex> tempList;
    private ArrayList<Hex> propagationList;


    public FieldController(GameController gameController) {
        this.gameController = gameController;

        cos60 = (float) Math.cos(Math.PI / 3d);
        sin60 = (float) Math.sin(Math.PI / 3d);
        fieldPos = new PointYio();
        compensatoryOffset = 0;
        updateFieldPos();

        hexSize = 0.05f * Gdx.graphics.getWidth(); // radius

        hexhight = (float) Math.sqrt(3) * hexSize; // height

        hexStep2 = (float) Yio.distance(0, 0, 1.5 * hexSize, 0.5 * hexhight);

        fWidth = 85;

        fHeight = 55;

        activeHexes = new ArrayList<>();

        selectedHexes = new ArrayList<>();

        animHexes = new ArrayList<>();

        solidObjects = new ArrayList<>();

        moveZoneManager = new MoveZoneManager(this);
        field = new Hex[fWidth][fHeight];
        responseAnimFactor = new FactorYio();
        provinces = new ArrayList<>();
        nullHex = new Hex(-1, -1, new PointYio(), this);
        nullHex.active = false;
        defenseTipFactor = new FactorYio();
        defenseTips = new ArrayList<>();
        fogOfWarManager = new FogOfWarManager(this);
        diplomacyManager = new DiplomacyManager(this);
        initialLevelString = null;
        tempList = new ArrayList<>();
        propagationList = new ArrayList<>();
    }


    private void updateFieldPos() {
        fieldPos.y = -1.1f * GraphicsYio.height + compensatoryOffset;
    }


    public void updateHexInsideLevelStatuses() {
        for (int i = 0; i < fWidth; i++) {
            for (int j = 0; j < fHeight; j++) {
                field[i][j].updateCanContainsObjects();
            }
        }
    }


    public void clearField() {
        gameController.selectionManager.setSelectedUnit(null);
        solidObjects.clear();
        gameController.getUnitList().clear();
        clearProvincesList();
        moveZoneManager.clear();
        clearActiveHexesList();
    }


    public void cleanOutAllHexesInField() {
        for (int i = 0; i < fWidth; i++) {
            for (int j = 0; j < fHeight; j++) {
                if (!gameController.fieldController.field[i][j].active) continue;
                gameController.cleanOutHex(gameController.fieldController.field[i][j]);
            }
        }
    }


    public void clearProvincesList() {
        provinces.clear();
    }


    public void defaultValues() {
        selectedProvince = null;
        moveZoneManager.defaultValues();
        compensatoryOffset = 0;
    }


    public void clearActiveHexesList() {
        ListIterator listIterator = activeHexes.listIterator();
        while (listIterator.hasNext()) {
            listIterator.next();
            listIterator.remove();
        }
    }


    public void createField() {
        clearField();
        updateFieldPos();
    }


    public void generateMap() {
        generateMap(GameRules.slayRules);
    }


    public void generateMap(boolean slayRules) {
        if (slayRules) {
            gameController.getMapGeneratorSlay().generateMap(gameController.getPredictableRandom(), field);
        } else {
            gameController.getMapGeneratorGeneric().generateMap(gameController.getPredictableRandom(), field);
        }

        detectProvinces();
        gameController.selectionManager.deselectAll();
        detectNeutralLands();
        gameController.takeAwaySomeMoneyToAchieveBalance();
    }


    public void detectNeutralLands() {
        if (GameRules.slayRules) return;

        for (Hex activeHex : activeHexes) {
            activeHex.genFlag = false;
        }

        for (Province province : provinces) {
            for (Hex hex : province.hexList) {
                hex.genFlag = true;
            }
        }

        for (Hex activeHex : activeHexes) {
            if (activeHex.genFlag) continue;

            activeHex.setColorIndex(NEUTRAL_LANDS_INDEX);
        }
    }


    public void killUnitByStarvation(Hex hex) {
        cleanOutHex(hex);
        addSolidObject(hex, Obj.GRAVE);
        hex.animFactor.appear(1, 2);

        gameController.replayManager.onUnitDiedFromStarvation(hex);
    }


    public void killEveryoneByStarvation(Province province) {
        for (Hex hex : province.hexList) {
            if (hex.containsUnit()) {
                killUnitByStarvation(hex);
            }
        }
    }


    public void moveResponseAnimHex() {
        if (responseAnimHex != null) {
            responseAnimFactor.move();
            if (responseAnimFactor.get() < 0.01) responseAnimHex = null;
        }
    }


    public void moveAnimHexes() {
        for (Hex hex : animHexes) {
            if (!hex.selected) hex.move(); // to prevent double call of move()
            if (!letsCheckAnimHexes && hex.animFactor.get() > 0.99) {
                letsCheckAnimHexes = true;
            }

            // animation is off because it's buggy
            if (hex.animFactor.get() < 1) hex.animFactor.setValues(1, 0);
        }
    }


    public boolean isThereOnlyOneKingdomOnMap() {
        // kingdom can be multiple provinces of same color
        int activeColor = -1;
        for (Province province : provinces) {
            if (province.hexList.get(0).isNeutral()) continue;

            if (activeColor == -1) {
                activeColor = province.getColor();
                continue;
            }

            if (province.getColor() != activeColor) {
                return false;
            }
        }

        return true;
    }


    public int numberOfDifferentActiveProvinces() {
        int c = 0;
        for (Province province : provinces) {
            if (province.hexList.get(0).isNeutral()) continue;
            c++;
        }
        return c;
    }


    public int[] getPlayerHexCount() {
        for (int i = 0; i < playerHexCount.length; i++) {
            playerHexCount[i] = 0;
        }

        for (Hex activeHex : activeHexes) {
            if (activeHex.isNeutral()) continue;
            if (activeHex.isInProvince() && activeHex.colorIndex >= 0 && activeHex.colorIndex < playerHexCount.length) {
                playerHexCount[activeHex.colorIndex]++;
            }
        }

        return playerHexCount;
    }


    public String getFullLevelString() {
        //        detectProvinces();
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(getBasicInfoString());
        stringBuffer.append("/");
        stringBuffer.append(gameController.gameSaver.getActiveHexesString());
        return stringBuffer.toString();
    }


    private String getBasicInfoString() {
        StringBuilder builder = new StringBuilder();
        builder.append(GameRules.difficulty).append(" ");
        builder.append(getLevelSize()).append(" ");
        builder.append(gameController.playersNumber).append(" ");
        builder.append(GameRules.colorNumber).append("");
        return builder.toString();
    }


    public int getLevelSize() {
        return gameController.levelSizeManager.levelSize;
    }


    private boolean checkRefuseStatistics() {
        RefuseStatistics instance = RefuseStatistics.getInstance();

        int sum = instance.refusedEarlyGameEnd + instance.acceptedEarlyGameEnd;
        if (sum < 5) return true;

        double ratio = (double) instance.acceptedEarlyGameEnd / (double) sum;

        if (ratio < 0.1) return false;

        return true;
    }


    public int possibleWinner() {
        if (!checkRefuseStatistics()) return -1;

        int numberOfAllHexes = activeHexes.size();
        //        for (Province province : provinces) {
        //            if (province.hexList.size() > 0.52 * numberOfAllHexes) {
        //                return province.getColor();
        //            }
        //        }

        int playerHexCount[] = getPlayerHexCount();
        for (int i = 0; i < playerHexCount.length; i++) {
            if (playerHexCount[i] > 0.7 * numberOfAllHexes) {
                return i;
            }
        }

        return -1;
    }


    public boolean hasAtLeastOneProvince() {
        return provinces.size() > 0;
    }


    public int numberOfProvincesWithColor(int color) {
        int count = 0;
        for (Province province : provinces) {
            if (province.getColor() == color)
                count++;
        }
        return count;
    }


    public void transformGraves() {
        for (Hex hex : activeHexes) {
            if (gameController.isCurrentTurn(hex.colorIndex) && hex.objectInside == Obj.GRAVE) {
                spawnTree(hex);
                hex.blockToTreeFromExpanding = true;
            }
        }
    }


    public void detectProvinces() {
        if (gameController.isInEditorMode()) return;

        clearProvincesList();
        MoveZoneDetection.unFlagAllHexesInArrayList(activeHexes);
        tempList.clear();
        propagationList.clear();

        for (Hex hex : activeHexes) {
            if (hex.isNeutral()) continue;
            if (hex.flag) continue;

            tempList.clear();
            propagationList.clear();
            propagationList.add(hex);
            hex.flag = true;
            propagateHex(tempList, propagationList);
            if (tempList.size() >= 2) {
                Province province = new Province(gameController, tempList);
                addProvince(province);
            }
        }

        for (Province province : provinces) {
            if (province.hasCapital()) continue;

            province.placeCapitalInRandomPlace(gameController.predictableRandom);
        }
    }


    public void tryToDetectAddiotionalProvinces() {
        // this method doesn't erase already existing provinces, it just adds new ones

        if (gameController.isInEditorMode()) return;

        MoveZoneDetection.unFlagAllHexesInArrayList(activeHexes);
        tempList.clear();
        propagationList.clear();

        for (Hex hex : activeHexes) {
            if (hex.isNeutral()) continue;
            if (hex.flag) continue;
            if (getProvinceByHex(hex) != null) continue;

            tempList.clear();
            propagationList.clear();
            propagationList.add(hex);
            hex.flag = true;
            propagateHex(tempList, propagationList);
            if (tempList.size() >= 2) {
                Province province = new Province(gameController, tempList);
                addProvince(province);
            }
        }

        for (Province province : provinces) {
            if (!province.hasCapital()) {
                province.placeCapitalInRandomPlace(gameController.predictableRandom);
            }
        }
    }


    private void propagateHex(ArrayList<Hex> tempList, ArrayList<Hex> propagationList) {
        Hex tempHex;
        Hex adjHex;
        while (propagationList.size() > 0) {
            tempHex = propagationList.get(0);
            tempList.add(tempHex);
            propagationList.remove(0);
            for (int dir = 0; dir < 6; dir++) {
                adjHex = tempHex.getAdjacentHex(dir);

                if (!adjHex.active) continue;
                if (!adjHex.sameColor(tempHex)) continue;
                if (adjHex.flag) continue;

                propagationList.add(adjHex);
                adjHex.flag = true;
            }
        }
    }


    public void forceAnimEndInHex(Hex hex) {
        hex.animFactor.setValues(1, 0);
    }


    public int howManyPalms() {
        int c = 0;
        for (Hex activeHex : activeHexes) {
            if (activeHex.objectInside == Obj.PALM) c++;
        }
        return c;
    }


    public void expandTrees() {
        if (GameRules.replayMode) return;

        ArrayList<Hex> newPalmsList = getNewPalmsList();
        ArrayList<Hex> newPinesList = getNewPinesList();

        for (int i = newPalmsList.size() - 1; i >= 0; i--) {
            spawnPalm(newPalmsList.get(i));
        }

        for (int i = newPinesList.size() - 1; i >= 0; i--) {
            spawnPine(newPinesList.get(i));
        }

        for (Hex activeHex : activeHexes) {
            if (activeHex.containsTree() && activeHex.blockToTreeFromExpanding) {
                activeHex.blockToTreeFromExpanding = false;
            }
        }
    }


    private ArrayList<Hex> getNewPinesList() {
        ArrayList<Hex> newPinesList = new ArrayList<Hex>();

        for (Hex hex : activeHexes) {
            if (gameController.ruleset.canSpawnPineOnHex(hex)) {
                newPinesList.add(hex);
            }
        }

        return newPinesList;
    }


    private ArrayList<Hex> getNewPalmsList() {
        ArrayList<Hex> newPalmsList = new ArrayList<Hex>();

        for (Hex hex : activeHexes) {
            if (gameController.ruleset.canSpawnPalmOnHex(hex)) {
                newPalmsList.add(hex);
            }
        }

        return newPalmsList;
    }


    private void spawnPine(Hex hex) {
        if (!hex.canContainObjects) return;

        addSolidObject(hex, Obj.PINE);
        addAnimHex(hex);
        hex.animFactor.setValues(1, 0);
        gameController.replayManager.onPineSpawned(hex);
    }


    private void spawnPalm(Hex hex) {
        if (!hex.canContainObjects) return;

        addSolidObject(hex, Obj.PALM);
        addAnimHex(hex);
        hex.animFactor.setValues(1, 0);
        gameController.replayManager.onPalmSpawned(hex);
    }


    public void createPlayerHexCount() {
        playerHexCount = new int[GameRules.colorNumber];
    }


    public void checkAnimHexes() {
        // important
        // this fucking anims hexes have to live long enough
        // if killed too fast, graphic bugs will show
        if (gameController.isSomethingMoving()) {
            timeToCheckAnimHexes = gameController.getCurrentTime() + 100;
            return;
        }
        letsCheckAnimHexes = false;
        ListIterator iterator = animHexes.listIterator();
        while (iterator.hasNext()) {
            Hex h = (Hex) iterator.next();
            if (h.animFactor.get() > 0.99 && !(h.containsUnit() && h.unit.moveFactor.get() < 1) && System.currentTimeMillis() > h.animStartTime + 250) {
                h.changingColor = false;
                iterator.remove();
            }
        }
    }


    public boolean atLeastOneUnitIsReadyToMove() {
        int size = gameController.getUnitList().size();
        for (Unit unit : gameController.getUnitList()) {
            if (unit.isReadyToMove()) return true;
        }
        return false;
    }


    public int getPredictionForWinner() {
        int numbers[] = new int[GameRules.colorNumber];
        for (Hex activeHex : activeHexes) {
            if (activeHex.isNeutral()) continue;
            numbers[activeHex.colorIndex]++;
        }

        int max = numbers[0];
        int maxIndex = 0;
        for (int i = 0; i < numbers.length; i++) {
            if (numbers[i] > max) {
                max = numbers[i];
                maxIndex = i;
            }
        }

        return maxIndex;
    }


    public boolean areConditionsGoodForPlayer() {
        int numbers[] = new int[GameRules.colorNumber];
        for (Hex activeHex : activeHexes) {
            if (activeHex.isNeutral()) continue;
            numbers[activeHex.colorIndex]++;
        }

        int max = GameController.maxNumberFromArray(numbers);
        return max - numbers[0] < 2;
    }


    public void onEndCreation() {
        clearAnims();
        updateHexInsideLevelStatuses();
        defenseTips.clear();

        diplomacyManager.onEndCreation();
        fogOfWarManager.onEndCreation();
        updateInitialLevelString();
    }


    private void updateInitialLevelString() {
        initialLevelString = getFullLevelString();
    }


    public void clearAnims() {
        ListIterator iterator = animHexes.listIterator();
        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
    }


    public void createFieldMatrix() {
        for (int x = 0; x < fWidth; x++) {
            field[x] = new Hex[fHeight];
            for (int y = 0; y < fHeight; y++) {
                field[x][y] = new Hex(x, y, fieldPos, this);
                field[x][y].ignoreTouch = false;
            }
        }
    }


    public void marchUnitsToHex(Hex toWhere) {
        if (!gameController.selectionManager.isSomethingSelected()) return;
        if (!toWhere.isSelected()) return;
        if (selectedProvince.hasSomeoneReadyToMove()) {
            gameController.takeSnapshot();
            for (Hex hex : selectedProvince.hexList) {
                if (hex.containsUnit() && hex.unit.isReadyToMove()) {
                    hex.unit.marchToHex(toWhere, selectedProvince);
                }
            }
        }
        setResponseAnimHex(toWhere);
        SoundManagerYio.playSound(SoundManagerYio.soundHoldToMarch);
    }


    public void setResponseAnimHex(Hex hex) {
        responseAnimHex = hex;
        responseAnimFactor.setValues(1, 0.07);
        responseAnimFactor.destroy(1, 2);
    }


    public void selectAdjacentHexes(Hex startHex) {
        setSelectedProvince(startHex);
        if (selectedProvince == null) return;

        ListIterator listIterator = selectedHexes.listIterator();
        for (Hex hex : selectedProvince.hexList) {
            hex.select();
            if (!selectedHexes.contains(hex)) listIterator.add(hex);
        }
        showBuildOverlay();
        gameController.updateBalanceString();
    }


    private void showBuildOverlay() {
        if (SettingsManager.fastConstructionEnabled) {
            Scenes.sceneFastConstructionPanel.create();
        } else {
            Scenes.sceneSelectionOverlay.create();
        }
    }


    public void setSelectedProvince(Hex hex) {
        selectedProvince = getProvinceByHex(hex);
        if (selectedProvince == null) return;

        selectedProvinceMoney = selectedProvince.money;
        gameController.selectionManager.getSelMoneyFactor().setDy(0);
        gameController.selectionManager.getSelMoneyFactor().appear(3, 2);
    }


    public String getColorName(int colorIndex) {
        return gameController.yioGdxGame.menuControllerYio.getColorNameByIndexWithOffset(colorIndex, "_player");
    }


    public void updateHexPositions() {
        updateFieldPos();

        for (int i = 0; i < fWidth; i++) {
            for (int j = 0; j < fHeight; j++) {
                Hex hex = field[i][j];

                hex.updatePos();
                if (hex.containsUnit()) {
                    hex.unit.updateCurrentPos();
                }
            }
        }
    }


    /**
     * 根据游戏六边坐标系获取定位的6边型
     *
     * @param x
     * @param y
     * @return
     */
    public Hex getHexByPos(double x, double y) {
        int j = (int) ((x - fieldPos.x) / (hexStep2 * sin60));
        int i = (int) ((y - fieldPos.y - hexStep2 * j * cos60) / hexhight);
        if (i < 0 || i > fWidth - 1 || j < 0 || j > fHeight - 1) return null;

        Hex adjHex, resHex = field[i][j];
        x -= gameController.getYioGdxGame().gameView.hexViewSize;
        y -= gameController.getYioGdxGame().gameView.hexViewSize;

        double currentDistance, minDistance = Yio.distance(resHex.pos.x, resHex.pos.y, x, y);
        for (int k = 0; k < 6; k++) {
            adjHex = adjacentHex(field[i][j], k);
            if (adjHex == null || !adjHex.active) continue;
            currentDistance = Yio.distance(adjHex.pos.x, adjHex.pos.y, x, y);
            if (currentDistance < minDistance) {
                minDistance = currentDistance;
                resHex = adjHex;
            }
        }

        return resHex;
    }


    public Hex getHex(int i, int j) {
        if (i < 0 || i > fWidth - 1 || j < 0 || j > fHeight - 1) return null;

        return field[i][j];
    }


    /**
     * 指定点的相邻六边形  共6个方向（左上，上，右上，左下，下，右下）
     *
     * @param i         当前点的x坐标
     * @param j         当前点的y坐标
     * @param direction 方向
     * @return
     */
    public Hex adjacentHex(int i, int j, int direction) {
        switch (direction) {
            case 0:
                if (i >= fWidth - 1) return nullHex;
                return field[i + 1][j];
            case 1:
                if (j >= fHeight - 1) return nullHex;
                return field[i][j + 1];
            case 2:
                if (i <= 0 || j >= fHeight - 1) return nullHex;
                return field[i - 1][j + 1];
            case 3:
                if (i <= 0) return nullHex;
                return field[i - 1][j];
            case 4:
                if (j <= 0) return nullHex;
                return field[i][j - 1];
            case 5:
                if (i >= fWidth - 1 || j <= 0) return nullHex;
                return field[i + 1][j - 1];
            default:
                return nullHex;
        }
    }


    /**
     * 添加树
     *
     * @param hex
     */
    public void spawnTree(Hex hex) {
        if (!hex.active) return;
        if (hex.isNearWater()) addSolidObject(hex, Obj.PALM);
        else addSolidObject(hex, Obj.PINE);
    }


    /**
     * 添加固定对象，如 树，人，房子
     *
     * @param hex
     * @param type
     */
    public void addSolidObject(Hex hex, int type) {
        if (hex == null || !hex.active) return;
        if (hex.objectInside == type) return;
        if (!hex.canContainObjects) return;

        if (solidObjects.contains(hex)) {
            cleanOutHex(hex);
        }

        hex.setObjectInside(type);
        solidObjects.listIterator().add(hex);
    }


    /**
     * 清除点状态，如果该点有单位，则清除该单位
     *
     * @param hex
     */
    public void cleanOutHex(Hex hex) {
        if (hex.containsUnit()) {
            gameController.getMatchStatistics().onUnitKilled();
            gameController.getUnitList().remove(hex.unit);
            hex.unit = null;
        }
        hex.setObjectInside(0);
        addAnimHex(hex);
        ListIterator iterator = solidObjects.listIterator();
        while (iterator.hasNext()) {
            if (iterator.next() == hex) {
                iterator.remove();
                return;
            }
        }
    }


    public void destroyBuildingsOnHex(Hex hex) {
        boolean hadHouse = (hex.objectInside == Obj.TOWN);
        if (hex.containsBuilding()) cleanOutHex(hex);
        //        if (hex.containsUnit()) killUnitOnHex(hex);
        if (hadHouse) {
            spawnTree(hex);
        }
    }


    public boolean buildUnit(Province province, Hex hex, int strength) {
        if (province == null || hex == null) return false;

        if (!province.canBuildUnit(strength)) {
            if (gameController.isPlayerTurn()) {
                gameController.tickleMoneySign();
            }
            return false;
        }

        // check for unmergeable situation
        if (hex.sameColor(province) && hex.containsUnit() && !gameController.canMergeUnits(strength, hex.unit.strength)) {
            return false;
        }

        gameController.takeSnapshot();
        province.money -= GameRules.PRICE_UNIT * strength;
        gameController.getMatchStatistics().onMoneySpent(gameController.turn, GameRules.PRICE_UNIT * strength);
        gameController.replayManager.onUnitBuilt(province, hex, strength);
        updateSelectedProvinceMoney();

        if (isUnitBuildingPeaceful(province, hex)) { // build unit peacefully
            if (hex.containsUnit()) { // merge units
                Unit bUnit = new Unit(gameController, hex, strength);
                bUnit.setReadyToMove(true);
                gameController.matchStatistics.unitsDied++;
                gameController.mergeUnits(hex, bUnit, hex.unit);
            } else {
                addUnit(hex, strength);
            }
        } else { // attack
            setHexColor(hex, province.getColor()); // must be called before object in hex destroyed
            addUnit(hex, strength);
            hex.unit.setReadyToMove(false);
            hex.unit.stopJumping();
            province.addHex(hex);
            addAnimHex(hex);
            gameController.updateCacheOnceAfterSomeTime();
        }
        gameController.updateBalanceString();
        return true;
    }


    private boolean isUnitBuildingPeaceful(Province province, Hex hex) {
        return hex.sameColor(province);
    }


    public boolean buildTower(Province province, Hex hex) {
        if (province == null) return false;
        if (province.hasMoneyForTower()) {
            gameController.takeSnapshot();
            gameController.replayManager.onTowerBuilt(hex, false);
            addSolidObject(hex, Obj.TOWER);
            addAnimHex(hex);
            province.money -= GameRules.PRICE_TOWER;
            gameController.getMatchStatistics().onMoneySpent(gameController.turn, GameRules.PRICE_TOWER);
            updateSelectedProvinceMoney();
            gameController.updateCacheOnceAfterSomeTime();
            return true;
        }

        // can't build tower
        if (gameController.isPlayerTurn()) gameController.tickleMoneySign();
        return false;
    }


    public boolean buildStrongTower(Province province, Hex hex) {
        if (province == null) return false;

        if (province.hasMoneyForStrongTower()) {
            gameController.takeSnapshot();
            gameController.replayManager.onTowerBuilt(hex, true);
            addSolidObject(hex, Obj.STRONG_TOWER);
            addAnimHex(hex);
            province.money -= GameRules.PRICE_STRONG_TOWER;
            gameController.getMatchStatistics().onMoneySpent(gameController.turn, GameRules.PRICE_STRONG_TOWER);
            updateSelectedProvinceMoney();
            gameController.updateCacheOnceAfterSomeTime();
            return true;
        }

        // can't build tower
        if (gameController.isPlayerTurn()) gameController.tickleMoneySign();
        return false;
    }


    public boolean buildFarm(Province province, Hex hex) {
        if (province == null) return false;

        if (!hex.hasThisObjectNearby(Obj.TOWN) && !hex.hasThisObjectNearby(Obj.FARM)) {
            return false;
        }

        if (province.hasMoneyForFarm()) {
            gameController.takeSnapshot();
            gameController.replayManager.onFarmBuilt(hex);
            province.money -= province.getCurrentFarmPrice();
            gameController.getMatchStatistics().onMoneySpent(gameController.turn, province.getCurrentFarmPrice());
            addSolidObject(hex, Obj.FARM);
            addAnimHex(hex);
            updateSelectedProvinceMoney();
            gameController.updateCacheOnceAfterSomeTime();
            return true;
        }

        // can't build farm
        if (gameController.isPlayerTurn()) gameController.tickleMoneySign();
        return false;
    }


    public boolean buildTree(Province province, Hex hex) {
        if (province == null) return false;
        if (province.hasMoneyForTree()) {
            gameController.takeSnapshot();
            spawnTree(hex);
            addAnimHex(hex);
            province.money -= GameRules.PRICE_TREE;
            gameController.getMatchStatistics().onMoneySpent(gameController.turn, GameRules.PRICE_TREE);
            updateSelectedProvinceMoney();
            gameController.updateCacheOnceAfterSomeTime();
            return true;
        }

        // can't build tree
        if (gameController.isPlayerTurn()) gameController.tickleMoneySign();
        return false;
    }


    public void updateSelectedProvinceMoney() {
        if (selectedProvince != null)
            selectedProvinceMoney = selectedProvince.money;
        else selectedProvinceMoney = -1;
        gameController.updateBalanceString();
    }


    public Unit addUnit(Hex hex, int strength) {
        if (hex == null) return null;
        if (hex.containsObject()) {
            gameController.ruleset.onUnitAdd(hex);
            cleanOutHex(hex);
            gameController.updateCacheOnceAfterSomeTime();
            hex.addUnit(strength);
        } else {
            hex.addUnit(strength);
            if (gameController.isCurrentTurn(hex.colorIndex)) {
                hex.unit.setReadyToMove(true);
                hex.unit.startJumping();
            }
        }
        return hex.unit;
    }


    public void addProvince(Province province) {
        if (provinces.contains(province)) return;
        if (containsEqualProvince(province)) {
            System.out.println("Problem in FieldController.addProvince()");
            Yio.printStackTrace();
            return;
        }

        provinces.add(province);
    }


    public boolean containsEqualProvince(Province province) {
        for (Province p : provinces) {
            if (p.equals(province)) {
                return true;
            }
        }

        return false;
    }


    /**
     * 指定六边形的相邻六边形 共6个方向（左上，上，右上，左下，下，右下）
     * @param hex
     * @param direction
     * @return
     */
    public Hex adjacentHex(Hex hex, int direction) {
        return adjacentHex(hex.indexX, hex.indexY, direction);
    }


    public boolean hexHasSelectedNearby(Hex hex) {
        for (int i = 0; i < 6; i++)
            if (hex.getAdjacentHex(i).selected) return true;
        return false;
    }


    public static float distanceBetweenHexes(Hex one, Hex two) {
        PointYio pOne = one.getPos();
        PointYio pTwo = two.getPos();
        return (float) pOne.distanceTo(pTwo);
    }


    public boolean isSomethingSelected() {
        return selectedHexes.size() > 0;
    }


    public void giveMoneyToPlayerProvinces(int amount) {
        for (Province province : provinces) {
            if (province.getColor() == 0) {
                province.money += amount;
            }
        }
    }


    /**
     * 相邻点是否相同颜色
     *
     * @param hex
     * @param color
     * @return
     */
    public boolean hexHasNeighbourWithColor(Hex hex, int color) {
        Hex neighbour;
        for (int i = 0; i < 6; i++) {
            neighbour = hex.getAdjacentHex(i);
            if (neighbour != null && neighbour.active && neighbour.sameColor(color)) return true;
        }
        return false;
    }


    public void addAnimHex(Hex hex) {
        if (animHexes.contains(hex)) return;
        if (DebugFlags.testMode) return;

        animHexes.listIterator().add(hex);

        hex.animFactor.setValues(0, 0);
        hex.animFactor.appear(1, 1);
        hex.animStartTime = System.currentTimeMillis();

        gameController.updateCacheOnceAfterSomeTime();
    }


    public Province findProvinceCopy(Province src) {
        Province result;
        for (Hex hex : src.hexList) {
            result = getProvinceByHex(hex);
            if (result == null) continue;
            return result;
        }
        return null;
    }


    public Province findProvince(int color) {
        for (Province province : provinces) {
            if (province.getColor() != color) continue;

            return province;
        }

        return null;
    }


    public Province getRandomProvince() {
        int index = YioGdxGame.random.nextInt(provinces.size());
        return provinces.get(index);
    }


    public void checkToFocusCameraOnCurrentPlayer() {
        if (gameController.playersNumber < 2) return;
        if (!gameController.isPlayerTurn()) return;

        Province province = findProvince(gameController.turn);
        if (province == null) return;

        province.focusCameraOnThis();
    }


    public Province getProvinceByHex(Hex hex) {
        for (Province province : provinces) {
            if (province.containsHex(hex)) {
                return province;
            }
        }

        return null;
    }


    public Hex getRandomActivehex() {
        int index = YioGdxGame.random.nextInt(activeHexes.size());
        return activeHexes.get(index);
    }


    public Province getMaxProvinceFromList(ArrayList<Province> list) {
        if (list.size() == 0) return null;
        Province max, temp;
        max = list.get(0);
        for (int k = list.size() - 1; k >= 0; k--) {
            temp = list.get(k);
            if (temp.hexList.size() > max.hexList.size()) max = temp;
        }
        return max;
    }


    public void splitProvince(Hex hex, int color) {
        Province oldProvince = getProvinceByHex(hex);
        if (oldProvince == null) return;
        MoveZoneDetection.unFlagAllHexesInArrayList(oldProvince.hexList);
        tempList.clear();
        propagationList.clear();
        ArrayList<Province> provincesAdded = new ArrayList<Province>();
        Hex startHex, tempHex, adjHex;
        hex.flag = true;
        gameController.getPredictableRandom().setSeed(hex.indexX + hex.indexY);
        for (int k = 0; k < 6; k++) {
            startHex = hex.getAdjacentHex(k);
            if (!startHex.active || startHex.colorIndex != color || startHex.flag) continue;
            tempList.clear();
            propagationList.clear();
            propagationList.add(startHex);
            startHex.flag = true;
            while (propagationList.size() > 0) {
                tempHex = propagationList.get(0);
                tempList.add(tempHex);
                propagationList.remove(0);
                for (int i = 0; i < 6; i++) {
                    adjHex = tempHex.getAdjacentHex(i);
                    if (adjHex.active && adjHex.sameColor(tempHex) && !adjHex.flag) {
                        propagationList.add(adjHex);
                        adjHex.flag = true;
                    }
                }
            }
            if (tempList.size() >= 2) {
                Province province = new Province(gameController, tempList);
                province.money = 0;
                if (!province.hasCapital()) {
                    province.placeCapitalInRandomPlace(gameController.getPredictableRandom());
                }
                addProvince(province);
                provincesAdded.add(province);
            } else {
                destroyBuildingsOnHex(startHex);
            }
        }
        if (provincesAdded.size() > 0 && !(hex.objectInside == Obj.TOWN)) {
            getMaxProvinceFromList(provincesAdded).money = oldProvince.money;
        }
        removeProvince(oldProvince);
        diplomacyManager.updateEntityAliveStatus(color);
    }


    public void checkToUniteProvinces(Hex hex) {
        ArrayList<Province> adjacentProvinces = new ArrayList<Province>();
        Province p;
        for (int i = 0; i < 6; i++) {
            p = getProvinceByHex(hex.getAdjacentHex(i));
            if (p != null && hex.sameColor(p) && !adjacentProvinces.contains(p)) adjacentProvinces.add(p);
        }
        if (adjacentProvinces.size() >= 2) {
            int sum = 0;
            Hex capital = getMaxProvinceFromList(adjacentProvinces).getCapital();
            ArrayList<Hex> hexArrayList = new ArrayList<Hex>();
            //            YioGdxGame.say("uniting provinces: " + adjacentProvinces.size());
            for (Province province : adjacentProvinces) {
                sum += province.money;
                hexArrayList.addAll(province.hexList);
                removeProvince(province);
            }
            Province unitedProvince = new Province(gameController, hexArrayList);
            unitedProvince.money = sum;
            unitedProvince.setCapital(capital);
            addProvince(unitedProvince);
        }
    }


    private void removeProvince(Province province) {
        provinces.remove(province);
    }


    public void joinHexToAdjacentProvince(Hex hex) {
        Province p;
        for (int i = 0; i < 6; i++) {
            p = getProvinceByHex(hex.getAdjacentHex(i));
            if (p != null && hex.sameColor(p)) {
                p.addHex(hex);
                Hex h;
                for (int j = 0; j < 6; j++) {
                    h = adjacentHex(hex, j);
                    if (h.active && h.sameColor(hex) && getProvinceByHex(h) == null) p.addHex(h);
                }
                return;
            }
        }
    }


    /**
     * 根据六边形的坐标点设置实际对应的画布的位置
     * @param pointYio 实际坐标点
     * @param index1 x
     * @param index2 y
     */
    public void updatePointByHexIndexes(PointYio pointYio, int index1, int index2) {
        pointYio.x = fieldPos.x + hexStep2 * index2 * sin60;
        pointYio.y = fieldPos.y + hexhight * index1 + hexStep2 * index2 * cos60;
    }


    public void setHexColor(Hex hex, int color) {
        cleanOutHex(hex);
        int oldColor = hex.colorIndex;
        hex.setColorIndex(color);
        splitProvince(hex, oldColor);
        checkToUniteProvinces(hex);
        joinHexToAdjacentProvince(hex);
        ListIterator animIterator = animHexes.listIterator();

        for (int dir = 0; dir < 6; dir++) {
            Hex adj = hex.getAdjacentHex(dir);
            if (adj != null && adj.active && adj.sameColor(hex)) {
                if (!animHexes.contains(adj)) {
                    animIterator.add(adj);
                }
                if (!adj.changingColor) {
                    adj.animFactor.setValues(1, 0);
                }
            }
        }
        hex.changingColor = true;
        if (!animHexes.contains(hex)) animIterator.add(hex);
        hex.animFactor.setValues(0, 0);
        hex.animFactor.appear(1, 1);

        if (!gameController.isPlayerTurn()) {
            forceAnimEndInHex(hex);
        }
    }


    /**
     * 查找聚焦的地块
     */
    public void updateFocusedHex() {
        updateFocusedHex(gameController.touchPoint.x, gameController.touchPoint.y);
    }


    public void updateFocusedHex(float screenX, float screenY) {
        OrthographicCamera orthoCam = gameController.cameraController.orthoCam;
        SelectionManager selectionManager = gameController.selectionManager;

        selectionManager.selectX = (screenX - 0.5f * GraphicsYio.width) * orthoCam.zoom + orthoCam.position.x;
        selectionManager.selectY = (screenY - 0.5f * GraphicsYio.height) * orthoCam.zoom + orthoCam.position.y;
        gameController.convertedTouchPoint.set(selectionManager.selectX, selectionManager.selectY);

        GameView gameView = gameController.getYioGdxGame().gameView;
        float x = selectionManager.selectX + gameView.hexViewSize;
        float y = selectionManager.selectY + gameView.hexViewSize;

        focusedHex = getHexByPos(x, y);

    }


}