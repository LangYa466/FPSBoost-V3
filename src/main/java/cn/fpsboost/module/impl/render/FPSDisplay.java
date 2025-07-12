package cn.fpsboost.module.impl.render;

import cn.fpsboost.event.EventTarget;
import cn.fpsboost.event.impl.Render2DEvent;
import cn.fpsboost.event.impl.TickEvent;
import cn.fpsboost.module.Category;
import cn.fpsboost.module.Module;
import cn.fpsboost.util.drag.Drag;
import cn.fpsboost.util.render.font.FontUtil;
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
            font18.drawStringWithShadow(Minecraft.getDebugFPS() + " FPS", 0, 0, -1);
        });
    }
    @EventTarget
    public void onTick(TickEvent event) {
        drag.setWidth(font18.getStringWidth(Minecraft.getDebugFPS() + " FPS"));
        drag.setHeight(font18.getHeight());
    }
}
