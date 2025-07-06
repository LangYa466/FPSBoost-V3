package net.fpsboost.manager.impl;

import net.fpsboost.Client;
import net.fpsboost.Wrapper;
import net.fpsboost.command.Command;
import net.fpsboost.command.impl.ToggleCommand;
import net.fpsboost.command.impl.BindCommand;
import net.fpsboost.event.EventTarget;
import net.fpsboost.event.impl.SendMessageEvent;
import net.fpsboost.manager.Manager;
import net.fpsboost.module.impl.client.ClientCommand;

import java.util.HashMap;
import java.util.Map;

/**
 * @author LangYa466
 * @date 2025/5/24
 */
public class CommandManager extends Manager {
    private Map<String, Command> commandMap;
    private Map<String, Command> aliasMap;

    public CommandManager() {
        super("Command");
    }

    @Override
    protected void init() {
        commandMap = new HashMap<>();
        aliasMap = new HashMap<>();

        addCommand(new ToggleCommand());
        addCommand(new BindCommand());

        Client.eventManager.register(this);
        super.init();
    }

    @EventTarget
    public void onSendMessage(SendMessageEvent event) {
        if (!Client.moduleManager.isEnabled(ClientCommand.class)) return;
        String prefix = ClientCommand.prefix.getValue();
        String message = event.getMessage();
        if (message.length() <= prefix.length() || !message.startsWith(prefix)) {
            return;
        }
        
        event.setCancelled(true);
        String[] args = message.substring(prefix.length()).trim().split("\\s+");
        if (args.length == 0) return;
        
        String commandName = args[0];
        
        if (commandName.equalsIgnoreCase("help")) {
            showUsage();
            return;
        }
        
        Command command = getCommand(commandName);
        if (command == null) {
            Wrapper.addMessage(String.format("§c%s: %s", I18nManager.get("unknowCommand"), commandName));
        } else {
            if (!command.run(args)) command.logUsage();
        }
    }

    private void addCommand(Command command) {
        commandMap.put(command.getName(), command);
        for (String alias : command.getAliases()) {
            aliasMap.put(alias.toLowerCase(), command);
        }
    }

    public Command getCommand(String commandName) {
        Command command = aliasMap.get(commandName.toLowerCase());
        if (command != null) {
            return command;
        }
        return commandMap.get(commandName);
    }

    public void showUsage() {
        Wrapper.addMessage("§6=== FPSBoost 命令列表 ===");
        for (Command command : commandMap.values()) {
            Wrapper.addMessage(String.format("§e%s §7- %s", command.getName(), command.getAliasPart()));
        }
        Wrapper.addMessage(String.format("§6使用格式: %s<命令> [参数]", ClientCommand.prefix.getValue()));
    }
}