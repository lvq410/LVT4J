package com.lvt4j.spring;

import java.io.IOException;
import java.nio.charset.Charset;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import com.lvt4j.basic.TVerify;
import com.sun.istack.internal.Nullable;

/**
 *
 * @author lichenxi
 */
public class JsonpSupporter {
    
    private static final String[] CallbackParamKeys = {"callback","jsonpcallback","jsoncallback"};
    
    public static class JsonpCallback {

        private static MediaType jsonMediaType = new MediaType("application", "json", Charset.defaultCharset());
        private static MediaType jsonpMediaType = new MediaType("application", "x-javascript", Charset.defaultCharset());
        
        private static ThreadLocal<String> callback = new ThreadLocal<String>();
        
        public static void extractCallback(HttpServletRequest req) {
            String callback = null;
            for (String callbackParamKey: CallbackParamKeys) {
                callback = req.getParameter(callbackParamKey);
                if (!TVerify.strNullOrEmpty(callback)) break;
            }
            JsonpCallback.callback.set(callback);
        }
        
        private byte[] data;
        
        public JsonpCallback(@Nullable Object rst) {
            String callback = JsonpCallback.callback.get();
            if (rst==null) rst = "";
            String data = callback==null?rst.toString():callback+"("+rst.toString()+");";
            this.data = data.getBytes();
        }
        
        public JsonpCallback() {
            this(null);
        }
        
        @Override
        public String toString() {
            return new String(data);
        }
        
        public MediaType mediaType() {
            return callback.get()==null?jsonMediaType:jsonpMediaType;
        }
        
        public byte[] getData() {
            return data;
        }
        
        public int length() {
            return data.length;
        }
        
        public void toResponse(HttpServletResponse res) throws IOException{
            res.setContentType(mediaType().toString());
            res.setContentLength(data.length);
            res.getOutputStream().write(data);
            res.getOutputStream().close();
        }
        
    }
    
    public static class JsonpFilter implements Filter{

        @Override
        public void doFilter(ServletRequest request, ServletResponse response,
                FilterChain chain) throws IOException, ServletException {
            HttpServletRequest req = (HttpServletRequest)request;
            HttpServletResponse res = (HttpServletResponse) response;
            res.setHeader("P3P", "CP=\"IDC DSP COR ADM DEVi TAIi PSA PSD IVAi IVDi CONi HIS OUR IND CNT\"");
            res.setHeader("Access-Control-Allow-Origin", "*");
            JsonpCallback.extractCallback(req);
            chain.doFilter(req, res);
        }

        @Override
        public void init(FilterConfig config) throws ServletException {
        }
        
        @Override
        public void destroy() {
        }
    }
    
    public static class JsonpCallbackHttpMessageConverter extends AbstractHttpMessageConverter<JsonpCallback>{
        
        @Override
        public boolean supports(Class<?> clazz) {
            return JsonpCallback.class==clazz;
        }

        @Override
        protected Long getContentLength(JsonpCallback jsonpCallback, MediaType contentType)
                throws IOException {
            return (long) jsonpCallback.toString().getBytes().length;
        }
        
        @Override
        protected JsonpCallback readInternal(Class<? extends JsonpCallback> clazz,
                HttpInputMessage inputMessage) throws IOException,
                HttpMessageNotReadableException {
            return null;
        }
        
        @Override
        protected void writeInternal(JsonpCallback jsonpCallback, HttpOutputMessage outputMessage)
                throws IOException, HttpMessageNotWritableException {
            outputMessage.getHeaders().setContentType(jsonpCallback.mediaType());
            outputMessage.getHeaders().setContentLength(jsonpCallback.length());
            outputMessage.getBody().write(jsonpCallback.data);
            outputMessage.getBody().close();
        }
    }

    
}
