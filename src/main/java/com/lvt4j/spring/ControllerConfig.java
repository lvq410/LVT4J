package com.lvt4j.spring;

import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.AbstractJsonpResponseBodyAdvice;

import com.lvt4j.basic.TPager;

/**
 * 一些基本的Controller配置<br>
 * 需要commons-lang3包的支持<br>
 * 包括:<br>
 * ⊙jsonp支持,支持的回调函数参数名(callback,jsoncallback,jsonpcallback)<br>
 * ⊙{@link com.lvt4j.spring.Err Err}异常处理<br>
 * ⊙数据转换绑定支持<br>
 * @author LV
 */
@ControllerAdvice
public class ControllerConfig extends AbstractJsonpResponseBodyAdvice{

    /** 父类的构造函数里配置jsonp的回调函数参数名 */
    public ControllerConfig() {
        super("callback", "jsoncallback", "jsonpcallback");
    }
    
    /**
     * 父类该方法在返回jsonp数据前会被调用<br>
     * 重写此方法,在调用前向response header插入允许跨域支持的配置
     */
    @Override
    protected MediaType getContentType(MediaType contentType,
            ServerHttpRequest request, ServerHttpResponse response) {
        ServletServerHttpResponse res = (ServletServerHttpResponse) response;
        HttpHeaders headers = res.getHeaders();
        headers.set("P3P", "CP=\"IDC DSP COR ADM DEVi TAIi PSA PSD IVAi IVDi CONi HIS OUR IND CNT\"");
        headers.set("Access-Control-Allow-Origin", "*");
        return super.getContentType(contentType, request, response);
    }
    
    /**
     * 统一的{@link com.lvt4j.spring.Err Err}异常处理<br>
     * @param req
     * @param res
     * @param ex
     * @throws IOException
     */
    @ExceptionHandler(Err.class)
    @ResponseBody
    public JsonResult errExceptionHandler(HttpServletRequest req,
            HttpServletResponse res,
            Exception ex) throws IOException {
        int errCode = Err.DefFail;
        Throwable e = ex;
        if(ex instanceof Err){
            errCode = ((Err) ex).errCode();
            if(ex.getCause()!=null) e = ex.getCause();
        }
        e.printStackTrace();
        return JsonResult.fail(errCode, ex.getMessage(), e);
    }

    /**
     * Date绑定,支持格式包括<br>
     * ●'yyyy-MM-dd HH:mm:ss'<br>
     * ●'yyyyMMdd HH:mm:ss'<br>
     * ●'yyyy-MM-dd HH:mm'<br>
     * ●'yyyy-MM-dd'<br>
     * ●'yyyyMMdd'<br>
     */
    private PropertyEditorSupport dateSupport = new PropertyEditorSupport(){
        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            try {
                setValue(text==null?null:
                    DateUtils.parseDate(text, 
                            "yyyy-MM-dd HH:mm:ss", 
                            "yyyyMMdd HH:mm:ss",
                            "yyyy-MM-dd HH:mm",
                            "yyyy-MM-dd",
                            "yyyyMMdd"));
            } catch (ParseException e) {
                throw new IllegalArgumentException(e);
            }
        }
    };
    /** {@link net.sf.json.JSONObject JSONObject}绑定 */
    /** {@link net.sf.json.JSONObject JSONObject}绑定 */
    private PropertyEditorSupport jsonObjectSupport = new PropertyEditorSupport(){
        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            setValue(text==null?new JSONObject():JSONObject.fromObject(text));
        }
    };
    /** {@link net.sf.json.JSONArray JSONArray}绑定 */
    private PropertyEditorSupport jsonArraySupport = new PropertyEditorSupport(){
        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            setValue(text==null?new JSONArray():JSONArray.fromObject(text));
        }
    };
    /**
     * int[] 绑定<br>
     * 要求请求数据是int组成的jsonarray形式
     */
    private PropertyEditorSupport intArraySupport = new PropertyEditorSupport(){
        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            JSONArray jsonArray = JSONArray.fromObject(text);
            int[] ints = new int[jsonArray.size()];
            for (int i = 0; i < jsonArray.size(); i++) {
                ints[i] = jsonArray.getInt(i);
            }
            setValue(ints);
        }
    };
    /**
     * int[][] 绑定<br>
     * 要求请求数据是int组成的二维jsonarray形式
     */
    private PropertyEditorSupport intTwoDimensionArraySupport = new PropertyEditorSupport(){
        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            JSONArray jsonArray = JSONArray.fromObject(text);
            int[][] intArrs = new int[jsonArray.size()][];
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONArray subJsonArr = jsonArray.optJSONArray(i);
                if (subJsonArr==null) {
                    intArrs[i] = null;
                    continue;
                }
                int[] subArr = new int[subJsonArr.size()];
                for (int j = 0; j < subJsonArr.size(); j++) {
                    subArr[j] = subJsonArr.getInt(j);
                }
                intArrs[i] = subArr;
            }
            setValue(intArrs);
        }
    };
    /**
     * long[] 绑定<br>
     * 要求请求数据是long组成的jsonarray形式
     */
    private PropertyEditorSupport longArraySupport = new PropertyEditorSupport(){
        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            JSONArray jsonArray = JSONArray.fromObject(text);
            long[] longs = new long[jsonArray.size()];
            for (int i = 0; i < jsonArray.size(); i++) {
                longs[i] = jsonArray.getLong(i);
            }
            setValue(longs);
        }
    };
    /**
     * long[][] 绑定<br>
     * 要求请求数据是long组成的二维jsonarray形式
     */
    private PropertyEditorSupport longTwoDimensionArraySupport = new PropertyEditorSupport(){
        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            JSONArray jsonArray = JSONArray.fromObject(text);
            long[][] longArrs = new long[jsonArray.size()][];
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONArray subJsonArr = jsonArray.optJSONArray(i);
                if (subJsonArr==null) {
                    longArrs[i] = null;
                    continue;
                }
                long[] subArr = new long[subJsonArr.size()];
                for (int j = 0; j < subJsonArr.size(); j++) {
                    subArr[j] = subJsonArr.getLong(j);
                }
                longArrs[i] = subArr;
            }
            setValue(longArrs);
        }
    };
    /**
     * double[] 绑定<br>
     * 要求请求数据是double组成的jsonarray形式
     */
    private PropertyEditorSupport doubleArraySupport = new PropertyEditorSupport(){
        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            JSONArray jsonArray = JSONArray.fromObject(text);
            double[] doubles = new double[jsonArray.size()];
            for (int i = 0; i < jsonArray.size(); i++) {
                doubles[i] = jsonArray.getDouble(i);
            }
            setValue(doubles);
        }
    };
    /**
     * double[][] 绑定<br>
     * 要求请求数据是double组成的二维jsonarray形式
     */
    private PropertyEditorSupport doubleTwoDimensionArraySupport = new PropertyEditorSupport(){
        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            JSONArray jsonArray = JSONArray.fromObject(text);
            double[][] doubleArrs = new double[jsonArray.size()][];
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONArray subJsonArr = jsonArray.optJSONArray(i);
                if (subJsonArr==null) {
                    doubleArrs[i] = null;
                    continue;
                }
                double[] subArr = new double[subJsonArr.size()];
                for (int j = 0; j < subJsonArr.size(); j++) {
                    subArr[j] = subJsonArr.getDouble(j);
                }
                doubleArrs[i] = subArr;
            }
            setValue(doubleArrs);
        }
    };
    /**
     * str[] 绑定<br>
     * 要求请求数据是str组成的jsonarray形式
     */
    private PropertyEditorSupport strArraySupport = new PropertyEditorSupport(){
        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            JSONArray jsonArray = JSONArray.fromObject(text);
            String[] strArrs = new String[jsonArray.size()];
            for (int i = 0; i < jsonArray.size(); i++) {
                strArrs[i] = jsonArray.getString(i);
            }
            setValue(strArrs);
        }
    };
    /**
     * {@link com.lvt4j.basic.TPager TPager} 绑定<br>
     * 要求请求数据是jsonobject形式
     */
    private PropertyEditorSupport tPagerSupport = new PropertyEditorSupport(){
        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            TPager pager = new TPager();
            JSONObject pagerJson = JSONObject.fromObject(text);
            if(pagerJson.containsKey("pageNo")) pager.setPageNo(pagerJson.optInt("pageNo"));
            if(pagerJson.containsKey("pageSize")) pager.setPageSize(pagerJson.optInt("pageSize"));
            setValue(pager);
        }
    };
    
    
    /** 注入各种数据转换绑定支持 */
    @InitBinder
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) {
        binder.registerCustomEditor(Date.class, dateSupport);
        binder.registerCustomEditor(JSONObject.class, jsonObjectSupport);
        binder.registerCustomEditor(JSONArray.class, jsonArraySupport);
        binder.registerCustomEditor(int[].class, intArraySupport);
        binder.registerCustomEditor(int[][].class, intTwoDimensionArraySupport);
        binder.registerCustomEditor(long[].class, longArraySupport);
        binder.registerCustomEditor(long[][].class, longTwoDimensionArraySupport);
        binder.registerCustomEditor(double[].class, doubleArraySupport);
        binder.registerCustomEditor(double[][].class, doubleTwoDimensionArraySupport);
        binder.registerCustomEditor(String[].class, strArraySupport);
        binder.registerCustomEditor(TPager.class, tPagerSupport);
    }
    
}
