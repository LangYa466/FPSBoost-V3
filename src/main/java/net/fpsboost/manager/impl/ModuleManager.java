package net.fpsboost.manager.impl;

import lombok.Getter;
import net.fpsboost.Client;
import net.fpsboost.Wrapper;
import net.fpsboost.event.EventTarget;
import net.fpsboost.event.impl.KeyEvent;
import net.fpsboost.manager.Manager;
import net.fpsboost.module.Module;
import net.fpsboost.module.impl.dev.TestDragGUI;
import net.fpsboost.module.impl.misc.DiscordRPC;
import net.fpsboost.module.impl.misc.Sprint;
import net.fpsboost.module.impl.render.FPSDisplay;
import net.fpsboost.module.impl.render.ModuleList;

import java.util.HashMap;
import java.util.Map;

/**
 * @author LangYa466
 * @date 2025/5/17
 */
@Getter
public class ModuleManager extends Manager {
    private Map<String, Module> modules;

    public ModuleManager() {
        super("Module");
    }

    @Override
    public void init() {
        modules = new HashMap<>();

        // Misc
        addModule(new Sprint());
        addModule(new DiscordRPC());

        // Render
        addModule(new FPSDisplay());
        addModule(new ModuleList());

        // Dev
        addModule(new TestDragGUI());

        Client.eventManager.register(this);
        super.init();
    }

    @EventTarget
    public void onKey(KeyEvent event) {
        modules.values().forEach(module -> {
            if(event.getKeyCode() == module.getKeyCode()) module.toggle();
        });
    }

    public void addModule(Module module) {
        String moduleName = module.getName();
        modules.put(moduleName, module);
        module.onInit();
        logger.info("已注册模块 {}", moduleName);

        // dev
        if (Client.isDev) module.setEnabled(true);
    }

    public Module getModule(String moduleName) {
        return modules.get(moduleName);
    }
}
