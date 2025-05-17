package net.fpsboost.util.drag;

import lombok.Data;
import net.minecraft.client.renderer.GlStateManager;

/**
 * @author LangYa466
 * @date 2025/5/17
 */
@Data
public class Drag {
    private final String moduleName;
    private float x, y, width, height;

    public void setXY(float initX, float initY) {
        this.x = initX;
        this.y = initY;
    }

    public void render(Runnable runnable) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        runnable.run();
        GlStateManager.popMatrix();
    }
}
