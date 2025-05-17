package net.fpsboost.module;

import lombok.*;
import net.fpsboost.Client;
import net.fpsboost.Wrapper;
import net.fpsboost.util.drag.Drag;
import net.fpsboost.value.Value;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LangYa466
 * @date 2025/5/17
 */
@Getter
@Setter
public class Module implements Wrapper {
    private final String name;
    private final Category category;
    private boolean enabled;
    private final List<Value<?>> values = new ArrayList<>();

    public Module(String name, Category category) {
        this.name = name;
        this.category = category;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;

        if (enabled) {
            Client.eventManager.register(this);
        } else {
            Client.eventManager.unregister(this);
        }
    }

    protected Drag createDrag(float initX, float initY) {
        Drag drag = new Drag(this.name);
        drag.setXY(initX, initY);
        return drag;
    }

    protected Drag createDrag() {
        return createDrag(0, 0);
    }
}
