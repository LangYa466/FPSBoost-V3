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
        if (args.length != 2) {
            logUsage();
            return false;
        }
        
        String moduleName = args[1];
        Module module = Client.moduleManager.getModule(moduleName);
        if (module == null) {
            log("error", "模块不存在:", moduleName);
            return false;
        }
        
        module.toggle();
        log("ok", module.getDisplayName(), module.isEnabled());
        return true;
    }

    @Override
    public void logUsage() {
        log("usage", "用法: #toggle <模块名>");
        log("usage", "别名: #t <模块名>");
    }
}
