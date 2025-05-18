package net.fpsboost.event;

import java.lang.annotation.*;

/**
 * @author LangYa466
 * @since 4/28/2025 5:35 PM
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventTarget {
    EventPriority priority() default EventPriority.NORMAL;
}
