package net.fpsboost;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

/**
 * @author LangYa466
 * @date 2025/5/17
 */
public interface Wrapper {
    Minecraft mc = Minecraft.getMinecraft();

    static void log(String message) {
        System.out.println(Client.getDisplayName() + " " + message);
    }

    static void addMessage(String message) {
        if (mc.thePlayer == null) {
            log("[ChatGUI] " + message);
            return;
        }
        mc.thePlayer.addChatMessage(new ChatComponentText(String.format("%sFPS%sBoost%s%s»» ", EnumChatFormatting.BLUE, EnumChatFormatting.WHITE, EnumChatFormatting.BOLD, EnumChatFormatting.GOLD) + EnumChatFormatting.RESET + message));

    }

    static void debugLog(String message) {
        if (!Client.isDev) return;
        log("[Debug] " + message);
    }

    static boolean isNull() {
        return mc.thePlayer == null || mc.theWorld == null;
    }
}
