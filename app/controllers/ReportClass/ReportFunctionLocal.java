package controllers.ReportClass;

import com.google.gson.JsonArray;
import controllers.Consts;
import controllers.Functions;
import controllers.Users;
import models.*;

import java.util.*;

/**
 * Created by enkhamgalan on 6/26/15.
 */
public class ReportFunctionLocal {
    public static ReportMainDate getReportDate(ReportDateBetween dateBetween, Project project, ProjectObject object, Task task, DailyLogMyPlan myPlan) {
        Date today = Functions.convertHourNull(new Date());
        ReportMainDate mainDate = new ReportMainDate();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateBetween.dateS);
        calendar.setFirstDayOfWeek(Consts.firstDayOfWeek);
        Map<Date, Float> mapPercentHistoryProjects = new HashMap<Date, Float>();
        Map<Date, Float> mapPercentHistoryObjects = new HashMap<Date, Float>();
        List<DailyLogScheduledWork> dailyLogScheduledWorks = null;
        Map<Date, DailyLogScheduledWork> mapDailyLogScheduledWorks = new HashMap<Date, DailyLogScheduledWork>();
        float max_completedPercent = 0;
        if (task != null) {
            dailyLogScheduledWorks = DailyLogScheduledWork.find("task.id=?1 AND date>=?2 AND date<?3 order by date", task.id, dateBetween.dateS, dateBetween.dateF).fetch();
            mainDate.beforeScheduledWork = DailyLogScheduledWork.find("task.id=?1 AND date<?2 order by date desc", task.id, dateBetween.dateS).first();
            mainDate.afterScheduledWork = DailyLogScheduledWork.find("task.id=?1 AND date<?2 order by date desc", task.id, dateBetween.dateF).first();
        } else if (myPlan != null) {
            dailyLogScheduledWorks = DailyLogScheduledWork.find("myPlan.id=?1 AND date>=?2 AND date<?3 order by date", myPlan.id, dateBetween.dateS, dateBetween.dateF).fetch();
            mainDate.beforeScheduledWork = DailyLogScheduledWork.find("myPlan.id=?1 AND date<?2 order by date desc", myPlan.id, dateBetween.dateS).first();
            mainDate.afterScheduledWork = DailyLogScheduledWork.find("myPlan.id=?1 AND date<?2 order by date desc", myPlan.id, dateBetween.dateF).first();
        } else if (project != null) {
            List<PercentHistoryProject> historyProjects = PercentHistoryProject.find("project.id=?1 and date>=?2 and date<?3", project.id, dateBetween.dateS, dateBetween.dateF).fetch();
            for (PercentHistoryProject historyProject : historyProjects)
                mapPercentHistoryProjects.put(historyProject.date, historyProject.completedPercent);
        } else if (object != null) {
            List<PercentHistoryObject> historyObjects = PercentHistoryObject.find("projectObject.id=?1 and date>=?2 and date<?3", object.id, dateBetween.dateS, dateBetween.dateF).fetch();
            for (PercentHistoryObject historyObject : historyObjects)
                mapPercentHistoryObjects.put(historyObject.date, historyObject.completedPercent);
        }
        if (mainDate.beforeScheduledWork != null) max_completedPercent = mainDate.beforeScheduledWork.completedPercent;

        if (dailyLogScheduledWorks != null)
            for (DailyLogScheduledWork dsw : dailyLogScheduledWorks) mapDailyLogScheduledWorks.put(dsw.date, dsw);
        boolean thisAdd = true;
        int weekCount = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (Consts.firstDayOfWeek == Calendar.MONDAY) weekCount--;
        for (int w = 0; w <= dateBetween.maxDay; w++) {
            DailyLogScheduledWork scheduledWork = null;
            if (task != null || myPlan != null) {
                scheduledWork = mapDailyLogScheduledWorks.get(calendar.getTime());
                if (scheduledWork != null && scheduledWork.completedPercent > max_completedPercent)
                    max_completedPercent = scheduledWork.completedPercent;
            }
            if (thisAdd && today.getTime() >= calendar.getTime().getTime() &&
                    ReportFunctionLocal.containDate(calendar.getTime(), project, object, task, myPlan)) {
                ReportDate reportDate = new ReportDate();
                reportDate.scheduledWork = scheduledWork;
                reportDate.dateS = calendar.getTime();
                if (Consts.firstDayOfWeek == Calendar.SUNDAY)
                    reportDate.text = Functions.dayNames[weekCount];
                else reportDate.text = Functions.dayName2[weekCount];
                reportDate.isHoliday = Functions.checkIsHoliday(Users.selectedProject(), reportDate.dateS);
                mainDate.reportDates.add(reportDate);
                thisAdd = (max_completedPercent < 100 || containPercent(reportDate.dateS, project, object, task, myPlan, mapPercentHistoryProjects, mapPercentHistoryObjects, scheduledWork));
            }
            if (weekCount == 6) weekCount = 0;
            else weekCount++;
            calendar.add(Calendar.DATE, 1);
        }
        return mainDate;
    }

    public static boolean containDate(Date date, Project project, ProjectObject object, Task task, DailyLogMyPlan myPlan) {
        if (task != null)
            return (task.startDate.getTime() <= date.getTime() && date.getTime() <= task.actualFinish.getTime());
        if (myPlan != null)
            return (myPlan.startDate.getTime() <= date.getTime() && date.getTime() <= myPlan.actualFinish.getTime());
        else if (project != null)
            return (project.startDate.getTime() <= date.getTime() && date.getTime() <= project.actualFinish.getTime());
        else if (object != null)
            return (object.startDate.getTime() <= date.getTime() && date.getTime() <= object.actualFinish.getTime());
        return false;
    }

    public static boolean containPercent(Date date, Project project, ProjectObject object, Task task, DailyLogMyPlan myPlan,
                                         Map<Date, Float> mapPercentHistoryProjects,
                                         Map<Date, Float> mapPercentHistoryObjects, DailyLogScheduledWork scheduledWork) {
        if (task != null)
            return (scheduledWork != null && scheduledWork.completedPercent < 100);
        else if (myPlan != null)
            return (scheduledWork != null && scheduledWork.completedPercent < 100);
        else if (project != null)
            return mapPercentHistoryProjects.get(date) < 100;
        else if (object != null)
            return mapPercentHistoryObjects.get(date) < 100;
        return false;
    }

    public static Float getMaterialAmount(List<DailyLogMaterial> dailyLogMaterials, Date date, Inventory inventory) {
        Float value = 0f;
        for (DailyLogMaterial material : dailyLogMaterials) {
            if (material.scheduledWork.date.compareTo(date) == 0 && material.material.id.compareTo(inventory.id) == 0)
                value += material.amount;
        }
        return value;
    }

    public static ReportChartValue getManPowerAmount(List<DailyLogManpower> dailyLogManpowers, Date date, ManPower manPower) {
        ReportChartValue reportChartValue = new ReportChartValue();
        for (DailyLogManpower manpower : dailyLogManpowers) {
            if (manpower.scheduledWork.date.compareTo(date) == 0 && manpower.mergejil.id.compareTo(manPower.id) == 0) {
                reportChartValue.value += manpower.manHours;
                reportChartValue.count += manpower.workers;
            }
        }
        return reportChartValue;
    }

    public static Float getEquipmentAmount(List<DailyLogEquipment> dailyLogEquipments, Date date, Equipment equipment) {
        Float value = 0f;
        for (DailyLogEquipment eq : dailyLogEquipments) {
            if (eq.scheduledWork.date.compareTo(date) == 0 && eq.equipmentType.id.compareTo(equipment.id) == 0)
                return value += eq.motHours;
        }
        return value;
    }

    public static Float getMaterial(List<TaskInventoryRel> rels, Inventory inventory) {
        for (TaskInventoryRel rel : rels) {
            if (rel.inventory.id.compareTo(inventory.id) == 0) return rel.value;
        }
        return 0F;
    }

    public static Float getMaterials(int type, Long id, Inventory inventory) {
        float tot = 0, totDuration = 0;
        List<TaskInventoryRel> taskInventoryRels = TaskInventoryRel.find("inventory.id=?1 AND " + (type == 0 ? "task.projectObject.project" : "task.projectObject") + ".id=?2", inventory.id, id).fetch();
        for (TaskInventoryRel rel : taskInventoryRels) {
            tot += rel.value;
            totDuration += rel.task.duration;
        }
        return tot / totDuration;
    }

    public static Float getManPower(List<TaskManPowerRel> rels, ManPower manPower) {
        for (TaskManPowerRel rel : rels) {
            if (rel.manPower.id.compareTo(manPower.id) == 0) return rel.amount;
        }
        return 0F;
    }

    public static Float getManPowers(int type, Long id, ManPower manPower) {
        float tot = 0, totDuration = 0;
        ;
        List<TaskManPowerRel> taskManPowerRels = TaskManPowerRel.find("manPower.id=?1 AND " + (type == 0 ? "task.projectObject.project" : "task.projectObject") + ".id=?2", manPower.id, id).fetch();
        for (TaskManPowerRel rel : taskManPowerRels) {
            tot += rel.amount;
            totDuration += rel.task.duration;
        }
        return tot / totDuration;
    }

    public static Float getEquipment(List<TaskEquipmentRel> rels, Equipment equipment) {
        for (TaskEquipmentRel rel : rels) {
            if (rel.equipment.id.compareTo(equipment.id) == 0) return rel.value;
        }
        return 0F;
    }

    public static Float getEquipments(int type, Long id, Equipment equipment) {
        float tot = 0, totDuration = 0;
        ;
        List<TaskEquipmentRel> taskEquipmentRels = TaskEquipmentRel.find("equipment.id=?1 AND " + (type == 0 ? "task.projectObject.project" : "task.projectObject") + ".id=?2", equipment.id, id).fetch();
        for (TaskEquipmentRel rel : taskEquipmentRels) {
            tot += rel.value;
            totDuration += rel.task.duration;
        }
        return tot / totDuration;
    }

    public static ReportDateBetween beforeReportDateBetween(ReportDateBetween dateBetween) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateBetween.dateS);
        ReportDateBetween between = new ReportDateBetween();
        between.dateF = dateBetween.dateS;
        calendar.add(Calendar.WEEK_OF_YEAR, -1);
        between.dateS = calendar.getTime();
        return between;
    }

    public static List<ReportDate> beforeReportDate(Date dateS) {
        List<ReportDate> reportDates = new ArrayList<ReportDate>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateS);
        for (int w = 0; w < 7; w++) {
            ReportDate reportDate = new ReportDate();
            reportDate.dateS = calendar.getTime();
            calendar.add(Calendar.DATE, 1);
            reportDates.add(reportDate);
        }
        return reportDates;
    }

    public static ReportChartValue getBeforeReportMaterialTotal(List<ReportMaterial> reportMaterials, Inventory inventory) {
        ReportChartValue reportChartValue = new ReportChartValue();
        for (ReportMaterial material : reportMaterials) {
            if (material.inventory.id.compareTo(inventory.id) == 0) {
                for (ReportChartValue chartValue : ReportFunction.getChartValues(material.charts)) {
                    reportChartValue.normal += chartValue.normal;
                    reportChartValue.value += chartValue.value;
                }
                return reportChartValue;
            }
        }
        return reportChartValue;
    }

    public static ReportChartValue getBeforeReportManPowerTotal(List<ReportManPower> reportManPowers, ManPower manPower) {
        ReportChartValue reportChartValue = new ReportChartValue();
        for (ReportManPower manPower1 : reportManPowers) {
            if (manPower1.manPower.id.compareTo(manPower.id) == 0) {
                for (ReportChartValue chartValue : ReportFunction.getChartValues(manPower1.charts)) {
                    reportChartValue.count += chartValue.count;
                    reportChartValue.value += chartValue.value;
                }
                return reportChartValue;
            }
        }
        return reportChartValue;
    }

    public static ReportChartValue getBeforeReportEquipmentTotal(List<ReportEquipment> reportEquipments, Equipment equipment) {
        ReportChartValue reportChartValue = new ReportChartValue();
        for (ReportEquipment reportEquipment : reportEquipments) {
            if (reportEquipment.equipment.id.compareTo(equipment.id) == 0) {
                for (ReportChartValue chartValue : ReportFunction.getChartValues(reportEquipment.charts)) {
                    reportChartValue.normal += chartValue.normal;
                    reportChartValue.value += chartValue.value;
                }
                return reportChartValue;
            }
        }
        return reportChartValue;
    }

    public static String getBreakLine(String data) {
        String value = "";
        for (int c = 0; c < data.length(); c++) {
            if ((int) data.charAt(c) == 10) value += "</br>";
            else value += data.charAt(c);
        }
        return value;
    }
}
