package cn.fpsboost.module.impl.render;

import cn.fpsboost.event.EventTarget;
import cn.fpsboost.event.impl.Render2DEvent;
import cn.fpsboost.module.Module;
import cn.fpsboost.util.drag.Drag;
import cn.fpsboost.util.render.RenderUtil;
import cn.fpsboost.util.render.font.FontUtil;
import cn.fpsboost.value.impl.BooleanValue;
import sun.font.FontManager;

import java.awt.*;
import java.util.function.Supplier;

/**
 * @author LangYa466
 * @since 2/9/2025
 */
public class TextDisplay extends Module {
    private final Supplier<String> textSupplier;

    public TextDisplay(String name, String cnName, Supplier<String> textSupplier) {
        super(name, cnName);
        this.textSupplier = textSupplier;
        this.values.add(backgroundValue);
        this.values.add(textShadowValue);
        this.values.add(clientFontValue);
    }

    private final BooleanValue backgroundValue = new BooleanValue("背景", true);
    private final BooleanValue textShadowValue = new BooleanValue("字体阴影", true);
    private final BooleanValue clientFontValue = new BooleanValue("更好的字体", true);
    private final ColorValue bgColorValue = new ColorValue("背景颜色", "Background Color", new Color(0, 0, 0, 80), this);
    private final ColorValue textColorValue = new ColorValue("文本颜色", "Text Color", Color.white, this);

    private final Drag drag = createDrag();
    public Supplier<String> getText() {
        return textSupplier;
    }
    public static int drawText(String text, int x, int y, boolean bg, int bgColor, int textColor, boolean textShadow, boolean clientFont) {
        int width = clientFont ? FontUtil.font16.getStringWidth(text) : mc.fontRendererObj.getStringWidth(text);
        int height = clientFont ? FontUtil.font16.getHeight() : mc.fontRendererObj.FONT_HEIGHT;

        int width1 = width + (clientFont ? 7 : 6);


        if (clientFont) {
            if (bg) RenderUtil.drawRect(x - 2, y - 2,
                    x - 2 + width1, y - 2 + height + 5, bgColor);
            FontUtil.font16.drawString(text, x + 1, y - 1, textColor, textShadow);
        } else {
            int offsetX = mc.gameSettings.forceUnicodeFont ? 0 : 1;
            int offsetY = mc.gameSettings.forceUnicodeFont ? 0 : 1;
            if (bg) RenderUtil.drawRect(offsetX - 2, offsetY - 2, width1, height + 6, bgColor);
            mc.fontRendererObj.drawString(text, x + offsetX, y + offsetY, textColor, textShadow);
        }

        return width1;
    }


    @EventTarget
    public void onRender2D(Render2DEvent event) {
        drag.setWidth(drawText(getText().get(), 0, 0, backgroundValue.getValue(), bgColorValue.getValue(), textColorValue.getValue(), textShadowValue.getValue(), clientFontValue.getValue()));
        drag.setHeight(mc.fontRendererObj.FONT_HEIGHT);
    }
}