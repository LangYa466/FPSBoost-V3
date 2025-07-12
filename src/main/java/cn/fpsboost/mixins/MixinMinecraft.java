package cn.fpsboost.mixins;

import cn.fpsboost.Client;
import cn.fpsboost.event.impl.KeyEvent;
import cn.fpsboost.event.impl.LoadWorldEvent;
import cn.fpsboost.event.impl.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author LangYa466
 * @date 2025/7/12
 */
@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Shadow
    public GuiScreen currentScreen;

    @Inject(method = "startGame", at = @At("RETURN"))
    public void startGame(CallbackInfo ci){
        Client.initClient();
    }

    @Inject(method = "runTick", at = @At("HEAD"))
    public void onTick(CallbackInfo ci) {
        TickEvent tickEvent = new TickEvent();
        Client.eventManager.call(tickEvent);
    }

    @Inject(method = "loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V", at = @At("HEAD"))
    public void onLoadWorld(WorldClient worldClient, String s, CallbackInfo ci){
        LoadWorldEvent loadWorldEvent = new LoadWorldEvent(worldClient);
        Client.eventManager.call(loadWorldEvent);
    }

    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;dispatchKeypresses()V", shift = At.Shift.AFTER))
    private void onKey(CallbackInfo callbackInfo) {
        if (Keyboard.getEventKeyState() && currentScreen == null) {
            KeyEvent keyEvent = new KeyEvent(Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey());
            Client.eventManager.call(keyEvent);
        }
    }
}
