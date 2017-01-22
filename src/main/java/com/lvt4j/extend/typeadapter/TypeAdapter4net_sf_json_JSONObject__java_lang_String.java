package com.lvt4j.extend.typeadapter;

import net.sf.json.JSONObject;

import com.lvt4j.basic.TTypeAdapter.TypeAdapter;

/**
 * {@link net.sf.json.JSONObject JSONObject}与String转换<br>
 * @author LV
 */
public class TypeAdapter4net_sf_json_JSONObject__java_lang_String extends TypeAdapter<JSONObject, String> {
    @Override public Class<JSONObject> clsA() { return JSONObject.class; }
    @Override public Class<String> clsB() { return String.class; }

    @Override
    protected String changeA2B(JSONObject jsonObject) {
        return jsonObject.toString();
    }

    @Override
    protected JSONObject changeB2A(String text) {
        return JSONObject.fromObject(text);
    }
}
