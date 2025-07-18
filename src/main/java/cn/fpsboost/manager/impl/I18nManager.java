package cn.fpsboost.manager.impl;

import cn.fpsboost.manager.Manager;
import cn.fpsboost.util.lang.Langs;
import cn.fpsboost.util.misc.ResourceUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author LangYa466
 * @date 2025/5/18
 */
public class I18nManager extends Manager {
    private static Map<String, String> i18nMap;

    public I18nManager() {
        super("I18n");
    }

    @Override
    protected void init() {
        try {
            loadLang(Langs.CN);
        } catch (Exception e) {
            logger.error("读取语言文件出错", e);
        }
        super.init();
    }

    public void loadLang(Langs langs) throws IOException {
        logger.info("加载语言资源({})中...", langs.getDisplayName());
        String lang = langs.name().toLowerCase();
        InputStream resource = ResourceUtil.getResource("langs/" + lang + ".lang");
        if (resource == null) throw new RuntimeException("语言资源(" + langs.getDisplayName() + ")获取失败");

        i18nMap = new ConcurrentHashMap<>(); // reload

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue; // #是注释

                int index = line.indexOf('=');
                if (index == -1) {
                    logger.warn("无效行: {}", line);
                    continue;
                }

                String key = line.substring(0, index).trim().toLowerCase();
                String value = line.substring(index + 1).trim();

                i18nMap.put(key, value);
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public static String get(String key) {
        if (key == null) return "NullKey";
        return i18nMap.getOrDefault(key.toLowerCase(), "NoneI18n");
    }
}