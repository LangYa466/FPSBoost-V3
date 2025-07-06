package net.fpsboost.value.impl;

import lombok.Getter;
import net.fpsboost.value.Value;

/**
 * @author LangYa466
 * @date 2025/7/6
 */
@Getter
public class NumberValue extends Value<Number> {
    private final Number max;
    private final Number min;
    private final Number inc;

    public NumberValue(String name, Number defaultValue, Number max, Number min, Number inc) {
        super(name, defaultValue);
        this.max = max;
        this.min = min;
        this.inc = inc;
    }

    public void setValue(Number value) {
        if (value.doubleValue() > max.doubleValue()) {
            value = max;
        } else if (value.doubleValue() < min.doubleValue()) {
            value = min;
        }
        super.setValue(value);
    }

    public void increment() {
        Number current = getValue();
        Number newValue = current.doubleValue() + inc.doubleValue();
        setValue(newValue);
    }

    public void decrement() {
        Number current = getValue();
        Number newValue = current.doubleValue() - inc.doubleValue();
        setValue(newValue);
    }
}
