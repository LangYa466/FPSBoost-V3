package cn.fpsboost.util.misc;

import cn.fpsboost.event.EventPriority;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;

/**
 * @author LangYa466
 * @date 2025/7/12
 */
@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class ListenerMethod {
    private final Object parent;
    private final Method method;
    private final EventPriority priority;
}
