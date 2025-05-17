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

    // shit
    public void setNextValue() {
        int count = 0;
        int index = 0;
        for (String mode : modes) {
            if (mode.equals(value)) {
                count = index;
            }
            count++;
        }

        if (count == (modes.length - 1)) {
            value = modes[0];
        } else {
            value = modes[count + 1];
        }
    }
}
