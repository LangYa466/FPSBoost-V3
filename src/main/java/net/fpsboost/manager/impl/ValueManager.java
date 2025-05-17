package net.fpsboost.manager.impl;

import lombok.val;
import net.fpsboost.Client;
import net.fpsboost.manager.Manager;
import net.fpsboost.module.Module;
import net.fpsboost.util.drag.Drag;
import net.fpsboost.value.Value;

import java.lang.reflect.Field;

/**
 * @author LangYa466
 * @date 2025/5/17
 */
public class ValueManager extends Manager {
    public ValueManager() {
        super("Value");
    }

    @Override
    protected void init() {
        for (Module module : Client.moduleManager.getModules().values()) {
            for (Field field : module.getClass().getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    val fieldObj = field.get(module);
                    if (Value.class.isAssignableFrom(field.getType())) {
                        module.getValues().add((Value<?>) fieldObj);
                    }

                    if (Drag.class.isAssignableFrom(field.getType())) {
                        Client.dragManager.addDrag(module.getName(), (Drag) fieldObj);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(String.format("获取%s的%s发生错误",
                            module.getName(), field.getName()));
                }
                field.setAccessible(true);
            }
        }
        super.init();
    }
}