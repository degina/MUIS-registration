package controllers.ReportClass;

import models.DailyLogScheduledWork;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by enkhamgalan on 6/26/15.
 */
public class ReportMainDate {
    public DailyLogScheduledWork beforeScheduledWork; // only task
    public DailyLogScheduledWork afterScheduledWork; // only task
    public List<ReportDate> reportDates = new ArrayList<ReportDate>();
}
