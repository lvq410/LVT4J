package com.lvt4j.basic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

/**
 * 集合工具
 * @author LV
 */
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
     * 自动填充map<br>
     * 当调用get方法时,若不包含该key<br>
     * 则使用构造函数的ValueBuilder.build创建值并插入<br>
     * 以此来实现链式api而不会出现空指针异常,如:<br>
     * TAutoMap.get('someKey').doSomeThing();
     */
    public static class TAutoMap<K, V> implements Map<K, V>, Serializable{
        
        private static final long serialVersionUID = 1L;
        
        private Map<K, V> map;
        private ValueBuilder<K, V> valueBuilder;
        
        /**
         * 没有指定map的实现,默认用hashmap
         * @param valueBuilder 值构建器
         */
        public TAutoMap(ValueBuilder<K, V> valueBuilder) {
            this(new HashMap<K, V>(), valueBuilder);
        }
        
        /**
         * 指定map的实现
         * @param map
         * @param valueBuilder
         */
        public TAutoMap(Map<K, V> map, ValueBuilder<K, V> valueBuilder) {
            this.map = map;
            this.valueBuilder = valueBuilder;
        }
        
        @Override
        public V get(Object key) {
            V val = map.get(key);
            if (val!=null) return val;
            val = valueBuilder.build((K) key);
            map.put((K) key, val);
            return val;
        }
    
        @Override public int size() { return map.size();}
        @Override public boolean isEmpty() { return map.isEmpty();}
        @Override public boolean containsKey(Object key) { return map.containsKey(key); }
        @Override public boolean containsValue(Object value) { return map.containsValue(value); }
        @Override public V put(K key, V value) { return map.put(key, value); }
        @Override public V remove(Object key) { return map.remove(key); }
        @Override public void putAll(Map<? extends K, ? extends V> m) { map.putAll(m); }
        @Override public void clear() { map.clear(); }
        @Override public Set<K> keySet() { return map.keySet(); }
        @Override public Collection<V> values() { return map.values(); }
        @Override public Set<Entry<K, V>> entrySet() { return map.entrySet(); }
        @Override public int hashCode() { return map.hashCode(); }
        @Override public String toString() { return map.toString(); }
        
        public interface ValueBuilder<K, V> extends Serializable {
            
            V build(K key);
            
        }
    }
    
    /**
     * 自增长list<br>
     * 当调用get,set等方法时,若大小没到该位置<br>
     * 则使用ValueBuilder.build不断创建值并加入,直到大小到达该位置
     */
    public static class TAutoList<E> implements List<E>, Serializable{

        private static final long serialVersionUID = 1L;

        private List<E> list;
        private ValueBuilder<E> valueBuilder;
        
        /**
         * 未指定的list实现,默认使用ArrayList
         * @param list
         * @param valueBuilder
         */
        public TAutoList(ValueBuilder<E> valueBuilder) {
            this(new ArrayList<E>(), valueBuilder);
        }
        /**
         * 使用指定的list实现
         * @param list
         * @param valueBuilder
         */
        public TAutoList(List<E> list, ValueBuilder<E> valueBuilder) {
            this.list = list;
            this.valueBuilder = valueBuilder;
        }
        
        @Override
        public boolean addAll(int index, Collection<? extends E> c) {
            increaseBefore(index);
            return list.addAll(index, c);
        }
        @Override
        public E get(int index) {
            increaseTo(index);
            return list.get(index);
        }
        @Override
        public E set(int index, E element) {
            increaseBefore(index);
            return list.set(index, element);
        }
        @Override
        public void add(int index, E element) {
            increaseBefore(index);
            list.add(index, element);
        }
        @Override
        public E remove(int index) {
            increaseTo(index);
            return list.remove(index);
        }
        @Override
        public ListIterator<E> listIterator(int index) {
            increaseTo(index);
            return list.listIterator(index);
        }
        @Override
        public List<E> subList(int fromIndex, int toIndex) {
            increaseTo(toIndex);
            return list.subList(fromIndex, toIndex);
        }
        
        @Override public int size() { return list.size(); }
        @Override public boolean isEmpty() { return list.isEmpty(); }
        @Override public boolean contains(Object o) { return list.contains(o); }
        @Override public Iterator<E> iterator() { return list.iterator(); }
        @Override public Object[] toArray() { return list.toArray(); }
        @Override public <T> T[] toArray(T[] a) { return list.toArray(a); }
        @Override public boolean add(E e) { return list.add(e); }
        @Override public boolean remove(Object o) { return list.remove(o); }
        @Override public boolean containsAll(Collection<?> c) { return list.containsAll(c); }
        @Override public boolean addAll(Collection<? extends E> c) { return list.addAll(c); }
        @Override public boolean removeAll(Collection<?> c) { return list.removeAll(c); }
        @Override public boolean retainAll(Collection<?> c) { return list.retainAll(c); }
        @Override public void clear() { list.clear(); }
        @Override public int indexOf(Object o) { return list.indexOf(o); }
        @Override public int lastIndexOf(Object o) { return list.lastIndexOf(o); }
        @Override public ListIterator<E> listIterator() { return list.listIterator(); }
        
        private void increaseBefore(int idx) {
            while (list.size()<idx) {
                list.add(valueBuilder.build(list.size()));
            }
        }
        private void increaseTo(int idx) {
            while (list.size()<idx) {
                list.add(valueBuilder.build(list.size()));
            }
        }
        
        public interface ValueBuilder<E> extends Serializable{
            
            E build(int idx);
            
        }
        
    }
    
}
