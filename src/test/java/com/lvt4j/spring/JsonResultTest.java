package com.lvt4j.spring;

import net.sf.json.JSONObject;

import org.junit.Test;

/**
 *
 * @author LV
 *
 */
public class JsonResultTest {

    @Test
    public void stackTest() {
        Exception e = new Exception("root");
        for (int i = 0; i < 10; i++) {
            e = new Exception(String.valueOf(i), e);
        }
        System.out.println(JSONObject.fromObject(JsonResult.fail(1, "msg", e)));
    }
    
}
