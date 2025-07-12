package cn.fpsboost.manager.impl;

import lombok.Getter;
import cn.fpsboost.Client;
import cn.fpsboost.manager.Manager;
import cn.fpsboost.module.Module;
import cn.fpsboost.util.drag.Drag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        return drags.values().stream()
                .filter(drag -> {
                    Module dragModule = Client.moduleManager.getModule(drag.getModuleName());
                    return dragModule != null && dragModule.isEnabled();
                })
                .collect(Collectors.toList());
    }

    public Drag getHovered(float mouseX, float mouseY) {
        for (Drag drag : getEnabledDrags()) {
            if (drag.isHovered(mouseX, mouseY)) {
                return drag;
            }
        }
        return null;
    }

    public void addDrag(String moduleName, Drag drag) {
        drags.put(moduleName, drag);
    }
}
