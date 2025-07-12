package cn.fpsboost.module.impl.client;

import cn.fpsboost.module.Category;
import cn.fpsboost.module.Module;
import cn.fpsboost.value.impl.TextValue;

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
