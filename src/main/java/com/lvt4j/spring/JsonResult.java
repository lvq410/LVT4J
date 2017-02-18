package com.lvt4j.spring;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.lvt4j.basic.TVerify;

/**
 * 统一的json返回数据格式<br>
 * <pre>{
 *   err:0,    //错误码
 *   msg:'',   //消息,如错误原因或者堆栈等,可没有
 *   stack:[], //错误的堆栈数据,可没有
 *   data:{}   //需要传的数据,各种json格式都可以,可没有
 * }</pre>
 * @author LV
 */
public class JsonResult extends HashMap<String, Object> {

    private static final long serialVersionUID = 1L;

    public static JsonResult success() {
        return success(null, null);
    }
    
    public static JsonResult success(Object data) {
        return success(data, null);
    }
    
    public static JsonResult success(Object data, String msg) {
        JsonResult rst = new JsonResult();
        rst.put("err", Err.Success);
        if (!TVerify.strNullOrEmpty(msg)) rst.put("msg", msg);
        if (data!=null) rst.put("data", data);
        return rst;
    }
    
    public static JsonResult fail() {
        return fail(Err.DefFail, null, null);
    }
    
    public static JsonResult fail(int errCode) {
        return fail(errCode, null, null);
    }
    
    public static JsonResult fail(String msg) {
        return fail(Err.DefFail, msg, null);
    }
    
    public static JsonResult fail(int errCode, String msg) {
        return fail(errCode, msg, null);
    }
    
    public static JsonResult fail(int errCode, String msg, Throwable e) {
        JsonResult rst = new JsonResult();
        rst.put("err", errCode);
        if (!TVerify.strNullOrEmpty(msg)) rst.put("msg", msg);
        if(e!=null) rst.put("stack", stack(e));
        return rst;
    }

    private static List<String> stack(Throwable e) {
        return stack(new LinkedList<String>(), e);
    }
    private static List<String> stack(List<String> stacks, Throwable e) {
        if(e==null) return stacks;
        StackTraceElement[] stackTraceElements = e.getStackTrace();
        if(stackTraceElements==null) return stacks;
        String message = e.getMessage();
        if(!TVerify.strNullOrEmpty(message)) stacks.add(message);
        for (int i = 0; i < stackTraceElements.length; i++) {
            stacks.add(stackTraceElements[i].toString());
        }
        return stack(stacks, e.getCause());
    }
    
    
    public JsonResult dataPut(Object key, Object val) {
        Map<Object, Object> data = dataGet();
        data.put(key, val);
        return this;
    }
    
    public JsonResult dataPutAll(Map<?, ?> datas) {
        dataGet().putAll(datas);
        return this;
    }
    
    @SuppressWarnings("unchecked")
    private Map<Object, Object> dataGet() {
        Object rawData = get("data");
        if(rawData instanceof Map) return (Map<Object, Object>) rawData;
        if (rawData==null) {
            Map<Object, Object> data = new HashMap<Object, Object>();
            put("data", data);
            return data;
        }
        throw new IllegalArgumentException("JsonResult的data数据["+rawData
                +"]已存在且不是个map,不能往data里继续put!");
    }
}
