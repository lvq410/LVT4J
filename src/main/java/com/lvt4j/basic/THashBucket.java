package com.lvt4j.basic;


/**
 *
 * @author LV
 */
public class THashBucket {

    private boolean[] bits;
    
    private THashBucket() {}
    
    /**
     * @param capacity 容量
     */
    public THashBucket(int capacity) {
        capacity += 8-capacity%8;
        bits = new boolean[capacity];
    }
    
    /**
     * @param obj
     * @return true表示不存在，false表存在
     */
    public boolean add(Object obj) {
        int pos = Math.abs(obj.hashCode()%bits.length);
        if(bits[pos]) return false;
        bits[pos] = true;
        return true;
    }
    
    public boolean contains(Object obj) {
        int pos = Math.abs(obj.hashCode()%bits.length);
        return bits[pos];
    }
    
    public byte[] serialize() {
        return TBaseDataConvert.bitS2ByteS(bits);
    }
    
    public static THashBucket deserialize(byte[] bytes) {
        THashBucket hashBucket = new THashBucket();
        hashBucket.bits = TBaseDataConvert.byteS2BitS(bytes);
        return hashBucket;
    }
    
}
