package net.fpsboost.manager.impl;

import net.fpsboost.Client;
import net.fpsboost.Wrapper;
import net.fpsboost.command.Command;
import net.fpsboost.command.impl.ToggleCommand;
import net.fpsboost.event.EventTarget;
import net.fpsboost.event.impl.SendMessageEvent;
import net.fpsboost.manager.Manager;

import java.util.HashMap;
import java.util.Map;

/**
 * @author LangYa466
 * @date 2025/5/24
 */
public class CommandManager extends Manager {
    private final String prefix = "#";
    private Map<String, Command> commandMap;

    public CommandManager() {
        super("Command");
    }

    @Override
    protected void init() {
        commandMap = new HashMap<>();

        addCommand(new ToggleCommand());

        Client.eventManager.register(this);
        super.init();
    }

    @EventTarget
    public void onSendMessage(SendMessageEvent event) {
        String message = event.getMessage();
        if (message.startsWith(prefix)) {
            event.setCancelled(true);
            String[] args = message.replace(prefix, "").split(" ");
            String commandName = args[0];
            Command command = getCommand(commandName);
            if (command == null) {
                Wrapper.addMessage(String.format("§c%s: %s", I18nManager.get("unknowCommand"), commandName));
            } else {
                if (!command.run(args)) command.logUsage();
            }
        }
    }

    private void addCommand(Command command) {
        commandMap.put(command.getName(), command);
    }

    public Command getCommand(String commandName) {
        for (Command command : commandMap.values()) {
            for (String alias : command.getAliases()) {
                if (commandName.equalsIgnoreCase(alias)) {
                    return command;
                }
            }
        }
        return null;
    }
}