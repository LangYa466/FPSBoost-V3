package cn.fpsboost.util.misc;

import cn.fpsboost.Wrapper;

/**
 * @author LangYa466
 * @since 2/9/2025
 */
public class PingUtil implements Wrapper {
    public static long getPing() {
        return mc.getCurrentServerData() == null ? 0 : mc.getCurrentServerData().pingToServer;
    }
}