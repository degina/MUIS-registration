package controllers.ReportClass;

import models.DailyLogScheduledWork;

import java.util.Date;

/**
 * Created by enkhamgalan on 4/13/15.
 */
public class ReportDate {
    public Date dateS;
    public String text;
    public DailyLogScheduledWork scheduledWork; // only task
    public Float completedPercent;
    public boolean isHoliday=false;
}
