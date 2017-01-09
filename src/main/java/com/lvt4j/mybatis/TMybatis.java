package com.lvt4j.mybatis;

import java.lang.reflect.Field;

import com.lvt4j.basic.TDB;
import com.lvt4j.basic.TReflect;
import com.lvt4j.basic.TStr;

public class TMybatis {

    public static void genCols(Class<?> cls) {
        StringBuilder rstmap = new StringBuilder();
        StringBuilder cols = new StringBuilder();
        StringBuilder insert = new StringBuilder();
        StringBuilder set = new StringBuilder();
        for (Field field : TReflect.allField(cls)) {
            String jdbcType = null;
            try {
                jdbcType = TDB.colType2Str(field.getType());
            } catch (Exception e) {
                
                jdbcType = "VARCHAR";
            }
            rstmap.append("<result property=\"").append(field.getName())
                    .append("\" column=\"").append(field.getName())
                    .append("\" javaType=\"").append(field.getType().getName())
                    .append("\" jdbcType=\"").append(jdbcType)
                    .append("\"/>\r\n");
            cols.append("\"").append(field.getName()).append("\",");
            insert.append("#{").append(field.getName()).append(",javaType=")
                    .append(field.getType().getName()).append(",jdbcType=")
                    .append(jdbcType).append("},\r\n");
            set.append("\"").append(field.getName()).append("\"=#{")
                    .append(field.getName())
                    .append(",javaType=")
                    .append(field.getType().getName()).append(",jdbcType=")
                    .append(jdbcType)
                    .append("},\r\n");
        }
        System.out.println(rstmap.toString());
        System.out.println(TStr.rTrim(cols.toString(), ","));
        System.out.println();
        System.out.println(TStr.rTrim(insert.toString(), ","));
        System.out.println(TStr.rTrim(set.toString(), ","));
    }
}
