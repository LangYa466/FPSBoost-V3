package net.fpsboost.manager;

import lombok.*;
import net.fpsboost.Client;
import net.fpsboost.Wrapper;
import net.fpsboost.util.misc.LogUtil;

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
    protected void save() {}
    protected void load() {}
}
