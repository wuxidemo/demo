/*
 * @Copyright:  江苏一家一数据服务股份有限公司
 * @Description:  时间格式化类 
 * @author:  lvyongxu 
 * @data:  2015-4-8 下午5:55:16 
 */
package com.dw.merchant.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 时间格式化类
 *
 * @author lvyongxu
 * @date 2015-4-8 下午5:55:16
 */
public class DateTimeUtils {

    public static long getTodayZero() {
        Date date = new Date();
        long l = 24 * 60 * 60 * 1000; //每天的毫秒数
        // date.getTime()是现在的毫秒数，它 减去 当天零点到现在的毫秒数（ 现在的毫秒数%一天总的毫秒数，取余。），
        // 理论上等于零点的毫秒数，不过这个毫秒数是UTC+0时区的。减8个小时的毫秒值是为了解决时区的问题。
        return (date.getTime() - (date.getTime() % l) - 8 * 60 * 60 * 1000) / 1000;
    }

    /**
     * 获取当前时间的毫秒数（以1970-01-01 00:00:00 000开始计算）
     *
     * @return 1970-01-01 00:00:00至当前时间的总毫秒数 1450168684822
     * @author lvyongxu
     * @date 2015-4-8 下午5:56:20
     */

    public static long getMilliSecond() {
        return new Date().getTime();
    }

    /**
     * 获取当前时间的秒数（以1970-01-01 00:00:00 开始计算）
     *
     * @return 1970-01-01 00:00:00至当前时间的总秒数 1451374303
     * @author lvyongxu
     * @date 2015-12-29 下午3:56:20
     */
    public static long getSecond() {
        return (new Date().getTime() / 1000);
    }

    /**
     * 返回yyyyMMddHHmmss格式的时间字符串
     *
     * @return “20150408175532”格式的时间字符串
     * @author lvyongxu
     * @date 2015-4-8 下午5:55:32
     */
    public static String getTimeStamp() {
        return new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA)
                .format(new Date());
    }


    /**
     * 获得指定日期的前一天
     *
     * @param specifiedDay
     * @return
     * @throws Exception
     */
    public static String getTheDayBefore(String specifiedDay) {
        Calendar c = Calendar.getInstance();
        Date date = str2Date(specifiedDay, EnumDateFmt._yyyyMMdd);
        c.setTime(date);

        int day = c.get(Calendar.DATE);
        c.set(Calendar.DATE, day - 1);

        String dayBefore = date2Str(c.getTime(), EnumDateFmt._yyyyMMdd);
        return dayBefore;
    }

    /**
     * 获得指定日期的后一天
     *
     * @param specifiedDay
     * @return
     */
    public static String getTheDayAfter(String specifiedDay) {
        Calendar c = Calendar.getInstance();
        Date date = str2Date(specifiedDay, EnumDateFmt._yyyyMMdd);
        c.setTime(date);

        int day = c.get(Calendar.DATE);
        c.set(Calendar.DATE, day + 1);

        String dayAfter = date2Str(c.getTime(), EnumDateFmt._yyyyMMdd);
        return dayAfter;
    }


    /**
     * 时间格式化（DateToString>
     *
     * @param date   时间对象
     * @param format 格式化的格式
     * @return 根据format的格式返回对应格式的时间字符串
     * @author lvyongxu
     * @date 2015-4-8 下午5:57:18
     */
    public static String date2Str(Date date, String format) {
        return new SimpleDateFormat(format, Locale.CHINA).format(date);
    }

    /**
     * 时间格式化（String2Date）
     *
     * @param dateStr  20150108
     * @param template eg:yyyyMMdd
     * @return 根据传入的EnumDateFmt类型返回对应格式的时间对象
     * @author lvyongxu
     * @date 2015-4-8 下午5:57:18
     */
    public static Date str2Date(String dateStr, String template) {
        Date date = null;
        try {
            date = new SimpleDateFormat(template, Locale.CHINA).parse(dateStr);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return date;
    }

    /**
     * 时间格式化（DateToString）
     *
     * @param date    时间对象
     * @param enumFmt 枚举类型：时间格式化类型
     * @return 根据传入的EnumDateFmt类型返回对应格式的字符串
     * @author lvyongxu
     * @date 2015-4-8 下午5:58:40
     * @see EnumDateFmt
     */
    public static String date2Str(Date date, EnumDateFmt enumFmt) {
        switch (enumFmt) {
            case _yyyyMMddHHmmss:
                return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
                        .format(date);
            case _yyMMddHHmmss:
                return new SimpleDateFormat("yy-MM-dd HH:mm:ss", Locale.CHINA)
                        .format(date);
            case _yyyyMMddHHmm:
                return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
                        .format(date);
            case _MMddHHmm:
                return new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA)
                        .format(date);
            case _yyyyMMdd:
                return new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
                        .format(date);
            case yyyyMMddHHmmss:
                return new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA)
                        .format(date);

            case yyyyMMddHHmm:
                return new SimpleDateFormat("yyyyMMddHHmm", Locale.CHINA)
                        .format(date);
            case HHmmss:
                return new SimpleDateFormat("HH:mm:ss", Locale.CHINA).format(date);
            case MMdd:
                return new SimpleDateFormat("MM-dd", Locale.CHINA).format(date);
            case EEEE:
                return new SimpleDateFormat("EEEE", Locale.CHINA).format(date);
            default:
                return "";
        }
    }

    /**
     * 时间格式化（String2Date）
     *
     * @param dateStr 时间字符串
     * @param enumFmt 枚举类型：时间格式化类型
     * @return 根据传入的EnumDateFmt类型返回对应格式的时间对象
     * @author lvyongxu
     * @date 2015-4-8 下午6:01:12
     * @see EnumDateFmt
     */
    public static Date str2Date(String dateStr, EnumDateFmt enumFmt) {
        try {
            switch (enumFmt) {
                case _yyyyMMddHHmmss:
                    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
                            .parse(dateStr);
                case _yyMMddHHmmss:
                    return new SimpleDateFormat("yy-MM-dd HH:mm:ss", Locale.CHINA)
                            .parse(dateStr);
                case _yyyyMMddHHmm:
                    return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
                            .parse(dateStr);
                case _yyyyMMdd:
                    return new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
                            .parse(dateStr);
                case _MMddHHmm:
                    return new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA)
                            .parse(dateStr);
                case yyyyMMddHHmmss:
                    return new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA)
                            .parse(dateStr);
                case yyyyMMddHHmm:
                    return new SimpleDateFormat("yyyyMMddHHmm", Locale.CHINA)
                            .parse(dateStr);
                case HHmmss:
                    return new SimpleDateFormat("HH:mm:ss", Locale.CHINA)
                            .parse(dateStr);
                case MMdd:
                    return new SimpleDateFormat("MM-dd", Locale.CHINA)
                            .parse(dateStr);
                case EEEE:
                    return new SimpleDateFormat("EEEE", Locale.CHINA)
                            .parse(dateStr);
                default:
                    return null;
            }
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 时间类型枚举集合
     *
     * @author lvyongxu
     * @date 2015-4-8 下午6:02:41
     */
    public enum EnumDateFmt {
        _yyyyMMddHHmmss, _yyyyMMddHHmm, _yyMMddHHmmss, _yyyyMMdd, _MMddHHmm, yyyyMMddHHmmss, yyyyMMddHHmm, HHmmss, MMdd, EEEE
    }
}
