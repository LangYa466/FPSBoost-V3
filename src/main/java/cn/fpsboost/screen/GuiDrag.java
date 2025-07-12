package cn.fpsboost.screen;

import cn.fpsboost.Client;
import cn.fpsboost.event.impl.Render2DEvent;
import cn.fpsboost.util.drag.Drag;
import cn.fpsboost.util.render.RenderUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;

import java.io.IOException;

/**
 * @author LangYa466
 * @date 2025/5/24
 */
public class GuiDrag extends GuiScreen {
    public static final GuiDrag INSTANCE = new GuiDrag();
    private final int gridSize = 25;

    private int offsetX = 0;
    private int offsetY = 0;
    private Drag currentDrag = null;

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        for (int x = 0; x <= width; x += gridSize) {
            drawVerticalLine(x, 0, height, 0x44FFFFFF);
        }
        for (int y = 0; y <= height; y += gridSize) {
            drawHorizontalLine(0, width, y, 0x44FFFFFF);
        }

        if (Mouse.isButtonDown(0) && currentDrag != null) {
            currentDrag.setXY(mouseX - offsetX, mouseY - offsetY);
            RenderUtil.drawOutline(currentDrag.getX() - 4, currentDrag.getY() - 4,
                    currentDrag.getWidth() + 8, currentDrag.getHeight() + 8, -1);
        }

        Render2DEvent render2DEvent = new Render2DEvent(partialTicks, new ScaledResolution(mc));
        Client.eventManager.call(render2DEvent);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0) {
            Drag hovered = Client.dragManager.getHovered(mouseX, mouseY);
            if (hovered != null) {
                currentDrag = hovered;
                offsetX = (int)(mouseX - hovered.getX());
                offsetY = (int)(mouseY - hovered.getY());
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (currentDrag != null) {
            // 自动对齐
            currentDrag.setXY(snapToGrid(currentDrag.getX()), snapToGrid(currentDrag.getY()));
            currentDrag = null;
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    private float snapToGrid(float value) {
        return Math.round(value / gridSize) * gridSize;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
