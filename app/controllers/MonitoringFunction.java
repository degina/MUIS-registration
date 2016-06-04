package controllers;

import controllers.ReportClass.ReportDate;
import controllers.ReportClass.ReportDateBetween;
import controllers.ReportClass.ReportFunction;
import models.PercentHistoryObject;
import models.Project;
import models.ProjectObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by enkhamgalan on 7/21/15.
 */
public class MonitoringFunction extends CRUD {
    public static List<ReportDate> getReportDate(ReportDateBetween dateBetween) {
        List<ReportDate> reportDates = new ArrayList<ReportDate>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateBetween.dateS);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd");
        for (int d = 0; d <= dateBetween.maxDay; d++) {
            ReportDate reportDate = new ReportDate();
            reportDate.dateS = calendar.getTime();
            reportDate.text = simpleDateFormat.format(reportDate.dateS);
            reportDates.add(reportDate);
            calendar.add(Calendar.DATE, 1);
        }
        return reportDates;
    }

    public static boolean checkStart(Date dateS, Date date, Date dateF) {
        return (dateS.getTime() <= date.getTime() && date.getTime() < dateF.getTime());
    }

    public static List<PercentHistoryObject> getHistoryObjects(ReportDateBetween dateBetween, ProjectObject object) {
        return PercentHistoryObject.find("projectObject.id=?1 and date>=?2 and date<?3", object.id, dateBetween.dateS, dateBetween.dateF).fetch();
    }

    public static Float getPercentObjects(List<PercentHistoryObject> historyObjects, Date date) {
        for (PercentHistoryObject historyObject : historyObjects) {
            if (historyObject.date.compareTo(date) == 0) return historyObject.completedPercent;
        }
        return 0F;
    }

    public static boolean checkFinish(Date date, Date dateF, Float percent) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return ((dateF.getTime() == calendar.getTime().getTime()) || percent >= 100);
    }

    public static float mustPercent(Project project,Date today, Date dateS, Long duration) {
        float per = 100 / duration;
        return per * Functions.getDifferenceWorkDays(project, dateS, today);
    }
}
