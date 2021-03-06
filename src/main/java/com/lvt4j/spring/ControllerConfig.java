package com.lvt4j.spring;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.AbstractJsonpResponseBodyAdvice;

import com.lvt4j.basic.TTypeAdapter.TypeAdapter;
import com.lvt4j.extend.typeadapter.TypeAdapterRegister;

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
public class ControllerConfig extends AbstractJsonpResponseBodyAdvice
    implements PropertyEditorRegistrar{

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
    @ExceptionHandler
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

    /** 数据绑定支持的类型(基于{@link com.lvt4j.basic.TTypeAdapter TTypeAdapter}) */
    private Class<?>[] propertyEditorSupportClses;
    private PropertyEditorSupport[] propertyEditorSupports;
    /**
     * 设置数据绑定支持的类型<br>
     * 基于{@link com.lvt4j.basic.TTypeAdapter TTypeAdapter}
     * @param propertyEditorSupportClses
     */
    public void setPropertyEditorSupportClses(
            Class<?>... propertyEditorSupportClses) {
        this.propertyEditorSupportClses = propertyEditorSupportClses;
    }
    
    /** 额外的PropertyEditor */
    private Map<Class<?>, PropertyEditor> extraPropertyEditors = new HashMap<Class<?>, PropertyEditor>();
    
    /** 添加额外的PropertyEditor */
    public PropertyEditor addExtraPropertyEditor(Class<?> cls, PropertyEditor propertyEditor) {
        return extraPropertyEditors.put(cls, propertyEditor);
    }
    
    /** 注入各种数据转换绑定支持 */
    @InitBinder
    public void initBinder(DataBinder binder) {
        binder.setAutoGrowCollectionLimit(Integer.MAX_VALUE);
        registerCustomEditors(binder);
    }

    @Override
    public void registerCustomEditors(PropertyEditorRegistry registry) {
        if(propertyEditorSupportClses==null) return;
        loadPropertySupports();
        for (int i = 0; i < propertyEditorSupportClses.length; i++) {
            registry.registerCustomEditor(propertyEditorSupportClses[i], propertyEditorSupports[i]);
        }
        for (Entry<Class<?>, PropertyEditor> entry : extraPropertyEditors.entrySet()) {
            registry.registerCustomEditor(entry.getKey(), entry.getValue());
        }
    }
    private void loadPropertySupports() {
        if(propertyEditorSupports!=null) return;
        synchronized (this) {
            propertyEditorSupports = new PropertyEditorSupport[propertyEditorSupportClses.length];
            for (int i = 0; i < propertyEditorSupportClses.length; i++) {
                Class<?> cls = propertyEditorSupportClses[i];
                propertyEditorSupports[i] = new PropertyEditorSupport(){
                    @Override
                    public void setAsText(String text)
                            throws IllegalArgumentException {
                        TypeAdapter<?, ?> typeAdapter = TypeAdapterRegister.getAdapter(cls, String.class);
                        setValue(typeAdapter.change(text));
                    }
                };
            }
        }
    }
    
}
