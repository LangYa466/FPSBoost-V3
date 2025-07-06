package net.fpsboost.module.impl.client;

import net.fpsboost.module.Category;
import net.fpsboost.module.Module;
import net.fpsboost.value.impl.TextValue;

/**
 * @author LangYa466
 * @date 2025/7/6
 */
public class ClientCommand extends Module {
    public ClientCommand() {
        super(Category.CLIENT);
        enabled = true;
    }

    public static final TextValue prefix = new TextValue("Prefix", "#");
}
