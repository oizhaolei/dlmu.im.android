package com.ruptech.chinatalk.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.util.Log;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.BuildConfig;
import com.ruptech.chinatalk.R;

public class DateCommonUtils {

	public final static String TAG = Utils.CATEGORY
			+ DateCommonUtils.class.getSimpleName();
	public static String DF_yyyyMM = "yyyy-MM";
	public static String DF_yyyyMMddHHmmss = "yyyy-MM-dd HH:mm:ss";
	public static String DF_yyyyMMddHHmmssSSS = "yyyy-MM-dd HH:mm:ss.SSS";
	public static String DF_yyyyMMddHHmmss2 = "yyyyMMddHHmmss";
	public static String DF_yyyyMMddHHmmssSSS2 = "yyyyMMddHHmmssSSS";
	public final static String DF_yyyyMMdd = "yyyy-MM-dd";
	public static String DF_MMddHHmm = "MM-dd HH:mm";
	public static String DF_HHmm = "HH:mm";

	public static Locale defaultLocale = Locale.getDefault();

	public static final long DAY_SPAN_SIZE = 24 * 60 * 60 * 1000;

	public static final long CHAT_TIME_SPAN_SIZE = 60 * 60 * 1000;

	public static boolean chatDiffTime(String prevPubDate, String pubDate) {
		boolean isDiff = false;
		Date currentDate = Calendar.getInstance().getTime();
		Date itemDate = parseToDateFromString(pubDate);
		if (itemDate != null)
			itemDate.setSeconds(0);
		else
			return isDiff;

		TimeSpan timeSpan = new TimeSpan();

		if (prevPubDate == null) {
			timeSpan.set(itemDate.getTime(), currentDate.getTime());
			isDiff = true;
		} else {

			Date prevDate = parseToDateFromString(prevPubDate);
			if (prevDate == null) {
				return false;
			}
			prevDate.setSeconds(0);

			timeSpan.set(prevDate.getTime(), itemDate.getTime());

			if (Math.abs(timeSpan.size()) - CHAT_TIME_SPAN_SIZE > 0) {
				isDiff = true;
			} else {
				isDiff = false;
			}
		}
		return isDiff;
	}

	public static String convUtcDateString(Date date, String pattern) {
		try {
			long t = date.getTime() + TimeZone.getDefault().getRawOffset();
			SimpleDateFormat df = new SimpleDateFormat(pattern,
					defaultLocale);
			return df.format(t);
		} catch (Exception e) {
			return "";
		}
	}

	public static String convUtcDateString(String str, String pattern) {
		try {
			Date parseDate = parseToDateFromString(str);
			long utcTimeMillis = parseDate.getTime()
					+ TimeZone.getDefault().getRawOffset();

			SimpleDateFormat df = new SimpleDateFormat(pattern,
					defaultLocale);
			String utcDatetimeStr = df.format(utcTimeMillis);

			return utcDatetimeStr;
		} catch (Exception e) {
			return "";
		}
	}

	public static String dateFormat(Date dd, String pattern) {
		if (dd == null)
			return "";
		try {
			SimpleDateFormat df = new SimpleDateFormat(pattern, defaultLocale);
			return df.format(dd);
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * 计算两个日期之间相差的天数
	 * 
	 * @param smdate
	 *            较小的时间
	 * @param bdate
	 *            较大的时间
	 * @return 相差天数
	 * @throws ParseException
	 */
	private static int daysBetween(Date smdate, Date bdate)
			throws ParseException {
		SimpleDateFormat df = new SimpleDateFormat(DF_yyyyMMdd, defaultLocale);
		
		String smdateStr = DateCommonUtils.dateFormat(smdate, DF_yyyyMMdd);
		smdate = df.parse(smdateStr);
		String bdateStr = DateCommonUtils.dateFormat(bdate, DF_yyyyMMdd);
		bdate = df.parse(bdateStr);
		Calendar cal = Calendar.getInstance();
		cal.setTime(smdate);
		long time1 = cal.getTimeInMillis();
		cal.setTime(bdate);
		long time2 = cal.getTimeInMillis();
		long between_days = (time2 - time1) / (1000 * 3600 * 24);

		return Integer.parseInt(String.valueOf(between_days));
	}

	/**
	 * 同年不显示年，同年同月不显示年月，同年同月同日不显示年月日
	 * 
	 * @param date
	 *            时间
	 * @param withHM
	 *            是否需要显示时分：0否，1是
	 * @param dayFormat
	 *            今天显示的格式：0显示时间，1显示今天
	 * @return
	 */
	public static String formatConvUtcDateString(String date, boolean withHM,
			boolean dayFormat) {
		try {
			String formatConvUtcDateStr = " ";
			Calendar cal = Calendar.getInstance();

			if (Utils.isEmpty(date)) {
				return formatConvUtcDateStr;
			}

			Date parseDate = DateCommonUtils.parseToDateFromString(date);

			long utcTimeMillis = parseDate.getTime()
					+ TimeZone.getDefault().getRawOffset();
			Date utcDatetime = new Date(utcTimeMillis);
			cal.setTime(utcDatetime);// utc
			int utcYear = cal.get(Calendar.YEAR);
			int utcMonth = cal.get(Calendar.MONTH);
			int utcDay = cal.get(Calendar.DATE);
			int utcHour = cal.get(Calendar.HOUR_OF_DAY);
			int utcMinute = cal.get(Calendar.MINUTE);
			String formatUtcMinute = utcMinute < 10 ? "0"
					+ String.valueOf(utcMinute) : String.valueOf(utcMinute);// 分钟两位数

			String currentDateStr = DateCommonUtils.dateFormat(new Date(),
					DateCommonUtils.DF_yyyyMMddHHmmssSSS);
			Date currentDatetime = parseToDateFromString(currentDateStr);
			cal.setTime(currentDatetime);
			int currentYear = cal.get(Calendar.YEAR);
			int currentMonth = cal.get(Calendar.MONTH);
			int currentDay = cal.get(Calendar.DATE);

			if (utcYear == currentYear && utcMonth == currentMonth
					&& utcDay == currentDay) {// 同一天显示
				if (dayFormat) {
					formatConvUtcDateStr += App.mContext
							.getString(R.string.today);
				} else {
					formatConvUtcDateStr += utcHour + ":" + formatUtcMinute
							+ " ";
					withHM = false;
				}

			} else if (utcYear == currentYear) {// 不是同一天且同一年显示
				int daysBetween = daysBetween(utcDatetime, currentDatetime);
				if (daysBetween == 1) {// 昨天
					formatConvUtcDateStr += App.mContext
							.getString(R.string.yesterday);
				} else if (daysBetween == 2) {// 前天
					formatConvUtcDateStr += App.mContext
							.getString(R.string.before_yesterday);
				} else {// 显示月日
					formatConvUtcDateStr += DateCommonUtils.dateFormat(
							new Date(utcTimeMillis),
							App.mContext.getString(R.string.df_mmdd));
				}
			} else {// 不是同一年显示
				formatConvUtcDateStr += DateCommonUtils.dateFormat(new Date(
						utcTimeMillis), App.mContext
						.getString(R.string.df_yyyymmdd));
			}
			if (withHM) {
				formatConvUtcDateStr += " " + utcHour + ":" + formatUtcMinute
						+ " ";
			}
			return formatConvUtcDateStr;
		} catch (Exception e) {
			if (BuildConfig.DEBUG)
				Log.w(TAG, "Could not parse date string: " + date);
			return "";
		}
	}

	// 跨时区时间转换成分钟
	public static int getTimezoneMinuteOffset() {
		int minute = TimeZone.getDefault().getRawOffset() / (1000 * 60);
		return minute;
	}

	public static String getUtcDate(Date date, String pattern) {
		try {
			long t = date.getTime() - TimeZone.getDefault().getRawOffset();
			SimpleDateFormat df = new SimpleDateFormat(pattern, defaultLocale);
			return df.format(t);
		} catch (Exception e) {
			return "";
		}
	}

	public static Date parseToDateFromString(String str) {
		if (Utils.isEmpty(str))
			return null;
		try {
			SimpleDateFormat df = new SimpleDateFormat(DF_yyyyMMddHHmmssSSS,
					defaultLocale);
			return df.parse(str);
		} catch (Throwable e) {
			try {
				SimpleDateFormat df = new SimpleDateFormat(DF_yyyyMMddHHmmss,
						defaultLocale);
				return df.parse(str);
			} catch (Throwable e1) {
				return null;
			}
		}
	}

    /**
     * 同年不显示年，同年同月不显示年月，同年同月同日不显示年月日
     *
     * @param timeMillis
     *            !显示的时间
     * @param withHM
     *            是否需要显示时分：0否，1是
     * @param dayFormat
     *            今天显示的格式：0显示时间，1显示今天
     * @return
     */
    public static String formatDateToString(long  timeMillis, boolean withHM,
                                                 boolean dayFormat) {
        try {
            String formatConvUtcDateStr = " ";
            Calendar cal = Calendar.getInstance();
            Date datetime = new Date(timeMillis);
            cal.setTime(datetime);
            int utcYear = cal.get(Calendar.YEAR);
            int utcMonth = cal.get(Calendar.MONTH);
            int utcDay = cal.get(Calendar.DATE);
            int utcHour = cal.get(Calendar.HOUR_OF_DAY);
            int utcMinute = cal.get(Calendar.MINUTE);
            String formatUtcMinute = utcMinute < 10 ? "0"
                    + String.valueOf(utcMinute) : String.valueOf(utcMinute);// 分钟两位数

            String currentDateStr = DateCommonUtils.dateFormat(new Date(),
                    DateCommonUtils.DF_yyyyMMddHHmmssSSS);
            Date currentDatetime = parseToDateFromString(currentDateStr);
            cal.setTime(currentDatetime);
            int currentYear = cal.get(Calendar.YEAR);
            int currentMonth = cal.get(Calendar.MONTH);
            int currentDay = cal.get(Calendar.DATE);

            if (utcYear == currentYear && utcMonth == currentMonth
                    && utcDay == currentDay) {// 同一天显示
                if (dayFormat) {
                    formatConvUtcDateStr += App.mContext
                            .getString(R.string.today);
                } else {
                    formatConvUtcDateStr += utcHour + ":" + formatUtcMinute
                            + " ";
                    withHM = false;
                }

            } else if (utcYear == currentYear) {// 不是同一天且同一年显示
                int daysBetween = daysBetween(datetime, currentDatetime);
                if (daysBetween == 1) {// 昨天
                    formatConvUtcDateStr += App.mContext
                            .getString(R.string.yesterday);
                } else if (daysBetween == 2) {// 前天
                    formatConvUtcDateStr += App.mContext
                            .getString(R.string.before_yesterday);
                } else {// 显示月日
                    formatConvUtcDateStr += DateCommonUtils.dateFormat(
                            new Date(timeMillis),
                            App.mContext.getString(R.string.df_mmdd));
                }
            } else {// 不是同一年显示
                formatConvUtcDateStr += DateCommonUtils.dateFormat(new Date(
                        timeMillis), App.mContext
                        .getString(R.string.df_yyyymmdd));
            }
            if (withHM) {
                formatConvUtcDateStr += " " + utcHour + ":" + formatUtcMinute
                        + " ";
            }
            return formatConvUtcDateStr;
        } catch (Exception e) {
            if (BuildConfig.DEBUG)
                Log.w(TAG, "Could not parse date string: " + timeMillis);
            return "";
        }
    }

}
