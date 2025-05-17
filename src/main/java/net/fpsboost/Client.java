package net.fpsboost;

import com.cubk.event.EventManager;
import net.fpsboost.manager.Manager;
import net.fpsboost.manager.impl.DragManager;
import net.fpsboost.manager.impl.ModuleManager;
import net.fpsboost.manager.impl.ValueManager;
import net.fpsboost.util.misc.LogUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LangYa466
 * @date 2025/5/17
 */
public class Client {
    public static final String name = "FPSBoost V3";
    public static final String version = ".01";
    private static final LogUtil logger = new LogUtil();
    public static final EventManager eventManager = new EventManager();
    public static List<Manager> managers = new ArrayList<>();
    public static ModuleManager moduleManager;
    public static DragManager dragManager;
    public static ValueManager valueManager;

    public static void initClient() {
        logger.info("初始化中");

        try {
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
            logger.warn("初始化发生错误");
            logger.error(e);
        }
    }

    public static String getDisplayName() {
        return name + version;
    }
}
