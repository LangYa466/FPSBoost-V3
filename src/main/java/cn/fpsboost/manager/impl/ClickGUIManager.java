package cn.fpsboost.manager.impl;

import cn.fpsboost.Client;
import cn.fpsboost.manager.Manager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author LangYa466
 * @date 2025/7/13
 */
public class ClickGUIManager extends Manager {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final File configFile = new File(Client.configDir, "clickgui.json");
    private Map<String, PanelConfig> panelConfigs;
    
    public ClickGUIManager() {
        super("ClickGUI");
    }
    
    @Override
    protected void init() {
        panelConfigs = new HashMap<>();
        super.init();
    }
    
    @Override
    public void save() {
        try {
            JsonObject config = new JsonObject();
            
            for (Map.Entry<String, PanelConfig> entry : panelConfigs.entrySet()) {
                JsonObject panelConfig = new JsonObject();
                PanelConfig config1 = entry.getValue();
                panelConfig.addProperty("x", config1.x);
                panelConfig.addProperty("y", config1.y);
                panelConfig.addProperty("expanded", config1.expanded);
                config.add(entry.getKey(), panelConfig);
            }
            
            try (FileWriter writer = new FileWriter(configFile)) {
                gson.toJson(config, writer);
            }
            
            logger.info("ClickGUI配置已保存");
        } catch (IOException e) {
            logger.error("保存ClickGUI配置失败", e);
        }
    }
    
    @Override
    public void load() {
        if (!configFile.exists()) {
            logger.info("ClickGUI配置文件不存在，跳过加载");
            return;
        }
        
        try {
            JsonObject config;
            try (FileReader reader = new FileReader(configFile)) {
                config = new JsonParser().parse(reader).getAsJsonObject();
            }
            
            for (Map.Entry<String, com.google.gson.JsonElement> entry : config.entrySet()) {
                String panelName = entry.getKey();
                JsonObject panelConfig = entry.getValue().getAsJsonObject();
                
                PanelConfig config1 = new PanelConfig();
                config1.x = panelConfig.has("x") ? panelConfig.get("x").getAsInt() : 0;
                config1.y = panelConfig.has("y") ? panelConfig.get("y").getAsInt() : 0;
                config1.expanded = !panelConfig.has("expanded") || panelConfig.get("expanded").getAsBoolean();
                
                panelConfigs.put(panelName, config1);
            }
            
            logger.info("ClickGUI配置已加载");
        } catch (Exception e) {
            logger.error("加载ClickGUI配置失败", e);
        }
    }
    
    public void savePanelConfig(String panelName, int x, int y, boolean expanded) {
        PanelConfig config = new PanelConfig();
        config.x = x;
        config.y = y;
        config.expanded = expanded;
        panelConfigs.put(panelName, config);
    }
    
    public PanelConfig getPanelConfig(String panelName) {
        return panelConfigs.getOrDefault(panelName, new PanelConfig());
    }
    
    public static class PanelConfig {
        public int x = 0;
        public int y = 0;
        public boolean expanded = true;
    }
} 