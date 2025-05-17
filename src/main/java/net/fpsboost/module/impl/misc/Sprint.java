package net.fpsboost.module.impl.misc;

import com.cubk.event.annotations.EventTarget;
import net.fpsboost.events.UpdateEvent;
import net.fpsboost.module.Category;
import net.fpsboost.module.Module;

/**
 * @author LangYa466
 * @date 2025/5/17
 */
public class Sprint extends Module {
    public Sprint() {
        super("自动疾跑", Category.MISC);
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        mc.gameSettings.keyBindSprint.pressed = true;
    }
}
