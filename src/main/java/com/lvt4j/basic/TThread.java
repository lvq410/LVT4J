package com.lvt4j.basic;

import java.util.List;

/**
 * 多线程相关工具
 * @author LV
 */
public class TThread {

    /**
     * 将一个list拆分为多个子list，用多个线程进行工作<br>
     * 每个线程都会调用worker的doJob方法来处理单个list中元素<br>
     * 工作执行期间会block住直到任务完成
     * @param list
     * @param threadNum
     * @param worker
     */
    public static <E> void splitListJob(List<E> list, int threadNum, SplitListJobWorker<E> worker) {
        int subSize = list.size()/threadNum;
        TCounter locker = new TCounter();
        for (int i = 0; i < threadNum; i++) {
            int toIndex = (i+1)*subSize;
            if(i==threadNum-1) toIndex += list.size()%threadNum;
            List<E> subList = list.subList(i*subSize, toIndex);
            new SplitListJob<E>(locker, subList, worker).start();
        }
        while (!locker.is(0)) {
            synchronized (locker) {
                try {
                    locker.wait();
                } catch (InterruptedException ingore) {}
            }
        }
    }
    
    private static class SplitListJob<E> extends Thread {
        
        private TCounter locker;
        private List<E> list;
        private SplitListJobWorker<E> worker;
        
        private SplitListJob(TCounter locker, List<E> list, SplitListJobWorker<E> worker) {
            locker.inc();
            this.locker = locker;
            this.list = list;
            this.worker = worker;
        }
        
        @Override
        public void run() {
            for (E job : list) {
                try {
                    worker.doJob(job);
                } catch (Throwable e) {
                    new RuntimeException("Err on run SplitListJob for job:"+job, e).printStackTrace();
                }
            }
            locker.dec();
            synchronized (locker) {
                locker.notify();
            }
        }
        
    }
    
    /**
     * list拆分作业的具体执行者<br>
     * 注意doJob方法要尽量线程无关并且没有锁之类的
     * @author LV
     * @param <E>
     */
    public static interface SplitListJobWorker<E> {
        void doJob(E job);
    }
    
}
