package net.fpsboost;

import net.fpsboost.manager.impl.*;
import net.fpsboost.manager.Manager;
import net.fpsboost.util.misc.LogUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LangYa466
 * @date 2025/5/17
 */
public class Client implements Wrapper {
    public static final String name = "FPSBoost V3";
    public static final String version = ".01";
    private static final LogUtil logger = new LogUtil();
    public static List<Manager> managers = new ArrayList<>();
    public static EventManager eventManager;
    public static ModuleManager moduleManager;
    public static DragManager dragManager;
    public static ValueManager valueManager;
    public static I18nManager i18nManager;
    public static final File configDir = new File(mc.mcDataDir, name);

    public static void initClient() {
        logger.info("初始化中");

        try {
            if (!configDir.exists()) {
                if (configDir.mkdir()) {
                    logger.info("创建配置文件夹成功");
                } else {
                    throw new IOException("创建配置文件夹失败");
                }
            }

            eventManager = new EventManager();
            i18nManager = new I18nManager();
            moduleManager = new ModuleManager();
            dragManager = new DragManager();
            valueManager = new ValueManager();

            managers.forEach(manager -> {
                if (!manager.initHasLog()) {
                    throw new RuntimeException(manager.getName() + " 初始化失败!");
                }
            });
            logger.info("初始化成功");
        } catch (Exception e) {
            logger.error("初始化发生错误");
            logger.error(e);
        }
    }

    public static String getDisplayName() {
        return name + version;
    }
}
