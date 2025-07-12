package cn.fpsboost.module;

import lombok.*;
import cn.fpsboost.Client;
import cn.fpsboost.Wrapper;
import cn.fpsboost.manager.impl.I18nManager;
import cn.fpsboost.util.drag.Drag;
import cn.fpsboost.value.Value;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LangYa466
 * @date 2025/5/17
 */
@Getter
@Setter
public class Module implements Wrapper {
    protected final String name;
    protected final Category category;
    protected boolean enabled;
    protected final List<Value<?>> values = new ArrayList<>();
    protected boolean canDisplay;
    protected int keyCode;

    public Module(Category category) {
        this.name = this.getClass().getSimpleName();
        this.category = category;
    }

    public Module(String name, Category category) {
        this.name = name;
        this.category = category;
    }

    public String getDisplayName() {
        return I18nManager.get(name);
    }

    public void onEnable() { }
    public void onDisable() { }
    public void onInit() { }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;

        if (enabled) {
            onEnable();
            Client.eventManager.register(this);
        } else {
            onDisable();
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

    public boolean isCanDisplay() {
        if (category == Category.DEV) {
            return Client.isDev;
        }
        return canDisplay;
    }

    public void toggle() {
        setEnabled(!enabled);
    }
}