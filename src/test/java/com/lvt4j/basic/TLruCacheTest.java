package com.lvt4j.basic;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

/**
 *
 * @author LV
 *
 */
public class TLruCacheTest {

    @Test
    public void test() {
        List<Integer> datas = new LinkedList<Integer>();
        for (int i = 0; i < 10000; i++) {
            datas.add((int)((Math.random()*100)%10));
        }
        final TLruCache<Integer, Integer> cache = new TLruCache<Integer, Integer>(20);
        TThread.splitListJob(datas, 10, (data)->{
            cache.put(data, data);
            cache.get(data);
        });
        System.out.println(cache);
        cache.clear();
        System.out.println(cache);
        cache.put(100, 100);
        TThread.splitListJob(datas, 10, (data)->{
            cache.put(data, null);
            cache.get(data);
        });
        System.out.println(cache);
        cache.put(100, 100);
        System.out.println(cache);
    }
    
}
