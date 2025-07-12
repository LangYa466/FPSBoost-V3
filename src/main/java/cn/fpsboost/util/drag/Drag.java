package cn.fpsboost.util.drag;

import lombok.Data;
import cn.fpsboost.util.misc.LogUtil;
import cn.fpsboost.util.render.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;

/**
 * @author LangYa466
 * @date 2025/5/17
 */
@Data
public class Drag {
    private final String moduleName;
    private float x, y, width, height;
    private boolean dragging = false;
    private final LogUtil logger = new LogUtil();

    public void setXY(float initX, float initY) {
        this.x = initX;
        this.y = initY;
    }

    public void render(Runnable runnable) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        try {
            runnable.run();
        } catch (Exception e) {
            logger.error(moduleName, e);
        }
        GlStateManager.popMatrix();
    }

    public boolean isHovered(float mouseX, float mouseY) {
        return RenderUtil.isHovered(x, y, width, height, mouseX, mouseY);
    }
}
