package net.fpsboost.value;

import lombok.Data;
import net.fpsboost.Client;

import java.util.function.BooleanSupplier;

/**
 * @author LangYa466
 * @date 2025/5/17
 */
@Data
public class Value<T> {
    protected T value;
    private final String name;
    private BooleanSupplier isHide = () -> false;
    public String moduleName;

    public Value(String name, T value) {
        this.name = name;
        this.value = value;
    }

    public Value(String name, T value, BooleanSupplier isHide) {
        this.name = name;
        this.value = value;
        this.isHide = isHide;
    }

    public String getDisplayName() {
        return Client.i18nManager.get(String.format("%s.%s", moduleName, name));
    }
}