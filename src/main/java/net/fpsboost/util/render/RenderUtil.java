package net.fpsboost.util.render;

import net.minecraft.client.gui.Gui;

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
}