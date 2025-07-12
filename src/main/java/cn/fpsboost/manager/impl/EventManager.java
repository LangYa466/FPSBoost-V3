package cn.fpsboost.manager.impl;

import cn.fpsboost.util.misc.ListenerMethod;
import lombok.Getter;
import cn.fpsboost.event.Event;
import cn.fpsboost.event.EventPriority;
import cn.fpsboost.event.EventTarget;
import cn.fpsboost.manager.Manager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author LangYa466
 * @since 4/28/2025 5:36 PM
 */
@Getter
public class EventManager extends Manager {
    private final Map<Class<?>, List<ListenerMethod>> listeners = new ConcurrentHashMap<>();

    public EventManager() {
        super("Event");
    }

    public void register(Object listener) {
        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(EventTarget.class)) {
                Class<?>[] params = method.getParameterTypes();
                if (params.length == 1 && Event.class.isAssignableFrom(params[0])) {
                    method.setAccessible(true);
                    EventTarget annotation = method.getAnnotation(EventTarget.class);
                    EventPriority priority = annotation != null && annotation.priority() != null
                            ? annotation.priority()
                            : EventPriority.NORMAL;
                    listeners.computeIfAbsent(params[0], k -> new CopyOnWriteArrayList<>())
                            .add(new ListenerMethod(listener, method, priority));
                }
            }
        }
        // 排序仍然可以安全进行
        listeners.values().forEach(list ->
                list.sort(Comparator.comparing((ListenerMethod m) -> m.getPriority().ordinal()).reversed()));
    }

    public void unregister(Object listener) {
        for (List<ListenerMethod> methods : listeners.values()) {
            methods.removeIf(method -> method.getParent() == listener);
        }
    }

    public void call(Event event) {
        List<ListenerMethod> methods = listeners.get(event.getClass());
        if (methods != null) {
            for (ListenerMethod method : methods) {
                try {
                    method.getMethod().setAccessible(true);
                    method.getMethod().invoke(method.getParent(), event);
                } catch (InvocationTargetException e) {
                    Throwable cause = e.getCause();
                    logger.warn("[EventManager] Listener {}.{} threw an exception",
                            method.getParent().getClass().getName(),
                            method.getMethod().getName(),
                            cause);
                } catch (Exception e) {
                    logger.warn("[EventManager] Failed to dispatch event to {}.{}",
                            method.getParent().getClass().getName(),
                            method.getMethod().getName(),
                            e);
                }
            }
        }
    }
}