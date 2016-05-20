package com.lvt4j.office;


import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;

import com.lvt4j.basic.TChar;

public class TChinese {

    private static HanyuPinyinOutputFormat pinyinFormat = new HanyuPinyinOutputFormat();
    static {
        pinyinFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        pinyinFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
    }

    public static final String toSpell(String chinese) {
        StringBuilder spell = new StringBuilder();
        char[] chars = chinese.toCharArray();
        for (char c : chars) {
            if (TChar.checkType(c) == TChar.CHINESE) {
                try {
                    spell.append(PinyinHelper.toHanyuPinyinStringArray(
                            c, pinyinFormat)[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                    spell.append(c);
                }
            } else {
                spell.append(c);
            }
        }
        return spell.toString();
    }
    
}
