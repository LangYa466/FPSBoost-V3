package net.fpsboost.manager.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import net.fpsboost.Client;
import net.fpsboost.event.EventTarget;
import net.fpsboost.event.impl.KeyEvent;
import net.fpsboost.manager.Manager;
import net.fpsboost.module.Module;
import net.fpsboost.module.impl.client.ClientCommand;
import net.fpsboost.module.impl.dev.TestClickGUI;
import net.fpsboost.module.impl.dev.TestDragGUI;
import net.fpsboost.module.impl.dev.TestValue;
import net.fpsboost.module.impl.misc.DiscordRPC;
import net.fpsboost.module.impl.misc.Sprint;
import net.fpsboost.module.impl.render.FPSDisplay;
import net.fpsboost.module.impl.render.ModuleList;
import net.fpsboost.value.Value;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author LangYa466
 * @date 2025/5/17
 */
@Getter
public class ModuleManager extends Manager {
    private Map<String, Module> modules;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final File configFile = new File(Client.configDir, "modules.json");

    public ModuleManager() {
        super("Module");
    }

    @Override
    public void init() {
        modules = new HashMap<>();

        // Misc
        addModule(new Sprint());
        addModule(new DiscordRPC());

        // Render
        addModule(new FPSDisplay());
        addModule(new ModuleList());

        // Dev
        addModule(new TestDragGUI());
        addModule(new TestClickGUI());
        addModule(new TestValue());

        // Client
        addModule(new ClientCommand());

        Client.eventManager.register(this);
        super.init();
    }

    @Override
    public void save() {
        try {
            JsonObject config = new JsonObject();
            
            for (Module module : modules.values()) {
                JsonObject moduleConfig = new JsonObject();
                moduleConfig.addProperty("enabled", module.isEnabled());
                moduleConfig.addProperty("keyCode", module.getKeyCode());
                
                JsonObject valuesConfig = new JsonObject();
                for (Value<?> value : module.getValues()) {
                    valuesConfig.addProperty(value.getName(), value.getValue().toString());
                }
                moduleConfig.add("values", valuesConfig);
                
                config.add(module.getName(), moduleConfig);
            }
            
            try (FileWriter writer = new FileWriter(configFile)) {
                gson.toJson(config, writer);
            }
            
            logger.info("模块配置已保存");
        } catch (IOException e) {
            logger.error("保存模块配置失败", e);
        }
    }

    @Override
    public void load() {
        if (!configFile.exists()) {
            logger.info("模块配置文件不存在，跳过加载");
            return;
        }
        
        try {
            JsonObject config;
            try (FileReader reader = new FileReader(configFile)) {
                config = new JsonParser().parse(reader).getAsJsonObject();
            }
            
            for (Map.Entry<String, com.google.gson.JsonElement> entry : config.entrySet()) {
                String moduleName = entry.getKey();
                Module module = modules.get(moduleName);
                
                if (module == null) {
                    logger.warn("配置文件中存在未知模块: {}", moduleName);
                    continue;
                }
                
                JsonObject moduleConfig = entry.getValue().getAsJsonObject();
                
                if (moduleConfig.has("enabled")) {
                    module.setEnabled(moduleConfig.get("enabled").getAsBoolean());
                }
                
                if (moduleConfig.has("keyCode")) {
                    module.setKeyCode(moduleConfig.get("keyCode").getAsInt());
                }
                
                if (moduleConfig.has("values")) {
                    JsonObject valuesConfig = moduleConfig.getAsJsonObject("values");
                    for (Value<?> value : module.getValues()) {
                        if (valuesConfig.has(value.getName())) {
                            String valueStr = valuesConfig.get(value.getName()).getAsString();
                            setValueFromString(value, valueStr);
                        }
                    }
                }
            }
            
            logger.info("模块配置已加载");
        } catch (Exception e) {
            logger.error("加载模块配置失败", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void setValueFromString(Value<?> value, String valueStr) {
        try {
            Object val = value.getValue();
            if (val instanceof Boolean) {
                ((Value<Boolean>) value).setValue(Boolean.parseBoolean(valueStr));
            } else if (val instanceof Integer) {
                ((Value<Integer>) value).setValue(Integer.parseInt(valueStr));
            } else if (val instanceof Double) {
                ((Value<Double>) value).setValue(Double.parseDouble(valueStr));
            } else if (val instanceof Float) {
                ((Value<Float>) value).setValue(Float.parseFloat(valueStr));
            } else if (val instanceof String) {
                ((Value<String>) value).setValue(valueStr);
            }
        } catch (Exception e) {
            logger.warn("无法解析值 {} 为类型 {}", valueStr, value.getValue().getClass().getSimpleName());
        }
    }

    @EventTarget
    public void onKey(KeyEvent event) {
        modules.values().forEach(module -> {
            if(event.getKeyCode() == module.getKeyCode()) module.toggle();
        });
    }

    public void addModule(Module module) {
        String moduleName = module.getName();
        modules.put(moduleName, module);
        module.onInit();
        logger.info("已注册模块 {}", moduleName);

        // dev
        if (Client.isDev) module.setEnabled(true);
    }

    public Module getModule(String moduleName) {
        return modules.get(moduleName);
    }

    public boolean isEnabled(Class<? extends Module> moduleClass) {
        for (Module module : modules.values()) {
            if (module.getClass() == moduleClass) return module.isEnabled();
        }
        return false;
    }
}
