package cn.fpsboost.util.misc;

import lombok.Getter;

@Getter
public class MouseButton {
    private final long lastMs;

    public MouseButton(long lastMs) {
        this.lastMs = lastMs;
    }

    public boolean canBeReduced() {
        return System.currentTimeMillis() - lastMs >= 1000L;
    }
}