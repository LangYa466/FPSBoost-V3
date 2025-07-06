package net.fpsboost.util.render;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import static net.fpsboost.Wrapper.mc;

/**
 * @author LangYa466
 * @date 2025/5/24
 */
public class RenderUtil {
    public static boolean isHovered(float x, float y, float width, float height, int mouseX, int mouseY) {
        return isHovered(x, y, width, height, (float) mouseX, (float) mouseY);
    }

    public static boolean isHovered(float x, float y, float width, float height, float mouseX, float mouseY) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }

    public static void drawOutline(float x, float y, float width, float height, int color) {
        Gui.drawHorizontalLine(x, x + width, y, color);
        Gui.drawHorizontalLine(x, x + width, y + height, color);

        Gui.drawVerticalLine(x, y, y + height, color);
        Gui.drawVerticalLine(x + width, y, y + height, color);
    }

    public static void drawImage(ResourceLocation texture, float x, float y, int width, int height) {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1, 1, 1, 1); // 确保每次绘制时重置颜色
        mc.getTextureManager().bindTexture(texture);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
        GlStateManager.disableBlend();
    }
}