package com.lvt4j.basic;

import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

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
        JSONObject b = JSONObject.fromObject("{a:null}");
        System.out.println(b.get("a")==JSONNull.getInstance());
    }
    
}
