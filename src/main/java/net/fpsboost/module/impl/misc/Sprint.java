package net.fpsboost.module.impl.misc;

import net.fpsboost.event.EventTarget;
import net.fpsboost.event.impl.UpdateEvent;
import net.fpsboost.module.Category;
import net.fpsboost.module.Module;

/**
 * @author LangYa466
 * @date 2025/5/17
 */
public class Sprint extends Module {
    public Sprint() {
        super(Category.MISC);
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        mc.gameSettings.keyBindSprint.pressed = true;
    }
}
