package net.fpsboost.util.misc;

import lombok.Data;
import net.fpsboost.Client;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author LangYa
 * @date 2025/5/17
 */
@Data
public class LogUtil {
    private static final LinkedBlockingQueue<String> logQueue = new LinkedBlockingQueue<>();
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    private final String prefix;
    private String cachePrefix;

    static {
        Thread loggerThread = new Thread(() -> {
            while (true) {
                try {
                    String log = logQueue.take();
                    System.out.println(log);
                } catch (InterruptedException ignored) {}
            }
        }, "Log-Dispatcher");

        loggerThread.setDaemon(true);
        loggerThread.start();
    }

    public LogUtil() {
        this.prefix = "";
    }

    public LogUtil(String prefix) {
        this.prefix = prefix;
    }

    public void info(String message, Object... args) {
        log("INFO", message, args);
    }

    public void warn(String message, Object... args) {
        log("WARN", message, args);
    }

    public void error(String message, Object... args) {
        log("ERROR", message, args);
    }

    public void error(Exception e) {
        log("ERROR", exceptionToString(e));
    }

    public void error(String message, Exception e) {
        log("ERROR", message + " " + exceptionToString(e));
    }

    private void log(String level, String message, Object... args) {
        String formattedMessage = formatMessage(message, args);
        String time = timeFormat.format(new Date());
        String thread = Thread.currentThread().getName();
        String line = String.format("%s [%s] [%s/%s]: %s", getLogPrefix(), time, thread, level, formattedMessage);
        logQueue.offer(line);
    }

    private String formatMessage(String message, Object... args) {
        if (args == null || args.length == 0) return message;
        StringBuilder sb = new StringBuilder();
        int argIndex = 0;
        int cursor = 0;
        while (cursor < message.length()) {
            int brace = message.indexOf("{}", cursor);
            if (brace == -1) {
                sb.append(message.substring(cursor));
                break;
            }
            sb.append(message, cursor, brace);
            if (argIndex < args.length) {
                Object arg = args[argIndex++];
                if (!Client.isDev && (arg instanceof Double || arg instanceof Float)) {
                    sb.append(String.format("%.2f", ((Number) arg).doubleValue()));
                } else {
                    sb.append(arg);
                }
            } else {
                sb.append("{}");
            }
            cursor = brace + 2;
        }
        return sb.toString();
    }

    private String getLogPrefix() {
        if (cachePrefix != null) return cachePrefix;
        if (!prefix.isEmpty()) {
            cachePrefix = String.format("[%s]-[%s]", Client.getDisplayName(), prefix);
        } else {
            cachePrefix = String.format("[%s]", Client.getDisplayName());
        }
        return cachePrefix;
    }

    private String exceptionToString(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
