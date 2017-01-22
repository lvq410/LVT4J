package com.lvt4j.extend;

import java.util.Date;

import org.junit.Test;

import com.lvt4j.basic.TTypeAdapter;
import com.lvt4j.basic.TTypeAdapter.TypeAdapter;
import com.lvt4j.extend.typeadapter.TypeAdapter4java_util_Date__java_lang_String;

/**
 *
 * @author LV
 *
 */
public class TTypeAdapterTest {

    @Test
    public void adapterTest() {
        TTypeAdapter.registerAdapter(new TypeAdapter4java_util_Date__java_lang_String());
        TypeAdapter<?, ?> typeAdapter = TTypeAdapter.getAdapter(String.class, Date.class);
        System.out.println(typeAdapter.change(new Date()));
        System.out.println(typeAdapter.change("2017"));
    }
    
}
