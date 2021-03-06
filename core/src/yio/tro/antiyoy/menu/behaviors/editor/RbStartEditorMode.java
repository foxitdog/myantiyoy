package yio.tro.antiyoy.menu.behaviors.editor;

import yio.tro.antiyoy.gameplay.editor.LevelEditor;
import yio.tro.antiyoy.menu.ButtonYio;
import yio.tro.antiyoy.menu.behaviors.Reaction;
import yio.tro.antiyoy.menu.scenes.Scenes;

/**
 * Created by yiotro on 27.11.2015.
 */
public class RbStartEditorMode extends Reaction {

    @Override
    public void perform(ButtonYio buttonYio) {
        LevelEditor levelEditor = getGameController(buttonYio).getLevelEditor();

        if (levelEditor.isCurrentSlotEmpty()) {
            Scenes.sceneEditorCreate.create();
        } else {
            levelEditor.loadSlot();
        }
    }
}
