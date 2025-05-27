package net.fpsboost.module.impl.misc;

import net.fpsboost.Client;
import net.fpsboost.module.Category;
import net.fpsboost.module.Module;

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
        Client.discordRpcThread.start();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        Client.discordRpcThread.interrupt();
        super.onDisable();
    }
}
