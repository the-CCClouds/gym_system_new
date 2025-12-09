package utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期时间工具类
 * 统一处理项目中所有日期时间相关的操作
 */
public class DateUtils {

    // ==================== 日期格式常量 ====================
    
    /** 日期格式：yyyy-MM-dd */
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    
    /** 日期时间格式：yyyy-MM-dd HH:mm:ss */
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    /** 时间格式：HH:mm:ss */
    public static final String TIME_FORMAT = "HH:mm:ss";
    
    /** 中文日期格式：yyyy年MM月dd日 */
    public static final String DATE_FORMAT_CN = "yyyy年MM月dd日";
    
    /** 中文日期时间格式：yyyy年MM月dd日 HH:mm:ss */
    public static final String DATETIME_FORMAT_CN = "yyyy年MM月dd日 HH:mm:ss";

    // SimpleDateFormat 不是线程安全的，每次创建新实例
    private static SimpleDateFormat getDateFormat(String pattern) {
        return new SimpleDateFormat(pattern);
    }

    // ==================== 字符串 -> 日期 转换 ====================

    /**
     * 字符串转 java.util.Date（日期格式：yyyy-MM-dd）
     * @param dateStr 日期字符串
     * @return Date对象，解析失败返回null
     */
    public static Date parseDate(String dateStr) {
        return parseDate(dateStr, DATE_FORMAT);
    }

    /**
     * 字符串转 java.util.Date（日期时间格式：yyyy-MM-dd HH:mm:ss）
     * @param dateTimeStr 日期时间字符串
     * @return Date对象，解析失败返回null
     */
    public static Date parseDateTime(String dateTimeStr) {
        return parseDate(dateTimeStr, DATETIME_FORMAT);
    }

    /**
     * 字符串转 java.util.Date（自定义格式）
     * @param dateStr 日期字符串
     * @param pattern 日期格式
     * @return Date对象，解析失败返回null
     */
    public static Date parseDate(String dateStr, String pattern) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return getDateFormat(pattern).parse(dateStr);
        } catch (ParseException e) {
            System.err.println("日期解析失败: " + dateStr + " (格式: " + pattern + ")");
            return null;
        }
    }

    // ==================== 字符串 -> SQL日期 转换 ====================

    /**
     * 字符串转 java.sql.Date（日期格式：yyyy-MM-dd）
     * 用于：birthDate, hireDate, startDate, endDate
     * @param dateStr 日期字符串
     * @return java.sql.Date对象
     */
    public static java.sql.Date toSqlDate(String dateStr) {
        Date date = parseDate(dateStr);
        return date != null ? new java.sql.Date(date.getTime()) : null;
    }

    /**
     * java.util.Date 转 java.sql.Date
     * @param date java.util.Date对象
     * @return java.sql.Date对象
     */
    public static java.sql.Date toSqlDate(Date date) {
        return date != null ? new java.sql.Date(date.getTime()) : null;
    }

    /**
     * 字符串转 java.sql.Timestamp（日期时间格式：yyyy-MM-dd HH:mm:ss）
     * 用于：registerDate, bookingTime, checkinTime, checkoutTime, orderTime
     * @param dateTimeStr 日期时间字符串
     * @return java.sql.Timestamp对象
     */
    public static java.sql.Timestamp toSqlTimestamp(String dateTimeStr) {
        Date date = parseDateTime(dateTimeStr);
        return date != null ? new java.sql.Timestamp(date.getTime()) : null;
    }

    /**
     * java.util.Date 转 java.sql.Timestamp
     * @param date java.util.Date对象
     * @return java.sql.Timestamp对象
     */
    public static java.sql.Timestamp toSqlTimestamp(Date date) {
        return date != null ? new java.sql.Timestamp(date.getTime()) : null;
    }

    // ==================== 日期 -> 字符串 转换 ====================

    /**
     * 日期转字符串（日期格式：yyyy-MM-dd）
     * @param date 日期对象
     * @return 格式化后的字符串
     */
    public static String formatDate(Date date) {
        return formatDate(date, DATE_FORMAT);
    }

    /**
     * 日期转字符串（日期时间格式：yyyy-MM-dd HH:mm:ss）
     * @param date 日期对象
     * @return 格式化后的字符串
     */
    public static String formatDateTime(Date date) {
        return formatDate(date, DATETIME_FORMAT);
    }

    /**
     * 日期转字符串（自定义格式）
     * @param date 日期对象
     * @param pattern 日期格式
     * @return 格式化后的字符串
     */
    public static String formatDate(Date date, String pattern) {
        if (date == null) {
            return null;
        }
        return getDateFormat(pattern).format(date);
    }

    /**
     * 日期转中文格式（yyyy年MM月dd日）
     * @param date 日期对象
     * @return 中文格式字符串
     */
    public static String formatDateCN(Date date) {
        return formatDate(date, DATE_FORMAT_CN);
    }

    // ==================== 获取当前日期时间 ====================

    /**
     * 获取当前日期（java.util.Date）
     * @return 当前日期时间
     */
    public static Date now() {
        return new Date();
    }

    /**
     * 获取当前日期（java.sql.Date，只有日期部分）
     * @return 当前日期
     */
    public static java.sql.Date today() {
        return new java.sql.Date(System.currentTimeMillis());
    }

    /**
     * 获取当前时间戳（java.sql.Timestamp）
     * @return 当前时间戳
     */
    public static java.sql.Timestamp nowTimestamp() {
        return new java.sql.Timestamp(System.currentTimeMillis());
    }

    // ==================== 日期计算 ====================

    /**
     * 日期加减天数
     * @param date 原日期
     * @param days 天数（正数加，负数减）
     * @return 计算后的日期
     */
    public static Date addDays(Date date, int days) {
        if (date == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, days);
        return cal.getTime();
    }

    /**
     * 日期加减月数
     * @param date 原日期
     * @param months 月数（正数加，负数减）
     * @return 计算后的日期
     */
    public static Date addMonths(Date date, int months) {
        if (date == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, months);
        return cal.getTime();
    }

    /**
     * 日期加减年数
     * @param date 原日期
     * @param years 年数（正数加，负数减）
     * @return 计算后的日期
     */
    public static Date addYears(Date date, int years) {
        if (date == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.YEAR, years);
        return cal.getTime();
    }

    /**
     * 计算月卡到期日期（当前日期 + 1个月）
     * @return 月卡到期日期
     */
    public static Date getMonthlyCardEndDate() {
        return addMonths(now(), 1);
    }

    /**
     * 计算年卡到期日期（当前日期 + 1年）
     * @return 年卡到期日期
     */
    public static Date getYearlyCardEndDate() {
        return addYears(now(), 1);
    }

    // ==================== 日期比较 ====================

    /**
     * 判断日期是否在今天之前（已过期）
     * @param date 要判断的日期
     * @return true表示已过期
     */
    public static boolean isExpired(Date date) {
        if (date == null) {
            return true;
        }
        return date.before(today());
    }

    /**
     * 判断日期是否在今天之后（未过期）
     * @param date 要判断的日期
     * @return true表示未过期
     */
    public static boolean isNotExpired(Date date) {
        return !isExpired(date);
    }

    /**
     * 判断是否是今天
     * @param date 要判断的日期
     * @return true表示是今天
     */
    public static boolean isToday(Date date) {
        if (date == null) {
            return false;
        }
        return formatDate(date).equals(formatDate(now()));
    }

    /**
     * 判断日期范围是否有效（开始日期 < 结束日期）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return true表示有效
     */
    public static boolean isValidDateRange(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            return false;
        }
        return startDate.before(endDate);
    }

    /**
     * 计算两个日期之间的天数差
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 天数差（可能为负数）
     */
    public static long daysBetween(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        long diff = endDate.getTime() - startDate.getTime();
        return diff / (24 * 60 * 60 * 1000);
    }

    /**
     * 计算会员卡剩余天数
     * @param endDate 到期日期
     * @return 剩余天数（负数表示已过期）
     */
    public static long daysRemaining(Date endDate) {
        return daysBetween(now(), endDate);
    }

    // ==================== 日期部分获取 ====================

    /**
     * 获取年份
     * @param date 日期
     * @return 年份
     */
    public static int getYear(Date date) {
        if (date == null) {
            return 0;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.YEAR);
    }

    /**
     * 获取月份（1-12）
     * @param date 日期
     * @return 月份
     */
    public static int getMonth(Date date) {
        if (date == null) {
            return 0;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.MONTH) + 1;
    }

    /**
     * 获取日（1-31）
     * @param date 日期
     * @return 日
     */
    public static int getDay(Date date) {
        if (date == null) {
            return 0;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 根据出生日期计算年龄
     * @param birthDate 出生日期
     * @return 年龄
     */
    public static int calculateAge(Date birthDate) {
        if (birthDate == null) {
            return 0;
        }
        Calendar birth = Calendar.getInstance();
        birth.setTime(birthDate);
        Calendar today = Calendar.getInstance();
        
        int age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
        
        // 如果还没过生日，年龄减1
        if (today.get(Calendar.MONTH) < birth.get(Calendar.MONTH) ||
            (today.get(Calendar.MONTH) == birth.get(Calendar.MONTH) && 
             today.get(Calendar.DAY_OF_MONTH) < birth.get(Calendar.DAY_OF_MONTH))) {
            age--;
        }
        return age;
    }

    // ==================== 时间计算（用于签到时长） ====================

    /**
     * 计算两个时间之间的分钟数
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 分钟数
     */
    public static long minutesBetween(Date startTime, Date endTime) {
        if (startTime == null || endTime == null) {
            return 0;
        }
        long diff = endTime.getTime() - startTime.getTime();
        return diff / (60 * 1000);
    }

    /**
     * 格式化时长（分钟转为 x小时x分钟）
     * @param minutes 分钟数
     * @return 格式化后的时长字符串
     */
    public static String formatDuration(long minutes) {
        if (minutes < 0) {
            return "0分钟";
        }
        long hours = minutes / 60;
        long mins = minutes % 60;
        if (hours > 0) {
            return hours + "小时" + mins + "分钟";
        }
        return mins + "分钟";
    }

    /**
     * 计算签到时长（用于CheckIn）
     * @param checkinTime 签到时间
     * @param checkoutTime 签退时间（null则用当前时间）
     * @return 格式化后的时长字符串
     */
    public static String calculateCheckinDuration(Date checkinTime, Date checkoutTime) {
        if (checkinTime == null) {
            return "未签到";
        }
        Date endTime = checkoutTime != null ? checkoutTime : now();
        long minutes = minutesBetween(checkinTime, endTime);
        return formatDuration(minutes);
    }
}

