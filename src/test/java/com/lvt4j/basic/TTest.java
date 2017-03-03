package com.lvt4j.basic;

import java.lang.reflect.ParameterizedType;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

/**
 *
 * @author LV
 *
 */
public class TTest {

    @Test
    public void test() {
    }
    
    public static void main(String[] args) {
        List<String> a = new LinkedList<String>();
        ParameterizedType parameterizedType = (ParameterizedType)a.getClass().getGenericSuperclass();
        System.out.println(parameterizedType.getActualTypeArguments()[0]);
    }
    
}
