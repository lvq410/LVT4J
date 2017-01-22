package com.lvt4j.extend.typeadapter;

import net.sf.json.JSONArray;

import com.lvt4j.basic.TTypeAdapter.TypeAdapter;

/**
 * {@link net.sf.json.JSONArray JSONArray}与String转换<br>
 * @author LV
 */
public class TypeAdapter4net_sf_json_JSONArray__java_lang_String extends TypeAdapter<JSONArray, String> {
    @Override public Class<JSONArray> clsA() { return JSONArray.class; }
    @Override public Class<String> clsB() { return String.class; }

    @Override
    protected String changeA2B(JSONArray jsonArray) {
        return jsonArray.toString();
    }

    @Override
    protected JSONArray changeB2A(String text) {
        return JSONArray.fromObject(text);
    }
}