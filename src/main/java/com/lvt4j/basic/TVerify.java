package com.lvt4j.basic;

import java.lang.reflect.Array;

/**
 * Verify something.
 * @author LV
 *
 */
public class TVerify {
    /**
     * Verify string is null or "".
     * @param str String to verify.
     * @return null or "" is true, else false.
     */
    public final static boolean strNullOrEmpty(String str) {
        return str==null || str.isEmpty();
    }
    /**
     * Verify Array is null or length==0.
     * @param arr Array to verify.
     * @return null or length==0 is true, else false.
     */
    public final static boolean arrNullOrEmpty(Object arr) {
        return arr==null || (arr.getClass().isArray() && Array.getLength(arr)==0);
    }
}
