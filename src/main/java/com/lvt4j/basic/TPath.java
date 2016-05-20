package com.lvt4j.basic;

import java.net.URI;

public class TPath {
	private static String classPath;
	private static String appPath;
	static{
		try {
			classPath = TStr.trim(new URI(Thread.currentThread().getContextClassLoader().getResource("").toString()).getPath(),"/");
			appPath = System.getProperty("user.dir");
		} catch (Exception e) {}
	}
	public static String classPath() {
		return classPath;
	}
	public static<E> String classPath(Class<E> cls) {
		return cls.getClassLoader().getResource("").toString();
	}
	public static String appPath() {
		return appPath;
	}
}
