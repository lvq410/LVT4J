package com.lvt4j.basic;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import com.lvt4j.basic.TThread.SplitListJobWorker;

/**
 *
 * @author LV
 *
 */
public class TThreadTest {

    @Test
    public void splitJobTest() {
        List<Integer> allJob = new LinkedList<Integer>();
        for (int i = 0; i < 1001; i++) {
            allJob.add(i);
        }
        final TCounter counter = new TCounter();
        SplitListJobWorker<Integer> worker = new SplitListJobWorker<Integer>() {
            @Override
            public void doJob(Integer job) {
                if(job%100==0) System.out.println(job);
                counter.inc();
            }
            
        };
        TThread.splitListJob(allJob, 100, worker);
        System.out.println(counter.get());
    }
    
}
