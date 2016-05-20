package com.lvt4j.basic;

/**
 * 计数器
 * @author lichenxi
 */
public class TCounter {

    private long num;

    public synchronized long get() {
        return num;
    }

    public synchronized void set(long num) {
        this.num = num;
    }

    public synchronized long inc() {
        num += 1;
        return num;
    }

    public synchronized long dec() {
        num -= 1;
        return num;
    }
    
    public synchronized boolean is(long num) {
        return this.num==num;
    }
    
    public void waitUntil(long num, long sleep) {
        while (!is(num)) {
            try {
                Thread.sleep(sleep);
            } catch (Exception e) {
            }
        }
    }
    
}
