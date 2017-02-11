package com.lvt4j.basic;

import java.util.Stack;


/**
 * 缓存对象寄存器
 * 可以将暂时不用的对象存储在这里，需要的时候再取出来
 * 使用接口CacheHanlder来创建新对象或重置一个对象
 * TCacheHolder.get()来取得一个对象
 * 不用时用TCacheHolder.release()将对象再放回到缓存对象寄存器中
 * @author LV
 */
public class TCacheHolder<E> {

    private Stack<E> stack = new Stack<E>();
    private CacheHanlder<E> cacheHandler;
    
    public TCacheHolder(CacheHanlder<E> cacheHandler) {
        this.cacheHandler = cacheHandler;
    }
    
    public E get() {
        try {
            E cache = stack.pop();
            cacheHandler.reset(cache);
            return cache;
        } catch (Exception e) {
            return cacheHandler.newInstance();
        }
    }
    
    public void release(E cache) {
        stack.push(cache);
    }
    
    public static interface CacheHanlder<E>{
        
        public E newInstance();
        
        public void reset(E cache);
        
    }
    
}
