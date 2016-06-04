package controllers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import controllers.ReportClass.*;
import models.*;
import play.db.jpa.JPA;
import play.mvc.With;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by enkhamgalan on 3/10/15.
 */
@With(Secure.class)
@Check(Consts.permissionReport)
public class Reports extends CRUD {
    public static void reportMain(String day, int rtype, Long id) {
        User user = Users.getUser();
        int admin = user.getPermissionType(Consts.permissionReport);
        String reportObjType = "User";
        int reportTimeType = 2;
        Long reportID = Users.getUser().id;

        Calendar calendar = Calendar.getInstance();
        Date date = new Date(calendar.get(Calendar.YEAR) - 1900,
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        Project project = Project.findById(Users.pid());
        calendar.setTime(date);
        calendar.setFirstDayOfWeek(Consts.firstDayOfWeek);
        calendar.set(Calendar.DAY_OF_WEEK, Consts.firstDayOfWeek);
        Date nowWeek = calendar.getTime();
        render(date, project, reportObjType, reportTimeType, reportID, admin, nowWeek, day, rtype, id);
    }

    public static void getTree(String id) {
        JsonArray array = new JsonArray();
        if (id.equals("#")) {
            User user = Users.getUser();
            if (user.getPermissionType(Consts.permissionReport) == 3) {
                JsonObject treeJson = new JsonObject();
                treeJson.addProperty("id", "t0");
                treeJson.addProperty("text", "Төсөл");
                treeJson.addProperty("parent", "#");
                treeJson.addProperty("icon", "fa fa-flag");
                treeJson.addProperty("children", true);
                array.add(treeJson);
            }
            JsonObject treeUser = new JsonObject();
            treeUser.addProperty("id", "u0");
            treeUser.addProperty("text", "Ажилтан");
            treeUser.addProperty("parent", "#");
            treeUser.addProperty("icon", "fa fa-male");
            treeUser.addProperty("children", true);
            array.add(treeUser);
        } else if (id.equals("t0")) {
            List<ProjectObject> projectObjects = ProjectObject.find("project.id=?1 order by startDate, name", Users.pid()).fetch();
            for (ProjectObject object : projectObjects) {
                JsonObject treeJson = new JsonObject();
                treeJson.addProperty("id", "o" + object.id.toString());
                treeJson.addProperty("text", object.name);
                treeJson.addProperty("parent", id);
                treeJson.addProperty("icon", "fa fa-sitemap");
                treeJson.addProperty("children", (object.tasks.size() > 0));
                array.add(treeJson);
            }
        } else if (id.equals("u0")) {
            List<OrganizationTeam> userTeams;
            User user = Users.getUser();
            Long selectedProject = Users.pid();
            OrganizationChart organizationChart = user.getOrganizationChart();
            int perId = user.getPermissionType(Consts.permissionReport);
            if (perId == 3) userTeams = OrganizationTeam.find("project.id=?1", selectedProject).fetch();
            else if (perId == 2)
                userTeams = OrganizationTeam.find("id=?1 order by queue, name", organizationChart.team.id).fetch();
            else userTeams = new ArrayList<OrganizationTeam>();
            for (OrganizationTeam team : userTeams) {
                JsonObject treeJson = new JsonObject();
                treeJson.addProperty("id", "b" + team.id.toString());
                treeJson.addProperty("text", team.name);
                treeJson.addProperty("parent", id);
                treeJson.addProperty("icon", "fa fa-group");
                treeJson.addProperty("children", (team.organizationCharts.size() > 0));
                array.add(treeJson);
            }
        } else {
            char ss = id.charAt(0);
            Long od = Long.parseLong(id.substring(1, id.length()));
            switch (ss) {
                case 'b':
                    List<OrganizationChart> organizationCharts = OrganizationChart.find("team.id=?1 order by userPosition.rate,user.firstName", od).fetch();
                    for (OrganizationChart chart : organizationCharts) {
                        JsonObject treeJson = new JsonObject();
                        treeJson.addProperty("id", "u" + chart.user.id.toString());
                        treeJson.addProperty("text", chart.user.toString());
                        treeJson.addProperty("parent", id);
                        treeJson.addProperty("icon", "fa fa-user");
                        treeJson.addProperty("children", false);
                        array.add(treeJson);
                    }
                    break;
                case 'o':
                    List<Task> tasks = Task.find("projectObject.id=?1 and task=null order by startDate, name", od).fetch();
                    for (Task task : tasks) {
                        JsonObject treeJson = new JsonObject();
                        if (!task.hasChild) treeJson.addProperty("id", "t" + task.id.toString());
                        else treeJson.addProperty("id", "z" + task.id.toString());
                        treeJson.addProperty("text", task.name);
                        treeJson.addProperty("parent", id);
                        treeJson.addProperty("icon", "fa fa-puzzle-piece");
                        treeJson.addProperty("children", task.hasChild);
                        array.add(treeJson);
                    }
                    break;
                case 'z':
                    List<Task> task1s = Task.find("task.id=?1 order by startDate, name", od).fetch();
                    for (Task task : task1s) {
                        JsonObject treeJson = new JsonObject();
                        if (!task.hasChild) treeJson.addProperty("id", "t" + task.id.toString());
                        else treeJson.addProperty("id", "z" + task.id.toString());
                        treeJson.addProperty("text", task.name);
                        treeJson.addProperty("parent", id);
                        treeJson.addProperty("icon", "fa fa-puzzle-piece");
                        treeJson.addProperty("children", task.hasChild);
                        array.add(treeJson);
                    }
                    break;
            }
        }
        renderJSON(array);
    }

    public static void loadReportUser2(Long id, int timeType, Date date, Date dateS, int change) {
        ReportDateBetween dateBetween = ReportFunction.getDateBetween(timeType, dateS, change);
        User ownerUser = User.findById(id);
        ReportInfo reportInfo = ReportFunction.getUserRifs_Punchlists(ownerUser.id, 0, dateBetween.dateS, dateBetween.dateF);
        render(ownerUser, dateBetween, date, reportInfo);
    }

    public static void loadReportUser1(Long id, int timeType, Date date, Date dateS, int change) {
        ReportDateBetween dateBetween = ReportFunction.getDateBetween(timeType, dateS, change);
        User ownerUser = User.findById(id);
        List<ReportDate> reportDates = null;
        ReportInfo reportInfo = ReportFunction.getUserRifs_Punchlists(ownerUser.id, 0, dateBetween.dateS, dateBetween.dateF);
        render(timeType, ownerUser, dateBetween, reportDates, date, reportInfo);
    }

    public static void loadReportUser0(Long id, int timeType, Date date, Date dateS, int change) {
        ReportDateBetween dateBetween = ReportFunction.getDateBetween(timeType, dateS, change);
        List<ReportDate> reportDates = null;
        User ownerUser = User.findById(id);
        ReportInfo reportInfo = ReportFunction.getUserRifs_Punchlists(ownerUser.id, 0, dateBetween.dateS, dateBetween.dateF);
        render("/Reports/loadReportUser1.html", timeType, ownerUser, dateBetween, reportDates, date, reportInfo);
    }

    public static void loadReportTask2(Long id, int timeType, Date date, Date dateS, int change) {
        ReportDateBetween dateBetween = ReportFunction.getDateBetween(timeType, dateS, change);
        Task task = Task.findById(id);
        render(task, date, dateBetween);
    }

    public static void loadReportTask1(Long id, int timeType, Date date, Date dateS, int change) {
        ReportDateBetween dateBetween = ReportFunction.getDateBetween(timeType, dateS, change);
        Task task = Task.findById(id);
        ReportMainDate reportMainDate = ReportFunctionLocal.getReportDate(dateBetween, null, null, task, null);

      /*  JsonArray jsArrayDateNames = new JsonArray();
        jsArrayDateNames.add(new JsonPrimitive('x'));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd");

        for (ReportDate reportDate : reportMainDate.reportDates) {
            jsArrayDateNames.add(new JsonPrimitive("(" + simpleDateFormat.format(reportDate.dateS) + ") " + reportDate.text));
        }
        List<ReportMaterial> reportMaterials = new ArrayList<ReportMaterial>();
        if (task.taskInventoryRels.size() > 0) {
            reportMaterials = getReportMaterials(2, null, null, task, task.id, dateBetween, reportMainDate.reportDates, jsArrayDateNames);
        }
        List<ReportManPower> reportManPowers = new ArrayList<ReportManPower>();
        if (task.taskInventoryRels.size() > 0) {
            reportManPowers = getReportManPowers(2, null, null, task, task.id, dateBetween, reportMainDate.reportDates, jsArrayDateNames);
        }
        List<ReportEquipment> reportEquipments = new ArrayList<ReportEquipment>();
        if (task.taskEquipmentRels.size() > 0) {
            reportEquipments = getReportEquipments(2, null, null, task, task.id, dateBetween, reportMainDate.reportDates, jsArrayDateNames);
        }
        render(task, dateBetween, reportMainDate, date, reportMaterials, reportManPowers, reportEquipments, timeType);
        */
        render(task, dateBetween, reportMainDate, date, timeType);
    }

    // type=2=Task, type=1=ProjectObject, type=0=Project,
    public static List<ReportMaterial> getReportMaterials(int type, Project project, ProjectObject projectObject, Task task, Long id,
                                                          ReportDateBetween dateBetween, List<ReportDate> reportDates, JsonArray jsArrayDateNames) {
        List<ReportMaterial> reportMaterials = new ArrayList<ReportMaterial>();
        List<DailyLogMaterial> dailyLogMaterials = ReportFunction.getDailyLogMaterials(type, id, dateBetween.dateS, dateBetween.dateF);
        Long oid = 0L;
        for (DailyLogMaterial result : dailyLogMaterials) {
            if (result.material.id.compareTo(oid) != 0) {
                ReportMaterial myReport = new ReportMaterial();
                myReport.inventory = result.material;
                myReport.owner = result.owner;
                reportMaterials.add(myReport);
                oid = result.material.id;
            }
            reportMaterials.get(reportMaterials.size() - 1).value += result.amount;//too
        }
        float normal = 0;
        for (ReportMaterial myReport : reportMaterials) {
            if (type == 2) {
                normal = ReportFunctionLocal.getMaterial(task.taskInventoryRels, myReport.inventory);
                normal /= task.duration;
            } else if (type == 1) {
                normal = ReportFunctionLocal.getMaterials(type, projectObject.id, myReport.inventory);
            } else if (type == 0) {
                normal = ReportFunctionLocal.getMaterials(type, project.id, myReport.inventory);
            }
            JsonArray chartNorm = new JsonArray();
            JsonArray chartTake = new JsonArray();
            JsonArray chartUse = new JsonArray();
            chartNorm.add(new JsonPrimitive("Хэвийн"));
            chartTake.add(new JsonPrimitive("Авсан"));
            chartUse.add(new JsonPrimitive("Зарцуулсан"));
            for (ReportDate reportDate : reportDates) {
                chartNorm.add(new JsonPrimitive(Functions.getFloatFormat(normal, 2)));
                chartTake.add(new JsonPrimitive(0));
                chartUse.add(new JsonPrimitive(ReportFunctionLocal.getMaterialAmount(dailyLogMaterials, reportDate.dateS, myReport.inventory)));
            }
            myReport.charts.add(jsArrayDateNames);
            myReport.charts.add(chartNorm);
            myReport.charts.add(chartUse);
            myReport.charts.add(chartTake);
        }
        return reportMaterials;
    }

    public static List<ReportManPower> getReportManPowers(int type, Project project, ProjectObject projectObject, Task task, Long id,
                                                          ReportDateBetween dateBetween, List<ReportDate> reportDates, JsonArray jsArrayDateNames) {
        List<ReportManPower> reportManPowers = new ArrayList<ReportManPower>();
        List<DailyLogManpower> dailyLogManpowers = ReportFunction.getDailyLogManpowers(type, id, dateBetween.dateS, dateBetween.dateF);
        Long oid = 0L;
        for (DailyLogManpower result : dailyLogManpowers) {
            if (result.mergejil.id.compareTo(oid) != 0) {
                ReportManPower myReport = new ReportManPower();
                myReport.manPower = result.mergejil;
                myReport.owner = result.owner;
                reportManPowers.add(myReport);
                oid = result.mergejil.id;
            }
//            reportManPowers.get(reportManPowers.size() - 1).value += result.manHours;//too
//            reportManPowers.get(reportManPowers.size() - 1).workers += result.workers;//ажилчидын тоо
        }
        float normal = 0;
        for (ReportManPower myReport : reportManPowers) {
            if (type == 2) {
                normal = ReportFunctionLocal.getManPower(task.taskManPowerRels, myReport.manPower);
                normal /= task.duration;
            } else if (type == 1) {
                normal = ReportFunctionLocal.getManPowers(type, projectObject.id, myReport.manPower);
            } else if (type == 0) {
                normal = ReportFunctionLocal.getManPowers(type, project.id, myReport.manPower);
            }
            JsonArray chartNorm = new JsonArray();
            JsonArray chartUse = new JsonArray();
            JsonArray chartWorkers = new JsonArray();
            chartNorm.add(new JsonPrimitive("Хэвийн"));
            chartUse.add(new JsonPrimitive("Ажилласан"));
            chartWorkers.add(new JsonPrimitive("Ажилчид"));
            for (ReportDate reportDate : reportDates) {
                chartNorm.add(new JsonPrimitive(Functions.getFloatFormat(normal, 2)));
                ReportChartValue reportChartValue = ReportFunctionLocal.getManPowerAmount(dailyLogManpowers, reportDate.dateS, myReport.manPower);
                chartUse.add(new JsonPrimitive(Functions.getFloatFormat(reportChartValue.value, 2)));
                chartWorkers.add(new JsonPrimitive(reportChartValue.count));
            }
            myReport.charts.add(jsArrayDateNames);
            myReport.charts.add(chartNorm);
            myReport.charts.add(chartUse);
            myReport.charts.add(chartWorkers);
        }
        return reportManPowers;
    }

    public static List<ReportEquipment> getReportEquipments(int type, Project project, ProjectObject projectObject, Task task, Long id,
                                                            ReportDateBetween dateBetween, List<ReportDate> reportDates, JsonArray jsArrayDateNames) {
        List<ReportEquipment> reportEquipments = new ArrayList<ReportEquipment>();
        List<DailyLogEquipment> dailyLogEquipments = ReportFunction.getDailyLogEquipments(type, id, dateBetween.dateS, dateBetween.dateF);
        Long oid = 0L;
        for (DailyLogEquipment result : dailyLogEquipments) {
            if (result.equipmentType.id.compareTo(oid) != 0) {
                ReportEquipment myReport = new ReportEquipment();
                myReport.equipment = result.equipmentType;
                myReport.owner = result.owner;
                reportEquipments.add(myReport);
                oid = result.equipmentType.id;
            }
            reportEquipments.get(reportEquipments.size() - 1).value += result.motHours;//too
        }
        float normal = 0;
        for (ReportEquipment myReport : reportEquipments) {
            if (type == 2) {
                normal = ReportFunctionLocal.getEquipment(task.taskEquipmentRels, myReport.equipment);
                normal /= task.duration;
            } else if (type == 1) {
                normal = ReportFunctionLocal.getEquipments(type, projectObject.id, myReport.equipment);
            } else if (type == 0) {
                normal = ReportFunctionLocal.getEquipments(type, project.id, myReport.equipment);
            }
            JsonArray chartNorm = new JsonArray();
            JsonArray chartUse = new JsonArray();
            chartNorm.add(new JsonPrimitive("Хэвийн"));
            chartUse.add(new JsonPrimitive("Ажилласан"));
            for (ReportDate reportDate : reportDates) {
                chartNorm.add(new JsonPrimitive(Functions.getFloatFormat(normal, 2)));
                chartUse.add(new JsonPrimitive(ReportFunctionLocal.getEquipmentAmount(dailyLogEquipments, reportDate.dateS, myReport.equipment)));
            }
            myReport.charts.add(jsArrayDateNames);
            myReport.charts.add(chartNorm);
            myReport.charts.add(chartUse);
        }
        return reportEquipments;
    }

    public static void loadReportTask0(Long id, int timeType, Date date, Date dateS, int change) {
        ReportDateBetween dateBetween = ReportFunction.getDateBetween(timeType, dateS, change);
        Task task = Task.findById(id);
        ReportMainDate reportMainDate = ReportFunctionLocal.getReportDate(dateBetween, null, null, task, null);
        List<ReportMaterial> reportMaterials = new ArrayList<ReportMaterial>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd");
        JsonArray jsArrayDateNames = new JsonArray();
        jsArrayDateNames.add(new JsonPrimitive('x'));
        for (ReportDate reportDate : reportMainDate.reportDates)
            jsArrayDateNames.add(new JsonPrimitive(simpleDateFormat.format(reportDate.dateS)));
        if (task.taskInventoryRels.size() > 0) {
            reportMaterials = getReportMaterials(2, null, null, task, task.id, dateBetween, reportMainDate.reportDates, jsArrayDateNames);
        }
        List<ReportManPower> reportManPowers = new ArrayList<ReportManPower>();
        if (task.taskInventoryRels.size() > 0) {
            reportManPowers = getReportManPowers(2, null, null, task, task.id, dateBetween, reportMainDate.reportDates, jsArrayDateNames);
        }
        List<ReportEquipment> reportEquipments = new ArrayList<ReportEquipment>();
        if (task.taskEquipmentRels.size() > 0) {
            reportEquipments = getReportEquipments(2, null, null, task, task.id, dateBetween, reportMainDate.reportDates, jsArrayDateNames);
        }
        render("/Reports/loadReportTask1.html", task, dateBetween, reportMainDate, date, reportMaterials, reportManPowers, reportEquipments, timeType);
    }

    public static void loadReportProject0(Long id, int timeType, Date date, Date dateS, int change) {
        ReportDateBetween dateBetween = ReportFunction.getDateBetween(timeType, dateS, change);
        Project project = Project.findById(Users.pid());
        List<ReportDate> reportDates = ReportFunctionLocal.getReportDate(dateBetween, project, null, null, null).reportDates;
        JsonArray jsArrayDateNames = new JsonArray();
        jsArrayDateNames.add(new JsonPrimitive('x'));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd");
        ReportValues reportValues = new ReportValues();

        List<DailyLogManpower> manpowers = DailyLogManpower.find("scheduledWork.task.projectObject.project.id=?1 AND scheduledWork.date>=?2 AND scheduledWork.date<?3 ORDER BY scheduledWork.date", project.id, dateBetween.dateS, dateBetween.dateF).fetch();
        List<DailyLogManpower> manpowerDatas = new ArrayList<DailyLogManpower>();
        Date dateMan = new Date();
        for (DailyLogManpower manpower : manpowers) {
            if (dateMan.compareTo(manpower.scheduledWork.date) == 0) {
                manpowerDatas.get(manpowerDatas.size() - 1).c_workers += manpower.workers;
                manpowerDatas.get(manpowerDatas.size() - 1).c_manHours += manpower.manHours;
            } else {
                manpower.c_manHours = manpower.manHours;
                manpower.c_workers = manpower.workers;
                manpowerDatas.add(manpower);
                dateMan = manpower.scheduledWork.date;
            }
        }
        JsonArray manPowerData = new JsonArray();
        JsonObject obj1 = new JsonObject();
        JsonArray value1 = new JsonArray();
        JsonObject obj2 = new JsonObject();
        JsonArray value2 = new JsonArray();
        for (DailyLogManpower manpower : manpowerDatas) {
            JsonArray dateAndValue = new JsonArray();
            dateAndValue.add(new JsonPrimitive(manpower.scheduledWork.date.getTime()));
            dateAndValue.add(new JsonPrimitive(manpower.c_workers));
            value1.add(dateAndValue);
            JsonArray dateAndValue2 = new JsonArray();
            dateAndValue2.add(new JsonPrimitive(manpower.scheduledWork.date.getTime()));
            dateAndValue2.add(new JsonPrimitive(manpower.c_manHours));
            value2.add(dateAndValue2);
            reportValues.totalManpower += manpower.c_workers;
            reportValues.totalManHour += manpower.c_manHours;
        }
        obj1.addProperty("key", "Ажиллах хүчний хөдөлгөөн");
        obj1.addProperty("bar", true);
        obj1.add("values", value1);
        obj2.addProperty("key", "Хүн цаг");
        obj2.add("values", value2);
        manPowerData.add(obj1);
        manPowerData.add(obj2);
        float projMustStep = 100 / (float) project.duration;
        float startPercent;
        int diff = Functions.getDifferenceWorkDays(project, project.startDate, dateBetween.dateS);
        startPercent = (float) diff * projMustStep;

        List<PercentHistoryProject> historyProjects = PercentHistoryProject.find("project.id=?1 AND date>=?2 AND date<?3 ORDER BY date", project.id, dateBetween.dateS, dateBetween.dateF).fetch();
        PercentHistoryProject beforehistoryProject = PercentHistoryProject.find("project.id=?1 AND date<?2 ORDER BY date desc", project.id, dateBetween.dateS).first();
        PercentHistoryProject beforeDayHistoryProject = PercentHistoryProject.find("project.id=?1 AND date=?2 ORDER BY date desc", project.id, Functions.PrevNextDay(dateBetween.dateS, 1)).first();
        PercentHistoryProject afterhistoryProject = PercentHistoryProject.find("project.id=?1 AND date<?2 ORDER BY date desc", project.id, dateBetween.dateF).first();
        Map<Date, PercentHistoryProject> mapPercentHistoryProjects = new HashMap<Date, PercentHistoryProject>();
        for (PercentHistoryProject historyProject : historyProjects)
            mapPercentHistoryProjects.put(historyProject.date, historyProject);

//        if (dateBetween.dateS.getTime() <= date.getTime() && date.getTime() < dateBetween.dateF.getTime()) {
        PercentHistoryProject historyProjectLast = new PercentHistoryProject();
        historyProjectLast.completedPercent = project.completedPercent;
        historyProjectLast.date = date;
        mapPercentHistoryProjects.put(historyProjectLast.date, historyProjectLast);
//        }
        int count = 0;
        for (ReportDate reportDate : reportDates) {
            jsArrayDateNames.add(new JsonPrimitive(simpleDateFormat.format(reportDate.dateS)));
            PercentHistoryProject historyProject = mapPercentHistoryProjects.get(reportDate.dateS);
            reportDate.completedPercent = (historyProject != null ? historyProject.completedPercent : 0);

            JsonArray dateAndValue = new JsonArray();
            dateAndValue.add(new JsonPrimitive(count));
            dateAndValue.add(new JsonPrimitive(reportDate.completedPercent));
            reportValues.projPerDay.add(dateAndValue);
            dateAndValue = new JsonArray();
            dateAndValue.add(new JsonPrimitive(count));
            dateAndValue.add(new JsonPrimitive(simpleDateFormat.format(reportDate.dateS)));
            reportValues.projPerName.add(dateAndValue);
            dateAndValue = new JsonArray();
            startPercent += projMustStep;
            if (startPercent < 0) startPercent = 0;
            else if (startPercent > 100) startPercent = 100;
            dateAndValue.add(new JsonPrimitive(count));
            dateAndValue.add(new JsonPrimitive(startPercent));
            reportValues.projPerMust.add(dateAndValue);
            count++;
        }
        // history graphi

        List<ReportMaterial> reportMaterials = null;
        List<ReportManPower> reportManPowers = null;
        List<ReportEquipment> reportEquipments = null;
        if (controllers.CompanyConf.type == 0) {
            reportMaterials = getReportMaterials(0, project, null, null, project.id, dateBetween, reportDates, jsArrayDateNames);
            reportManPowers = getReportManPowers(0, project, null, null, project.id, dateBetween, reportDates, jsArrayDateNames);
            reportEquipments = getReportEquipments(0, project, null, null, project.id, dateBetween, reportDates, jsArrayDateNames);
        }
        render(date, timeType, dateBetween, project, reportDates, reportValues, manPowerData, reportMaterials, reportManPowers, reportEquipments, beforehistoryProject, beforeDayHistoryProject, afterhistoryProject);
    }

    public static String getReportSummary(int objecttype, Long tid, int timeType, Date date) {
        String value = "";
        ReportSummary summary = ReportSummary.find((objecttype == 2 ? "task" : "myPlan") + ".id=?1 AND timeType=?2 AND date=?3", tid, timeType, date).first();
        if (summary != null) value = summary.executiveSummary;
        return value;
    }

    public static void changeReportSummary(int objecttype, Long tid, int timeType, Date date, String text) {
        ReportSummary summary = ReportSummary.find((objecttype == 2 ? "task" : "myPlan") + ".id=?1 AND timeType=?2 AND date=?3", tid, timeType, date).first();
        boolean save = true;
        if (summary == null) {
            save = false;
            summary = new ReportSummary();
            if (objecttype == 2) summary.task = Task.findById(tid);
            else if (objecttype == 3) summary.myPlan = DailyLogMyPlan.findById(tid);
            summary.timeType = timeType;
            summary.date = date;
        }
        summary.executiveSummary = text;
        if (save) summary._save();
        else summary.create();
    }
}
