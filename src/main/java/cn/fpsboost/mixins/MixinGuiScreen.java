package cn.fpsboost.mixins;

import cn.fpsboost.Client;
import cn.fpsboost.event.impl.SendMessageEvent;
import cn.fpsboost.module.impl.client.ClientCommand;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static cn.fpsboost.Wrapper.mc;

/**
 * @author LangYa466
 * @date 2025/7/12
 */
@Mixin(GuiScreen.class)
public class MixinGuiScreen {
    @Inject(method = "sendChatMessage(Ljava/lang/String;Z)V", at = @At("HEAD"), cancellable = true)
    private void onSendMessage(String msg, boolean addToChat, final CallbackInfo callbackInfo) {
        if (msg.startsWith(ClientCommand.prefix.getValue()) && addToChat) {
            mc.ingameGUI.getChatGUI().addToSentMessages(msg);

            SendMessageEvent sendMessageEvent = new SendMessageEvent(msg);
            Client.eventManager.call(sendMessageEvent);
            if(sendMessageEvent.isCancelled()) callbackInfo.cancel();
        }
    }

    @Shadow
    protected void keyTyped(char typedChar, int keyCode) { }

    @Inject(method = "handleKeyboardInput", at = @At("HEAD"), cancellable = true)
    private void onHandleKeyboardInput(CallbackInfo callbackInfo) {
        char eventCharacter = Keyboard.getEventCharacter();
        int eventKey = Keyboard.getEventKey();
        boolean eventKeyState = Keyboard.getEventKeyState();

        if ((eventKey == 0 && eventCharacter >= ' ') || eventKeyState) {
            this.keyTyped(eventCharacter, eventKey);
        }

        mc.dispatchKeypresses();
        callbackInfo.cancel();
    }
}
