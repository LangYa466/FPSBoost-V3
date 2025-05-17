package net.fpsboost.manager.impl;

import lombok.Getter;
import net.fpsboost.Client;
import net.fpsboost.manager.Manager;
import net.fpsboost.module.Module;
import net.fpsboost.util.drag.Drag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author LangYa466
 * @date 2025/5/17
 */
@Getter
public class DragManager extends Manager {
    private Map<String, Drag> drags;

    public DragManager() {
        super("Drag");
    }

    @Override
    protected void init() {
        drags = new HashMap<>();
        super.init();
    }

    public List<Drag> getEnabledDrags() {
        return drags.values().stream().filter(drag -> {
            Module dragModule = Client.moduleManager.getModule(drag.getModuleName());
            return dragModule != null && dragModule.isEnabled();
        }).toList();
    }

    public void addDrag(String moduleName, Drag drag) {
        drags.put(moduleName, drag);
    }
}
