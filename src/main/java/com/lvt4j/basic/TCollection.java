package com.lvt4j.basic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class TCollection {
    
    public static final <E, F> Map<E, F> cloneMap(Map<E, F> map) {
        try {
            Map<E, F> clone = map.getClass().newInstance();
            clone.putAll(map);
            return clone;
        } catch (Exception e) {
        }
        return null;
    }

    public static final <E> boolean isEqual(Collection<E> set1,
            Collection<E> set2) {
        if (set1==set2) return true;
        if (set1==null && set2!=null) return false;
        if (set1!=null && set2==null) return false;
        if (set1.size() != set2.size()) return false;
        for (E e : set1) if (!set2.contains(e)) return false;
        return true;
    }
    
    public static class TAutoHashMap<K, V> extends HashMap<K, V>{
        
        private static final long serialVersionUID = 1L;
        
        private ValueBuilder<V> valueBuilder;
        
        public TAutoHashMap(ValueBuilder<V> valueBuilder) {
            this.valueBuilder = valueBuilder;
        }

        @Override
        public V get(Object key) {
            V val = super.get(key);
            if (val==null) {
                val = valueBuilder.build();
                put((K) key, val);
            }
            return val;
        }
    
        public interface ValueBuilder<V> {
            
            V build();
            
        }
    }

    public static class TDistinctList<E> extends ArrayList<E>{

        private static final long serialVersionUID = 1L;
        
        @Override
        public boolean add(E object) {
            if (!contains(object)) {
                return super.add(object);
            }
            return false;
        }
        
        @Override
        public void add(int index, E object) {
            if (!contains(object)) {
                super.add(index, object);
            }
        }
        
        @Override
        public boolean addAll(Collection<? extends E> collection) {
            boolean changed = false;
            for (E e: collection) {
                changed |= add(e);
            }
            return changed;
        }
        
        @Override
        public boolean addAll(int index, Collection<? extends E> collection) {
            boolean changed = false;
            for (E e: collection) {
                if (!contains(e)) {
                    add(index++, e);
                    changed = true;
                }
            }
            return changed;
        }
        
    }
    
}
