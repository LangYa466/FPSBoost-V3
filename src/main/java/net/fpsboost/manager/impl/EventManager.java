package net.fpsboost.manager.impl;

import lombok.Getter;
import net.fpsboost.event.Event;
import net.fpsboost.event.EventPriority;
import net.fpsboost.event.EventTarget;
import net.fpsboost.manager.Manager;

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
                list.sort(Comparator.comparing((ListenerMethod m) -> m.priority.ordinal()).reversed()));
    }

    public void unregister(Object listener) {
        for (List<ListenerMethod> methods : listeners.values()) {
            methods.removeIf(method -> method.parent == listener);
        }
    }

    public void call(Event event) {
        List<ListenerMethod> methods = listeners.get(event.getClass());
        if (methods != null) {
            for (ListenerMethod method : methods) {
                try {
                    method.method.setAccessible(true);
                    method.method.invoke(method.parent, event);
                } catch (InvocationTargetException e) {
                    Throwable cause = e.getCause();
                    logger.warn("[EventManager] Listener {}.{} threw an exception",
                            method.parent.getClass().getName(),
                            method.method.getName(),
                            cause);
                } catch (Exception e) {
                    logger.warn("[EventManager] Failed to dispatch event to {}.{}",
                            method.parent.getClass().getName(),
                            method.method.getName(),
                            e);
                }
            }
        }
    }

    private record ListenerMethod(Object parent, Method method, EventPriority priority) {}
}