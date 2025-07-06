package net.fpsboost.module.impl.render;

import net.fpsboost.Client;
import net.fpsboost.event.EventTarget;
import net.fpsboost.event.impl.Render2DEvent;
import net.fpsboost.event.impl.TickEvent;
import net.fpsboost.module.Category;
import net.fpsboost.module.Module;
import net.fpsboost.util.drag.Drag;
import net.fpsboost.util.render.font.FontUtil;

/**
 * @author LangYa466
 * @date 2025/5/18
 */
public class ModuleList extends Module implements FontUtil {
    private final Drag drag = createDrag(0, 10);

    public ModuleList() {
        super(Category.RENDER);
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        drag.render(() -> {
            try {
                int index = 0;
                for (Module module : Client.moduleManager.getModules().values()) {
                    if (module.isEnabled()) {
                        font18.drawStringWithShadow(module.getDisplayName(), 0, index * font18.FONT_HEIGHT, -1);
                        index++;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    @EventTarget
    public void onTick(TickEvent event) {
        try {
            int maxWidth = 0;
            int enabledCount = 0;
            
            for (Module module : Client.moduleManager.getModules().values()) {
                if (module.isEnabled()) {
                    int moduleWidth = font18.getStringWidth(module.getDisplayName());
                    if (moduleWidth > maxWidth) {
                        maxWidth = moduleWidth;
                    }
                    enabledCount++;
                }
            }
            
            drag.setWidth(maxWidth);
            drag.setHeight(enabledCount * font18.getHeight());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}