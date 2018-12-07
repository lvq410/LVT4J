package com.lvt4j.basic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

/**
 * 集合工具
 * @author LV
 */
public class TCollection {
    
    /**
     * 克隆参数map,浅度克隆<br>
     * 会使用参数map类的无参构造函数<br>
     * 出现异常时会抛出运行时异常
     * @param map
     * @return 克隆的map
     */
    @SuppressWarnings("unchecked")
    public static final <K, V> Map<K, V> cloneMap(Map<K, V> map) {
        try {
            Map<K, V> clone = map.getClass().newInstance();
            clone.putAll(map);
            return clone;
        } catch (Throwable e) {
            throw new RuntimeException("复制map["+map+"]失败!", e);
        }
    }

    /**
     * 判断多个集合是否相同<br>
     * 集合数量小于2时,永真<br>
     * 不检查集合的类是否相同,只检查集合的元素是否相同
     * @param collections
     * @return 参数中的集合是否相同
     */
    public static final boolean isEqual(Collection<?>... collections) {
        if(collections==null || collections.length<2) return true;
        Collection<?> first = collections[0];
        for(int i=1; i < collections.length; i++){
            Collection<?> collection = collections[i];
            if(first==null && collection!=null) return false;
            if(first!=null && collection==null) return false;
            if(first==collection) continue;
            if (first.size() != collection.size()) return false;
            for (Object e : first) if (!collection.contains(e)) return false;
        }
        return true;
    }
    
    /**
     * 多个集合的并集<br>
     * 参数若为null当空集合处理
     * @param collections
     * @return
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static final Set union(Collection... collections){
        Set set = new HashSet();
        if(collections==null) return set;
        for(Collection collection: collections){
            if(collection==null) continue;
            set.addAll(collection);
        }
        return set;
    }
    
    /**
     * 多个集合的交集<br>
     * 若参数数量小于2,返回空集合<br>
     * 参数若为null当空集合处理
     * @param collections
     * @return
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static final Set intersection(Collection... collections){
        if(collections==null || collections.length<2) return new HashSet();
        Set intersection = new HashSet();
        intersection.addAll(collections[0]);
        Set tmp = new HashSet();
        for(int i=1; i < collections.length; i++){
            Collection collection = collections[i];
            if(collection==null || collection.isEmpty()) return new HashSet();
            for(Object e: collection){
                if(!intersection.contains(e)) continue;
                tmp.add(e);
            }
            intersection.clear();
            intersection.addAll(tmp);
            tmp.clear();
        }
        return intersection;
    }
    
    /**
     * 计算第一个集合与其后所有集合的差集<br>
     * 若参数数量小于1,返回空集合
     * @param collections
     * @return
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static final Set diff(Collection... collections){
        if(collections==null || collections.length==0) return new HashSet();
        Set diff = new HashSet();
        diff.addAll(collections[0]);
        for(int i=1; i < collections.length; i++){
            Collection collection = collections[i];
            if(collection==null) continue;
            for(Object e: collection)
                if(diff.contains(e)) diff.remove(e);
        }
        return diff;
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
