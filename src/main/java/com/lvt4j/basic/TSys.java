package com.lvt4j.basic;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 系统相关工具
 * @author LV
 */
public class TSys {
    
    /** 操作系统名 */
    public static final String OSName = System.getProperty("os.name",
            "Unknown OS");

    /** 换行分隔符 */
    public static final String lineSeparator = System.getProperty(
            "line.separator", "\r\n");

    public static final boolean isLinux() {
        return OSName.toLowerCase().contains("linux");
    }

    public static final boolean isMacOS() {
        String os = OSName.toLowerCase();
        return os.contains("mac") && !os.contains("x");
    }

    public static final boolean isMacOSX() {
        String os = OSName.toLowerCase();
        return os.contains("mac") && os.contains("x");
    }

    public static final boolean isWindows() {
        return OSName.toLowerCase().contains("windows");
    }

    public static final boolean isOS2() {
        return OSName.toLowerCase().contains("os/2");
    }

    public static final boolean isSolaris() {
        return OSName.toLowerCase().contains("solaris");
    }

    public static final boolean isSunOS() {
        return OSName.toLowerCase().contains("sunos");
    }

    public static final boolean isMPEiX() {
        return OSName.toLowerCase().contains("mpe/ix");
    }

    public static final boolean isHPUX() {
        return OSName.toLowerCase().contains("hp-ux");
    }

    public static final boolean isAix() {
        return OSName.toLowerCase().contains("aix");
    }

    public static final boolean isOS390() {
        return OSName.toLowerCase().contains("os/390");
    }

    public static final boolean isFreeBSD() {
        return OSName.toLowerCase().contains("freebsd");
    }

    public static final boolean isDigitalUnix() {
        String os = OSName.toLowerCase();
        return os.contains("digital") && os.contains("unix");
    }

    public static final boolean isNetWare() {
        return OSName.toLowerCase().contains("netware");
    }

    public static final boolean isOSF1() {
        return OSName.toLowerCase().contains("osf1");
    }

    public static final boolean isOpenVMS() {
        return OSName.toLowerCase().contains("openvms");
    }
    
    /** 打印异常堆栈到字符串 */
    public static String printThrowStackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        sw.write(e.getMessage());
        sw.write("\n");
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
    
}
