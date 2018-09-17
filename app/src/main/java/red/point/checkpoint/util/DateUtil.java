package red.point.checkpoint.util;


import android.text.format.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtil {

    private static final String TAG = DateUtil.class.getSimpleName();

    public static String addDay(String date, String dateFormat, int day) {
        SimpleDateFormat parser = new SimpleDateFormat(dateFormat);
        Date mDate = null;
        try {
            mDate = parser.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = formatter.format(mDate);

        SimpleDateFormat returnFormat = new SimpleDateFormat("dd/MM/yyyy");
        Calendar c = Calendar.getInstance();

        try {
            c.setTime(formatter.parse(formattedDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        c.add(Calendar.DATE, day);

        return returnFormat.format(c.getTime());
    }

    public static String getCurrentFormattedDate() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
        return format1.format(cal.getTime());
    }

    public static String timestampToTime(Long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp * 1000);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        return simpleDateFormat.format(cal.getTime());
    }

    public static String getCurrentDbDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        return format1.format(cal.getTime());
    }

    public static String getThisMonthDbDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DATE, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        return format1.format(cal.getTime());
    }

    public static String formattedDbDate(String date) {
        String[] string = StringUtil.split(date, "/");

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(string[0]));
        cal.set(Calendar.MONTH, Integer.parseInt(string[1]) - 1);
        cal.set(Calendar.YEAR, Integer.parseInt(string[2]));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String formatted = sdf.format(cal.getTime());
        return formatted;
    }

    public static String formattedDbDate(long timeInMillis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeInMillis);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(cal.getTime());
    }

    public static String formattedHumanDate(long date) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String formatted = sdf.format(cal.getTime());
        return formatted;
    }

    public static String formattedFullHumanDate(String date) {
        String[] string = StringUtil.split(date, "-");

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(string[2]));
        cal.set(Calendar.MONTH, Integer.parseInt(string[1]) - 1);
        cal.set(Calendar.YEAR, Integer.parseInt(string[0]));

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
        String formatted = sdf.format(cal.getTime());
        return formatted;
    }

    public static String formattedFullHumanDate(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(timestamp));

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
        String formatted = sdf.format(cal.getTime());
        return formatted;
    }

    public static String formattedFullHumanDateTime(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(timestamp));

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy kk:mm");
        String formatted = sdf.format(cal.getTime());
        return formatted;
    }

    public static int getCurrentHour() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.HOUR_OF_DAY);
    }

    public static String getCurrentTime() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        return String.format("%02d:%02d", hour, minute);
    }

    public static Long getCurrentTimeInMillis() {
        Calendar cal = Calendar.getInstance();
        return cal.getTimeInMillis();
    }

    public static long getTime(String date) {
        String[] string = StringUtil.split(date, "/");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(string[0]));
        cal.set(Calendar.MONTH, Integer.parseInt(string[1]));
        cal.set(Calendar.YEAR, Integer.parseInt(string[2]));
        return cal.getTimeInMillis();
    }

    public static long getMinutesLate(String time) {

        String[] string = StringUtil.split(time, ":");
        int hour = Integer.valueOf(string[0]);
        int minute = Integer.valueOf(string[1]);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);

        long now = System.currentTimeMillis();
        long difference = now - cal.getTimeInMillis();

        long differenceInMinute = difference / DateUtils.MINUTE_IN_MILLIS;
        return differenceInMinute;
    }

    public static long getTimeDiff(String time, String time2) {

        String[] string = StringUtil.split(time, ":");
        int hour = Integer.valueOf(string[0]);
        int minute = Integer.valueOf(string[1]);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);

        String[] string2 = StringUtil.split(time2, ":");
        int hour2 = Integer.valueOf(string2[0]);
        int minute2 = Integer.valueOf(string2[1]);

        Calendar cal2 = Calendar.getInstance();
        cal2.set(Calendar.HOUR_OF_DAY, hour2);
        cal2.set(Calendar.MINUTE, minute2);

        long difference = cal2.getTimeInMillis() - cal.getTimeInMillis();

        long differenceInMinute = difference / DateUtils.MINUTE_IN_MILLIS;
        return differenceInMinute;
    }

    public static long dateToMilis(String date) {
        String[] string = StringUtil.split(date, "-");

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(string[2]));
        cal.set(Calendar.MONTH, Integer.parseInt(string[1]) -1);
        cal.set(Calendar.YEAR, Integer.parseInt(string[0]));

        return cal.getTimeInMillis();
    }

    public static String timestampToDate(String timezone, String date) {

        Calendar originalTimeStamp = convertOriginalTimestamp(timezone, date);

        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getDefault());
        cal.setTimeInMillis(originalTimeStamp.getTimeInMillis());

        return String.format(Locale.getDefault(),
                "%02d/%02d/%04d",
                cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.YEAR));
    }

    public static String timestampToDateTime(String timezone, String date) {

        Calendar originalTimeStamp = convertOriginalTimestamp(timezone, date);

        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getDefault());
        cal.setTimeInMillis(originalTimeStamp.getTimeInMillis());

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return simpleDateFormat.format(cal.getTime());
    }

    private static Calendar convertOriginalTimestamp(String timezone, String date) {
        String[] dateAndTime = StringUtil.split(date, " ");
        String[] stringDate = StringUtil.split(dateAndTime[0], "-");
        String[] stringTimeMillisecond = StringUtil.split(dateAndTime[1], "\\.");
        String[] stringTime = StringUtil.split(stringTimeMillisecond[0], ":");

        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone(timezone));
        cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(stringTime[0]));
        cal.set(Calendar.MINUTE, Integer.parseInt(stringTime[1]));
        cal.set(Calendar.SECOND, Integer.parseInt(stringTime[2]));
        cal.set(Calendar.MILLISECOND, Integer.parseInt(stringTimeMillisecond[1]));
        cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(stringDate[2]));
        cal.set(Calendar.MONTH, Integer.parseInt(stringDate[1]) -1);
        cal.set(Calendar.YEAR, Integer.parseInt(stringDate[0]));

        return cal;
    }

    public static boolean isCheckInTimeAllowed(String shiftStart) {

        String[] time = StringUtil.split(shiftStart, ":");

        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getDefault());
        cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
        cal.set(Calendar.MINUTE, Integer.parseInt(time[1]));
        long shiftTime = cal.getTimeInMillis();

        Calendar calNow = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getDefault());
        long checkInTime = calNow.getTimeInMillis();

        // Check in time should not be done more than one hour
        return shiftTime - checkInTime >= 60 * 60 * 1000;
    }
}
