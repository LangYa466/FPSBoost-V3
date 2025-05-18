package net.fpsboost.value.impl;

import lombok.*;
import net.fpsboost.value.Value;

/**
 * @author LangYa466
 * @date 2025/5/17
 */
@Getter
@Setter
public class ModeValue extends Value<String> {
    private final String[] modes;

    public ModeValue(String name, String defaultMode, String... modes) {
        super(name, defaultMode);
        this.modes = modes;
    }

    // 查了下更好的写法 之前那个不太好
    public void setNextValue() {
        int index = -1;
        for (int i = 0; i < modes.length; i++) {
            if (modes[i].equals(value)) {
                index = i;
                break;
            }
        }

        if (index == -1) {
            value = modes[0];
        } else {
            value = modes[(index + 1) % modes.length];
        }
    }
}
