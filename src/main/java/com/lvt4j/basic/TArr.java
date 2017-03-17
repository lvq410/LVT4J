package com.lvt4j.basic;

import java.lang.reflect.Array;

/**
 * Some array tools for Java.
 * @author LV
 */
public class TArr{
    /**
     * Check an array contained an item.
     * @param arr Array to check.
     * @param item Item to check.
     * @return Contain true,not contain false.
     */
    public final static boolean contain(Object arr, Object item){
        for(int i = 0; i < len(arr); i++){
            if(Array.get(arr, i) == item) return true;
            if(Array.get(arr, i) == null) continue;
            if(Array.get(arr, i).equals(item)) return true;
        }
        return false;
    }

    public static int len(Object arr){
        if(arr == null) return 0;
        if(!arr.getClass().isArray()) return 0;
        return Array.getLength(arr);
    }

    public static final boolean equal(Object[] arr1, Object[] arr2){
        if(arr1 == arr2){ return true; }
        if((arr1 == null && arr2 != null) || (arr1 != null && arr2 == null)){ return false; }
        if(arr1.length != arr2.length){ return false; }
        for(int i = 0; i < arr1.length; i++){
            if(arr1[i] == arr2[i]) continue;
            if((arr1[i] != null && arr1[i].equals(arr2[i]))
                    || (arr2[i] != null && arr2[i].equals(arr1[i])))
                continue;
            return false;
        }
        return true;
    }
}
