package net.fpsboost.util.render.font;

import java.awt.*;
import java.io.InputStream;

/**
 * @author LangYa466
 * @date 2025/5/17
 */
public interface FontUtil {
    CFontRenderer font18 = getFont(18);

    static CFontRenderer getFont(int fontSize) {
        try (InputStream inputStream = FontUtil.class.getResourceAsStream("/assets/minecraft/fpsboost/fonts/client.ttf")) {
            assert inputStream != null;
            Font font = Font.createFont(Font.TRUETYPE_FONT, inputStream)
                    .deriveFont((float) fontSize);
            return new CFontRenderer(font);
        } catch (Exception e) {
            e.printStackTrace();
            return new CFontRenderer(null);
        }
    }
}