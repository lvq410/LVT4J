package com.lvt4j.basic;

import java.util.UUID;

public class TID {
	public final static String UU() {
		return UUID.randomUUID().toString();
	}

	public final static String Time() {
		return String.valueOf(System.nanoTime());//系统纳秒级时间
	}
}
