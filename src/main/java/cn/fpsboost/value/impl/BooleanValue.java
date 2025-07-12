package cn.fpsboost.value.impl;

import lombok.*;
import cn.fpsboost.value.Value;

/**
 * @author LangYa466
 * @date 2025/5/17
 */
@Getter
@Setter
public class BooleanValue extends Value<Boolean> {
    public BooleanValue(String name, Boolean defaultValue) {
        super(name, defaultValue);
    }

    public void toggle() {
        setValue(!value);
    }
}
