package net.fpsboost.events;

import com.cubk.event.impl.Event;
import lombok.Data;
import net.minecraft.client.gui.ScaledResolution;

/**
 * @author LangYa466
 * @date 2025/5/17
 */
@Data
public class Render2DEvent implements Event {
    private final float partialTicks;
    private final ScaledResolution scaledResolution;
}
