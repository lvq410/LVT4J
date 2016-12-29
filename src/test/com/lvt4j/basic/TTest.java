package com.lvt4j.basic;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import com.lvt4j.basic.TThread.SplitListJobWorker;

/**
 *
 * @author LV
 *
 */
public class TTest {

    @Test
    public void test() {
        Map<String, Integer> map = new HashMap<String, Integer>();
        for (int i = 0; i < 10000; i++) {
            map.put(String.valueOf(i), i);
        }
        TCounter counter = new TCounter();
        for (int i = 0; i < 1000; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        for (Entry<String, Integer> entry : map.entrySet()) {
                            int b = entry.getValue()+1;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    counter.inc();
                    System.out.println(1);
                }
            }).start();
        }
        counter.waitUntil(1000, -1);
    }
    
}
