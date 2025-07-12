package cn.fpsboost.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.client.settings.KeyBinding;

/**
 * @author LangYa466
 * @date 2025/7/12
 */
@Mixin(KeyBinding.class)
public interface KeyBindingAccessor {
    @Accessor("pressed")
    void setPressed(boolean pressed);

    @Accessor("pressed")
    boolean getPressed();
}
