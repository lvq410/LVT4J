package com.lvt4j.basic;

import org.junit.Test;

import junit.framework.TestCase;

/**
 *
 * @author LV
 *
 */
public class TBloomFilterTest extends TestCase {

    @Test
    public void test() throws Exception {
        TBloomFilter<String> bloomFilter = new TBloomFilter<String>(0.1, 10000);
        System.out.println(TDataConvert.obj2ByteS(bloomFilter).length);
        int errCount = 0;
        for (int i = 0; i < 10000; i++) {
            String e = String.valueOf(i);
            if (bloomFilter.contains(e)) {
                errCount++;
            } else {
                bloomFilter.add(e);
            }
        }
        System.out.println("count:"+errCount);
        System.out.println(TDataConvert.obj2ByteS(bloomFilter).length);
    }
    
}
