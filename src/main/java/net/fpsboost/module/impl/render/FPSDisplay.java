package net.fpsboost.module.impl.render;

import com.cubk.event.annotations.EventTarget;
import net.fpsboost.events.Render2DEvent;
import net.fpsboost.module.Category;
import net.fpsboost.module.Module;
import net.fpsboost.util.drag.Drag;
import net.fpsboost.util.render.font.FontUtil;
import net.minecraft.client.Minecraft;

/**
 * @author LangYa466
 * @date 2025/5/17
 */
public class FPSDisplay extends Module implements FontUtil {
    private final Drag drag = createDrag();

    public FPSDisplay() {
        super("FPS显示", Category.RENDER);
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        drag.render(() -> {
            font18.drawStringWithShadow(Minecraft.getDebugFPS() + " FPS", 0, 0, -1);
        });
    }
}
