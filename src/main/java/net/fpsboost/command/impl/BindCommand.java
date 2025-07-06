package net.fpsboost.command.impl;

import net.fpsboost.Client;
import net.fpsboost.command.Command;
import net.fpsboost.module.Module;
import org.lwjgl.input.Keyboard;

/**
 * @author LangYa466
 * @date 2025/7/6
 */
public class BindCommand extends Command {
    public BindCommand() {
        super("bind", "b");
    }

    @Override
    public boolean run(String[] args) {
        if (args.length != 3) {
            logUsage();
            return false;
        }
        
        String moduleName = args[1];
        String keyName = args[2];
        
        Module module = Client.moduleManager.getModule(moduleName);
        if (module == null) {
            log("error", "模块不存在:", moduleName);
            return false;
        }
        
        int keyCode;
        if (keyName.equalsIgnoreCase("none")) {
            keyCode = 0;
        } else {
            keyCode = Keyboard.getKeyIndex(keyName.toUpperCase());
            if (keyCode == Keyboard.KEY_NONE) {
                log("error", "无效的按键:", keyName);
                return false;
            }
        }
        
        module.setKeyCode(keyCode);
        if (keyCode == 0) {
            log("ok", module.getDisplayName(), "已解除绑定");
        } else {
            log("ok", module.getDisplayName(), "绑定到按键:", keyName.toUpperCase());
        }
        return true;
    }

    @Override
    public void logUsage() {
        log("usage", "用法: #bind <模块名> <按键>");
        log("usage", "别名: #b <模块名> <按键>");
        log("usage", "示例: #bind Sprint R");
        log("usage", "解除绑定: #bind <模块名> none");
    }
} 