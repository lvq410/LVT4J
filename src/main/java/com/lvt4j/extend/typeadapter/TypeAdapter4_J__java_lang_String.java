package com.lvt4j.extend.typeadapter;

import net.sf.json.JSONArray;

import com.lvt4j.basic.TTypeAdapter.TypeAdapter;

/**
 * long[]与String转换<br>
 * String是jsonarray形式
 * @author LV
 */
public class TypeAdapter4_J__java_lang_String extends TypeAdapter<long[], String> {
    @Override public Class<long[]> clsA() { return long[].class; }
    @Override public Class<String> clsB() { return String.class; }

    @Override
    protected String changeA2B(long[] longs) {
        return JSONArray.fromObject(longs).toString();
    }

    @Override
    protected long[] changeB2A(String text) {
        JSONArray jsonArray = JSONArray.fromObject(text);
        long[] longs = new long[jsonArray.size()];
        for (int i = 0; i < jsonArray.size(); i++) {
            longs[i] = jsonArray.getLong(i);
        }
        return longs;
    }
}
