package cn.fpsboost.module;

import cn.fpsboost.util.lang.Langs;
import lombok.*;
import cn.fpsboost.Client;
import cn.fpsboost.Wrapper;
import cn.fpsboost.manager.impl.I18nManager;
import cn.fpsboost.util.drag.Drag;
import cn.fpsboost.value.Value;
import net.minecraft.client.resources.I18n;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author LangYa466
 * @date 2025/5/17
 */
@Getter
@Setter
public class Module implements Wrapper {
    protected final String name;
    // skid留下来的 懒得弄i18n的
    protected String cnName;
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
        if (Objects.equals(I18nManager.get(name), name)) {
            if (Client.i18nManager.currentLang == Langs.CN) {
                return cnName;
            } else {
                return name;
            }
        }
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