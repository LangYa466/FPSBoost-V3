package cn.fpsboost.manager.impl;

import cn.fpsboost.Client;
import cn.fpsboost.module.impl.misc.NameProtect;
import cn.fpsboost.util.misc.DrawTextHook;

/**
 * @author LangYa466
 * @since 1/26/2025
 */
public class DrawTextHookManager {
    public static DrawTextHook hookMethod(String text) {
        try {
            final DrawTextHook drawTextHook = new DrawTextHook(text);
            RankManager.hook(drawTextHook);
            NameProtect.hook(drawTextHook);
            return drawTextHook;
        } catch (Exception e) {
            Client.logger.error(e);
        }
        return new DrawTextHook("Error");
    }
}
