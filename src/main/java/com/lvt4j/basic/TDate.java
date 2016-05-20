package com.lvt4j.basic;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Some date tools for Java.
 * @author LV
 */
public class TDate {
    /**
     * Add or reduce one day/one hour... to a date.
     * @param date Date to calculate.
     * @param day Day's number to calculate.
     * @param hour Hour's number to calculate.
     * @param minute Minute's number to calculate.
     * @param second Second's number to calculate.
     */
    public static Date calculte(Date date,int day,int hour,int minute,int second) {
        return new Date(date.getTime()+day*86400000+hour*3600000+minute*60000+second*1000);
    }
    public static Date calculte(Date date,long time) {
        return new Date(date.getTime()+time);
    }
    public static final class Format {
        public final static SimpleDateFormat Y_M_D = new SimpleDateFormat("y-M-d");
        public final static SimpleDateFormat YYYY_MM_DD = new SimpleDateFormat("yyyy-MM-dd");
        public final static SimpleDateFormat YYYYMMDD = new SimpleDateFormat("yyyyMMdd");
        public final static DateFormat YYYY_MM_DD_HH_MM_SS_12 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        public final static DateFormat YYYY_MM_DD_HH_MM_SS_24 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        public final static DateFormat YYYYMMDDHHMMSS12 = new SimpleDateFormat("yyyyMMddhhmmss");
        public final static DateFormat YYYYMMDDHHMMSS24 = new SimpleDateFormat("yyyyMMddHHmmss");
        public final static DateFormat YYYYMMDDHHMMSSSSS = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    }
    
}
