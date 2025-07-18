package cn.fpsboost.manager;

import lombok.*;
import cn.fpsboost.Client;
import cn.fpsboost.Wrapper;
import cn.fpsboost.util.misc.LogUtil;

/**
 * @author LangYa466
 * @date 2025/5/17
 */
@Getter
@Setter
public class Manager implements Wrapper {
    public final String name;
    protected final LogUtil logger;

    public Manager(String name) {
        this.name = name + "Manager";
        this.logger = new LogUtil(name);
        Client.managers.add(this);

        if (!initHasLog()) {
            throw new RuntimeException(name + " 初始化失败!");
        }
    }

    public boolean initHasLog() {
        logger.info("初始化中...");
        try {
            init();
            logger.info("初始化完毕!");
        } catch (Exception e) {
            logger.error("{} 初始化发生错误!!", e, name);
            logger.error(e);
            return false;
        }
        return true;
    }

    protected void init() {}

    public void save() {
        logger.info("{} 保存配置", name);
    }
    
    public void load() {
        logger.info("{} 加载配置", name);
    }
}
