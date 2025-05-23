package net.fpsboost.event.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.fpsboost.event.Event;

/**
 * @author LangYa466
 * @date 2025/5/24
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class KeyEvent extends Event {
    private final int keyCode;
}
