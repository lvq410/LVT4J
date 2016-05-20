package com.lvt4j.android;

public class TSetTimeOut extends THandlerThread {
	private String methodName;
	private long millisecond;
	private Object[] args;
	private Object obj;

	private TSetTimeOut(Object obj, String methodName, long millisecond,
			Object... args) {
		this.obj = obj;
		this.methodName = methodName;
		this.millisecond = millisecond;
		this.args = args;
	}

	@Override
	public void run() {
		try {
			sleep(millisecond);
			doMethod(obj, methodName, args);
		} catch (InterruptedException e) {
		}
	}

	public static void exe(Object obj, String methodName, long millisecond,
			Object... args) {
		new TSetTimeOut(obj, methodName, millisecond, args).start();
	}
}
