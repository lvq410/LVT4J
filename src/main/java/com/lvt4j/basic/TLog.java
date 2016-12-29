package com.lvt4j.basic;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TLog {
    public static final int ASSERT = 0;
    public static final int ERROR = 1;
    public static final int WARN = 2;
    public static final int INFO = 3;
    public static final int DEBUG = 4;
    public static final int VERBOSE = 5;

    private static final String[] LevelStr = new String[] { "ASSERT", "ERROR",
            "WARN", "INFO", "DEBUG", "VERBOSE" };

    /** 是否已初始化 */
    private static boolean initialized;
    private static Boolean tLogSwitch = false; // 日志文件总开关
    private static Boolean toConsoleSwitch = false; // 输出日志到控制台开关
    private static Boolean toFileSwitch = false; // 日志写入文件开关
    private static Boolean toAndoridSwitch = false; // 日志写入Andorid开关
    private static String logFolderPath = ""; // 日志文件夹
    private static String logFileName = ""; // 日志文件名
    private static String androidTag = ""; // Android Log TAG
    private static int logLevel = VERBOSE;
    private static DateFormat dateFormat = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss:SSS");
    
    /** 是否已初始化 */
    public static boolean isInitialized() {
        return initialized;
    }
    
    public static void openLog() {
        tLogSwitch = true;
        initialized = true;
    }

    public static void closeLog() {
        tLogSwitch = false;
    }

    public static void openLog2Console() {
        toConsoleSwitch = true;
    }

    public static void closeLog2Console() {
        toConsoleSwitch = false;
    }

    public static void openLog2Andorid(String tag) {
        toAndoridSwitch = true;
        androidTag = tag;
    }

    public static void closeLog2Android() {
        toAndoridSwitch = false;
    }

    public static void openLog2File(String logFolderPath, String logFileName) {
        toFileSwitch = true;
        File logFolder = new File(logFolderPath);
        if (!logFolder.exists())
            logFolder.mkdirs();
        TLog.logFolderPath = TStr.trim(logFolder.getPath(), "\\");
        TLog.logFileName = logFileName;
    }

    public static void closeLog2File() {
        toFileSwitch = false;
    }

    public static void setLevel(int level) {
        TLog.logLevel = level;
    }

    public static void v(String msg) {
        log(VERBOSE, msg, null);
    }

    public static void v(String msg, Throwable e) {
        log(VERBOSE, msg, e);
    }

    public static void d(String msg, Throwable e) {
        log(DEBUG, msg, e);
    }

    public static void d(String msg) {
        log(DEBUG, msg, null);
    }

    public static void i(String msg, Throwable e) {
        log(INFO, msg, e);
    }

    public static void i(String msg) {
        log(INFO, msg, null);
    }

    public static void w(String msg, Throwable e) {
        log(WARN, msg, e);
    }

    public static void w(String msg) {
        log(WARN, msg, null);
    }

    public static void e(String msg, Throwable e) {
        log(ERROR, msg, e);
    }

    public static void e(String msg) {
        log(ERROR, msg, null);
    }

    public static void a(String msg, Throwable e) {
        log(ASSERT, msg, e);
    }

    public static void a(String msg) {
        log(ASSERT, msg, null);
    }

    public static String printStackTrace(Throwable e) {
        StringBuilder sb = new StringBuilder();
        StackTraceElement[] stackS = e.getStackTrace();
        for (int i = 0; i < stackS.length; i++) {
            sb.append("    at ").append(stackS[i].toString());
            if (i < stackS.length - 1) {
                sb.append(TSys.lineSeparator);
            }
        }
        return sb.toString();
    }

    private static StringBuilder parseLog(int level, String msg, Throwable e) {
        StringBuilder log = new StringBuilder().append('[')
                .append(LevelStr[level]).append(':').append(TSys.lineSeparator)
                .append("    ").append(msg).append(TSys.lineSeparator)
                .append(']').append(TSys.lineSeparator);
        StackTraceElement curStack = Thread.currentThread().getStackTrace()[4];
        log.append('[').append(dateFormat.format(new Date()))
                .append(TSys.lineSeparator).append("    ")
                .append(curStack.toString()).append(TSys.lineSeparator)
                .append(']').append(TSys.lineSeparator);
        if (e != null) {
            log.append('[').append("Exception:").append(TSys.lineSeparator)
                    .append("    ").append(e.getMessage())
                    .append(TSys.lineSeparator).append(printStackTrace(e))
                    .append(TSys.lineSeparator).append(']')
                    .append(TSys.lineSeparator);
        }
        log.append(TSys.lineSeparator);
        return log;
    }

    private static void log(int level, String msg, Throwable e) {
        if (!tLogSwitch)
            return;
        if (level > logLevel)
            return;
        StringBuilder log = null;
        if (toConsoleSwitch) {
            if (level == ERROR) {
                System.err
                        .print(log == null ? (log = parseLog(level, msg, e))
                                : log);
            } else {
                System.out
                        .print(log == null ? (log = parseLog(level, msg, e))
                                : log);
            }
        }
        if (toAndoridSwitch)
            androidLog(level, msg, e);
        if (toFileSwitch)
            log2File(log == null ? (log = parseLog(level, msg, e)) : log);

    }

    private static void androidLog(int level, String msg, Throwable e) {
        try {
            Class<?> cls = Class.forName("android.util.Log");
            Method logMethod = null;
            switch (level) {
            case ASSERT:
            case ERROR:
                logMethod = e == null ? cls.getMethod("e", String.class,
                        String.class) : cls.getMethod("e", String.class,
                        String.class, Throwable.class);
                break;
            case INFO:
                logMethod = e == null ? cls.getMethod("i", String.class,
                        String.class) : cls.getMethod("i", String.class,
                        String.class, Throwable.class);
                break;
            case DEBUG:
                logMethod = e == null ? cls.getMethod("d", String.class,
                        String.class) : cls.getMethod("d", String.class,
                        String.class, Throwable.class);
                break;
            case VERBOSE:
                logMethod = e == null ? cls.getMethod("v", String.class,
                        String.class) : cls.getMethod("v", String.class,
                        String.class, Throwable.class);
                break;
            }
            if (e == null) {
                logMethod.invoke(cls, androidTag, msg);
            } else {
                logMethod.invoke(cls, androidTag, msg, e);
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    private static void log2File(StringBuilder log) {
        File logFile = new File(logFolderPath + "/" + logFileName + "."
                + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".tlog");
        try {
            FileWriter filerWriter = new FileWriter(logFile, true);
            BufferedWriter bufWriter = new BufferedWriter(filerWriter);
            bufWriter.write(log.toString());
            bufWriter.close();
            filerWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}