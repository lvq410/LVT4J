package com.lvt4j.extend.typeadapter;

import net.sf.json.JSONArray;

import com.lvt4j.basic.TTypeAdapter.TypeAdapter;

/**
 * long[][]与String转换<br>
 * String是jsonarray形式
 * @author LV
 */
public class TypeAdapter4__J__java_lang_String extends TypeAdapter<long[][], String> {
    @Override public Class<long[][]> clsA() { return long[][].class; }
    @Override public Class<String> clsB() { return String.class; }

    @Override
    protected String changeA2B(long[][] longArrs) {
        return JSONArray.fromObject(longArrs).toString();
    }

    @Override
    protected long[][] changeB2A(String text) {
        JSONArray jsonArray = JSONArray.fromObject(text);
        long[][] longArrs = new long[jsonArray.size()][];
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONArray subJsonArr = jsonArray.optJSONArray(i);
            if (subJsonArr==null) {
                longArrs[i] = null;
                continue;
            }
            long[] subArr = new long[subJsonArr.size()];
            for (int j = 0; j < subJsonArr.size(); j++) {
                subArr[j] = subJsonArr.getLong(j);
            }
            longArrs[i] = subArr;
        }
        return longArrs;
    }
}
