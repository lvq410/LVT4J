package com.lvt4j.extend.typeadapter;

import net.sf.json.JSONArray;

import com.lvt4j.basic.TTypeAdapter.TypeAdapter;

/**
 * String[]与String转换<br>
 * String是jsonarray形式
 * @author LV
 */
public class TypeAdapter4_Ljava_lang_String___java_lang_String extends TypeAdapter<String[], String> {
    @Override public Class<String[]> clsA() { return String[].class; }
    @Override public Class<String> clsB() { return String.class; }

    @Override
    protected String changeA2B(String[] strArrs) {
        return JSONArray.fromObject(strArrs).toString();
    }

    @Override
    protected String[] changeB2A(String text) {
        JSONArray jsonArray = JSONArray.fromObject(text);
        String[] strArrs = new String[jsonArray.size()];
        for (int i = 0; i < jsonArray.size(); i++) {
            strArrs[i] = jsonArray.getString(i);
        }
        return strArrs;
    }
}
