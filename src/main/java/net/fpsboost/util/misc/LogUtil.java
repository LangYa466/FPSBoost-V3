package net.fpsboost.util.misc;

import lombok.Data;
import net.fpsboost.Client;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author LangYa466
 * @date 2025/5/17
 */
@Data
public class LogUtil {
    private final String prefix;
    private final Logger logger;

    public LogUtil() {
        this.prefix = ""; // net/fpsboost/Client.java
        this.logger = LogManager.getLogger(prefix);
    }

    public LogUtil(String prefix) {
        this.prefix = prefix;
        this.logger = LogManager.getLogger(prefix);
    }

    public void info(String message, Object... args) {
        logger.info("{}{}", getLogPrefix(), message, args);
    }

    public void warn(String message, Object... args) {
        logger.warn("{}{}", getLogPrefix(), message, args);
    }

    public void error( Exception e) {
        logger.error("{}{}", getLogPrefix(), e.getLocalizedMessage());
    }

    public void error(String message, Exception e) {
        logger.error("{}{}", getLogPrefix(), message + " " + e.getLocalizedMessage());
    }

    public void error(String message, Object... args) {
        logger.error("{}{}", getLogPrefix(), message, args);
    }

    private String cache;
    public String getLogPrefix() {
        if (cache != null) return cache;
        if (!prefix.isEmpty()) {
            cache = String.format("[%s]-[%s] ", Client.getDisplayName(), prefix);
        } else {
            cache = String.format("[%s] ", Client.getDisplayName());
        }
        return cache;
    }
}