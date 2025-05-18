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

    private String cachePrefix;
    public String getLogPrefix() {
        if (cachePrefix != null) return cachePrefix;
        if (!prefix.isEmpty()) {
            cachePrefix = String.format("[%s]-[%s] ", Client.getDisplayName(), prefix);
        } else {
            cachePrefix = String.format("[%s] ", Client.getDisplayName());
        }
        return cachePrefix;
    }
}