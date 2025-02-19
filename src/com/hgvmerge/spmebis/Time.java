package com.hgvmerge.spmebis;

import com.codename1.l10n.ParseException;
import com.codename1.l10n.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Time {
    
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private Date date;
    
    public static final int MILLISECONDS = 1;
    public static final int SECONDS = 1000;
    public static final int MINUTES = 1000 * 60;
    public static final int HOURS = 1000 * 60 * 60;
    public static final int DAYS = 1000 * 60 * 60 * 24;
    public static final int WEEKS = 1000 * 60 * 60 * 24 * 7;    
    
    public static final int MONTHS = 30;
    public static final int YEARS = 365;
    
    public Time() {}
    
    public Time now() {
        date = new Date();
        return this;
    }
    
    public Time(long millis) {
        date = new Date(millis);
    }
    
    public Time(int days) {
        long millis = (long) days * 24 * 60 * 60 * 1000;
        date = new Date(millis);
    }
    
    public Time(String format) {
        try {
            date = sdf.parse(format);
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
    }
    
    public long getMillis() {
        return date.getTime();
    }
    
    public int toEpochDay() {
        return (int) date.getTime() / (1000 * 60 * 60 * 24);
    }
    
    public String format() {
        return sdf.format(date);
    }
    
    public boolean isWeekend() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int weekday = cal.get(Calendar.DAY_OF_WEEK);
        return weekday == Calendar.SATURDAY || weekday == Calendar.SUNDAY;
    }
    
    public Time add(int field, int value) {
        Calendar cal = Calendar.getInstance();
        switch(field) {
            case MONTHS:
                cal.setTime(date);
                cal.add(Calendar.MONTH, value);
                date.setTime(cal.getTime().getTime());
                break;
            case YEARS:
                cal.setTime(date);
                cal.add(Calendar.YEAR, value);
                date.setTime(cal.getTime().getTime());
                break;
            default:
                date.setTime(date.getTime() + value * field);
                break;
        }
        return this;
    }
    
    public long get(int field) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        switch(field) {
            case MILLISECONDS:
                return cal.get(Calendar.MILLISECOND);
            case SECONDS:
                return cal.get(Calendar.SECOND);
            case MINUTES:
                return cal.get(Calendar.MINUTE);
            case HOURS:
                return cal.get(Calendar.HOUR);
            case DAYS:
                return cal.get(Calendar.DAY_OF_MONTH); //Date
            case WEEKS:
                return cal.get(Calendar.WEEK_OF_YEAR); //KW
            case MONTHS:
                return cal.get(Calendar.MONTH);
            case YEARS:
                return cal.get(Calendar.YEAR);
            default: 
                return -1;
        }
    }
    
    public boolean compare(String operation, Time other) {
        switch(operation) {
            case "==":
                return getMillis() == other.getMillis();
            case "<":
                return getMillis() < other.getMillis();
            case ">":
                return getMillis() > other.getMillis();
            case "<=":
                return getMillis() <= other.getMillis();
            case ">=":
                return getMillis() >= other.getMillis();
            case "before":
                return getMillis() < other.getMillis();
            case "after":
                return getMillis() > other.getMillis();
            case "before/equals":
                return getMillis() <= other.getMillis();
            case "after/equals":
                return getMillis() >= other.getMillis();
            default:
                return false;
        }
    }
    
}
