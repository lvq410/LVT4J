package com.lvt4j.android;

import java.lang.reflect.Method;

import android.os.Message;

import com.lvt4j.basic.TLog;
import com.lvt4j.basic.TReflect;

public class THandler extends android.os.Handler {
	
	public void doMethod(Object obj,String methodName, Object... args) {
		Method method = TReflect.method(obj.getClass(), methodName);
		if (method==null) {
			TLog.e("No method<"+methodName+"> in "+obj.getClass());
		} else {
			Message msg = new Message();
			Object[] methodMsg = new Object[3];
			methodMsg[0] = obj;
			methodMsg[1] = method;
			methodMsg[2]  = args;
			msg.obj = methodMsg;
			sendMessage(msg);
		}
	}
	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		try {
			Object[] methodMsg =  (Object[]) msg.obj;
			Method method = (Method) methodMsg[1];
			method.invoke(methodMsg[0], (Object[])methodMsg[2]);
		} catch (Exception e) {
			TLog.e("Error on do method.", e);
		}
	}
}
