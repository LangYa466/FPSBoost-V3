package net.fpsboost.util.misc;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author LangYa466
 * @date 2025/5/18
 */
public class ResourceUtil {
    public static InputStream getResource(String name) throws IOException {
        return ResourceUtil.class.getResourceAsStream("/assets/minecraft/fpsboost/" + name);
    }
}
