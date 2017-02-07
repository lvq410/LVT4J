package com.lvt4j.basic;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author LV
 *
 */
public class TCollectionTest {

    Collection<Integer> collection1 = new HashSet<Integer>();
    Collection<Integer> collection2 = new HashSet<Integer>();
    Collection<Integer> collection3 = new HashSet<Integer>();
    Collection<Integer> collection4 = new HashSet<Integer>();
    
    @Before
    public void before(){
        for(int i=0; i < 100; i++) collection1.add(i);
        for(int i=25; i < 125; i++) collection2.add(i);
        for(int i=50; i < 150; i++) collection3.add(i);
        for(int i=0; i < 100; i++) collection4.add(i);
    }
    
    @Test
    public void cloneMapTest() {
        Map<Object, Object> map = new HashMap<Object, Object>();
        for(int i = 0; i < 100; i++){
            map.put(i, i);
        }
        Map<Object, Object> clone = TCollection.cloneMap(map);
        Assert.assertFalse(map==clone);
        Assert.assertEquals(map.size(), clone.size());
        for(Object key: clone.keySet()){
            Assert.assertEquals(key, map.get(key));
        }
    }
    
    @Test
    public void isEqualTest(){
        Assert.assertTrue(TCollection.isEqual(collection1, collection4));
        Assert.assertTrue(TCollection.isEqual(collection1, collection1));
        Assert.assertFalse(TCollection.isEqual(collection1, collection2));
        Assert.assertTrue(TCollection.isEqual(collection2, collection2));
        Assert.assertFalse(TCollection.isEqual(collection1, collection2, collection3, collection4));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void unionTest(){
        Set<Integer> union = TCollection.union(collection1, collection2);
        Assert.assertEquals(union.size(), 125);
        for(int i=0; i < 125; i++) Assert.assertTrue(union.contains(i));
        
        union = TCollection.union(collection1, collection2, collection3, collection4);
        Assert.assertEquals(union.size(), 150);
        for(int i=0; i < 150; i++) Assert.assertTrue(union.contains(i));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void intersectionTest() {
        Set<Integer> intersection = TCollection.intersection(collection1, collection2);
        Assert.assertEquals(intersection.size(), 75);
        for(int i=25; i < 100; i++) Assert.assertTrue(intersection.contains(i));
        
        intersection = TCollection.intersection(collection1, collection2, collection3);
        Assert.assertEquals(intersection.size(), 50);
        for(int i=50; i < 100; i++) Assert.assertTrue(intersection.contains(i));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void diffTest() {
        Set<Integer> diff = TCollection.diff(collection1, collection2);
        Assert.assertEquals(diff.size(), 25);
        for(int i=0; i < 25; i++) Assert.assertTrue(diff.contains(i));
        
        diff = TCollection.diff(collection1, collection2, collection3, collection4);
        Assert.assertEquals(diff.size(), 0);
    }
    
}
