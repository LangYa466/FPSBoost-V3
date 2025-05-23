package net.fpsboost.module.impl.dev;

import net.fpsboost.module.Category;
import net.fpsboost.module.Module;
import net.fpsboost.screen.GuiDrag;

/**
 * @author LangYa466
 * @date 2025/5/24
 */
public class TestDragGUI extends Module {
    public TestDragGUI() {
        super(Category.DEV);
    }

    @Override
    public void onEnable() {
        if (mc.thePlayer != null) {
            mc.displayGuiScreen(new GuiDrag());
        }
        enabled = false;
    }
}
