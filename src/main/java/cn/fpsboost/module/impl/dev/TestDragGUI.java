package cn.fpsboost.module.impl.dev;

import cn.fpsboost.module.Category;
import cn.fpsboost.module.Module;
import cn.fpsboost.screen.GuiDrag;
import org.lwjgl.input.Keyboard;

/**
 * @author LangYa466
 * @date 2025/5/24
 */
public class TestDragGUI extends Module {
    public TestDragGUI() {
        super(Category.DEV);
        keyCode = Keyboard.KEY_P;
    }

    @Override
    public void onEnable() {
        setEnabled(false);
    }

    @Override
    public void onDisable() {
        if (mc.thePlayer != null) {
            mc.displayGuiScreen(new GuiDrag());
        }
    }
}
