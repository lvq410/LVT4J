package com.lvt4j.extend.typeadapter;

import net.sf.json.JSONArray;

import com.lvt4j.basic.TTypeAdapter.TypeAdapter;

/**
 * int[][]与String转换<br>
 * String是jsonarray形式
 * @author LV
 */
public class TypeAdapter4__I__java_lang_String extends TypeAdapter<int[][], String> {
    @Override public Class<int[][]> clsA() { return int[][].class; }
    @Override public Class<String> clsB() { return String.class; }

    @Override
    protected String changeA2B(int[][] intArrs) {
        return JSONArray.fromObject(intArrs).toString();
    }

    @Override
    protected int[][] changeB2A(String text) {
        JSONArray jsonArray = JSONArray.fromObject(text);
        int[][] intArrs = new int[jsonArray.size()][];
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONArray subJsonArr = jsonArray.optJSONArray(i);
            if (subJsonArr==null) {
                intArrs[i] = null;
                continue;
            }
            int[] subArr = new int[subJsonArr.size()];
            for (int j = 0; j < subJsonArr.size(); j++) {
                subArr[j] = subJsonArr.getInt(j);
            }
            intArrs[i] = subArr;
        }
        return intArrs;
    }
}
