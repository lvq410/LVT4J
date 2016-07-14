package com.lvt4j.basic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import lombok.NonNull;

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
    
    /**
     * 自动填充map
     * 具体map实现由构造函数参数控制
     * 当调用get方法时，若不包含该key，
     * 则使用构造函数的TAutoMap.ValueBuilder.build创建值并插入
     */
    public static class TAutoMap<K, V> implements Map<K, V>, Serializable{
        
        private static final long serialVersionUID = 1L;
        
        private Map<K, V> map;
        private ValueBuilder<V> valueBuilder;
        
        public TAutoMap(@NonNull Map<K, V> map, @NonNull ValueBuilder<V> valueBuilder) {
            this.map = map;
            this.valueBuilder = valueBuilder;
        }

        @Override
        public V get(Object key) {
            V val = map.get(key);
            if (val!=null) return val;
            val = valueBuilder.build(key);
            map.put((K) key, val);
            return val;
        }
    
        @Override
        public int size() {
            return map.size();
        }

        @Override
        public boolean isEmpty() {
            return map.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return map.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return map.containsValue(value);
        }

        @Override
        public V put(K key, V value) {
            return map.put(key, value);
        }

        @Override
        public V remove(Object key) {
            return map.remove(key);
        }

        @Override
        public void putAll(Map<? extends K, ? extends V> m) {
            map.putAll(m);
        }

        @Override
        public void clear() {
            map.clear();
        }

        @Override
        public Set<K> keySet() {
            return map.keySet();
        }

        @Override
        public Collection<V> values() {
            return map.values();
        }

        @Override
        public Set<java.util.Map.Entry<K, V>> entrySet() {
            return map.entrySet();
        }

        @Override
        public int hashCode() {
            return map.hashCode();
        }
        
        @Override
        public String toString() {
            return map.toString();
        }
        
        public interface ValueBuilder<V> {
            
            V build(Object key);
            
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
