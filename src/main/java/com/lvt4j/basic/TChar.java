package com.lvt4j.basic;

import java.util.Arrays;
import java.util.List;

public enum TChar {
    
    /** 分割符 */
    DELIMITER,
    /** 数字 */
    NUM,
    /** 字母 */
    LETTER,
    /** 中文 */
    CHINESE,
    /** 其他 */
    OTHER;
    
    /**
     * 检查字符类型
     * 
     * @param c
     * @return
     */
    public static final TChar checkType(char c) {
        if ((c >= 0x4e00) && (c <= 0x9fbb)) {
            // 中文，编码区间0x4e00-0x9fbb
            return CHINESE;
        } else if ((c >= 0xff00) && (c <= 0xffef)) {
            // Halfwidth and Fullwidth Forms， 编码区间0xff00-0xffef
            if (((c >= 0xff21) && (c <= 0xff3a))
                    || ((c >= 0xff41) && (c <= 0xff5a))) {
                // 2字节英文字
                return LETTER;
            } else if ((c >= 0xff10) && (c <= 0xff19)) {
                // 2字节数字
                return NUM;
            } else
                // 其他字符，可以认为是标点符号
                return DELIMITER;
        } else if ((c >= 0x0021) && (c <= 0x007e)) {
            // basic latin，编码区间 0000-007f
            if ((c >= 0x0030) && (c <= 0x0039)) {
                // 1字节数字
                return NUM;
            } else if (((c >= 0x0041) && (c <= 0x005a))
                    || ((c >= 0x0061) && (c <= 0x007a))) {
                // 1字节字符
                return LETTER;
            } else
                // 其他字符，可以认为是标点符号
                return DELIMITER;
        } else if ((c >= 0x00a1) && (c <= 0x00ff)) {
            // latin-1，编码区间0080-00ff
            if ((c >= 0x00c0) && (c <= 0x00ff)) {
                return LETTER;
            } else
                return DELIMITER;
        } else
            return OTHER;
    }
    
    /**
     * 提取指定类型字符
     * @param plainText
     * @return
     */
    public static final String extract(String plainText,TChar type) {
        StringBuilder rst = new StringBuilder();
        for (char c: plainText.toCharArray()) {
            if (checkType(c)==type) {
                rst.append(c);
            }
        }
        return rst.toString();
    }
    /**
     * 提取指定类型字符
     * @param plainText
     * @return
     */
    public static final String extract(String plainText,TChar... type) {
        StringBuilder rst = new StringBuilder();
        List<TChar> types = Arrays.asList(type);
        for (char c: plainText.toCharArray()) {
            if (types.contains(checkType(c))) rst.append(c);
        }
        return rst.toString();
    }
    
    /**
     * 判断文本中是否包含指定类型字符
     * @param plainText
     * @param type
     * @return
     */
    public static final boolean contains(String plainText,TChar type) {
        for (int i = 0; i < plainText.length(); i++) {
            if (checkType(plainText.charAt(i))==type) return true;
        }
        return false;
    }
    /**
     * 判断文本中是否包含指定类型字符
     * @param plainText
     * @param type
     * @return
     */
    public static final boolean contains(String plainText,TChar... type) {
        List<TChar> types = Arrays.asList(type);
        for (int i = 0; i < plainText.length(); i++) {
            if (types.contains(checkType(plainText.charAt(i)))) return true;
        }
        return false;
    }
}
