package com.lvt4j.basic;

import java.io.Serializable;
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

        public V get(Object key) {
            V val = map.get(key);
            if (val!=null) return val;
            val = valueBuilder.build(key);
            map.put((K) key, val);
            return val;
        }
    
        public int size() {
            return map.size();
        }

        public boolean isEmpty() {
            return map.isEmpty();
        }

        public boolean containsKey(Object key) {
            return map.containsKey(key);
        }

        public boolean containsValue(Object value) {
            return map.containsValue(value);
        }

        public V put(K key, V value) {
            return map.put(key, value);
        }

        public V remove(Object key) {
            return map.remove(key);
        }

        public void putAll(Map<? extends K, ? extends V> m) {
            map.putAll(m);
        }

        public void clear() {
            map.clear();
        }

        public Set<K> keySet() {
            return map.keySet();
        }

        public Collection<V> values() {
            return map.values();
        }

        public Set<Entry<K, V>> entrySet() {
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
        
        public interface ValueBuilder<V> extends Serializable {
            
            V build(Object key);
            
        }
    }
    
}
