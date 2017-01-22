package com.lvt4j.extend.typeadapter;

import java.text.ParseException;
import java.util.Date;
import java.util.regex.Pattern;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import com.lvt4j.basic.TTypeAdapter.TypeAdapter;

/**
 * Date与String转换,支持格式包括<br>
 * ●'yyyy-MM-dd HH:mm:ss:SSS'<br>
 * ●'yyyy-MM-dd HH:mm:ss'<br>
 * ●'yyyyMMdd HH:mm:ss'<br>
 * ●'yyyy-MM-dd HH:mm'<br>
 * ●'yyyyMMdd HH:mm'<br>
 * ●'yyyy-MM-dd HH'<br>
 * ●'yyyyMMdd HH'<br>
 * ●'yyyy-MM-dd'<br>
 * ●'yyyyMMdd'<br>
 * ●'yyyy-MM'<br>
 * ●'yyyyMM'<br>
 * ●'yyyy'<br>
 * ●long型时间戳<br>
 * @author LV
 */
public class TypeAdapter4java_util_Date__java_lang_String extends TypeAdapter<Date, String> {

    private static final String DefFormatPattern = "yyyy-MM-dd HH:mm:ss";
    
    private Pattern[] regexPatterns = {
        Pattern.compile("^\\d{1,4}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2}:\\d{1,2}:\\d{1,3}$"),
        Pattern.compile("^\\d{1,4}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2}:\\d{1,2}$"),
        Pattern.compile("^\\d{4}\\d{2}\\d{2} \\d{1,2}:\\d{1,2}:\\d{1,2}$"),
        Pattern.compile("^\\d{1,4}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2}$"),
        Pattern.compile("^\\d{4}\\d{2}\\d{2} \\d{1,2}:\\d{1,2}$"),
        Pattern.compile("^\\d{1,4}-\\d{1,2}-\\d{1,2} \\d{1,2}$"),
        Pattern.compile("^\\d{4}\\d{2}\\d{2} \\d{1,2}$"),
        Pattern.compile("^\\d{1,4}-\\d{1,2}-\\d{1,2}$"),
        Pattern.compile("^\\d{4}\\d{2}\\d{2}$"),
        Pattern.compile("^\\d{1,4}-\\d{1,2}$"),
        Pattern.compile("^\\d{4}\\d{2}$"),
        Pattern.compile("^\\d{1,4}$")
    };
    private String[] parsePatterns = {
        "yyyy-MM-dd HH:mm:ss:SSS", 
        "yyyy-MM-dd HH:mm:ss", 
        "yyyyMMdd HH:mm:ss",
        "yyyy-MM-dd HH:mm",
        "yyyyMMdd HH:mm",
        "yyyy-MM-dd HH",
        "yyyyMMdd HH",
        "yyyy-MM-dd",
        "yyyyMMdd",
        "yyyy-MM",
        "yyyyMM",
        "yyyy"
    };
    
    @Override public Class<Date> clsA() { return Date.class; }
    @Override public Class<String> clsB() { return String.class; }

    @Override
    protected String changeA2B(Date date) {
        return DateFormatUtils.format(date, DefFormatPattern);
    }

    @Override
    protected Date changeB2A(String text) {
        try {
            for(int i = 0; i < regexPatterns.length; i++){
                if(!regexPatterns[i].matcher(text).matches()) continue;
                String parsePattern = parsePatterns[i];
                return DateUtils.parseDateStrictly(text, parsePattern);
            }
        } catch (ParseException ignore) {}
        try {
            return new Date(Long.valueOf(text));
        } catch (NumberFormatException ignore) {}
        throw new IllegalArgumentException("不支持的Date型字符串表示:"+text);
    }


}
