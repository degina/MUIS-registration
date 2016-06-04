package controllers;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by enkhamgalan on 5/19/15.
 */
public class FunctionDate {
    public static boolean isHoliday(Date date, String project_holidays, String project_weekend) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        String ymd = "#" + calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DATE) + "#";
        String md = "#" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DATE) + "#";
        System.out.println("ymd:" + ymd);
        System.out.println("md:" + md);
        return (checkIsDayHoy(calendar.get(Calendar.DATE), project_weekend.split("#")) || project_holidays.indexOf(ymd) > -1 || project_holidays.indexOf(md) > -1);
    }

    public static boolean checkIsDayHoy(int day, String[] project_weekends) {
        if (project_weekends.length > 0) {
            for (int i = 0; i < project_weekends.length - 1; i++) {
                if (Integer.parseInt(project_weekends[i]) == day) return true;
            }
        }
        return false;
    }

    public static int distanceInWorkingDays(Date start, Date end, String project_holidays, String project_weekend) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 0);
        Calendar calend = Calendar.getInstance();
        calend.setTime(end);
        calend.set(Calendar.HOUR_OF_DAY, 23);
        calend.set(Calendar.MINUTE, 59);
        calend.set(Calendar.SECOND, 59);
        calend.set(Calendar.MILLISECOND, 0);
        int days = 0;
        long nd = calend.getTime().getTime();
        while (calendar.getTime().getTime() <= nd) {
            days = days + (isHoliday(calendar.getTime(), project_holidays, project_weekend) ? 0 : 1);
            calend.add(Calendar.DATE, 1);
        }
        return days;
    }

    public static Date computeEndByDuration(Date start, int duration, String project_holidays, String project_weekend) {
        int q = duration - 1;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while (q > 0) {
            calendar.add(Calendar.DATE, 1);
            if (!isHoliday(calendar.getTime(), project_holidays, project_weekend))
                q--;
        }
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}
