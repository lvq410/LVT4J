package com.lvt4j.basic;

public class TSys {
	public static final String OSName = System.getProperty("os.name",
			"Unknown OS");

	public static final String lineSeparator = System.getProperty(
			"line.separator", "\r\n");

	public static boolean isLinux() {
		return OSName.toLowerCase().contains("linux");
	}

	public static boolean isMacOS() {
		String os = OSName.toLowerCase();
		return os.contains("mac") && !os.contains("x");
	}

	public static boolean isMacOSX() {
		String os = OSName.toLowerCase();
		return os.contains("mac") && os.contains("x");
	}

	public static boolean isWindows() {
		return OSName.toLowerCase().contains("windows");
	}

	public static boolean isOS2() {
		return OSName.toLowerCase().contains("os/2");
	}

	public static boolean isSolaris() {
		return OSName.toLowerCase().contains("solaris");
	}

	public static boolean isSunOS() {
		return OSName.toLowerCase().contains("sunos");
	}

	public static boolean isMPEiX() {
		return OSName.toLowerCase().contains("mpe/ix");
	}

	public static boolean isHPUX() {
		return OSName.toLowerCase().contains("hp-ux");
	}

	public static boolean isAix() {
		return OSName.toLowerCase().contains("aix");
	}

	public static boolean isOS390() {
		return OSName.toLowerCase().contains("os/390");
	}

	public static boolean isFreeBSD() {
		return OSName.toLowerCase().contains("freebsd");
	}

	public static boolean isDigitalUnix() {
		String os = OSName.toLowerCase();
		return os.contains("digital") && os.contains("unix");
	}

	public static boolean isNetWare() {
		return OSName.toLowerCase().contains("netware");
	}

	public static boolean isOSF1() {
		return OSName.toLowerCase().contains("osf1");
	}

	public static boolean isOpenVMS() {
		return OSName.toLowerCase().contains("openvms");
	}
}
