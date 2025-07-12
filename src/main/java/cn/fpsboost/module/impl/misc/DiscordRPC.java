package cn.fpsboost.module.impl.misc;

import cn.fpsboost.Client;
import cn.fpsboost.module.Category;
import cn.fpsboost.module.Module;

/**
 * @author LangYa466
 * @date 2025/5/27
 */
public class DiscordRPC extends Module {
    public DiscordRPC() {
        super(Category.MISC);
    }

    @Override
    public void onEnable() {
        if (Client.discordRpcThread.isAlive()) return;
        Client.discordRpcThread.start();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        Client.discordRpcThread.interrupt();
        super.onDisable();
    }
}
