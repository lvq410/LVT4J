package com.lvt4j.basic;

import org.junit.Test;

/**
 *
 * @author LV
 *
 */
public class TSysTest {

    @Test
    public void test() {
        TSys.printThrowStackTrace(new Exception("test ex"));
    }
}
