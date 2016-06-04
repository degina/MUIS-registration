package controllers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import models.*;
import play.mvc.With;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Enkh-Amgalan on 2/11/15.
 */
@With(Secure.class)
@Check(Consts.permissionDailyLog)
public class DailyLogs extends CRUD {
    public static void blankView(String date, int type) {
        Long pid = Users.pid();
        Date currentDate = new Date();
        boolean isToday;
        if (date != null && !date.equals("")) {
            try {
                currentDate = Consts.simpleDateFormat.parse(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (type == 1) currentDate = Functions.PrevNextDay(currentDate, -1);
            else if (type == 2) currentDate = Functions.PrevNextDay(currentDate, 1);
        }
        currentDate = Functions.convertHourNull(currentDate);
        Date today = new Date();
        today = Functions.convertHourNull(today);
        isToday = (currentDate.compareTo(today) == 0);
        if (Functions.addOrMinusDays(today, 3, false).after(currentDate)) {
            documentView(date, type);
        }
        List<Task> tasks = TaskUsers.getMyStartedTasks(currentDate);
        List<DailyLogMyPlan> myPlans = TaskUsers.getMyUnScheduledTask(currentDate);
        int workNumber = tasks.size() + myPlans.size();
        List<WeatherCondition> weatherConditions = WeatherCondition.findAll();
        boolean isList = true;
//        List<Inventory> inventories = Inventory.findAll();
        List<InventoryCategory> inventoryCategories = InventoryCategory.findAll();
        Long userId = Users.getUser().id;
        List<DailyLogTechnicalDelay> technicalDelays;
        List<DailyLogWeather> weathers;
        List<DailyLogDumpster> dumpsters;
        List<DailyLogInspection> inspections;
        List<DailyLogSafety> safeties = null;
        List<DailyLogNote> dailyLogNotes = null;
        List<DailyLogSanaachlaga> sanaachlagas = null;
        List<DailyLogVisitor> visitors = null;
        List<DailyLogWaste> wastes = null;
        List<DailyLogDelivery> deliveries = null;
        boolean contractorUser = Users.getUser().userTeam.contractor;
        if (contractorUser) {
            technicalDelays = DailyLogTechnicalDelay.find("date=?1 AND owner.id=?2", currentDate, userId).fetch();
            weathers = DailyLogWeather.find("date=?1 AND owner.id=?2", currentDate, userId).fetch();
            dumpsters = DailyLogDumpster.find("date=?1 AND owner.id=?2", currentDate, userId).fetch();
            dailyLogNotes = DailyLogNote.find("date=?1 AND owner.id=?2", currentDate, userId).fetch();
            sanaachlagas = DailyLogSanaachlaga.find("date=?1 AND owner.id=?2", currentDate, userId).fetch();
            wastes = DailyLogWaste.find("date=?1 AND owner.id=?2", currentDate, userId).fetch();
            deliveries = DailyLogDelivery.find("date=?1 AND owner.id=?2", currentDate, userId).fetch();
            safeties = DailyLogSafety.find("date=?1 AND owner.id=?2", currentDate, userId).fetch();
            inspections = DailyLogInspection.find("date=?1 AND owner.id=?2", currentDate, userId).fetch();
            visitors = DailyLogVisitor.find("date=?1 AND owner.id=?2", currentDate, userId).fetch();
        } else {
            String qr = "(";
            for (Task taskCur : tasks)
                qr += "task.id=" + taskCur.id + " OR ";
            for (DailyLogMyPlan myPlanCur : myPlans)
                qr += "myPlan.id=" + myPlanCur.id + " OR ";
            if (qr.length() > 3)
                qr = qr.substring(0, qr.length() - 3) + ") AND ";
            else
                qr = "";
            technicalDelays = DailyLogTechnicalDelay.find(qr + "date=?1 ORDER BY owner.userTeam.contractor desc", currentDate).fetch();
            weathers = DailyLogWeather.find(qr + "date=?1 ORDER BY owner.userTeam.contractor desc", currentDate).fetch();
            dumpsters = DailyLogDumpster.find(qr + "date=?1 ORDER BY owner.userTeam.contractor desc", currentDate).fetch();
            inspections = DailyLogInspection.find(qr + "date=?1", currentDate).fetch();
            safeties = DailyLogSafety.find(qr + "date=?1 ORDER BY owner.userTeam.contractor desc", currentDate).fetch();
            dailyLogNotes = DailyLogNote.find(qr + "date=?1 ORDER BY owner.userTeam.contractor desc", currentDate).fetch();
            sanaachlagas = DailyLogSanaachlaga.find(qr + "date=?1 ORDER BY owner.userTeam.contractor desc", currentDate).fetch();
            visitors = DailyLogVisitor.find(qr + "date=?1", currentDate).fetch();
            wastes = DailyLogWaste.find(qr + "date=?1 ORDER BY owner.userTeam.contractor desc", currentDate).fetch();
            deliveries = DailyLogDelivery.find(qr + "date=?1", currentDate).fetch();
        }
        render(tasks, currentDate, myPlans, weatherConditions, isList, isToday, inventoryCategories, deliveries
                , technicalDelays, weathers, dumpsters, inspections, safeties, contractorUser, dailyLogNotes, sanaachlagas, visitors, wastes
                , workNumber);
    }

    public static void documentView(String date, int type) {
        Date currentDate = new Date();
        if (date != null && !date.equals("")) {
            try {
                currentDate = Consts.simpleDateFormat.parse(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (type == 1) currentDate = Functions.PrevNextDay(currentDate, -1);
            else if (type == 2) currentDate = Functions.PrevNextDay(currentDate, 1);
        }
        currentDate = Functions.convertHourNull(currentDate);
        Date today = new Date();
        today = Functions.convertHourNull(today);
        if (Functions.addOrMinusDays(today, 4, false).before(currentDate)) {
            blankView(date, type);
        }
        List<Task> tasks = TaskUsers.getMyStartedTasks(currentDate);
        List<DailyLogMyPlan> myPlans = TaskUsers.getMyUnScheduledTask(currentDate);
        render(tasks, myPlans, currentDate);
    }

    public static void createWeather(String taskId, Date date, Long conditionId, String startHour,
                                     String finishHour, String notes, String[] filename, String[] filedir, String[] extension, Float[] filesize) {
        Date startDate = Functions.convertHourNull(date);
        Date finishDate = startDate;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.set(Calendar.HOUR, Integer.parseInt(startHour.substring(0, 2)));
        calendar.set(Calendar.MINUTE, Integer.parseInt(startHour.substring(3, 5)));
        startDate = calendar.getTime();
        calendar.setTime(finishDate);
        calendar.set(Calendar.HOUR, Integer.parseInt(finishHour.substring(0, 2)));
        calendar.set(Calendar.MINUTE, Integer.parseInt(finishHour.substring(3, 5)));
        finishDate = calendar.getTime();

        WeatherCondition condition = WeatherCondition.findById(conditionId);

        DailyLogWeather weather;
        if (taskId.charAt(0) == 't') {
            Task task = Task.findById(Long.parseLong(taskId.substring(1, taskId.length())));
            weather = new DailyLogWeather(condition, date, startDate, finishDate, notes, task, Users.getUser());
        } else {
            DailyLogMyPlan myPlan = DailyLogMyPlan.findById(Long.parseLong(taskId.substring(1, taskId.length())));
            weather = new DailyLogWeather(condition, date, startDate, finishDate, notes, myPlan, Users.getUser());
        }
        weather.attaches = new ArrayList<DailyLogWeatherAttach>();
        if (filename != null) {
            for (int i = 0; i < filename.length; i++) {
                DailyLogWeatherAttach attach = new DailyLogWeatherAttach(filename[i], filedir[i], extension[i], filesize[i], weather);
                FunctionDatabase.createImg(weather.owner, weather.task, filename[i], filedir[i], extension[i]);
                weather.attaches.add(attach);
            }
        }
        weather.create();

        boolean isList = false;
        render(weather, isList);
    }

    public static void deleteWeather(Long id) {
        DailyLogWeather weather = DailyLogWeather.findById(id);
        for (DailyLogWeatherAttach attach : weather.attaches) {
            if (DailyLogWeatherAttach.count("weather.id!=?1 AND dir=?2 AND extension=?3", weather.id, attach.dir, attach.extension) == 0)
                Functions.deleteUploadFile(attach.dir, attach.extension);
            FunctionDatabase.checkImg(attach.dir);
        }
        weather.delete();
    }

    public static void createTechnical(String taskId, Date date, String condition, String startHour,
                                       String finishHour, String notes, String[] filename, String[] filedir, String[] extension, Float[] filesize) {
        Date startDate = Functions.convertHourNull(date);
        Date finishDate = startDate;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.set(Calendar.HOUR, Integer.parseInt(startHour.substring(0, 2)));
        calendar.set(Calendar.MINUTE, Integer.parseInt(startHour.substring(3, 5)));
        startDate = calendar.getTime();
        calendar.setTime(finishDate);
        calendar.set(Calendar.HOUR, Integer.parseInt(finishHour.substring(0, 2)));
        calendar.set(Calendar.MINUTE, Integer.parseInt(finishHour.substring(3, 5)));
        finishDate = calendar.getTime();

        DailyLogTechnicalDelay technical;
        if (taskId.charAt(0) == 't') {
            Task task = Task.findById(Long.parseLong(taskId.substring(1, taskId.length())));
            technical = new DailyLogTechnicalDelay(condition, startDate, finishDate, date, notes, task, Users.getUser());
        } else {
            DailyLogMyPlan myPlan = DailyLogMyPlan.findById(Long.parseLong(taskId.substring(1, taskId.length())));
            technical = new DailyLogTechnicalDelay(condition, startDate, finishDate, date, notes, myPlan, Users.getUser());
        }

        technical.attaches = new ArrayList<DailyLogTechnicalDelayAttach>();
        if (filename != null) {
            for (int i = 0; i < filename.length; i++) {
                DailyLogTechnicalDelayAttach attach = new DailyLogTechnicalDelayAttach(filename[i], filedir[i], extension[i], filesize[i], technical);
                FunctionDatabase.createImg(technical.owner, technical.task, filename[i], filedir[i], extension[i]);
                technical.attaches.add(attach);
            }
        }
        technical.create();

        boolean isList = false;
        render(technical, isList);
    }

    public static void deleteTechnical(Long id) {
        DailyLogTechnicalDelay technical = DailyLogTechnicalDelay.findById(id);
        for (DailyLogTechnicalDelayAttach attach : technical.attaches) {
            if (DailyLogTechnicalDelayAttach.count("technical.id!=?1 AND dir=?2 AND extension=?3", technical.id, attach.dir, attach.extension) == 0) {
                Functions.deleteUploadFile(attach.dir, attach.extension);
                FunctionDatabase.checkImg(attach.dir);
            }
        }
        technical.delete();
    }

    public static void showManPower(String taskId, Date date) {
        List<DailyLogManpower> manpowers = new ArrayList<DailyLogManpower>();
        List<ManPower> projectManPowers = new ArrayList<ManPower>();
        DailyLogScheduledWork dailyLogScheduledWork = getScheduledWork(taskId, date);
        List<OrganizationTeam> orgTeams = new ArrayList<OrganizationTeam>();
        if (dailyLogScheduledWork.task != null) {
            orgTeams = OrganizationTeam.find("project.id=?1 ORDER BY name", dailyLogScheduledWork.task.projectObject.project.id).fetch();
            projectManPowers = ManPower.find("project.id=?1 ORDER BY name", dailyLogScheduledWork.task.projectObject.project.id).fetch();
        } else {
            orgTeams = OrganizationTeam.find("ORDER BY project.name,name").fetch();
            projectManPowers = ManPower.find("ORDER BY project.name,name").fetch();
        }

        if (dailyLogScheduledWork != null) {
            if (Users.getUser().userTeam.contractor)
                manpowers = DailyLogManpower.find("scheduledWork.id=?1 AND owner.id=?2 ORDER BY owner.userTeam.contractor desc,owner.userPosition.rate", dailyLogScheduledWork.id, Users.getUser().id).fetch();
            else
                manpowers = DailyLogManpower.find("scheduledWork.id=?1 ORDER BY owner.userTeam.contractor desc,owner.userPosition.rate", dailyLogScheduledWork.id).fetch();
        }
        boolean isList = true;
        render("DailyLogs/createManPower.html", manpowers, isList, projectManPowers, orgTeams);
    }

    public static DailyLogScheduledWork getScheduledWork(String taskId, Date date) {
        DailyLogScheduledWork scheduledWork;
        if (taskId.charAt(0) == 't') {
            Task task = Task.findById(Long.parseLong(taskId.substring(1, taskId.length())));
            scheduledWork = task.getScheduledWork(date);
            if (scheduledWork == null) {
                scheduledWork = new DailyLogScheduledWork("task", task.completedPercent, date, task);
                scheduledWork.create();
            }
        } else {
            DailyLogMyPlan myPlan = DailyLogMyPlan.findById(Long.parseLong(taskId.substring(1, taskId.length())));
            scheduledWork = myPlan.getScheduledWork(date);
            if (scheduledWork == null) {
                scheduledWork = new DailyLogScheduledWork("myPlan", myPlan.completedPercent, date, myPlan);
                scheduledWork.create();
            }
        }
        return scheduledWork;
    }


    public static void createManPower(String taskId, Date date, Long userTeamId, Long mergejilId, String workers, String hours, String manHours,
                                      String location, String notes, String[] filename, String[] filedir, String[] extension, Float[] filesize) {

        DailyLogScheduledWork scheduledWork = getScheduledWork(taskId, date);
        ManPower mergejil = ManPower.findById(mergejilId);
        OrganizationTeam orgTeam = OrganizationTeam.findById(userTeamId);
        DailyLogManpower manpower = new DailyLogManpower(orgTeam, mergejil, Long.parseLong(workers), Float.parseFloat(hours), Float.parseFloat(manHours),
                location, notes, scheduledWork, Users.getUser());
        manpower.attaches = new ArrayList<DailyLogManpowerAttach>();
        if (filename != null) {
            for (int i = 0; i < filename.length; i++) {
                DailyLogManpowerAttach attach = new DailyLogManpowerAttach(filename[i], filedir[i], extension[i], filesize[i], manpower);
                FunctionDatabase.createImg(manpower.owner, manpower.scheduledWork.task, filename[i], filedir[i], extension[i]);
                attach.manpower = manpower;
                manpower.attaches.add(attach);
            }
        }
        manpower.create();
        boolean contractor = Users.getUser().userTeam.contractor;
        long totalWorkers = 0l;
        String totalManHours;
        if (!contractor) {
            if (scheduledWork.totalManHours != null) scheduledWork.totalManHours += Float.parseFloat(manHours);
            else scheduledWork.totalManHours = Float.parseFloat(manHours);
            if (scheduledWork.totalWorkers != null) scheduledWork.totalWorkers += Long.parseLong(workers);
            else scheduledWork.totalWorkers = Long.parseLong(workers);
            scheduledWork._save();
            totalWorkers = scheduledWork.totalWorkers;
            totalManHours = Functions.getFloatFormat(scheduledWork.totalManHours, 1);
        } else {
            List<DailyLogManpower> manpowers = DailyLogManpower.find("owner.id=?1 AND scheduledWork.id=?2", Users.getUser().id, scheduledWork.id).fetch();
            Float manhoursT = 0f;
            for (DailyLogManpower manpower1 : manpowers) {
                totalWorkers += manpower1.workers;
                manhoursT += manpower1.manHours;
            }
            totalManHours = Functions.getFloatFormat(manhoursT, 1);
        }
        boolean isList = false;

        render(manpower, isList, totalWorkers, totalManHours, taskId);
    }

    public static void deleteManPower(Long id) {
        DailyLogManpower manpower = DailyLogManpower.findById(id);
        DailyLogScheduledWork scheduledWork = manpower.scheduledWork;
        for (DailyLogManpowerAttach attach : manpower.attaches) {
            if (DailyLogManpowerAttach.count("manpower.id!=?1 AND dir=?2 AND extension=?3", manpower.id, attach.dir, attach.extension) == 0) {
                Functions.deleteUploadFile(attach.dir, attach.extension);
                FunctionDatabase.checkImg(attach.dir);
            }
        }
        if (!manpower.owner.userTeam.contractor) {
            scheduledWork.totalWorkers -= manpower.workers;
            scheduledWork.totalManHours -= manpower.manHours;
            scheduledWork._save();
        }
        manpower._delete();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("totalWorkers", scheduledWork.totalWorkers);
        jsonObject.addProperty("totalManHours", Functions.getFloatFormat(scheduledWork.totalManHours, 1));
        renderJSON(jsonObject);
    }

    public static void createMaterial(String taskId, Date date, Long inventoryId) {
        Inventory inventory = Inventory.findById(inventoryId);
        boolean isList = false;
        boolean contractorUser = Users.getUser().userTeam.contractor;
        List<DailyLogMaterial> materials = null;
        DailyLogScheduledWork dailyLogScheduledWork = getScheduledWork(taskId, date);
        if (!contractorUser)
            materials = DailyLogMaterial.find("material.id=?1 AND scheduledWork.id=?2 AND owner.userTeam.contractor=1", inventoryId, dailyLogScheduledWork.id).fetch();
        render(inventory, isList, contractorUser, materials);
    }

    public static void showMaterial(String taskId, Date date) {
        List<DailyLogMaterial> materials = new ArrayList<DailyLogMaterial>();
        List<Inventory> hasInventories = null;
        boolean contractorUser = Users.getUser().userTeam.contractor;
        boolean isList = true;
        DailyLogScheduledWork dailyLogScheduledWork = getScheduledWork(taskId, date);
        if (dailyLogScheduledWork != null) {
            if (contractorUser)
                materials = DailyLogMaterial.find("scheduledWork.id=?1 AND owner.id=?2 ORDER BY material.id", dailyLogScheduledWork.id, Users.getUser().id).fetch();
            else
                materials = DailyLogMaterial.find("scheduledWork.id=?1 ORDER BY material.id,owner.userTeam.contractor", dailyLogScheduledWork.id).fetch();
            if (taskId.charAt(0) == 't') {
                hasInventories = Inventory.find("SELECT DISTINCT t FROM tb_inventory t LEFT JOIN t.taskInventoryRels AS r WHERE r.inventory = t AND r.task.id=?1", Long.parseLong(taskId.substring(1, taskId.length()))).fetch();
            }
        }
        render("DailyLogs/createMaterial.html", isList, materials, hasInventories, contractorUser);
    }

    public static void saveMaterial(String taskId, Date date, String materials) {
        DailyLogScheduledWork scheduledWork = getScheduledWork(taskId, date);
        JsonParser parser = new JsonParser();
        JsonElement eventElement = parser.parse(materials);
        JsonArray materialArray = eventElement.getAsJsonArray();
        for (JsonElement material : materialArray) {
            JsonObject materialObj = material.getAsJsonObject();
            if (!materialObj.get("quantity").getAsString().equals("")) {
                DailyLogMaterial logMaterial;
                Inventory inventory = Inventory.findById(materialObj.get("inventoryId").getAsLong());
                Long materialId = materialObj.get("materialId").getAsLong();
                if (materialId == 0) {
                    logMaterial = new DailyLogMaterial(inventory, materialObj.get("quantity").getAsFloat(),
                            materialObj.get("note").getAsString(), Users.getUser(), scheduledWork);
                    logMaterial.create();
                } else {
                    logMaterial = DailyLogMaterial.findById(materialId);
                    logMaterial.amount = materialObj.get("quantity").getAsFloat();
                    logMaterial.note = materialObj.get("note").getAsString();
                    logMaterial.lastEdited = (new Date()).getTime();
                    List<DailyLogMaterialAttach> attaches = DailyLogMaterialAttach.find("material.id=?1", logMaterial.id).fetch();
                    if (attaches != null)
                        for (DailyLogMaterialAttach attach : attaches) {
                            if (DailyLogMaterialAttach.count("material.id!=?1 AND dir=?2 AND extension=?3", logMaterial.id, attach.dir, attach.extension) == 0) {
                                Functions.deleteUploadFile(attach.dir, attach.extension);
                                FunctionDatabase.checkImg(attach.dir);
                            }
                            attach._delete();
                        }
                }
                logMaterial.attaches = new ArrayList<DailyLogMaterialAttach>();
                JsonArray attachs = materialObj.getAsJsonArray("attaches");
                for (JsonElement attach : attachs) {
                    JsonObject attachObj = attach.getAsJsonObject();
                    DailyLogMaterialAttach materialAttach = new DailyLogMaterialAttach(attachObj.get("filename").getAsString(),
                            attachObj.get("filedir").getAsString(), attachObj.get("extension").getAsString(), attachObj.get("filesize").getAsFloat(), logMaterial);
                    FunctionDatabase.createImg(logMaterial.owner, logMaterial.scheduledWork.task, attachObj.get("filename").getAsString(), attachObj.get("filedir").getAsString(), attachObj.get("extension").getAsString());
                    materialAttach.create();
                }
                logMaterial._save();
            }
        }
        renderText("Амжилттай хадгалагдлаа");
    }

    public static void deleteMaterial(Long id) {
        if (id != 0) {
            DailyLogMaterial material = DailyLogMaterial.findById(id);
            for (DailyLogMaterialAttach attach : material.attaches) {
                if (DailyLogMaterialAttach.count("material.id!=?1 AND dir=?2 AND extension=?3", material.id, attach.dir, attach.extension) == 0) {
                    Functions.deleteUploadFile(attach.dir, attach.extension);
                    FunctionDatabase.checkImg(attach.dir);
                }
            }
            material._delete();
        }
    }

    public static void showEquipment(String taskId, Date date) {
        List<DailyLogEquipment> equipments = null;
        List<Equipment> projectEquipments = new ArrayList<Equipment>();
        DailyLogScheduledWork dailyLogScheduledWork = getScheduledWork(taskId, date);
        if (dailyLogScheduledWork != null) {
            if (Users.getUser().userTeam.contractor)
                equipments = DailyLogEquipment.find("scheduledWork.id=?1 AND owner.id=?2 ORDER BY owner.userTeam.contractor desc,owner.userPosition.rate", dailyLogScheduledWork.id, Users.getUser().id).fetch();
            else
                equipments = DailyLogEquipment.find("scheduledWork.id=?1 ORDER BY owner.userTeam.contractor desc", dailyLogScheduledWork.id).fetch();

            if (dailyLogScheduledWork.task != null)
                projectEquipments = Equipment.find("project.id=?1", dailyLogScheduledWork.task.projectObject.project.id).fetch();
            else
                projectEquipments = Equipment.find("ORDER BY project.name,name").fetch();
        }
        boolean isList = true;
        String options = "";
        for (Equipment equipment : projectEquipments)
            options += "<option value=" + equipment.id + ">" + equipment.name + "</option> ";

        render("DailyLogs/createEquipment.html", equipments, isList, options);
    }

    public static void createEquipment(String taskId, Date date, Long type, String operator, String location, String startHour,
                                       String finishHour, String comments, String[] filename, String[] filedir, String[] extension, Float[] filesize) {

        DailyLogScheduledWork scheduledWork = getScheduledWork(taskId, date);
        Equipment equipmentType = Equipment.findById(type);
        Date startDate = Functions.convertHourNull(date);
        Date finishDate = startDate;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.set(Calendar.HOUR, Integer.parseInt(startHour.substring(0, 2)));
        calendar.set(Calendar.MINUTE, Integer.parseInt(startHour.substring(3, 5)));
        startDate = calendar.getTime();
        calendar.setTime(finishDate);
        calendar.set(Calendar.HOUR, Integer.parseInt(finishHour.substring(0, 2)));
        calendar.set(Calendar.MINUTE, Integer.parseInt(finishHour.substring(3, 5)));
        finishDate = calendar.getTime();
        DailyLogEquipment equipment = new DailyLogEquipment(equipmentType, operator, location, startDate, finishDate,
                comments, scheduledWork, Users.getUser());
        equipment.attaches = new ArrayList<DailyLogEquipmentAttach>();
        if (filename != null) {
            for (int i = 0; i < filename.length; i++) {
                DailyLogEquipmentAttach attach = new DailyLogEquipmentAttach(filename[i], filedir[i], extension[i], filesize[i], equipment);
                FunctionDatabase.createImg(equipment.owner, equipment.scheduledWork.task, filename[i], filedir[i], extension[i]);
                attach.equipment = equipment;
                equipment.attaches.add(attach);
            }
        }
        equipment.create();
        if (!equipment.owner.userTeam.contractor) {
            scheduledWork.totalMotHours += equipment.motHours;
            scheduledWork._save();
        }
        boolean isList = false;
        int size = scheduledWork.equipments.size();
        String totalMotHour = Functions.getFloatFormat(scheduledWork.totalMotHours, 1);
        render(equipment, isList, taskId, size, totalMotHour);
    }

    public static void deleteEquipment(Long id) {
        DailyLogEquipment equipment = DailyLogEquipment.findById(id);
        DailyLogScheduledWork scheduledWork = equipment.scheduledWork;
        for (DailyLogEquipmentAttach attach : equipment.attaches) {
            if (DailyLogEquipmentAttach.count("equipment.id!=?1 AND dir=?2 AND extension=?3", equipment.id, attach.dir, attach.extension) == 0) {
                Functions.deleteUploadFile(attach.dir, attach.extension);
                FunctionDatabase.checkImg(attach.dir);
            }
        }
        if (!equipment.owner.userTeam.contractor) {
            scheduledWork.totalMotHours -= equipment.motHours;
            scheduledWork._save();
        }
        equipment._delete();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("size", scheduledWork.equipments.size());
        jsonObject.addProperty("motHours", Functions.getFloatFormat(scheduledWork.totalMotHours, 1));
        renderJSON(jsonObject);
    }

    public static void deleteScheduledWork(Long id) {
        DailyLogWorkNote workNote = DailyLogWorkNote.findById(id);
        if (workNote != null) {
            for (DailyLogWorkNoteAttach attach : workNote.attaches) {
                if (DailyLogWorkNoteAttach.count("workNote.id!=?1 AND dir=?2 AND extension=?3", workNote.id, attach.dir, attach.extension) == 0) {
                    Functions.deleteUploadFile(attach.dir, attach.extension);
                    FunctionDatabase.checkImg(attach.dir);
                }
            }
            workNote._delete();
        }
    }

    public static void updateScheduledWork(String taskId, Date date, String hours, String percent, String comments, String[] filename, String[] filedir, String[] extension, Float[] filesize) {

        DailyLogScheduledWork scheduledWork = getScheduledWork(taskId, date);
        DailyLogWorkNote workNote = scheduledWork.getMyWorkNote();
        if (workNote == null) {
            workNote = new DailyLogWorkNote(Users.getUser(), scheduledWork);
            workNote.create();
        }
        workNote.notes = comments;
        if (hours.length() > 0) workNote.hours = Float.parseFloat(hours);
        if (!workNote.owner.userTeam.contractor) {
            Float percentfloat = Float.valueOf(percent);
            if (percentfloat >= 100f) percentfloat = 100f;
            // хувийг хадгалах болон ахицийг бодох
            if (scheduledWork.completedPercent.compareTo(percentfloat) != 0) {
                DailyLogScheduledWork lastScheduledWork = null;
                if (scheduledWork.task != null)
                    lastScheduledWork = DailyLogScheduledWork.find("task.id=?1 AND id!=?2 ORDER BY date desc", scheduledWork.task.id, scheduledWork.id).first();
                else if (scheduledWork.myPlan != null)
                    lastScheduledWork = DailyLogScheduledWork.find("myPlan.id=?1 AND id!=?2 ORDER BY date desc", scheduledWork.myPlan.id, scheduledWork.id).first();
                scheduledWork.progressPercent = percentfloat - (lastScheduledWork == null ? 0f : lastScheduledWork.completedPercent);
                scheduledWork.completedPercent = percentfloat;
            }
            //бодит дуусах өдрийг бодох
            Date actualFinish = Functions.convertDayLastSecond(scheduledWork.date);
            Task task = scheduledWork.task;
            if (task != null) {
                if (percentfloat == 100f) {
                    // хугцаанаасаа өмнө дууссан гэсэн үг
                    task.actualFinish = actualFinish;
                    if (true) {
                        boolean isNew = false;
                        Post post = Post.find("type='Ажлаа дуусгалаа' AND typeModelId=?1", workNote.id).first();
                        if (post == null) {
                            isNew = true;
                            post = new Post();
                        }
                        post.owner = Users.getUser();
                        post.seeAll = true;

                        post.content = "<strong>" + task.projectObject.project.name + " > " + task.projectObject.name + " > "
                                + task.name + "</strong> ажлыг хийж дуусгалаа баяр хүргэе! </br>" + workNote.notes;
                        post.type = "Ажлаа дуусгалаа";
                        post.typeIconName = "complete.jpg";
                        post.typeModelId = workNote.id;
                        if (isNew) {
                            post.create();
                        } else {
                            post.activeDate = new Date();
                            post.save();
                            List<PostAttach> postAttaches = PostAttach.find("post.id=?1", post.id).fetch();
                            for (PostAttach postAttach : postAttaches)
                                postAttach._delete();
                        }
                        if (filename != null) {
                            for (int i = 0; i < filename.length; i++) {
                                PostAttach attach = new PostAttach(filename[i], filedir[i], extension[i], filesize[i], post);
                                attach.create();
                            }
                        }
                    }
                } else if (task.actualFinish.before(scheduledWork.date) && percentfloat < 100L) {
                    task.actualFinish = actualFinish;
                }
                task.completedPercent = percentfloat;
                task._save();
                task.setValueCompletedPercent(percentfloat);
            } else if (scheduledWork.myPlan != null) {
                DailyLogMyPlan myPlan = scheduledWork.myPlan;
                myPlan.completedPercent = percentfloat;
                if (percentfloat == 100L) {
                    long diff = myPlan.actualFinish.getTime() - myPlan.startDate.getTime();
                    diff = diff / (24 * 60 * 60 * 1000);
                    if (diff >= 1)
                        myPlan.actualFinish = actualFinish;
                } else if (myPlan.actualFinish.before(scheduledWork.date) && percentfloat < 100L) {
                    myPlan.actualFinish = actualFinish;
                }
                myPlan._save();
            }
        }
        if (workNote.attaches != null) {
            for (DailyLogWorkNoteAttach attach : workNote.attaches) {
                FunctionDatabase.checkImg(attach.dir);
                attach._delete();
            }
        }
        workNote.attaches = new ArrayList<DailyLogWorkNoteAttach>();
        if (filename != null) {
            for (int i = 0; i < filename.length; i++) {
                DailyLogWorkNoteAttach attach = new DailyLogWorkNoteAttach(filename[i], filedir[i], extension[i], filesize[i], workNote);
                FunctionDatabase.createImg(workNote.owner, workNote.scheduledWork.task, filename[i], filedir[i], extension[i]);
                attach.create();
            }
        }
        workNote.lastEdited = (new Date()).getTime();
        workNote._save();
        scheduledWork._save();
        renderText(workNote.id);
    }

    public static void createInspection(String taskId, Date date, String startHour, String finishHour, String type, String name,
                                        String area, String comments, String[] filename, String[] filedir, String[] extension, Float[] filesize) {
        Date startDate = Functions.convertHourNull(date);
        Date finishDate = startDate;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.set(Calendar.HOUR, Integer.parseInt(startHour.substring(0, 2)));
        calendar.set(Calendar.MINUTE, Integer.parseInt(startHour.substring(3, 5)));
        startDate = calendar.getTime();
        calendar.setTime(finishDate);
        calendar.set(Calendar.HOUR, Integer.parseInt(finishHour.substring(0, 2)));
        calendar.set(Calendar.MINUTE, Integer.parseInt(finishHour.substring(3, 5)));
        finishDate = calendar.getTime();
        DailyLogInspection inspection;
        if (taskId.charAt(0) == 't') {
            Task task = Task.findById(Long.parseLong(taskId.substring(1, taskId.length())));
            inspection = new DailyLogInspection(startDate, finishDate, type, name, area, comments, date, Users.getUser(), task);
        } else {
            DailyLogMyPlan myPlan = DailyLogMyPlan.findById(Long.parseLong(taskId.substring(1, taskId.length())));
            inspection = new DailyLogInspection(startDate, finishDate, type, name, area, comments, date, Users.getUser(), myPlan);
        }
        inspection.attaches = new ArrayList<DailyLogInspectionAttach>();
        if (filename != null) {
            for (int i = 0; i < filename.length; i++) {
                DailyLogInspectionAttach attach = new DailyLogInspectionAttach(filename[i], filedir[i], extension[i], filesize[i], inspection);
                FunctionDatabase.createImg(inspection.owner, inspection.task, filename[i], filedir[i], extension[i]);
                inspection.attaches.add(attach);
            }
        }
        inspection.create();
        boolean isList = false;
        render(inspection, isList);
    }

    public static void deleteInspection(Long id) {
        DailyLogInspection inspection = DailyLogInspection.findById(id);
        for (DailyLogInspectionAttach attach : inspection.attaches) {
            FunctionDatabase.checkImg(attach.dir);
            Functions.deleteUploadFile(attach.dir, attach.extension);
        }
        inspection._delete();
    }

    public static void createSafety(String taskId, Date date, String hour, String notice,
                                    String issuedTo, String complianceDue, String comments,
                                    String[] filename, String[] filedir, String[] extension, Float[] filesize) {
        Date startDate = Functions.convertHourNull(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.set(Calendar.HOUR, Integer.parseInt(hour.substring(0, 2)));
        calendar.set(Calendar.MINUTE, Integer.parseInt(hour.substring(3, 5)));
        startDate = calendar.getTime();
        DailyLogSafety safety;
        if (taskId.charAt(0) == 't') {
            Task task = Task.findById(Long.parseLong(taskId.substring(1, taskId.length())));
            safety = new DailyLogSafety(startDate, notice, issuedTo, complianceDue, comments, date, Users.getUser(), task);
        } else {
            DailyLogMyPlan myPlan = DailyLogMyPlan.findById(Long.parseLong(taskId.substring(1, taskId.length())));
            safety = new DailyLogSafety(startDate, notice, issuedTo, complianceDue, comments, date, Users.getUser(), myPlan);
        }
        safety.attaches = new ArrayList<DailyLogSafetyAttach>();
        if (filename != null) {
            for (int i = 0; i < filename.length; i++) {
                DailyLogSafetyAttach attach = new DailyLogSafetyAttach(filename[i], filedir[i], extension[i], filesize[i], safety);
                FunctionDatabase.createImg(safety.owner, safety.task, filename[i], filedir[i], extension[i]);
                safety.attaches.add(attach);
            }
        }
        safety.create();
        boolean isList = false;
        render(safety, isList);
    }

    public static void deleteSafety(Long id) {
        DailyLogSafety safety = DailyLogSafety.findById(id);
        for (DailyLogSafetyAttach attach : safety.attaches) {
            Functions.deleteUploadFile(attach.dir, attach.extension);
            FunctionDatabase.checkImg(attach.dir);
        }
        safety._delete();
    }

    public static void createDelivery(String taskId, Date date, String hour, String from, String trackingNumber, String contents,
                                      String comments, String[] filename, String[] filedir, String[] extension, Float[] filesize) {

        Date startDate = Functions.convertHourNull(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.set(Calendar.HOUR, Integer.parseInt(hour.substring(0, 2)));
        calendar.set(Calendar.MINUTE, Integer.parseInt(hour.substring(3, 5)));
        startDate = calendar.getTime();
        DailyLogDelivery delivery;
        if (taskId.charAt(0) == 't') {
            Task task = Task.findById(Long.parseLong(taskId.substring(1, taskId.length())));
            delivery = new DailyLogDelivery(startDate, from, trackingNumber, contents, comments, date, Users.getUser(), task);
        } else {
            DailyLogMyPlan myPlan = DailyLogMyPlan.findById(Long.parseLong(taskId.substring(1, taskId.length())));
            delivery = new DailyLogDelivery(startDate, from, trackingNumber, contents, comments, date, Users.getUser(), myPlan);
        }

        delivery.attaches = new ArrayList<DailyLogDeliveryAttach>();
        if (filename != null) {
            for (int i = 0; i < filename.length; i++) {
                DailyLogDeliveryAttach attach = new DailyLogDeliveryAttach(filename[i], filedir[i], extension[i], filesize[i], delivery);
                FunctionDatabase.createImg(delivery.owner, delivery.task, filename[i], filedir[i], extension[i]);
                delivery.attaches.add(attach);
            }
        }
        delivery.create();
        boolean isList = false;
        render(delivery, isList);
    }

    public static void deleteDelivery(Long id) {
        DailyLogDelivery delivery = DailyLogDelivery.findById(id);
        for (DailyLogDeliveryAttach attach : delivery.attaches) {
            Functions.deleteUploadFile(attach.dir, attach.extension);
            FunctionDatabase.checkImg(attach.dir);
        }
        delivery.delete();
    }

    public static void createNotes(String taskId, Date date, Long issue, String comments, String[] filename, String[] filedir, String[] extension, Float[] filesize) {

        DailyLogNote dailyLogNote;
        if (taskId.charAt(0) == 't') {
            Task task = Task.findById(Long.parseLong(taskId.substring(1, taskId.length())));
            dailyLogNote = new DailyLogNote(issue, comments, date, Users.getUser(), task);
        } else {
            DailyLogMyPlan myPlan = DailyLogMyPlan.findById(Long.parseLong(taskId.substring(1, taskId.length())));
            dailyLogNote = new DailyLogNote(issue, comments, date, Users.getUser(), myPlan);
        }
        dailyLogNote.attaches = new ArrayList<DailyLogNoteAttach>();
        if (filename != null) {
            for (int i = 0; i < filename.length; i++) {
                DailyLogNoteAttach attach = new DailyLogNoteAttach(filename[i], filedir[i], extension[i], filesize[i], dailyLogNote);
                FunctionDatabase.createImg(dailyLogNote.owner, dailyLogNote.task, filename[i], filedir[i], extension[i]);
                dailyLogNote.attaches.add(attach);
            }
        }
        dailyLogNote.create();
        boolean isList = false;
        render(dailyLogNote, isList);
    }

    public static void deleteNotes(Long id) {
        DailyLogNote note = DailyLogNote.findById(id);
        for (DailyLogNoteAttach attach : note.attaches) {
            Functions.deleteUploadFile(attach.dir, attach.extension);
            FunctionDatabase.checkImg(attach.dir);
        }
        note.delete();
    }

    public static void createSanaachlaga(String taskId, Date date, String sanal, String[] filename, String[] filedir, String[] extension, Float[] filesize) {
        DailyLogSanaachlaga sanaachlaga;
        User owner = Users.getUser();
        if (taskId.charAt(0) == 't') {
            Task task = Task.findById(Long.parseLong(taskId.substring(1, taskId.length())));
            sanaachlaga = new DailyLogSanaachlaga(sanal, date, owner, task);
        } else {
            DailyLogMyPlan myPlan = DailyLogMyPlan.findById(Long.parseLong(taskId.substring(1, taskId.length())));
            sanaachlaga = new DailyLogSanaachlaga(sanal, date, owner, myPlan);
        }
        sanaachlaga.attaches = new ArrayList<DailyLogSanaachlagaAttach>();
        if (filename != null) {
            for (int i = 0; i < filename.length; i++) {
                DailyLogSanaachlagaAttach attach = new DailyLogSanaachlagaAttach(filename[i], filedir[i], extension[i], filesize[i], sanaachlaga);
                FunctionDatabase.createImg(sanaachlaga.owner, sanaachlaga.task, filename[i], filedir[i], extension[i]);
                sanaachlaga.attaches.add(attach);
            }
        }
        sanaachlaga.create();
        if (!owner.userTeam.contractor) {
            Post post = new Post();
            post.owner = Users.getUser();
            post.seeAll = true;
            post.content = sanal;
            post.type = "Санаачилга гаргалаа";
            post.typeIconName = "idea.jpg";
            post.typeModelId = sanaachlaga.id;
            post.create();
            if (filename != null) {
                for (int i = 0; i < filename.length; i++) {
                    PostAttach postAttach = new PostAttach(filename[i], filedir[i], extension[i], filesize[i], post);
                    postAttach.create();
                }
            }
        }
        boolean isList = false;
        render(sanaachlaga, isList);
    }

    public static void deleteSanaachlaga(Long id) {
        DailyLogSanaachlaga sanaachlaga = DailyLogSanaachlaga.findById(id);
        if (sanaachlaga != null) {
            for (DailyLogSanaachlagaAttach attach : sanaachlaga.attaches) {
                Functions.deleteUploadFile(attach.dir, attach.extension);
                FunctionDatabase.checkImg(attach.dir);
            }
            sanaachlaga.delete();
        }
        Post post = Post.find("type='Санаачилга гаргалаа' AND typeModelId=?1", sanaachlaga.id).first();
        if (post != null) post._delete();
    }

    public static void createVisitor(String taskId, Date date, String title, String from, String startHour, String finishHour, String description,
                                     String[] filename, String[] filedir, String[] extension, Float[] filesize) {

        Date startDate = Functions.convertHourNull(date);
        Date finishDate = startDate;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.set(Calendar.HOUR, Integer.parseInt(startHour.substring(0, 2)));
        calendar.set(Calendar.MINUTE, Integer.parseInt(startHour.substring(3, 5)));
        startDate = calendar.getTime();
        calendar.setTime(finishDate);
        calendar.set(Calendar.HOUR, Integer.parseInt(finishHour.substring(0, 2)));
        calendar.set(Calendar.MINUTE, Integer.parseInt(finishHour.substring(3, 5)));
        finishDate = calendar.getTime();
        DailyLogVisitor visitor;
        if (taskId.charAt(0) == 't') {
            Task task = Task.findById(Long.parseLong(taskId.substring(1, taskId.length())));
            visitor = new DailyLogVisitor(title, from, startDate, finishDate, description, date, Users.getUser(), task);
        } else {
            DailyLogMyPlan myPlan = DailyLogMyPlan.findById(Long.parseLong(taskId.substring(1, taskId.length())));
            visitor = new DailyLogVisitor(title, from, startDate, finishDate, description, date, Users.getUser(), myPlan);
        }

        visitor.attaches = new ArrayList<DailyLogVisitorAttach>();
        if (filename != null) {
            for (int i = 0; i < filename.length; i++) {
                DailyLogVisitorAttach attach = new DailyLogVisitorAttach(filename[i], filedir[i], extension[i], filesize[i], visitor);
                FunctionDatabase.createImg(visitor.owner, visitor.task, filename[i], filedir[i], extension[i]);
                visitor.attaches.add(attach);
            }
        }
        visitor.create();
        boolean isList = false;
        render(visitor, isList);
    }

    public static void deleteVisitor(Long id) {
        DailyLogVisitor visitor = DailyLogVisitor.findById(id);
        for (DailyLogVisitorAttach attach : visitor.attaches) {
            Functions.deleteUploadFile(attach.dir, attach.extension);
            FunctionDatabase.checkImg(attach.dir);
        }
        visitor.delete();
    }

    public static void createWaste(String taskId, Date date, String subject, String measure,
                                   Long quantity, String comments, String[] filename, String[] filedir, String[] extension, Float[] filesize) {
        DailyLogWaste waste;
        if (taskId.charAt(0) == 't') {
            Task task = Task.findById(Long.parseLong(taskId.substring(1, taskId.length())));
            waste = new DailyLogWaste(date, Users.getUser(), subject, measure, quantity, comments, task);
        } else {
            DailyLogMyPlan myPlan = DailyLogMyPlan.findById(Long.parseLong(taskId.substring(1, taskId.length())));
            waste = new DailyLogWaste(date, Users.getUser(), subject, measure, quantity, comments, myPlan);
        }
        waste.attaches = new ArrayList<DailyLogWasteAttach>();
        if (filename != null) {
            for (int i = 0; i < filename.length; i++) {
                DailyLogWasteAttach attach = new DailyLogWasteAttach(filename[i], filedir[i], extension[i], filesize[i], waste);
                FunctionDatabase.createImg(waste.owner, waste.task, filename[i], filedir[i], extension[i]);
                waste.attaches.add(attach);
            }
        }
        waste.create();
        boolean isList = false;
        render(waste, isList);
    }

    public static void deleteWaste(Long id) {
        DailyLogWaste waste = DailyLogWaste.findById(id);
        for (DailyLogWasteAttach attach : waste.attaches) {
            Functions.deleteUploadFile(attach.dir, attach.extension);
            FunctionDatabase.checkImg(attach.dir);
        }
        waste.delete();
    }

    public static void createDumpster(String dumpsterTask, Date date, Long dumpsterMaterial,
                                      Long quantity, String location, String comments,
                                      String[] filename, String[] filedir, String[] extension, Float[] filesize) {
        Inventory material = Inventory.findById(dumpsterMaterial);
        DailyLogDumpster dumpster;
        if (dumpsterTask.charAt(0) == 't') {
            Task task = Task.findById(Long.parseLong(dumpsterTask.substring(1, dumpsterTask.length())));
            dumpster = new DailyLogDumpster(quantity, location, comments, material, date, Users.getUser(), task);
        } else {
            DailyLogMyPlan myPlan = DailyLogMyPlan.findById(Long.parseLong(dumpsterTask.substring(1, dumpsterTask.length())));
            dumpster = new DailyLogDumpster(quantity, location, comments, material, date, Users.getUser(), myPlan);
        }
        dumpster.attaches = new ArrayList<DailyLogDumpsterAttach>();
        if (filename != null) {
            for (int i = 0; i < filename.length; i++) {
                DailyLogDumpsterAttach attach = new DailyLogDumpsterAttach(filename[i], filedir[i], extension[i], filesize[i], dumpster);
                FunctionDatabase.createImg(dumpster.owner, dumpster.task, filename[i], filedir[i], extension[i]);
                attach.dumpster = dumpster;
                dumpster.attaches.add(attach);
            }
        }
        dumpster.create();
        boolean isList = false;
        render(dumpster, isList);
    }

    public static void deleteDumpster(Long id) {
        DailyLogDumpster dumpster = DailyLogDumpster.findById(id);
        for (DailyLogDumpsterAttach attach : dumpster.attaches) {
            if (DailyLogDumpsterAttach.count("dumpster.id!=?1 AND dir=?2 AND extension=?3", dumpster.id, attach.dir, attach.extension) == 0)
                Functions.deleteUploadFile(attach.dir, attach.extension);
            FunctionDatabase.checkImg(attach.dir);
        }
        dumpster.delete();
    }

    public static void printOperation(Date currentDate) {
        List<Task> tasks = TaskUsers.getMyStartedTasks(currentDate);
        List<DailyLogMyPlan> myPlans = TaskUsers.getMyUnScheduledTask(currentDate);
        if (CompanyConf.type == 0)
            render(tasks, myPlans, currentDate);
        else
            render("DailyLogs/printDesign.html", tasks, myPlans, currentDate);
    }
}
