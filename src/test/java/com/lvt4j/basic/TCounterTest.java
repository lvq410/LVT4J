package com.lvt4j.basic;

import org.junit.Test;

/**
 *
 * @author LV
 *
 */
public class TCounterTest {

    @Test
    public void counterTest() {
        TCounter counter = new TCounter();
        for (int i = 0; i < 1000; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignore) {}
                    counter.inc();
                }
            }).start();
        }
        System.out.println(counter);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ignore) {}
        counter.waitUntil(1000, -1);
        System.out.println(counter);
        
        for (int i = 0; i < 1000; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignore) {}
                    counter.dec();
                }
            }).start();
        }
        System.out.println(counter);
        counter.waitUntil(0, -1);
        System.out.println(counter);
    }
    
}
