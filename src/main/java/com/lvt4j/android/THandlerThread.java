package com.lvt4j.android;

import java.lang.reflect.Method;

import com.lvt4j.basic.TLog;
import com.lvt4j.basic.TReflect;

import android.os.Handler;
import android.os.Message;

/**
 * 用于刷新UI的线程使用，与THandler配套使用
 * 
 * @author LV
 * 
 */
public class THandlerThread extends Thread {

	public static THandler handler = new THandler();

	public void doMethod(Object obj, String methodName, Object... args) {
		handler.doMethod(obj, methodName, args);
	}

	private static class THandler extends Handler {
		private void doMethod(Object obj, String methodName, Object... args) {
			Method method = TReflect.method(obj.getClass(), methodName);
			if (method == null) {
				TLog.e("No method<" + methodName + "> in " + obj.getClass());
			} else {
				Message msg = new Message();
				Object[] methodMsg = new Object[3];
				methodMsg[0] = obj;
				methodMsg[1] = method;
				methodMsg[2] = args;
				msg.obj = methodMsg;
				sendMessage(msg);
			}
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			try {
				Object[] methodMsg = (Object[]) msg.obj;
				((Method) methodMsg[1]).invoke(methodMsg[0],
						(Object[]) methodMsg[2]);
			} catch (Exception e) {
				TLog.e("Error on do method.", e);
			}
		}
	}
}
