package com.lvt4j.basic;

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
    public static Date calculte(Date date, int day, int hour, int minute, int second) {
        return new Date(date.getTime()+day*86400000+hour*3600000+minute*60000+second*1000);
    }
    public static Date calculte(Date date, long time) {
        return new Date(date.getTime()+time);
    }
    
}
