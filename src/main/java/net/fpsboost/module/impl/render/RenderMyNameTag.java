package net.fpsboost.module.impl.render;

import net.fpsboost.module.Category;
import net.fpsboost.module.Module;

/**
 * @author LangYa466
 * @since 2025/1/3
 */
public class RenderMyNameTag extends Module {
    public RenderMyNameTag() {
        super(Category.RENDER);
    }

    public static boolean isEnable;

    @Override
    public void onEnable() {
        isEnable = true;
    }

    @Override
    public void onDisable() {
        isEnable = false;
    }
}