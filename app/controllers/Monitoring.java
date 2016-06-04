package controllers;

import controllers.ReportClass.ReportDate;
import controllers.ReportClass.ReportDateBetween;
import controllers.ReportClass.ReportFunction;
import models.Project;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by enkhamgalan on 7/21/15.
 */
public class Monitoring extends CRUD {
    public static void panel() {
        Project project = Project.findById(Users.pid());
        Date dateS = Functions.convertHourNull(new Date());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateS);
        int month = calendar.get(Calendar.MONTH) + 1;
        ReportDateBetween dateBetween = ReportFunction.getDateBetween(0, dateS, 0);
        List<ReportDate> reportDates = MonitoringFunction.getReportDate(dateBetween);
        render(project, month, reportDates, dateS,dateBetween);
    }
}
