package net.fpsboost.command.impl;

import net.fpsboost.Client;
import net.fpsboost.command.Command;
import net.fpsboost.module.Module;

/**
 * @author LangYa466
 * @date 2025/5/24
 */
public class ToggleCommand extends Command {
    public ToggleCommand() {
        super("toggle", "t");
    }

    @Override
    public boolean run(String[] args) {
        if (args.length != 2) return false;
        Module module = Client.moduleManager.getModule(args[1]);
        if (module == null) return false;
        module.toggle();
        log("ok", module.getDisplayName(), module.isEnabled());
        return true;
    }
}
