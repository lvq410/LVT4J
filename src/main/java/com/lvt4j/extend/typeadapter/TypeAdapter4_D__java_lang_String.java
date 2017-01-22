package com.lvt4j.extend.typeadapter;

import net.sf.json.JSONArray;

import com.lvt4j.basic.TTypeAdapter.TypeAdapter;

/**
 * double[]与String转换<br>
 * String是jsonarray形式
 * @author LV
 */
public class TypeAdapter4_D__java_lang_String extends TypeAdapter<double[], String> {
    @Override public Class<double[]> clsA() { return double[].class; }
    @Override public Class<String> clsB() { return String.class; }

    @Override
    protected String changeA2B(double[] doubles) {
        return JSONArray.fromObject(doubles).toString();
    }

    @Override
    protected double[] changeB2A(String text) {
        JSONArray jsonArray = JSONArray.fromObject(text);
        double[] doubles = new double[jsonArray.size()];
        for (int i = 0; i < jsonArray.size(); i++) {
            doubles[i] = jsonArray.getDouble(i);
        }
        return doubles;
    }
}
