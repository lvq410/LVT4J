package com.lvt4j.basic;

import lombok.NonNull;

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
    public boolean add(@NonNull Object obj) {
        int pos = obj.hashCode()%bits.length;
        if(bits[pos]) return false;
        bits[pos] = true;
        return true;
    }
    
    public boolean contains(@NonNull Object obj) {
        int pos = obj.hashCode()%bits.length;
        return bits[pos];
    }
    
    public byte[] serialize() {
        return TDataConvert.bitS2ByteS(bits);
    }
    
    public static THashBucket deserialize(byte[] bytes) {
        THashBucket hashBucket = new THashBucket();
        hashBucket.bits = TDataConvert.byteS2BitS(bytes);
        return hashBucket;
    }
    
}
