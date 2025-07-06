package net.fpsboost.module.impl.dev;

import net.fpsboost.module.Category;
import net.fpsboost.module.Module;
import net.fpsboost.screen.ClickGUI;
import net.fpsboost.screen.GuiDrag;
import org.lwjgl.input.Keyboard;

/**
 * @author LangYa466
 * @date 2025/5/24
 */
public class TestClickGUI extends Module {
    public TestClickGUI() {
        super(Category.DEV);
        keyCode = Keyboard.KEY_RSHIFT;
    }

    @Override
    public void onEnable() {
        setEnabled(false);
    }

    @Override
    public void onDisable() {
        if (mc.thePlayer != null) {
            mc.displayGuiScreen(ClickGUI.INSTANCE);
        }
    }
}
