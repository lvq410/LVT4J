package com.lvt4j.basic;

import java.util.HashMap;
import java.util.Map;


/**
 * 类型转换器,提供一对类型直接的互相转换<br>
 * 一般用于非基本类的类型转换
 * @author LV
 */
public class TTypeAdapter {
    /** 所有已注册的类型转换器 */
    private static final Map<TypePair<?, ?>, TypeAdapter<?, ?>> TypeAdapters = new HashMap<TypePair<?, ?>, TypeAdapter<?, ?>>();
    /** 注册类型转换器 */
    public static <A, B> TypeAdapter<?, ?> registerAdapter(TypeAdapter<A, B> typeAdapter) {
        TypePair<A, B> typePair = new TypePair<A, B>(typeAdapter.clsA(), typeAdapter.clsB());
        return TypeAdapters.put(typePair, typeAdapter);
    }
    /** 获取类型转换器 */
    public static <A, B> TypeAdapter<?, ?> getAdapter(Class<A> typeA, Class<B> typeB) {
        TypePair<A, B> typePair = new TypePair<A, B>(typeA, typeB);
        return TypeAdapters.get(typePair);
    }
    
    /** 表示一组类型对 */
    private static final class TypePair<A, B> {
        private Class<A> typeA;
        private Class<B> typeB;
        
        private TypePair(Class<A> typeA, Class<B> typeB) {
            super();
            this.typeA = typeA;
            this.typeB = typeB;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj==null) return false;
            if(obj==this) return true;
            if(obj.getClass()!=TypePair.class) return false;
            return hashCode()==obj.hashCode();
        }

        @Override
        public int hashCode() {
            return typeA.hashCode()+typeB.hashCode();
        }
    }

    /**
     * 具体的类型转换器<br>
     * 应实现A与B的类型转换
     * @author LV
     * @param <A>
     * @param <B>
     */
    public static abstract class TypeAdapter<A, B>{
        public abstract Class<A> clsA();
        public abstract Class<B> clsB();
        
        @SuppressWarnings("unchecked")
        public final Object change(Object o){
            if(o==null) return null;
            if(o.getClass()==clsA()) return changeA2B((A) o);
            return changeB2A((B) o);
        }
        
        protected abstract B changeA2B(A a);
        protected abstract A changeB2A(B b);
    }
    
}
