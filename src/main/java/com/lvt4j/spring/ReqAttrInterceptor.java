package com.lvt4j.spring;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * 在每个请求里加入固定的attribute<br>
 * 由setAttr(s)方法指定
 * @author LV
 */
public class ReqAttrInterceptor implements HandlerInterceptor {

    private Map<String, Object> attrs;
    
    public void setAttr(String name, Object o) {
        if(attrs==null) attrs = new HashMap<String, Object>();
        attrs.put(name, o);
    }
    public void setAttrs(Map<String, Object> attrs) {
        if(this.attrs==null) this.attrs = new HashMap<String, Object>();
        this.attrs.putAll(attrs);
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler) throws Exception {
        if(attrs==null) return true;
        for (Entry<String, Object> entry : attrs.entrySet())
            request.setAttribute(entry.getKey(), entry.getValue());
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
            HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
    }

}
