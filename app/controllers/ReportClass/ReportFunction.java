package controllers.ReportClass;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import controllers.Consts;
import controllers.Functions;
import models.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by enkhamgalan on 4/13/15.
 */
public class ReportFunction {
    public static ReportDateBetween getDateBetween(int timeType, Date dateS, int change) {
        ReportDateBetween dateBetween = new ReportDateBetween();
        dateBetween.dateF = null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateS);
        switch (timeType) {
            case 0: // Month
                dateS = new Date(calendar.get(Calendar.YEAR) - 1900,
                        calendar.get(Calendar.MONTH), 1);
                dateBetween.maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH) - 1;
                calendar.setTime(dateS);
                if (change == 1) {
                    calendar.add(Calendar.MONTH, 1);
                    dateS = calendar.getTime();
                } else if (change == -1) {
                    calendar.add(Calendar.MONTH, -1);
                    dateS = calendar.getTime();
                }
                calendar.add(Calendar.MONTH, 1);
                dateBetween.dateF = calendar.getTime();
                calendar.add(Calendar.DATE, -1);
                dateBetween.title = "Сар: " + (calendar.get(Calendar.MONTH) + 1) + " (" + Consts.dateFormat2.format(dateS)
                        + " - " + Consts.dateFormat2.format(calendar.getTime()) + ")";
                break;
            case 1: // Week
                calendar.setFirstDayOfWeek(Consts.firstDayOfWeek);
                calendar.set(Calendar.DAY_OF_WEEK, Consts.firstDayOfWeek);
                dateS = calendar.getTime();
                if (change == 1) {
                    calendar.add(Calendar.WEEK_OF_YEAR, 1);
                    dateS = calendar.getTime();
                } else if (change == -1) {
                    calendar.add(Calendar.WEEK_OF_YEAR, -1);
                    dateS = calendar.getTime();
                }
                calendar.add(Calendar.WEEK_OF_YEAR, 1);
                dateBetween.dateF = calendar.getTime();
                calendar.add(Calendar.DATE, -1);
                dateBetween.title = "Долоо хоног: " + calendar.get(Calendar.WEEK_OF_YEAR) + " (" + Consts.dateFormat2.format(dateS)
                        + " - " + Consts.dateFormat2.format(calendar.getTime()) + ")";
                dateBetween.maxDay = 6;
                break;
            case 2: // Day
                if (change == 1) {
                    calendar.add(Calendar.DATE, 1);
                    dateS = calendar.getTime();
                } else if (change == -1) {
                    calendar.add(Calendar.DATE, -1);
                    dateS = calendar.getTime();
                }
                calendar.add(Calendar.DATE, 1);
                dateBetween.dateF = calendar.getTime();
                dateBetween.title = "Өдөр: " + Consts.dateFormat2.format(dateS);
                break;
        }
        dateBetween.dateS = dateS;
//        System.out.println(dateS + "==" + dateBetween.dateF);
        return dateBetween;
    }

    public static ReportMainDate getReportDateObject(int otype, Object object, ReportDateBetween dateBetween) {
        if (otype == 0)
            return ReportFunctionLocal.getReportDate(dateBetween, (Project) object, null, null, null);
        if (otype == 1)
            return ReportFunctionLocal.getReportDate(dateBetween, null, (ProjectObject) object, null, null);
        if (otype == 2)
            return ReportFunctionLocal.getReportDate(dateBetween, null, null, (Task) object, null);
        if (otype == 3)
            return ReportFunctionLocal.getReportDate(dateBetween, null, null, null, (DailyLogMyPlan) object);
        return null;
    }

    public static List<DailyLogManpower> getDailyLogManPowers(Long sid) {
        return DailyLogManpower.find("owner.userTeam.contractor=false AND scheduledWork.id=?1", sid).fetch();
    }

    public static List<DailyLogEquipment> getDailyLogEquipments(Long sid) {
        return DailyLogEquipment.find("owner.userTeam.contractor=false AND scheduledWork.id=?1", sid).fetch();
    }

    public static List<DailyLogMaterial> getDailyLogMaterials(Long sid) {
        return DailyLogMaterial.find("owner.userTeam.contractor=false AND scheduledWork.id=?1", sid).fetch();
    }

    public static List<DailyLogWeather> getDailyLogWeathers(Long tid, Long mid, Long oid, Date dateS, Date dateF) {
        if (oid != null) {
            if (mid == null)
                return DailyLogWeather.find("owner.id=?1 AND task.id=?2 AND date>=?3 AND date<?4", oid, tid, dateS, dateF).fetch();
            else
                return DailyLogWeather.find("owner.id=?1 AND myPlan.id=?2 AND date>=?3 AND date<?4", oid, mid, dateS, dateF).fetch();
        } else
            return DailyLogWeather.find("task.id=?1 AND date>=?2 AND date<?3 AND owner.userTeam.contractor=false", tid, dateS, dateF).fetch();
    }

    public static List<DailyLogWeather> getProjectWeathers(int type, Long pid, Date dateS, Date dateF) {
        return DailyLogWeather.find("task.projectObject" + (type == 0 ? ".project" : "") + ".id=?1 AND date>=?2 AND date<?3 AND owner.userTeam.contractor=false", pid, dateS, dateF).fetch();
    }

    public static List<DailyLogTechnicalDelay> getDailyLogTechnicalDelays(Long tid, Long mid, Long oid, Date dateS, Date dateF) {
        if (oid != null) {
            if (mid == null)
                return DailyLogTechnicalDelay.find("owner.id=?1 AND task.id=?2 AND date>=?3 AND date<?4", oid, tid, dateS, dateF).fetch();
            else
                return DailyLogTechnicalDelay.find("owner.id=?1 AND myPlan.id=?2 AND date>=?3 AND date<?4", oid, mid, dateS, dateF).fetch();
        } else
            return DailyLogTechnicalDelay.find("task.id=?1 AND date>=?2 AND date<?3 AND owner.userTeam.contractor=false", tid, dateS, dateF).fetch();
    }

    public static List<DailyLogTechnicalDelay> getProjectTechnicalDelays(int type, Long pid, Date dateS, Date dateF) {
        return DailyLogTechnicalDelay.find("task.projectObject" + (type == 0 ? ".project" : "") + ".id=?1 AND date>=?2 AND date<?3 AND owner.userTeam.contractor=false", pid, dateS, dateF).fetch();
    }

    public static List<DailyLogDumpster> getDailyLogDumpsters(Long tid, Long mid, Long oid, Date dateS, Date dateF) {
        if (oid != null) {
            if (mid == null)
                return DailyLogDumpster.find("owner.id=?1 AND task.id=?2 AND date>=?3 AND date<?4", oid, tid, dateS, dateF).fetch();
            else
                return DailyLogDumpster.find("owner.id=?1 AND myPlan.id=?2 AND date>=?3 AND date<?4", oid, mid, dateS, dateF).fetch();
        } else
            return DailyLogDumpster.find("task.id=?1 AND date>=?2 AND date<?3 AND owner.userTeam.contractor=false", tid, dateS, dateF).fetch();
    }

    public static List<DailyLogDumpster> getProjectDumpsters(int type, Long pid, Date dateS, Date dateF) {
        return DailyLogDumpster.find("task.projectObject" + (type == 0 ? ".project" : "") + ".id=?1 AND date>=?2 AND date<?3 AND owner.userTeam.contractor=false", pid, dateS, dateF).fetch();
    }

    public static List<DailyLogSafety> getDailyLogSafetys(Long tid, Long mid, Long oid, Date dateS, Date dateF) {
        if (oid != null) {
            if (mid == null)
                return DailyLogSafety.find("owner.id=?1 AND task.id=?2 AND date>=?3 AND date<?4", oid, tid, dateS, dateF).fetch();
            else
                return DailyLogSafety.find("owner.id=?1 AND myPlan.id=?2 AND date>=?3 AND date<?4", oid, mid, dateS, dateF).fetch();
        } else
            return DailyLogSafety.find("task.id=?1 AND date>=?2 AND date<?3 AND owner.userTeam.contractor=false", tid, dateS, dateF).fetch();
    }

    public static List<DailyLogSafety> getProjectSafetys(int type, Long pid, Date dateS, Date dateF) {
        return DailyLogSafety.find("task.projectObject" + (type == 0 ? ".project" : "") + ".id=?1 AND date>=?2 AND date<?3 AND owner.userTeam.contractor=false", pid, dateS, dateF).fetch();
    }

    public static List<DailyLogInspection> getDailyLogInspections(Long tid, Long mid, Long oid, Date dateS, Date dateF) {
        if (oid != null) {
            if (mid == null)
                return DailyLogInspection.find("owner.id=?1 AND task.id=?2 AND date>=?3 AND date<?4", oid, tid, dateS, dateF).fetch();
            else
                return DailyLogInspection.find("owner.id=?1 AND myPlan.id=?2 AND date>=?3 AND date<?4", oid, mid, dateS, dateF).fetch();
        } else
            return DailyLogInspection.find("task.id=?1 AND date>=?2 AND date<?3 AND owner.userTeam.contractor=false", tid, dateS, dateF).fetch();
    }

    public static List<DailyLogInspection> getProjectInspections(int type, Long pid, Date dateS, Date dateF) {
        return DailyLogInspection.find("task.projectObject" + (type == 0 ? ".project" : "") + ".id=?1 AND date>=?2 AND date<?3 AND owner.userTeam.contractor=false", pid, dateS, dateF).fetch();
    }

    public static List<DailyLogDelivery> getDailyLogDeliverys(Long tid, Long mid, Long oid, Date dateS, Date dateF) {
        if (oid != null) {
            if (mid == null)
                return DailyLogDelivery.find("owner.id=?1 AND task.id=?2 AND date>=?3 AND date<?4", oid, tid, dateS, dateF).fetch();
            else
                return DailyLogDelivery.find("owner.id=?1 AND myPlan.id=?2 AND date>=?3 AND date<?4", oid, mid, dateS, dateF).fetch();
        } else
            return DailyLogDelivery.find("task.id=?1 AND date>=?2 AND date<?3 AND owner.userTeam.contractor=false", tid, dateS, dateF).fetch();
    }

    public static List<DailyLogDelivery> getProjectDeliverys(int type, Long pid, Date dateS, Date dateF) {
        return DailyLogDelivery.find("task.projectObject" + (type == 0 ? ".project" : "") + ".id=?1 AND date>=?2 AND date<?3 AND owner.userTeam.contractor=false", pid, dateS, dateF).fetch();
    }

    public static List<DailyLogNote> getDailyLogNotes(Long tid, Long mid, Long oid, Date dateS, Date dateF) {
        if (oid != null) {
            if (mid == null)
                return DailyLogNote.find("owner.id=?1 AND task.id=?2 AND date>=?3 AND date<?4", oid, tid, dateS, dateF).fetch();
            else
                return DailyLogNote.find("owner.id=?1 AND myPlan.id=?2 AND date>=?3 AND date<?4", oid, mid, dateS, dateF).fetch();
        } else
            return DailyLogNote.find("task.id=?1 AND date>=?2 AND date<?3 AND owner.userTeam.contractor=false", tid, dateS, dateF).fetch();
    }

    public static List<DailyLogNote> getProjectNotes(int type, Long pid, Date dateS, Date dateF) {
        return DailyLogNote.find("task.projectObject" + (type == 0 ? ".project" : "") + ".id=?1 AND date>=?2 AND date<?3 AND owner.userTeam.contractor=false", pid, dateS, dateF).fetch();
    }

    public static List<DailyLogSanaachlaga> getDailyLogSanaachlagas(Long tid, Long mid, Long oid, Date dateS, Date dateF) {
        if (oid != null) {
            if (mid == null)
                return DailyLogSanaachlaga.find("owner.id=?1 AND task.id=?2 AND date>=?3 AND date<?4", oid, tid, dateS, dateF).fetch();
            else
                return DailyLogSanaachlaga.find("owner.id=?1 AND myPlan.id=?2 AND date>=?3 AND date<?4", oid, mid, dateS, dateF).fetch();
        } else
            return DailyLogSanaachlaga.find("task.id=?1 AND date>=?2 AND date<?3 AND owner.userTeam.contractor=false", tid, dateS, dateF).fetch();
    }

    public static List<DailyLogSanaachlaga> getProjectSanaachlagas(int type, Long pid, Date dateS, Date dateF) {
        return DailyLogSanaachlaga.find("task.projectObject" + (type == 0 ? ".project" : "") + ".id=?1 AND date>=?2 AND date<?3 AND owner.userTeam.contractor=false", pid, dateS, dateF).fetch();
    }

    public static List<DailyLogVisitor> getDailyLogVisitors(Long tid, Long mid, Long oid, Date dateS, Date dateF) {
        if (oid != null) {
            if (mid == null)
                return DailyLogVisitor.find("owner.id=?1 AND task.id=?2 AND date>=?3 AND date<?4", oid, tid, dateS, dateF).fetch();
            else
                return DailyLogVisitor.find("owner.id=?1 AND myPlan.id=?2 AND date>=?3 AND date<?4", oid, mid, dateS, dateF).fetch();
        } else
            return DailyLogVisitor.find("task.id=?1 AND date>=?2 AND date<?3 AND owner.userTeam.contractor=false", tid, dateS, dateF).fetch();
    }

    public static List<DailyLogVisitor> getProjectVisitors(int type, Long pid, Date dateS, Date dateF) {
        return DailyLogVisitor.find("task.projectObject" + (type == 0 ? ".project" : "") + ".id=?1 AND date>=?2 AND date<?3 AND owner.userTeam.contractor=false", pid, dateS, dateF).fetch();
    }

    public static List<DailyLogWaste> getDailyLogWastes(Long tid, Long mid, Long oid, Date dateS, Date dateF) {
        if (oid != null) {
            if (mid == null)
                return DailyLogWaste.find("owner.id=?1 AND task.id=?2 AND date>=?3 AND date<?4", oid, tid, dateS, dateF).fetch();
            else
                return DailyLogWaste.find("owner.id=?1 AND myPlan.id=?2 AND date>=?3 AND date<?4", oid, mid, dateS, dateF).fetch();
        } else
            return DailyLogWaste.find("task.id=?1 AND date>=?2 AND date<?3 AND owner.userTeam.contractor=false", tid, dateS, dateF).fetch();
    }

    public static List<DailyLogWaste> getProjectWastes(int type, Long pid, Date dateS, Date dateF) {
        return DailyLogWaste.find("task.projectObject" + (type == 0 ? ".project" : "") + ".id=?1 AND date>=?2 AND date<?3 AND owner.userTeam.contractor=false", pid, dateS, dateF).fetch();
    }

    public static List<TaskAssignRel> assignedTask(Long oid, Date dateS, Date dateF) {
        return TaskAssignRel.find("user.id=?1 AND " +
                        "((task.startDate>=?2 AND task.startDate<?3) OR (task.actualFinish>=?4 AND task.actualFinish<?5) OR" +
                        " (task.startDate<=?6 AND task.actualFinish>?7) OR (task.startDate<?8 AND task.actualFinish>?9)) " +
                        "AND task.hasChild=false " +
                        "AND task.projectObject.project.portfolio.isActive=true " +
                        "ORDER BY task.projectObject.project.id, task.projectObject.id",
                oid, dateS, dateF, dateS, dateF, dateS, dateS, dateF, dateF).fetch();
    }

    public static List<DailyLogMyPlan> assignedMyTask(Long oid, Date dateS, Date dateF) {
        return DailyLogMyPlan.find("owner.id=?1 AND " +
                        "((startDate>=?2 AND startDate<?3) OR (actualFinish>=?4 AND actualFinish<?5) OR" +
                        " (startDate<=?6 AND actualFinish>?7) OR (startDate<=?8 AND actualFinish>?9))",
                oid, dateS, dateF, dateS, dateF, dateS, dateS, dateF, dateF).fetch();
    }

    public static List<DailyLogWorkNote> getDailyLogWorkNotes(Long tid, Long oid, Date dateS, Date dateF) {
        if (oid != null)
            return DailyLogWorkNote.find("owner.id=?1 AND scheduledWork.task.id=?2 AND scheduledWork.date>=?3 AND scheduledWork.date<?4", oid, tid, dateS, dateF).fetch();
        else
            return DailyLogWorkNote.find("scheduledWork.task.id=?1 AND scheduledWork.date>=?2" +
                    " AND scheduledWork.date<?3", tid, dateS, dateF).fetch();
    }

    public static DailyLogWorkNote getDailyLogWorkNote(Long tid, Long mid, Long oid, Date dateS) {
        if (mid == null)
            return DailyLogWorkNote.find("owner.id=?1 AND scheduledWork.task.id=?2 AND scheduledWork.date=?3", oid, tid, dateS).first();
        else
            return DailyLogWorkNote.find("owner.id=?1 AND scheduledWork.myPlan.id=?2 AND scheduledWork.date=?3", oid, mid, dateS).first();
    }

    public static String getPercentHtml(Date startDate, Date date, Date finishDate, float percent, boolean plus, String additional) {
        if (!(startDate.getTime() <= date.getTime() && date.getTime() <= finishDate.getTime())) return "";
        else return getPercentHtmlFormatted(percent, 1, plus, additional);
    }

    public static String getPercentHtml2(Date startDate, Date dateS, Date dateF, Date finishDate, float percent, boolean plus) {
        if ((startDate.getTime() <= dateS.getTime() && dateS.getTime() <= finishDate.getTime()) ||
                (startDate.getTime() <= dateF.getTime() && dateF.getTime() <= finishDate.getTime()) ||
                (dateS.getTime() <= startDate.getTime() && startDate.getTime() <= dateF.getTime()) ||
                (dateS.getTime() <= finishDate.getTime() && finishDate.getTime() <= dateF.getTime()))
            return getPercentHtmlFormatted(percent, 1, plus, "%");
        else return "";
    }

    public static String getPercentHtmlFormatted(float percent, int decimal, boolean plus, String additional) {
        if (percent > 0) {
            if (plus)
                return "<strong class='c-green'> +" + controllers.Functions.getFloatFormat(percent, decimal) + additional + "</strong>";
            return "<strong class='c-green'> " + controllers.Functions.getFloatFormat(percent, decimal) + additional + "</strong>";
        } else
            return "<strong class='c-red'> " + controllers.Functions.getFloatFormat(percent, decimal) + additional + "</strong>";
    }

    public static DailyLogScheduledWork getDailyLogScheduledWork(Long tid, int ttype, Date date) {
        return DailyLogScheduledWork.find((ttype == 0 ? "task" : "myPlan") + ".id=?1 AND date=?2 ORDER BY progressPercent DESC", tid, date).first();
    }

    public static DailyLogScheduledWork getDailyLogProjectPrev(Long tid, Date date) {
        return DailyLogScheduledWork.find("task.id=?1 AND date<?2 order by date desc", tid, date).first();
    }

    public static ReportInfo getUserRifs_Punchlists(Long uid, int type, Date dateS, Date dateF) {
        ReportInfo reportInfo = new ReportInfo();
        List<RFI> rfis;
        if (type == 1) rfis = RFI.find("assignee.id=?1 AND " +
                        "((createDate>=?2 AND createDate<?3) OR (dueDate>=?4 AND dueDate<?5) OR " +
                        "(createDate<=?6 AND dueDate>?7) OR (createDate<=?8 AND dueDate>?9))",
                uid, dateS, dateF, dateS, dateF, dateS, dateS, dateF, dateF).fetch();
        else rfis = RFI.find("assignee.id =" + uid + " and status.status = 'Open'").fetch();
        Date today = new Date();
        today = Functions.convertHourNull(today);
        Date nextWeek = Functions.addOrMinusDays(today, 7, true);
        for (RFI rfi : rfis) {
            if (rfi.status.id.intValue() == 2) reportInfo.rfi_closed++;
            else {
                if (rfi.dueDate.getTime() < today.getTime()) reportInfo.rfi_over++;
                else if (rfi.dueDate.getTime() >= today.getTime() && rfi.dueDate.getTime() <= nextWeek.getTime())
                    reportInfo.rfi_inweek++;
                else reportInfo.rfi_nextWeek++;
            }
            reportInfo.rfi_all++;
        }
        List<PunchList> punchLists;
        if (type == 1) punchLists = PunchList.find("assignee.id=?1 AND " +
                        "(( createDate>=?2 AND createDate<?3 ) OR ( dueDate>=?4 AND dueDate<?5) OR " +
                        "( createDate<=?6 AND dueDate>?7 ) OR ( createDate<=?8 AND dueDate>?9 ))",
                uid, dateS, dateF, dateS, dateF, dateS, dateS, dateF, dateF).fetch();
        else punchLists = PunchList.find("assignee.id=" + uid + " and status.status = 'NotResolved'").fetch();
        for (PunchList punchList : punchLists) {
            if (punchList.status.id.intValue() == 2) reportInfo.punch_resolved++;
            else {
                if (punchList.dueDate.getTime() < today.getTime()) reportInfo.punch_over++;
                else if (punchList.dueDate.getTime() >= today.getTime() && punchList.dueDate.getTime() <= nextWeek.getTime())
                    reportInfo.punch_inweek++;
                else reportInfo.punch_nextWeek++;
            }
            reportInfo.punch_all++;
        }
        return reportInfo;
    }

    public static List<TaskAssignRel> getAssignedTasks(Long tid, boolean contractor) {
        return TaskAssignRel.find("task.id=?1 and user.userTeam.contractor=?2", tid, contractor).fetch();
    }

    public static List<DailyLogManpower> getDailyLogManpowers(int type, Long tid, Date dateS, Date dateF) {
        return DailyLogManpower.find("scheduledWork." + (type == 0 ? "task.projectObject.project" : (type == 1 ? "task.projectObject" : (type == 2 ? "task" : "myPlan"))) + ".id=?1" +
                " AND scheduledWork.date>=?2 AND scheduledWork.date<=?3 AND owner.userTeam.contractor=false ORDER BY mergejil.id", tid, dateS, dateF).fetch();
    }

    public static List<DailyLogEquipment> getDailyLogEquipments(int type, Long tid, Date dateS, Date dateF) {
        return DailyLogEquipment.find("scheduledWork." + (type == 0 ? "task.projectObject.project" : (type == 1 ? "task.projectObject" : (type == 2 ? "task" : "myPlan"))) + ".id=?1" +
                " AND scheduledWork.date>=?2 AND scheduledWork.date<=?3 AND owner.userTeam.contractor=false ORDER BY equipmentType.id", tid, dateS, dateF).fetch();
    }

    public static List<DailyLogMaterial> getDailyLogMaterials(int type, Long tid, Date dateS, Date dateF) {
        return DailyLogMaterial.find("scheduledWork." + (type == 0 ? "task.projectObject.project" : (type == 1 ? "task.projectObject" : (type == 2 ? "task" : "myPlan"))) + ".id=?1" +
                " AND scheduledWork.date>=?2 AND scheduledWork.date<=?3 AND owner.userTeam.contractor=false ORDER BY material.id", tid, dateS, dateF).fetch();
    }

    public static JsonArray getProgressCharts(String format, ReportMainDate reportMainDate) {
        JsonArray array = new JsonArray();
        JsonArray jsArrayDateNames = new JsonArray();
        JsonArray jsArrayProgress = new JsonArray();
        JsonArray jsArrayPercent = new JsonArray();
        jsArrayDateNames.add(new JsonPrimitive('x'));
        jsArrayProgress.add(new JsonPrimitive("Ахиц"));
        jsArrayPercent.add(new JsonPrimitive("Гүйцэтгэл"));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd");
        float progressPercent, completedPercent, max_completedPercent = 0;
        if (reportMainDate.beforeScheduledWork != null)
            max_completedPercent = reportMainDate.beforeScheduledWork.completedPercent;
        for (ReportDate reportDate : reportMainDate.reportDates) {
            if (reportDate.scheduledWork != null) {
                progressPercent = reportDate.scheduledWork.progressPercent;
                completedPercent = reportDate.scheduledWork.completedPercent;
                if (completedPercent > max_completedPercent) max_completedPercent = completedPercent;
            } else {
                progressPercent = 0;
                completedPercent = max_completedPercent;
            }
            if (format.equals("dd"))
                jsArrayDateNames.add(new JsonPrimitive("(" + simpleDateFormat.format(reportDate.dateS) + ") " + reportDate.text));
            else
                jsArrayDateNames.add(new JsonPrimitive(simpleDateFormat.format(reportDate.dateS)));
            jsArrayProgress.add(new JsonPrimitive(progressPercent));
            jsArrayPercent.add(new JsonPrimitive(completedPercent));
        }
        array.add(jsArrayDateNames);
        array.add(jsArrayProgress);
        array.add(jsArrayPercent);
        return array;
    }

    public static JsonArray getRelationWorkCharts(Object object, Object name) {
        List<Integer> values = (List<Integer>) object;
        List<String> names = (List<String>) name;
        JsonArray arrayMain = new JsonArray();
        int i = 0;
        for (String s : names) {
            if (values.get(i) > 0) {
                JsonArray array = new JsonArray();
                array.add(new JsonPrimitive(s));
                array.add(new JsonPrimitive(values.get(i)));
                arrayMain.add(array);
            }
            i++;
        }
        return arrayMain;
    }

    public static JsonArray getRfiPunchCharts(Long tid, Long uid, Date dateS, Date dateF) {
        int[] values = {0, 0, 0, 0, 0, 0, 0, 0};
        List<RFI> rfis = RFI.find("((createDate>=?1 AND createDate<?2) OR (dueDate>=?3 AND dueDate<?4) OR" +
                        "(createDate<=?5 AND dueDate>?6) OR (createDate<=?7 AND dueDate>?8)) AND" +
                        " status!=null AND " +
                        " task.id=?9 AND (assignee.id=?10 OR questionReceivedFrom.id=?11)",
                dateS, dateF, dateS, dateF, dateS, dateS, dateF, dateF, tid, uid, uid).fetch();
        for (RFI rfi : rfis) {
            if (rfi.assignee.id.compareTo(uid) == 0) {
                if (rfi.status.id.intValue() == 1) values[0]++;
                else if (rfi.status.id.intValue() == 2) values[1]++;
            } else if (rfi.questionReceivedFrom.id.compareTo(uid) == 0) {
                if (rfi.status.id.intValue() == 1) values[2]++;
                else if (rfi.status.id.intValue() == 2) values[3]++;
            }
        }
        List<PunchList> punchLists = PunchList.find("((createDate>=?1 AND createDate<?2) OR (dueDate>=?3 AND dueDate<?4) OR" +
                        "(createDate<=?5 AND dueDate>?6) OR (createDate<=?7 AND dueDate>?8)) AND" +
                        " status!=null AND " +
                        " task.id=?9 AND (assignee.id=?10 OR questionReceivedFrom.id=?11)",
                dateS, dateF, dateS, dateF, dateS, dateS, dateF, dateF, tid, uid, uid).fetch();
        for (PunchList punchList : punchLists) {
            if (punchList.assignee.id.compareTo(uid) == 0) {
                if (punchList.status.id.intValue() == 1) values[4]++;
                else if (punchList.status.id.intValue() == 2) values[5]++;
            } else if (punchList.questionReceivedFrom.id.compareTo(uid) == 0) {
                if (punchList.status.id.intValue() == 1) values[6]++;
                else if (punchList.status.id.intValue() == 2) values[7]++;
            }
        }
        String[] names = {"МХ ирсэн, нээлттэй байгаа", "МХ ирсэн, хаагдсан", "МХ явуулсан, нээлттэй байгаа", "МХ явуулсан, хаалгдсан",
                "ҮД ирсэн, нээлттэй байгаа", "ҮД ирсэн, хаагдсан", "ҮД явуулсан, нээлттэй байгаа", "ҮД явуулсан, хаалгдсан"};
        JsonArray arrayMain = new JsonArray();
        for (int i = 0; i < 8; i++) {
            if (values[i] > 0) {
                JsonArray array = new JsonArray();
                array.add(new JsonPrimitive(names[i]));
                array.add(new JsonPrimitive(values[i]));
                arrayMain.add(array);
            }
        }
        return arrayMain;
    }

    public static JsonArray getTaskRfiPunchCharts(int type, Long tid, Date dateS, Date dateF) {
        int[] values = {0, 0, 0, 0};
        List<RFI> rfis = RFI.find((type == 0 ? "project" : (type == 1 ? "task.projectObject" : "task")) + ".id=?1 AND " +
                "status!=null AND " +
                "((createDate>=?2 AND createDate<?3) OR (dueDate>=?4 AND dueDate<?5) OR" +
                " (createDate<=?6 AND dueDate>?7) OR (createDate<=?8 AND dueDate>?9))"
                , tid, dateS, dateF, dateS, dateF, dateS, dateS, dateF, dateF).fetch();
        for (RFI rfi : rfis) {
            if (rfi.status.id.intValue() == 1) values[0]++;
            else if (rfi.status.id.intValue() == 2) values[1]++;
        }
        List<PunchList> punchLists = PunchList.find((type == 0 ? "project" : (type == 1 ? "task.projectObject" : "task")) + ".id=?1 AND " +
                        "status!=null AND " +
                        "((createDate>=?2 AND createDate<?3) OR (dueDate>=?4 AND dueDate<?5) OR" +
                        "(createDate<=?6 AND dueDate>?7) OR (createDate<=?8 AND dueDate>?9))",
                tid, dateS, dateF, dateS, dateF, dateS, dateS, dateF, dateF).fetch();
        for (PunchList punchList : punchLists) {
            if (punchList.status.id.intValue() == 1) values[2]++;
            else if (punchList.status.id.intValue() == 2) values[3]++;
        }

        JsonArray arrayMain = new JsonArray();
        String[] names = {"МХ нээлттэй байгаа", "МХ хаагдсан", "ҮД нээлттэй байгаа", "ҮД хаагдсан"};
        for (int i = 0; i < 4; i++) {
            if (values[i] > 0) {
                JsonArray array = new JsonArray();
                array.add(new JsonPrimitive(names[i]));
                array.add(new JsonPrimitive(values[i]));
                arrayMain.add(array);
            }
        }
        return arrayMain;
    }

    public static List<ReportChartValue> getChartValues(List<JsonArray> charts) {
        List<ReportChartValue> reportChartValues = new ArrayList<ReportChartValue>();
        JsonArray normal = charts.get(1);
        JsonArray used = charts.get(2);
        JsonArray count = null;
        if (charts.size() > 3) count = charts.get(3);
        int i = 0;
        for (JsonElement element : normal) {
            if (i > 0) {
                ReportChartValue reportChartValue = new ReportChartValue();
                reportChartValue.normal = element.getAsFloat();
                reportChartValue.value = used.get(i).getAsFloat();
                reportChartValue.count = (count != null ? count.get(i).getAsInt() : 0);
                reportChartValues.add(reportChartValue);
            }
            i++;
        }
        return reportChartValues;
    }

    public static float getProgressedPercent(int type, int timeType, Long id, Date date, float nowPercent) {
        if (timeType == 2) {
//            Calendar calendar = Calendar.getInstance();
//            calendar.setTime(date);
            if (type == 0) { // project
                PercentHistoryProject percentHistoryProject = PercentHistoryProject.find("project.id=?1 AND date<?2 ORDER BY date DESC", id, date).first();
                if (percentHistoryProject != null) return nowPercent - percentHistoryProject.completedPercent;
            } else if (type == 1) { // projectObject
                PercentHistoryObject percentHistoryObject = PercentHistoryObject.find("projectObject.id=?1 AND date<?2 ORDER BY date DESC", id, date).first();
                if (percentHistoryObject != null) return nowPercent - percentHistoryObject.completedPercent;
            }
        }
        return 0;
    }
}
