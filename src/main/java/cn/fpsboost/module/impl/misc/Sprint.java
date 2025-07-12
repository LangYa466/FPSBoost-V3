package cn.fpsboost.module.impl.misc;

import cn.fpsboost.event.EventTarget;
import cn.fpsboost.event.impl.UpdateEvent;
import cn.fpsboost.mixins.KeyBindingAccessor;
import cn.fpsboost.module.Category;
import cn.fpsboost.module.Module;

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
        ((KeyBindingAccessor) mc.gameSettings.keyBindSprint).setPressed(true);
    }
}
