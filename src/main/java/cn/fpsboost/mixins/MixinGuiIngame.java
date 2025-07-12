package cn.fpsboost.mixins;

import cn.fpsboost.Client;
import cn.fpsboost.event.impl.Render2DEvent;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static cn.fpsboost.Wrapper.mc;

/**
 * @author LangYa466
 * @date 2025/7/12
 */
@Mixin(GuiIngame.class)
public class MixinGuiIngame {
    @Inject(method = "renderGameOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;color(FFFF)V", shift = At.Shift.AFTER))
    private void onRender2D(float partialTicks, CallbackInfo callbackInfo) {
        Render2DEvent render2DEvent = new Render2DEvent(partialTicks, new ScaledResolution(mc));
        Client.eventManager.call(render2DEvent);
    }
}
