
package cn.fpsboost.socket;

import cn.fpsboost.Client;
import cn.fpsboost.Wrapper;
import cn.fpsboost.event.EventTarget;
import cn.fpsboost.module.Category;
import cn.fpsboost.module.Module;
import cn.fpsboost.util.misc.RankUtil;

import java.io.IOException;

/**
 * @author LangYa466
 * @since 2025/1/3
 */
public class ClientIRC extends Module implements Wrapper {
    public static final ClientIRC INSTANCE = new ClientIRC();
    public static SocketClient handler;
    private static boolean initiated = false;

    public ClientIRC() {
        super(Category.CLIENT);
    }

    @Override
    public void onEnable() {
        init();
    }

    @Override
    public void onDisable() {
        this.enabled = true;
    }

    @EventTarget
    public void onWorldLoad() {
        handler.sendMessage("GET_USERS_REQUEST");
        RankUtil.getRanksAsync();
    }

    public static void init() {
        if (initiated) return;

        try {
            handler = new SocketClient("113.45.185.125", 11451, new IRCHandler() {
                @Override
                public void onMessage(String message) {
                    Client.logger.debug("Server Message: " + message);
                }

                @Override
                public void onDisconnected(String message) {
                    // 处理断开连接的情况
                    Client.logger.warn("断开连接: " + message);
                    SocketClient.transport = null;
                }

                @Override
                public void onConnected() {
                    // 处理连接成功的情况
                    Client.logger.info("链接服务器后端成功!!");
                }

                @Override
                public String getInGameUsername() {
                    // 返回游戏中的用户名，这里返回一个默认值
                    return mc.getSession().getUsername();
                }

                @Override
                public String getUsername() {
                    return mc.getSession().getUsername();
                }
            });

            handler.sendMessage("GET_USERS_REQUEST");

        } catch (IOException e) {
            Client.logger.error(e);
        }

        initiated = true;
    }

    public static boolean isUser(String ign) {
        return handler.isUser(ign);
    }
}
