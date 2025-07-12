package cn.fpsboost;

import cn.fpsboost.manager.impl.*;
import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;
import cn.fpsboost.manager.Manager;
import cn.fpsboost.util.misc.LogUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author LangYa466
 * @date 2025/5/17
 */
public class Client implements Wrapper {
    public static final String name = "FPSBoost V3";
    public static final String version = ".01";
    public static final LogUtil logger = new LogUtil();
    public static List<Manager> managers = new CopyOnWriteArrayList<>();
    public static EventManager eventManager;
    public static ModuleManager moduleManager;
    public static DragManager dragManager;
    public static ValueManager valueManager;
    public static I18nManager i18nManager;
    public static CommandManager commandManager;
    public static File configDir;
    public static final String web = "https://api.furry.luxe/";

    public static Thread discordRpcThread;

    public static boolean isDev = true;

    public static void initClient() {
        logger.info("初始化中");
        long initTime = System.currentTimeMillis();

        try {
            configDir = new File(mc.mcDataDir, name);
            if (!configDir.exists()) {
                if (configDir.mkdir()) {
                    logger.info("创建配置文件夹成功");
                } else {
                    throw new IOException("创建配置文件夹失败");
                }
            }

            initDiscordRPC();

            eventManager = new EventManager();
            i18nManager = new I18nManager();
            moduleManager = new ModuleManager();
            dragManager = new DragManager();
            valueManager = new ValueManager();
            commandManager = new CommandManager();

            loadAllConfigs();
            logger.info("初始化成功");
        } catch (Exception e) {
            logger.error("初始化发生错误");
            logger.error(e);
        }

        long endTime = System.currentTimeMillis();
        long durationMs = endTime - initTime;
        double durationSec = durationMs / 1000.0;
        logger.info("客户端初始化耗时: {} ms ({} s)", durationMs, durationSec);
    }

    private static void initDiscordRPC() {
        DiscordEventHandlers handlers = new DiscordEventHandlers.Builder().build();
        DiscordRPC.discordInitialize("1376802803049173012", handlers, true);

        DiscordRichPresence presence = new DiscordRichPresence.Builder("Playing Minecraft")
                .setDetails("Name: " + mc.getSession().getUsername())
                .setStartTimestamps(System.currentTimeMillis() / 1000)
                .build();

        DiscordRPC.discordUpdatePresence(presence);

        discordRpcThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                DiscordRPC.discordRunCallbacks();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            DiscordRPC.discordShutdown();
        });

        discordRpcThread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            saveAllConfigs();
            discordRpcThread.interrupt();
            try {
                discordRpcThread.join();
            } catch (InterruptedException ignored) { }
        }));
    }

    public static String getDisplayName() {
        if (isDev) return name + "-DevBuild";
        return name + version;
    }

    public static void saveAllConfigs() {
        logger.info("保存所有配置...");
        for (Manager manager : managers) {
            try {
                manager.save();
            } catch (Exception e) {
                logger.error("保存配置失败: {}", manager.getName(), e);
            }
        }
        logger.info("所有配置保存完成");
    }

    public static void loadAllConfigs() {
        logger.info("加载所有配置...");
        for (Manager manager : managers) {
            try {
                manager.load();
            } catch (Exception e) {
                logger.error("加载配置失败: {}", manager.getName(), e);
            }
        }
        logger.info("所有配置加载完成");
    }
}