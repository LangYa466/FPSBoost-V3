package cn.fpsboost.mixins;

import cn.fpsboost.Client;
import cn.fpsboost.event.impl.UpdateEvent;
import net.minecraft.client.entity.EntityPlayerSP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author LangYa466
 * @date 2025/7/12
 */
@Mixin(EntityPlayerSP.class)
public class MixinEntityPlayerSP {
    @Inject(method = "onUpdate", at = @At("HEAD"))
    private void onUpdate(CallbackInfo callbackInfo) {
        UpdateEvent updateEvent = new UpdateEvent();
        Client.eventManager.call(updateEvent);
    }
}
