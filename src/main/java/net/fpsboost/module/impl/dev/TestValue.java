package net.fpsboost.module.impl.dev;

import net.fpsboost.module.Category;
import net.fpsboost.module.Module;
import net.fpsboost.value.impl.BooleanValue;
import net.fpsboost.value.impl.ModeValue;
import net.fpsboost.value.impl.NumberValue;

/**
 * @author LangYa466
 * @date 2025/7/6
 */
public class TestValue extends Module {
    public TestValue() {
        super(Category.DEV);
    }

    public final NumberValue numberValue = new NumberValue("A",1,10,1,1);
    public final BooleanValue booleanValue = new BooleanValue("213131",true);
    public final ModeValue modeValue = new ModeValue("A","Mode1","Mode1",
            "Mode2");
}
