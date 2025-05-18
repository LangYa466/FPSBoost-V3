package net.fpsboost.event.impl;

import lombok.*;
import net.fpsboost.event.Event;
import net.minecraft.client.gui.ScaledResolution;

/**
 * @author LangYa466
 * @date 2025/5/17
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class Render2DEvent extends Event {
    private final float partialTicks;
    private final ScaledResolution scaledResolution;
}
