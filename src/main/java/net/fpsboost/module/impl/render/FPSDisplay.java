package net.fpsboost.module.impl.render;

import net.fpsboost.event.EventTarget;
import net.fpsboost.event.impl.Render2DEvent;
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
        super(Category.RENDER);
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        drag.render(() -> {
            font18.drawStringWithShadow(" TEST", 0, 0, -1);
        });
    }
}
