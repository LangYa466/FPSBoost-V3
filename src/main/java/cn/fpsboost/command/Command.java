package cn.fpsboost.command;

import lombok.Data;
import cn.fpsboost.Wrapper;
import cn.fpsboost.manager.impl.I18nManager;

/**
 * @author LangYa466
 * @date 2025/5/24
 */
@Data
public class Command implements Wrapper {
    private String[] aliases;
    private String name, aliasPart;

    public Command(String... aliases) {
        this.aliases = aliases;
        this.name = aliases[0];
        this.aliasPart = String.join("/", aliases);
    }

    public boolean run(String[] args) {
        return false;
    }

    public void log(String key, Object... args) {
        // System.out.printf("Isdev: %s%n", Client.isDev);
        String message = I18nManager.get(String.format("%s.%s", name, key));
        // Wrapper.debugLog("message " + message);
        String formattedMessage = String.format(message, args);
        // Wrapper.debugLog("formattedMessage " + formattedMessage);
        Wrapper.addMessage(formattedMessage);
    }

    public void logUsage() {
        log("usage", aliasPart);
    }
}
