package cn.fpsboost.util.render.font;

import cn.fpsboost.util.misc.ResourceUtil;

import java.awt.*;
import java.io.InputStream;

/**
 * @author LangYa466
 * @date 2025/5/17
 */
public interface FontUtil {
    CFontRenderer font16 = getFont(16);
    CFontRenderer font18 = getFont(18);
    CFontRenderer font20 = getFont(20);
    CFontRenderer font22 = getFont(22);
    CFontRenderer font24 = getFont(24);
    CFontRenderer font26 = getFont(26);
    CFontRenderer font28 = getFont(28);
    CFontRenderer font30 = getFont(30);
    CFontRenderer font32 = getFont(32);
    CFontRenderer font34 = getFont(34);
    CFontRenderer font36 = getFont(36);
    CFontRenderer font38 = getFont(38);
    CFontRenderer font40 = getFont(40);

    static CFontRenderer getFont(int fontSize) {
        try (InputStream inputStream = ResourceUtil.getResource("fonts/client.otf")) {
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