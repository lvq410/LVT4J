package com.lvt4j.extend.typeadapter;

import net.sf.json.JSONArray;

import com.lvt4j.basic.TTypeAdapter.TypeAdapter;

/**
 * double[][]与String转换<br>
 * String是jsonarray形式
 * @author LV
 */
public class TypeAdapter4__D__java_lang_String extends TypeAdapter<double[][], String> {
    @Override public Class<double[][]> clsA() { return double[][].class; }
    @Override public Class<String> clsB() { return String.class; }

    @Override
    protected String changeA2B(double[][] doubleArrs) {
        return JSONArray.fromObject(doubleArrs).toString();
    }

    @Override
    protected double[][] changeB2A(String text) {
        JSONArray jsonArray = JSONArray.fromObject(text);
        double[][] doubleArrs = new double[jsonArray.size()][];
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONArray subJsonArr = jsonArray.optJSONArray(i);
            if (subJsonArr==null) {
                doubleArrs[i] = null;
                continue;
            }
            double[] subArr = new double[subJsonArr.size()];
            for (int j = 0; j < subJsonArr.size(); j++) {
                subArr[j] = subJsonArr.getDouble(j);
            }
            doubleArrs[i] = subArr;
        }
        return doubleArrs;
    }
}
