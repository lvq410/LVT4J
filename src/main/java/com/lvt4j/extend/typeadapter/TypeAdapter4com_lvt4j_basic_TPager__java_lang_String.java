package com.lvt4j.extend.typeadapter;

import net.sf.json.JSONObject;

import com.lvt4j.basic.TPager;
import com.lvt4j.basic.TTypeAdapter.TypeAdapter;

/**
 * {@link com.lvt4j.basic.TPager TPager}与String转换<br>
 * String是jsonobject形式
 * @author LV
 */
public class TypeAdapter4com_lvt4j_basic_TPager__java_lang_String extends TypeAdapter<TPager, String> {
    @Override public Class<TPager> clsA() { return TPager.class; }
    @Override public Class<String> clsB() { return String.class; }

    @Override
    protected String changeA2B(TPager pager) {
        return JSONObject.fromObject(pager).toString();
    }

    @Override
    protected TPager changeB2A(String text) {
        TPager pager = new TPager();
        JSONObject pagerJson = JSONObject.fromObject(text);
        if(pagerJson.containsKey("pageNo")) pager.setPageNo(pagerJson.optInt("pageNo"));
        if(pagerJson.containsKey("pageSize")) pager.setPageSize(pagerJson.optInt("pageSize"));
        return pager;
    }
}
