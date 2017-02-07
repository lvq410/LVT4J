package com.lvt4j.basic;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 并发锁桶
 * @author LV
 */
public class TLockBucket {
    private ReentrantLock[] locks;
    
    public TLockBucket(int concurrentLevel) {
        locks = new ReentrantLock[concurrentLevel];
        for (int i = 0; i < concurrentLevel; i++) {
            locks[i] = new ReentrantLock();
        }
    }
    
    public Lock lock(Object lockKey) {
        Lock lock = locks[Math.abs(lockKey.hashCode())%locks.length];
        lock.lock();
        return lock;
    }
    
    public void unlock(Object lockKey) {
        locks[Math.abs(lockKey.hashCode())%locks.length].unlock();
    }
    
    public void run(Object lockKey, Runnable runnable) {
        lock(lockKey);
        try{
            runnable.run();
        }catch(Throwable e){
           throw new RuntimeException("锁桶内执行失败于锁:"+lockKey, e);
        }finally{
            unlock(lockKey);
        }
    }
}
