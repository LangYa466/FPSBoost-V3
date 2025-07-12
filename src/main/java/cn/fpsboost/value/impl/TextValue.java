package cn.fpsboost.value.impl;

import lombok.Getter;
import lombok.Setter;
import cn.fpsboost.value.Value;

/**
 * @author LangYa466
 * @date 2025/7/6
 */
@Getter
@Setter
public class TextValue extends Value<String> {
    public TextValue(String name, String defaultValue) {
        super(name, defaultValue);
    }
}
