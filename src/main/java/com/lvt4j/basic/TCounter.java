package com.lvt4j.basic;

/**
 * 计数器
 * @author LV
 */
public class TCounter {

    private volatile long num;

    /** 数值变化是否需要通知 */
    private volatile boolean isNeedNotify;
    
    public TCounter() {}
    
    public TCounter(long num) {
        this.num = num;
    }
    
    public long get() {
        return num;
    }

    public synchronized void set(long num) {
        this.num = num;
        notifyIfNeed();
    }

    public synchronized long inc() {
        num += 1;
        notifyIfNeed();
        return num;
    }

    public synchronized long dec() {
        num -= 1;
        notifyIfNeed();
        return num;
    }
    
    public boolean is(long num) {
        return this.num==num;
    }
    
    private void notifyIfNeed() {
        if(!isNeedNotify) return;
        synchronized (this) {
            notify();
        }
    }
    
    /**
     * 等待直到指定数值
     * @param num 指定数值
     * @param timeout 超时时间(ms),-1为不超时
     */
    public void waitUntil(long num, long timeout) {
        isNeedNotify = true;
        while (this.num!=num) {
            synchronized (this) {
                if(this.num==num) break;
                try {
                    if(timeout<=-1) {
                        wait();
                    } else {
                        wait(timeout);
                    }
                } catch (InterruptedException ignore) {}
            }
        }
        isNeedNotify = false;
    }
    /**
     * 一直等待直到指定数值
     * @param num 指定数值
     */
    public void waitUntil(long num) {
        waitUntil(num, -1);
    }
    
    @Override
    public String toString() {
        return String.valueOf(num);
    }
    
    @Override
    public int hashCode() {
        return Long.hashCode(num);
    }
    
}
