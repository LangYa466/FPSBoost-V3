package cn.fpsboost.module.impl.render;

import cn.fpsboost.module.Category;
import cn.fpsboost.module.Module;

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