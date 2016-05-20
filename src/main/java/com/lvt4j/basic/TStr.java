package com.lvt4j.basic;

/**
 * Some string tools for Java.
 * 
 * @author LV
 * 
 */
public class TStr {
    /**
     * Trim方法默认匹配字符串列表(null,制表符,换行,垂直制表符,回车,空格)
     */
    private final static String[] DEFAULT_TRIM_REGEX = { "\0", "\t", "\n",
            Character.toString((char) 0x0B), "\r", " " };

    /**
     * 移除字符串两侧的预定义字符。
     * 
     * @param str
     * @param regex
     *            预定义字符列表
     * @return
     */
    public final static String trim(String str, String... regex) {
        return lTrim(rTrim(str, regex), regex);
    }

    /**
     * 移除字符串左侧的预定义字符。
     * 
     * @param str
     * @param regex
     *            预定义字符列表
     * @return
     */
    public final static String lTrim(String str, String... regex) {
        String[] regS = TVerify.arrNullOrEmpty(regex) ? DEFAULT_TRIM_REGEX : regex;
        for (int i = 0; i < regS.length; i++)
            if (str.startsWith(regS[i])) {
                str = str.substring(regS[i].length());
                i = -1;
            }
        return str;
    }

    /**
     * 移除字符串右侧的预定义字符。
     * 
     * @param str
     * @param regex
     *            预定义字符列表
     * @return
     */
    public final static String rTrim(String str, String... regex) {
        String[] regS = TVerify.arrNullOrEmpty(regex) ? DEFAULT_TRIM_REGEX : regex;
        for (int i = 0; i < regS.length; i++)
            if (str.endsWith(regS[i])) {
                str = str.substring(0, str.length() - regS[i].length());
                i = -1;
            }
        return str;
    }

    /**
     * 计算字符串长度 字符串为空时返回0
     * 
     * @param str
     * @return
     */
    public final static long len(String str) {
        if (str != null)
            return str.length();
        return 0;
    }

    /**
     * 半角转全角
     * 
     * @param input
     * @return 全角字符串.
     */
    public static String toSBC(String input) {
        char c[] = input.toCharArray();
        for (int i = 0; i < c.length; i++)
            if (c[i] == ' ')
                c[i] = '\u3000';
            else if (c[i] < '\177')
                c[i] = (char) (c[i] + 65248);
        return new String(c);
    }

    /**
     * 全角转半角
     * 
     * @param input
     * @return 半角字符串
     */
    public static String toDBC(String input) {
        char c[] = input.toCharArray();
        for (int i = 0; i < c.length; i++)
            if (c[i] == '\u3000')
                c[i] = ' ';
            else if (c[i] > '\uFF00' && c[i] < '\uFF5F')
                c[i] = (char) (c[i] - 65248);
        return new String(c);
    }
}