package net.fpsboost.manager.impl;

import lombok.Getter;
import net.fpsboost.Wrapper;
import net.fpsboost.manager.Manager;
import net.fpsboost.module.Module;
import net.fpsboost.module.impl.misc.Sprint;
import net.fpsboost.module.impl.render.FPSDisplay;

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

        // Render
        addModule(new FPSDisplay());
        super.init();
    }

    public void addModule(Module module) {
        modules.put(module.getName(), module);

        // test
        module.setEnabled(true);
    }

    public Module getModule(String moduleName) {
        return modules.get(moduleName);
    }
}
