package cn.fpsboost.event.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import cn.fpsboost.event.Event;
import net.minecraft.client.multiplayer.WorldClient;

/**
 * @author LangYa466
 * @date 2025/7/6
 */
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Data
public class LoadWorldEvent extends Event {
    private final WorldClient worldClient;
}
