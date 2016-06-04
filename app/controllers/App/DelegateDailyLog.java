package controllers.App;

import com.google.gson.*;
import controllers.Consts;
import controllers.Functions;
import controllers.Users;
import models.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Enkh-Amgalan on 4/30/15.
 */
public class DelegateDailyLog extends Delegate {

    public static void list(String body) {
        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            Date currentDate = new Date();
            Long userId = user.id;
            try {
                currentDate = Consts.simpleDateFormat.parse(json.get("date").getAsString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            JsonArray jsonArray = new JsonArray();
            JsonObject object;
            object = new JsonObject();
            object.addProperty("name", "Цаг агаарын саатал");
            object.addProperty("dailyLogType", "Weather");
            object.addProperty("count", DailyLogWeather.count("date=?1 AND owner.id=?2", currentDate, userId));
            jsonArray.add(object);
            object = new JsonObject();
            object.addProperty("name", "Цаг агаарын саатал");
            object.addProperty("dailyLogType", "Weather");
            object.addProperty("count", DailyLogWeather.count("date=?1 AND owner.id=?2", currentDate, userId));
            jsonArray.add(object);
            object = new JsonObject();
            object.addProperty("name", "Техникийн саатал");
            object.addProperty("dailyLogType", "TechnicalDelay");
            object.addProperty("count", DailyLogTechnicalDelay.count("date=?1 AND owner.id=?2", currentDate, userId));
            jsonArray.add(object);
            object = new JsonObject();
            object.addProperty("name", "Материалын алдагдал");
            object.addProperty("dailyLogType", "MaterialLost");
            object.addProperty("count", "0");
            jsonArray.add(object);
            object = new JsonObject();
            object.addProperty("name", "БОХАБЭА-н зөрчил");
            object.addProperty("dailyLogType", "Safety");
            object.addProperty("count", "0");
            jsonArray.add(object);
            object = new JsonObject();
            object.addProperty("name", "Зочин хүлээн авсан");
            object.addProperty("dailyLogType", "Visitor");
            object.addProperty("count", DailyLogVisitor.count("date=?1 AND owner.id=?2", currentDate, userId));
            jsonArray.add(object);
            object = new JsonObject();
            object.addProperty("name", "Хүргэлт хүлээн авсан");
            object.addProperty("dailyLogType", "Delivery");
            object.addProperty("count", DailyLogDelivery.count("date=?1 AND owner.id=?2", currentDate, userId));
            jsonArray.add(object);
            object = new JsonObject();
            object.addProperty("name", "Тэмдэглэл/Асуудал");
            object.addProperty("dailyLogType", "Notes");
            object.addProperty("count", DailyLogNote.count("date=?1 AND owner.id=?2", currentDate, userId));
            jsonArray.add(object);
            object = new JsonObject();
            object.addProperty("name", "Санал санаачлага");
            object.addProperty("dailyLogType", "Sanaachlaga");
            object.addProperty("count", DailyLogSanaachlaga.count("date=?1 AND owner.id=?2", currentDate, userId));
            jsonArray.add(object);
            object = new JsonObject();
            object.addProperty("name", "Хог хаягдал");
            object.addProperty("dailyLogType", "Waste");
            object.addProperty("count", "0");
            jsonArray.add(object);
            renderJSON(jsonArray);
        }
    }

    public static void weatherList(String body) {

        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            boolean contractorUser = user.userTeam.contractor;
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            JsonArray obj = new JsonArray();
            Date currentDate = null;
            try {
                currentDate = dateFormat.parse(json.get("date").getAsString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            List<DailyLogWeather> weathers;
            //  if (contractorUser)
            weathers = DailyLogWeather.find("date>=?1 AND owner.id=?2", currentDate, user.id).fetch();
            // else
            //   weathers = DailyLogWeather.find("SELECT DISTINCT t FROM tb_dailylog_weather t JOIN t.task.taskAssignRels AS r WHERE r.task = t.task AND t.task.completedPercent<100F AND t.task.startDate<=?1 AND t.date>=?2 AND r.user.id=?3 ORDER BY t.owner.userTeam.contractor desc,t.task.name", currentDate, currentDate, user.id).fetch();
            for (DailyLogWeather weather : weathers) {
                JsonObject object = new JsonObject();
                object.addProperty("id", weather.id);
                object.addProperty("date", weather.id);
                object.addProperty("jobSubId", weather.task == null ? weather.myPlan.id : weather.task.id);
                object.addProperty("jobMainId", weather.task == null ? 0 : weather.task.projectObject.id);
                object.addProperty("jobSubName", weather.task == null ? weather.myPlan.name : weather.task.name);
                object.addProperty("jobMainName", weather.task == null ? "Хувийн төлөвлөгөөт" : weather.task.projectObject.project.name + " > " + weather.task.projectObject.name);
                object.addProperty("whichDate", Consts.dateFormat2.format(weather.startDate));
                object.addProperty("weatherName", weather.weatherCondition.conditionWeather);
                object.addProperty("userId", weather.owner.id);
                object.addProperty("weatherId", weather.weatherCondition.id);
                object.addProperty("startTime", timeFormat.format(weather.startDate));
                object.addProperty("endTime", timeFormat.format(weather.finishDate));
                object.addProperty("desc", weather.notes);
                JsonArray files = new JsonArray();
                for (DailyLogWeatherAttach attach : weather.attaches) {
                    files.add(new JsonPrimitive(attach.dir.replace("/", "-") + "." + attach.extension));
                }
                object.add("filenames", files);
                obj.add(object);
            }
            renderJSON(obj);
        }
    }

    public static void weatherNew(String body) {
        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            JsonObject responseObj = new JsonObject();
            JsonObject weatherLog = json.get("WeatherLog").getAsJsonObject();
            Long conditionId = weatherLog.get("weatherid").getAsLong();
            Date currentDate = null;
            try {
                currentDate = dateFormat.parse(weatherLog.get("whichdate").getAsString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            currentDate = Functions.convertHourNull(currentDate);
            String startHour = weatherLog.get("starttime").getAsString();
            String finishHour = weatherLog.get("endtime").getAsString();
            Long projectId = weatherLog.get("jobmainid").getAsLong();
            Long taskId = weatherLog.get("jobsubid").getAsLong();
            String notes = weatherLog.get("desc").getAsString();
            Date startDate = Functions.convertHourNull(currentDate);
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
            if (projectId != 0) {
                Task task = Task.findById(taskId);
                weather = new DailyLogWeather(condition, currentDate, startDate, finishDate, notes, task, user);
            } else {
                DailyLogMyPlan myPlan = DailyLogMyPlan.findById(taskId);
                weather = new DailyLogWeather(condition, currentDate, startDate, finishDate, notes, myPlan, user);
            }
            weather.create();
            JsonArray filenames = weatherLog.get("filenames").getAsJsonArray();
            if (filenames.size() > 0) {
                for (int i = 0; i < filenames.size(); i++) {
                    JsonPrimitive ids = filenames.get(i).getAsJsonPrimitive();
                    String[] file = ids.getAsString().split("\\.");
                    String[] fileName = file[0].split("/");
                    DailyLogWeatherAttach attach = new DailyLogWeatherAttach(fileName[fileName.length - 1], file[0], file[1], null, weather);
                    attach.create();
                }
            }
            responseObj.addProperty("newId", weather.id);
            responseObj.addProperty("date", startDate.getTime());
            responseObj.addProperty("tempId", weatherLog.get("tempid").getAsLong());
            renderJSON(responseObj);
        }
    }

    public static void workList(String body) {
        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            boolean contractorUser = user.userTeam.contractor;
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            JsonArray obj = new JsonArray();
            Date currentDate = null, today5 = Functions.addOrMinusDays(new Date(), 5, true);
            try {
                currentDate = dateFormat.parse(json.get("date").getAsString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            List<Task> tasks;
            List<DailyLogMyPlan> myPlans;
            int days = Functions.getDifferenceDays(currentDate, today5);
            for (int i = 0; i < days; i++) {
                tasks = Task.find("SELECT DISTINCT t FROM tb_task t LEFT JOIN t.taskAssignRels AS r LEFT JOIN t.dailyLogScheduledWorks AS s " +
                        "WHERE t.hasChild=false AND r.task.id = t.id AND t.startDate<=?1 AND r.user.id=?2 AND ( t.completedPercent < 100F OR (s.task.id = t.id AND s.date=?3)) ORDER BY t.startDate", currentDate, user.id, currentDate).fetch();
                Date nextDay = Functions.addOrMinusDays(currentDate, 1, true);
                myPlans = DailyLogMyPlan.find("SELECT DISTINCT m FROM tb_dailylog_myplan m LEFT JOIN m.logScheduledWorks AS s WHERE m.owner.id=?1 AND m.startDate<?2 AND ( m.completedPercent<100F OR (s.myPlan.id = m.id AND s.date=?3))", user.id, nextDay, currentDate).fetch();
                for (Task task : tasks) {
                    JsonObject object = new JsonObject();
                    object.addProperty("whichDate", dateFormat.format(currentDate));
                    object.addProperty("date", currentDate.getTime());
                    object.addProperty("startedTime", dateFormat.format(task.startDate));
                    object.addProperty("endTime", dateFormat.format(task.finishDate));
                    object.addProperty("jobMainId", task.projectObject.id);
                    object.addProperty("jobMainName", task.projectObject.project.name + " > " + task.projectObject.name);
                    object.addProperty("jobSubId", task.id);
                    object.addProperty("jobSubName", task.name);
                    DailyLogScheduledWork scheduledWork = task.getScheduledWork(currentDate);
                    if (scheduledWork != null) {
                        object.addProperty("progress", scheduledWork.completedPercent);
                        object.addProperty("workerCount", scheduledWork.totalWorkers.toString());
                        object.addProperty("workerManHour", Functions.getFloatFormat(scheduledWork.totalManHours, 2));
                        object.addProperty("technicalMachineCount", scheduledWork.equipments.size() + "");
                        object.addProperty("technicalMachineHour", Functions.getFloatFormat(scheduledWork.totalMotHours, 2));
                        object.addProperty("materialCount", scheduledWork.materials.size() + "");
                        DailyLogWorkNote workNote = DailyLogWorkNote.find("owner.id=?1 AND scheduledWork.id=?2", user.id, scheduledWork.id).first();
                        if (workNote != null) {
                            object.addProperty("id", workNote.id);
                            object.addProperty("userId", workNote.owner.toString());
                            object.addProperty("desc", workNote.notes);
                            object.addProperty("lastEdited", workNote.lastEdited);
                            JsonArray files = new JsonArray();
                            for (DailyLogWorkNoteAttach attach : workNote.attaches) {
                                files.add(new JsonPrimitive(attach.dir.replace("/", "-") + "." + attach.extension));
                            }
                            object.add("filenames", files);
                        } else {
                            object.addProperty("id", 0);
                            object.addProperty("userId", 0);
                            object.addProperty("desc", "");
                            object.addProperty("lastEdited", 0);
                            object.add("filenames", new JsonArray());
                        }
                    } else {
                        scheduledWork = DailyLogScheduledWork.find("task.id=?1 AND date <?2 ORDER BY date desc", task.id, currentDate).first();
                        object.addProperty("id", 0);
                        object.addProperty("workerCount", "0");
                        object.addProperty("workerManHour", "0");
                        object.addProperty("technicalMachineCount", "0");
                        object.addProperty("technicalMachineHour", "0");
                        object.addProperty("materialCount", "0");
                        object.addProperty("progress", (scheduledWork == null ? 0f : scheduledWork.completedPercent));
                        object.addProperty("userId", 0);
                        object.addProperty("desc", "");
                        object.addProperty("lastEdited", 0);
                        object.add("filenames", new JsonArray());
                    }
                    obj.add(object);
                }
                for (DailyLogMyPlan myPlan : myPlans) {
                    JsonObject object = new JsonObject();
                    object.addProperty("whichDate", dateFormat.format(currentDate));
                    object.addProperty("date", currentDate.getTime());
                    object.addProperty("startedTime", dateFormat.format(myPlan.startDate));
                    object.addProperty("endTime", dateFormat.format(myPlan.finishDate));
                    object.addProperty("jobMainId", 0);
                    object.addProperty("jobMainName", "Хувийн төлөвлөгөөт");
                    object.addProperty("jobSubId", myPlan.id);
                    object.addProperty("jobSubName", myPlan.name);
                    object.addProperty("progress", myPlan.completedPercent);
                    DailyLogScheduledWork scheduledWork = myPlan.getScheduledWork(currentDate);
                    if (scheduledWork != null) {
                        object.addProperty("progress", scheduledWork.completedPercent);
                        object.addProperty("workerCount", scheduledWork.totalWorkers.toString());
                        object.addProperty("workerManHour", Functions.getFloatFormat(scheduledWork.totalManHours, 2));
                        object.addProperty("technicalMachineCount", scheduledWork.equipments.size() + "");
                        object.addProperty("technicalMachineHour", Functions.getFloatFormat(scheduledWork.totalMotHours, 2));
                        object.addProperty("materialCount", scheduledWork.materials.size() + "");
                        DailyLogWorkNote workNote = DailyLogWorkNote.find("owner.id=?1 AND scheduledWork.id=?2", user.id, scheduledWork.id).first();
                        if (workNote != null) {
                            object.addProperty("id", workNote.id);
                            object.addProperty("userId", workNote.owner.toString());
                            object.addProperty("desc", workNote.notes);
                            object.addProperty("lastEdited", workNote.lastEdited);
                            JsonArray files = new JsonArray();
                            for (DailyLogWorkNoteAttach attach : workNote.attaches) {
                                files.add(new JsonPrimitive(attach.dir.replace("/", "-") + "." + attach.extension));
                            }
                            object.add("filenames", files);
                        } else {
                            object.addProperty("id", 0);
                            object.addProperty("userId", 0);
                            object.addProperty("desc", "");
                            object.addProperty("lastEdited", 0);
                            object.add("filenames", new JsonArray());
                        }
                    } else {
                        scheduledWork = DailyLogScheduledWork.find("myPlan.id=?1 AND date <?2 ORDER BY date desc", myPlan.id, currentDate).first();
                        object.addProperty("id", 0);
                        object.addProperty("lastEdited", 0);
                        object.addProperty("workerCount", "0");
                        object.addProperty("workerManHour", "0");
                        object.addProperty("technicalMachineCount", "0");
                        object.addProperty("technicalMachineHour", "0");
                        object.addProperty("materialCount", "0");
                        object.addProperty("progress", (scheduledWork == null ? 0f : scheduledWork.completedPercent));
                        object.addProperty("userId", 0);
                        object.addProperty("desc", "");
                        object.add("filenames", new JsonArray());
                    }
                    obj.add(object);
                }
                currentDate = Functions.addOrMinusDays(currentDate, 1, true);
            }
            renderJSON(obj);
        }
    }

    public static void workNew(String body) {
        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            boolean contractorUser = user.userTeam.contractor;
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            JsonObject obj = new JsonObject();
            JsonObject workObj = json.get("WorkProgressLog").getAsJsonObject();
            Date currentDate = null;
            try {
                currentDate = dateFormat.parse(workObj.get("whichdate").getAsString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            currentDate = Functions.convertHourNull(currentDate);
            DailyLogScheduledWork scheduledWork;
            Long workNoteId = workObj.get("id").getAsLong();
            Long taskId = workObj.get("jobsubid").getAsLong();
            Long objectId = workObj.get("jobmainid").getAsLong();
            DailyLogWorkNote workNote;
            boolean isEdit = true;
            if (workNoteId == 0) {
                if (objectId != 0) {
                    Task task = Task.findById(taskId);
                    scheduledWork = task.getScheduledWork(currentDate);
                    if (scheduledWork == null) {
                        scheduledWork = new DailyLogScheduledWork("task", task.completedPercent, currentDate, task);
                        scheduledWork.create();
                    }
                } else {
                    DailyLogMyPlan myPlan = DailyLogMyPlan.findById(taskId);
                    scheduledWork = myPlan.getScheduledWork(currentDate);
                    if (scheduledWork == null) {
                        scheduledWork = new DailyLogScheduledWork("myPlan", myPlan.completedPercent, currentDate, myPlan);
                        scheduledWork.create();
                    }
                }
                workNote = new DailyLogWorkNote(user, scheduledWork);
                workNote.create();
            } else {
                workNote = DailyLogWorkNote.findById(workNoteId);
                scheduledWork = workNote.scheduledWork;
                JsonElement lastEditedEl = workObj.get("lastEdited");
                if (lastEditedEl != null) {
                    Long lastEdited = lastEditedEl.getAsLong();
                    if (lastEdited < workNote.lastEdited)
                        isEdit = false;
                    else
                        workNote.lastEdited = lastEdited;
                }
            }
            if (isEdit) {
                workNote.notes = workObj.get("desc").getAsString();
                JsonArray filenames = workObj.get("filenames").getAsJsonArray();
                if (!workNote.owner.userTeam.contractor) {
                    Float percentfloat = workObj.get("progress").getAsFloat();
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
                    if (scheduledWork.task != null) {
                        if (percentfloat == 100f) {
                            // хугцаанаасаа өмнө дууссан гэсэн үг
                            scheduledWork.task.actualFinish = actualFinish;
                            if (true) {
                                boolean isNew = false;
                                Post post = Post.find("type='Ажлаа дуусгалаа' AND typeModelId=?1", workNote.id).first();
                                if (post == null) {
                                    isNew = true;
                                    post = new Post();
                                }
                                post.owner = Users.getUser();
                                post.seeAll = true;
                                post.content = "<strong>" + scheduledWork.task.projectObject.project.name + " > " + scheduledWork.task.projectObject.name + " > " + scheduledWork.task.name + "</strong> ажлийг хийж дуусгалаа баяр хүргье! </br>" + workNote.notes;
                                post.type = "Ажлаа дуусгалаа";
                                post.typeIconName = "complete.jpg";
                                post.typeModelId = workNote.id;
                                if (isNew) {
                                    post.create();
                                } else {
                                    post.save();
                                    List<PostAttach> postAttaches = PostAttach.find("post.id=?1", post.id).fetch();
                                    for (PostAttach postAttach : postAttaches)
                                        postAttach._delete();
                                }
                                if (filenames.size() > 0) {
                                    for (int i = 0; i < filenames.size(); i++) {
                                        JsonPrimitive ids = filenames.get(i).getAsJsonPrimitive();
                                        String[] file = ids.getAsString().split("\\.");
                                        String[] fileName = file[0].split("/");
                                        PostAttach attach = new PostAttach(fileName[fileName.length - 1], file[0], file[1],null, post);
                                        attach.create();
                                    }
                                }
                            }
                        } else if (scheduledWork.task.actualFinish.before(scheduledWork.date) && percentfloat < 100L) {
                            scheduledWork.task.actualFinish = actualFinish;
                        }
                        scheduledWork.task.completedPercent = percentfloat;
                        scheduledWork.task._save();
                        scheduledWork.task.setValueCompletedPercent(percentfloat);
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
                if (workNote.attaches != null)
                    for (DailyLogWorkNoteAttach attach : workNote.attaches) {
                        if (DailyLogWorkNoteAttach.count("workNote.id!=?1 AND dir=?2 AND extension=?3", workNote.id, attach.dir, attach.extension) == 0)
                            Functions.deleteUploadFile(attach.dir, attach.extension);
                        attach._delete();
                    }
                workNote.attaches = new ArrayList<DailyLogWorkNoteAttach>();
                if (filenames.size() > 0) {
                    for (int i = 0; i < filenames.size(); i++) {
                        JsonPrimitive ids = filenames.get(i).getAsJsonPrimitive();
                        String[] file = ids.getAsString().split("\\.");
                        String[] fileName = file[0].split("/");
                        DailyLogWorkNoteAttach attach = new DailyLogWorkNoteAttach(fileName[fileName.length - 1], file[0], file[1], null, workNote);
                        attach.create();
                    }
                }
                workNote._save();
                scheduledWork._save();
            }
            obj.addProperty("newId", workNote.id);
            obj.addProperty("date", currentDate.getTime());
            renderJSON(obj);
        }
    }

    public static void technicalProblemNew(String body) {
        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            JsonObject responseObj = new JsonObject();
            JsonObject technicalLog = json.get("TechnicalProblemLog").getAsJsonObject();
            String condition = technicalLog.get("reason").getAsString();
            Date currentDate = null;
            try {
                currentDate = dateFormat.parse(technicalLog.get("whichdate").getAsString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            currentDate = Functions.convertHourNull(currentDate);
            String startHour = technicalLog.get("starttime").getAsString();
            String finishHour = technicalLog.get("endtime").getAsString();
            Long projectId = technicalLog.get("jobmainid").getAsLong();
            Long taskId = technicalLog.get("jobsubid").getAsLong();
            String notes = technicalLog.get("desc").getAsString();
            Date startDate = Functions.convertHourNull(currentDate);
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

            DailyLogTechnicalDelay technicalDelay;
            if (projectId != 0) {
                Task task = Task.findById(taskId);
                technicalDelay = new DailyLogTechnicalDelay(condition, startDate, finishDate, currentDate, notes, task, user);
            } else {
                DailyLogMyPlan myPlan = DailyLogMyPlan.findById(taskId);
                technicalDelay = new DailyLogTechnicalDelay(condition, startDate, finishDate, currentDate, notes, myPlan, user);
            }
            technicalDelay.create();
            JsonArray filenames = technicalLog.get("filenames").getAsJsonArray();
            if (filenames.size() > 0) {
                for (int i = 0; i < filenames.size(); i++) {
                    JsonPrimitive ids = filenames.get(i).getAsJsonPrimitive();
                    String[] file = ids.getAsString().split("\\.");
                    String[] fileName = file[0].split("/");
                    DailyLogTechnicalDelayAttach attach = new DailyLogTechnicalDelayAttach(fileName[fileName.length - 1], file[0], file[1], null,technicalDelay);
                    attach.create();
                }
            }
            responseObj.addProperty("newId", technicalDelay.id);
            responseObj.addProperty("date", startDate.getTime());
            responseObj.addProperty("tempId", technicalLog.get("tempid").getAsLong());
            renderJSON(responseObj);
        }
    }

    public static void technicalProblemList(String body) {

        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            boolean contractorUser = user.userTeam.contractor;
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            JsonArray obj = new JsonArray();
            Date currentDate = null;
            try {
                currentDate = dateFormat.parse(json.get("date").getAsString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            List<DailyLogTechnicalDelay> technicalDelays;
            //  if (contractorUser)
            technicalDelays = DailyLogTechnicalDelay.find("date>=?1 AND owner.id=?2", currentDate, user.id).fetch();
            // else
            //   weathers = DailyLogWeather.find("SELECT DISTINCT t FROM tb_dailylog_weather t JOIN t.task.taskAssignRels AS r WHERE r.task = t.task AND t.task.completedPercent<100F AND t.task.startDate<=?1 AND t.date>=?2 AND r.user.id=?3 ORDER BY t.owner.userTeam.contractor desc,t.task.name", currentDate, currentDate, user.id).fetch();
            for (DailyLogTechnicalDelay technicalDelay : technicalDelays) {
                JsonObject object = new JsonObject();
                object.addProperty("id", technicalDelay.id);
                object.addProperty("date", technicalDelay.id);
                object.addProperty("jobSubId", technicalDelay.task == null ? technicalDelay.myPlan.id : technicalDelay.task.id);
                object.addProperty("jobMainId", technicalDelay.task == null ? 0 : technicalDelay.task.projectObject.id);
                object.addProperty("whichDate", Consts.dateFormat2.format(technicalDelay.startDate));
                object.addProperty("reason", technicalDelay.conditionTechnical);
                object.addProperty("userId", technicalDelay.owner.id);
                object.addProperty("startTime", timeFormat.format(technicalDelay.startDate));
                object.addProperty("endTime", timeFormat.format(technicalDelay.finishDate));
                object.addProperty("desc", technicalDelay.notes);
                JsonArray files = new JsonArray();
                for (DailyLogTechnicalDelayAttach attach : technicalDelay.attaches) {
                    files.add(new JsonPrimitive(attach.dir.replace("/", "-") + "." + attach.extension));
                }
                object.add("filenames", files);
                obj.add(object);
            }
            renderJSON(obj);
        }
    }

    public static void materialLossNew(String body) {

        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            JsonObject responseObj = new JsonObject();
            JsonObject technicalLog = json.get("MaterialLossLog").getAsJsonObject();
            Date currentDate = null;
            try {
                currentDate = dateFormat.parse(technicalLog.get("whichdate").getAsString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            currentDate = Functions.convertHourNull(currentDate);
            Long projectId = technicalLog.get("jobmainid").getAsLong();
            Long taskId = technicalLog.get("jobsubid").getAsLong();
            Long lossCount = technicalLog.get("count").getAsLong();
            String location = technicalLog.get("location").getAsString();
            String comments = technicalLog.get("desc").getAsString();
            Long materialId = technicalLog.get("materialid").getAsLong();
            Inventory material = Inventory.findById(materialId);
            DailyLogDumpster dumpster;
            if (projectId != 0) {
                Task task = Task.findById(taskId);
                dumpster = new DailyLogDumpster(lossCount, location, comments, material, currentDate, user, task);
            } else {
                DailyLogMyPlan myPlan = DailyLogMyPlan.findById(taskId);
                dumpster = new DailyLogDumpster(lossCount, location, comments, material, currentDate, user, myPlan);
            }
            dumpster.create();
            JsonArray filenames = technicalLog.get("filenames").getAsJsonArray();
            if (filenames.size() > 0) {
                for (int i = 0; i < filenames.size(); i++) {
                    JsonPrimitive ids = filenames.get(i).getAsJsonPrimitive();
                    String[] file = ids.getAsString().split("\\.");
                    String[] fileName = file[0].split("/");
                    DailyLogDumpsterAttach attach = new DailyLogDumpsterAttach(fileName[fileName.length - 1], file[0], file[1],null, dumpster);
                    attach.create();
                }
            }
            responseObj.addProperty("newId", dumpster.id);
            responseObj.addProperty("date", currentDate.getTime());
            responseObj.addProperty("tempId", technicalLog.get("tempid").getAsLong());
            renderJSON(responseObj);
        }
    }

    public static void materialLossList(String body) {

        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            boolean contractorUser = user.userTeam.contractor;
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            JsonArray obj = new JsonArray();
            Date currentDate = null;
            try {
                currentDate = dateFormat.parse(json.get("date").getAsString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            List<DailyLogDumpster> materialLost;
            //  if (contractorUser)
            materialLost = DailyLogDumpster.find("date>=?1 AND owner.id=?2", currentDate, user.id).fetch();
            // else
            //   weathers = DailyLogWeather.find("SELECT DISTINCT t FROM tb_dailylog_weather t JOIN t.task.taskAssignRels AS r WHERE r.task = t.task AND t.task.completedPercent<100F AND t.task.startDate<=?1 AND t.date>=?2 AND r.user.id=?3 ORDER BY t.owner.userTeam.contractor desc,t.task.name", currentDate, currentDate, user.id).fetch();
            for (DailyLogDumpster material : materialLost) {
                JsonObject object = new JsonObject();
                object.addProperty("id", material.id);
                object.addProperty("date", material.id);
                object.addProperty("jobSubId", material.task == null ? material.myPlan.id : material.task.id);
                object.addProperty("jobMainId", material.task == null ? 0 : material.task.projectObject.id);
                object.addProperty("whichDate", Consts.dateFormat2.format(material.date));
                object.addProperty("lossCount", material.quantity == null ? 0 : material.quantity);
                object.addProperty("materialId", material.material.id);
                object.addProperty("materialName", material.material.item);
                object.addProperty("userId", material.owner.id);
                object.addProperty("location", material.location);
                object.addProperty("desc", material.notes);
                JsonArray files = new JsonArray();
                for (DailyLogDumpsterAttach attach : material.attaches) {
                    files.add(new JsonPrimitive(attach.dir.replace("/", "-") + "." + attach.extension));
                }
                object.add("filenames", files);
                obj.add(object);
            }
            renderJSON(obj);
        }
    }

    public static void materialGroupList(String body) {

        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            JsonArray obj = new JsonArray();
            List<InventoryCategory> categories = InventoryCategory.findAll();
            for (InventoryCategory category : categories) {
                JsonObject object = new JsonObject();
                object.addProperty("id", category.id);
                object.addProperty("title", category.name);
                obj.add(object);
            }
            renderJSON(obj);
        }
    }

    public static void inspectionList(String body) {

        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            boolean contractorUser = user.userTeam.contractor;
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            JsonArray obj = new JsonArray();
            Date currentDate = null;
            try {
                currentDate = dateFormat.parse(json.get("date").getAsString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            List<DailyLogInspection> inspections;
            //  if (contractorUser)
            inspections = DailyLogInspection.find("date>=?1 AND owner.id=?2", currentDate, user.id).fetch();
            // else
            //   weathers = DailyLogWeather.find("SELECT DISTINCT t FROM tb_dailylog_weather t JOIN t.task.taskAssignRels AS r WHERE r.task = t.task AND t.task.completedPercent<100F AND t.task.startDate<=?1 AND t.date>=?2 AND r.user.id=?3 ORDER BY t.owner.userTeam.contractor desc,t.task.name", currentDate, currentDate, user.id).fetch();
            for (DailyLogInspection inspection : inspections) {
                JsonObject object = new JsonObject();
                object.addProperty("id", inspection.id);
                object.addProperty("date", inspection.id);
                object.addProperty("jobSubId", inspection.task == null ? inspection.myPlan.id : inspection.task.id);
                object.addProperty("jobMainId", inspection.task == null ? 0 : inspection.task.projectObject.id);
                object.addProperty("whichDate", Consts.dateFormat2.format(inspection.date));
                object.addProperty("startTime", timeFormat.format(inspection.startHour));
                object.addProperty("endTime", timeFormat.format(inspection.finishHour));
                object.addProperty("inspectionType", inspection.inspectionType);
                object.addProperty("inspector", inspection.inspectorName);
                object.addProperty("inspectionArea", inspection.area);
                object.addProperty("userId", inspection.owner.id);
                object.addProperty("desc", inspection.comments);
                JsonArray files = new JsonArray();
                for (DailyLogInspectionAttach attach : inspection.attaches) {
                    files.add(new JsonPrimitive(attach.dir.replace("/", "-") + "." + attach.extension));
                }
                object.add("filenames", files);
                obj.add(object);
            }
            renderJSON(obj);
        }
    }

    public static void inspectionNew(String body) {
        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            JsonObject responseObj = new JsonObject();
            JsonObject weatherLog = json.get("InspectionLog").getAsJsonObject();
            Date currentDate = null;
            try {
                currentDate = dateFormat.parse(weatherLog.get("whichdate").getAsString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            currentDate = Functions.convertHourNull(currentDate);
            String startHour = weatherLog.get("starttime").getAsString();
            String finishHour = weatherLog.get("endtime").getAsString();
            Long projectId = weatherLog.get("jobmainid").getAsLong();
            Long taskId = weatherLog.get("jobsubid").getAsLong();
            String notes = weatherLog.get("desc").getAsString();
            Date startDate = Functions.convertHourNull(currentDate);
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

            String type = weatherLog.get("inspectiontype").getAsString();
            String name = weatherLog.get("inspector").getAsString();
            String area = weatherLog.get("inspectionarea").getAsString();
            String comments = weatherLog.get("desc").getAsString();

            DailyLogInspection inspection;
            if (projectId != 0) {
                Task task = Task.findById(taskId);
                inspection = new DailyLogInspection(startDate, finishDate, type, name, area, comments, currentDate, user, task);
            } else {
                DailyLogMyPlan myPlan = DailyLogMyPlan.findById(taskId);
                inspection = new DailyLogInspection(startDate, finishDate, type, name, area, comments, currentDate, user, myPlan);
            }
            inspection.create();
            JsonArray filenames = weatherLog.get("filenames").getAsJsonArray();
            if (filenames.size() > 0) {
                for (int i = 0; i < filenames.size(); i++) {
                    JsonPrimitive ids = filenames.get(i).getAsJsonPrimitive();
                    String[] file = ids.getAsString().split("\\.");
                    String[] fileName = file[0].split("/");
                    DailyLogInspectionAttach attach = new DailyLogInspectionAttach(fileName[fileName.length - 1], file[0], file[1],null, inspection);
                    attach.create();
                }
            }
            responseObj.addProperty("newId", inspection.id);
            responseObj.addProperty("date", startDate.getTime());
            responseObj.addProperty("tempId", weatherLog.get("tempid").getAsLong());
            renderJSON(responseObj);
        }
    }

    public static void safetyViolationLogList(String body) {

        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            boolean contractorUser = user.userTeam.contractor;
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            JsonArray obj = new JsonArray();
            Date currentDate = null;
            try {
                currentDate = dateFormat.parse(json.get("date").getAsString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            List<DailyLogSafety> safeties;
            //  if (contractorUser)
            safeties = DailyLogSafety.find("date>=?1 AND owner.id=?2", currentDate, user.id).fetch();
            // else
            //   weathers = DailyLogWeather.find("SELECT DISTINCT t FROM tb_dailylog_weather t JOIN t.task.taskAssignRels AS r WHERE r.task = t.task AND t.task.completedPercent<100F AND t.task.startDate<=?1 AND t.date>=?2 AND r.user.id=?3 ORDER BY t.owner.userTeam.contractor desc,t.task.name", currentDate, currentDate, user.id).fetch();
            for (DailyLogSafety safety : safeties) {
                JsonObject object = new JsonObject();
                object.addProperty("id", safety.id);
                object.addProperty("date", safety.id);
                object.addProperty("jobSubId", safety.task == null ? safety.myPlan.id : safety.task.id);
                object.addProperty("jobMainId", safety.task == null ? 0 : safety.task.projectObject.id);
                object.addProperty("whichDate", Consts.dateFormat2.format(safety.date));
                object.addProperty("occurredTime", timeFormat.format(safety.safetyDate));
                object.addProperty("takenAction", safety.safetyNotice);
                object.addProperty("whoTo", safety.issuedTo);
                object.addProperty("actionDuration", safety.complianceDue);
                object.addProperty("userId", safety.owner.id);
                object.addProperty("desc", safety.comments);
                JsonArray files = new JsonArray();
                for (DailyLogSafetyAttach attach : safety.attaches) {
                    files.add(new JsonPrimitive(attach.dir.replace("/", "-") + "." + attach.extension));
                }
                object.add("filenames", files);
                obj.add(object);
            }
            renderJSON(obj);
        }
    }

    public static void safetyViolationLogNew(String body) {
        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            JsonObject responseObj = new JsonObject();
            JsonObject weatherLog = json.get("SafetyViolationLog").getAsJsonObject();
            Date currentDate = null;
            try {
                currentDate = dateFormat.parse(weatherLog.get("whichdate").getAsString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            currentDate = Functions.convertHourNull(currentDate);
            String occurredTime = weatherLog.get("occurredtime").getAsString();
            Long projectId = weatherLog.get("jobmainid").getAsLong();
            Long taskId = weatherLog.get("jobsubid").getAsLong();

            Date startDate = Functions.convertHourNull(currentDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);
            calendar.set(Calendar.HOUR, Integer.parseInt(occurredTime.substring(0, 2)));
            calendar.set(Calendar.MINUTE, Integer.parseInt(occurredTime.substring(3, 5)));
            startDate = calendar.getTime();

            String notice = weatherLog.get("takenaction").getAsString();
            String issuedTo = weatherLog.get("whoto").getAsString();
            String complianceDue = weatherLog.get("actionduration").getAsString();
            String comments = weatherLog.get("desc").getAsString();

            DailyLogSafety safety;
            if (projectId != 0) {
                Task task = Task.findById(taskId);
                safety = new DailyLogSafety(startDate, notice, issuedTo, complianceDue, comments, currentDate, user, task);
            } else {
                DailyLogMyPlan myPlan = DailyLogMyPlan.findById(taskId);
                safety = new DailyLogSafety(startDate, notice, issuedTo, complianceDue, comments, currentDate, user, myPlan);
            }
            safety.create();
            JsonArray filenames = weatherLog.get("filenames").getAsJsonArray();
            if (filenames.size() > 0) {
                for (int i = 0; i < filenames.size(); i++) {
                    JsonPrimitive ids = filenames.get(i).getAsJsonPrimitive();
                    String[] file = ids.getAsString().split("\\.");
                    String[] fileName = file[0].split("/");
                    DailyLogSafetyAttach attach = new DailyLogSafetyAttach(fileName[fileName.length - 1], file[0], file[1],null, safety);
                    attach.create();
                }
            }

            responseObj.addProperty("newId", safety.id);
            responseObj.addProperty("date", startDate.getTime());
            responseObj.addProperty("tempId", weatherLog.get("tempid").getAsLong());
            renderJSON(responseObj);
        }
    }

    public static void deliveryReceivedLogList(String body) {

        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            boolean contractorUser = user.userTeam.contractor;
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            JsonArray obj = new JsonArray();
            Date currentDate = null;
            try {
                currentDate = dateFormat.parse(json.get("date").getAsString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            List<DailyLogDelivery> deliveries;
            //  if (contractorUser)
            deliveries = DailyLogDelivery.find("date>=?1 AND owner.id=?2", currentDate, user.id).fetch();
            // else
            //   weathers = DailyLogWeather.find("SELECT DISTINCT t FROM tb_dailylog_weather t JOIN t.task.taskAssignRels AS r WHERE r.task = t.task AND t.task.completedPercent<100F AND t.task.startDate<=?1 AND t.date>=?2 AND r.user.id=?3 ORDER BY t.owner.userTeam.contractor desc,t.task.name", currentDate, currentDate, user.id).fetch();
            for (DailyLogDelivery delivery : deliveries) {
                JsonObject object = new JsonObject();
                object.addProperty("id", delivery.id);
                object.addProperty("date", delivery.id);
                object.addProperty("jobSubId", delivery.task == null ? delivery.myPlan.id : delivery.task.id);
                object.addProperty("jobMainId", delivery.task == null ? 0 : delivery.task.projectObject.id);
                object.addProperty("whichDate", Consts.dateFormat2.format(delivery.date));
                object.addProperty("receivedTime", timeFormat.format(delivery.deliveryHour));
                object.addProperty("fromWho", delivery.deliveryFrom);
                object.addProperty("numberCode", delivery.trackingNumber);
                object.addProperty("content", delivery.content);
                object.addProperty("userId", delivery.owner.id);
                object.addProperty("desc", delivery.comments);
                JsonArray files = new JsonArray();
                for (DailyLogDeliveryAttach attach : delivery.attaches) {
                    files.add(new JsonPrimitive(attach.dir.replace("/", "-") + "." + attach.extension));
                }
                object.add("filenames", files);
                obj.add(object);
            }
            renderJSON(obj);
        }
    }

    public static void deliveryReceivedLogNew(String body) {
        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            JsonObject responseObj = new JsonObject();
            JsonObject weatherLog = json.get("DeliveryReceivedLog").getAsJsonObject();
            Date currentDate = null;
            try {
                currentDate = dateFormat.parse(weatherLog.get("whichdate").getAsString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            currentDate = Functions.convertHourNull(currentDate);
            String receivedTime = weatherLog.get("receivedtime").getAsString();
            Long projectId = weatherLog.get("jobmainid").getAsLong();
            Long taskId = weatherLog.get("jobsubid").getAsLong();
            Date startDate = Functions.convertHourNull(currentDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);
            calendar.set(Calendar.HOUR, Integer.parseInt(receivedTime.substring(0, 2)));
            calendar.set(Calendar.MINUTE, Integer.parseInt(receivedTime.substring(3, 5)));
            startDate = calendar.getTime();

            String from = weatherLog.get("fromwho").getAsString();
            String trackingNumber = weatherLog.get("numbercode").getAsString();
            String contents = weatherLog.get("content").getAsString();
            String comments = weatherLog.get("desc").getAsString();

            DailyLogDelivery delivery;
            if (projectId != 0) {
                Task task = Task.findById(taskId);
                delivery = new DailyLogDelivery(startDate, from, trackingNumber, contents, comments, currentDate, user, task);
            } else {
                DailyLogMyPlan myPlan = DailyLogMyPlan.findById(taskId);
                delivery = new DailyLogDelivery(startDate, from, trackingNumber, contents, comments, currentDate, user, myPlan);
            }
            delivery.create();
            JsonArray filenames = weatherLog.get("filenames").getAsJsonArray();
            if (filenames.size() > 0) {
                for (int i = 0; i < filenames.size(); i++) {
                    JsonPrimitive ids = filenames.get(i).getAsJsonPrimitive();
                    String[] file = ids.getAsString().split("\\.");
                    String[] fileName = file[0].split("/");
                    DailyLogDeliveryAttach attach = new DailyLogDeliveryAttach(fileName[fileName.length - 1], file[0], file[1],null, delivery);
                    attach.create();
                }
            }
            responseObj.addProperty("newId", delivery.id);
            responseObj.addProperty("date", startDate.getTime());
            responseObj.addProperty("tempId", weatherLog.get("tempid").getAsLong());
            renderJSON(responseObj);
        }
    }

    public static void problemNoteLogList(String body) {

        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            boolean contractorUser = user.userTeam.contractor;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            JsonArray obj = new JsonArray();
            Date currentDate = null;
            try {
                currentDate = dateFormat.parse(json.get("date").getAsString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            List<DailyLogNote> notes;
            //  if (contractorUser)
            notes = DailyLogNote.find("date>=?1 AND owner.id=?2", currentDate, user.id).fetch();
            // else
            //   weathers = DailyLogWeather.find("SELECT DISTINCT t FROM tb_dailylog_weather t JOIN t.task.taskAssignRels AS r WHERE r.task = t.task AND t.task.completedPercent<100F AND t.task.startDate<=?1 AND t.date>=?2 AND r.user.id=?3 ORDER BY t.owner.userTeam.contractor desc,t.task.name", currentDate, currentDate, user.id).fetch();
            for (DailyLogNote note : notes) {
                JsonObject object = new JsonObject();
                object.addProperty("id", note.id);
                object.addProperty("date", note.id);
                object.addProperty("jobSubId", note.task == null ? note.myPlan.id : note.task.id);
                object.addProperty("jobMainId", note.task == null ? 0 : note.task.projectObject.id);
                object.addProperty("whichDate", Consts.dateFormat2.format(note.date));
                object.addProperty("significanceLevel", note.issue == 1 ? "Өндөр" : note.issue == 2 ? "Дунд" : "Бага");
                object.addProperty("desc", note.comment);
                JsonArray files = new JsonArray();
                for (DailyLogNoteAttach attach : note.attaches) {
                    files.add(new JsonPrimitive(attach.dir.replace("/", "-") + "." + attach.extension));
                }
                object.add("filenames", files);
                obj.add(object);
            }
            renderJSON(obj);
        }
    }

    public static void problemNoteLogNew(String body) {

        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            JsonObject responseObj = new JsonObject();
            JsonObject weatherLog = json.get("ProblemNoteLog").getAsJsonObject();
            Date currentDate = null;
            try {
                currentDate = dateFormat.parse(weatherLog.get("whichdate").getAsString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            currentDate = Functions.convertHourNull(currentDate);
            Long projectId = weatherLog.get("jobmainid").getAsLong();
            Long taskId = weatherLog.get("jobsubid").getAsLong();


            String issueStr = weatherLog.get("significancelevel").getAsString();
            Long issue = issueStr.equals("Өндөр") ? 1l : issueStr.equals("Дунд") ? 2l : 3l;
            String comments = weatherLog.get("desc").getAsString();
            DailyLogNote dailyLogNote;
            if (projectId != 0) {
                Task task = Task.findById(taskId);
                dailyLogNote = new DailyLogNote(issue, comments, currentDate, user, task);
            } else {
                DailyLogMyPlan myPlan = DailyLogMyPlan.findById(taskId);
                dailyLogNote = new DailyLogNote(issue, comments, currentDate, user, myPlan);
            }
            dailyLogNote.create();
            JsonArray filenames = weatherLog.get("filenames").getAsJsonArray();
            if (filenames.size() > 0) {
                for (int i = 0; i < filenames.size(); i++) {
                    JsonPrimitive ids = filenames.get(i).getAsJsonPrimitive();
                    String[] file = ids.getAsString().split("\\.");
                    String[] fileName = file[0].split("/");
                    DailyLogNoteAttach attach = new DailyLogNoteAttach(fileName[fileName.length - 1], file[0], file[1],null, dailyLogNote);
                    attach.create();
                }
            }
            responseObj.addProperty("newId", dailyLogNote.id);
            responseObj.addProperty("date", currentDate.getTime());
            responseObj.addProperty("tempId", weatherLog.get("tempid").getAsLong());

            renderJSON(responseObj);
        }
    }

    public static void ideaLogList(String body) {

        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            boolean contractorUser = user.userTeam.contractor;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            JsonArray obj = new JsonArray();
            Date currentDate = null;
            try {
                currentDate = dateFormat.parse(json.get("date").getAsString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            List<DailyLogSanaachlaga> sanaachlagas;
            //  if (contractorUser)
            sanaachlagas = DailyLogSanaachlaga.find("date>=?1 AND owner.id=?2", currentDate, user.id).fetch();
            // else
            //   weathers = DailyLogWeather.find("SELECT DISTINCT t FROM tb_dailylog_weather t JOIN t.task.taskAssignRels AS r WHERE r.task = t.task AND t.task.completedPercent<100F AND t.task.startDate<=?1 AND t.date>=?2 AND r.user.id=?3 ORDER BY t.owner.userTeam.contractor desc,t.task.name", currentDate, currentDate, user.id).fetch();
            for (DailyLogSanaachlaga sanaachlaga : sanaachlagas) {
                JsonObject object = new JsonObject();
                object.addProperty("id", sanaachlaga.id);
                object.addProperty("date", sanaachlaga.id);
                object.addProperty("jobSubId", sanaachlaga.task == null ? sanaachlaga.myPlan.id : sanaachlaga.task.id);
                object.addProperty("jobMainId", sanaachlaga.task == null ? 0 : sanaachlaga.task.projectObject.id);
                object.addProperty("whichDate", Consts.dateFormat2.format(sanaachlaga.date));
                object.addProperty("desc", sanaachlaga.sanal);
                JsonArray files = new JsonArray();
                for (DailyLogSanaachlagaAttach attach : sanaachlaga.attaches) {
                    files.add(new JsonPrimitive(attach.dir.replace("/", "-") + "." + attach.extension));
                }
                object.add("filenames", files);
                obj.add(object);
            }
            renderJSON(obj);
        }
    }

    public static void ideaLogNew(String body) {

        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            JsonObject responseObj = new JsonObject();
            JsonObject weatherLog = json.get("IdeaLog").getAsJsonObject();
            Date currentDate = null;
            try {
                currentDate = dateFormat.parse(weatherLog.get("whichdate").getAsString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            currentDate = Functions.convertHourNull(currentDate);
            Long projectId = weatherLog.get("jobmainid").getAsLong();
            Long taskId = weatherLog.get("jobsubid").getAsLong();
            String sanal = weatherLog.get("desc").getAsString();
            DailyLogSanaachlaga sanaachlaga;
            if (projectId != 0) {
                Task task = Task.findById(taskId);
                sanaachlaga = new DailyLogSanaachlaga(sanal, currentDate, user, task);
            } else {
                DailyLogMyPlan myPlan = DailyLogMyPlan.findById(taskId);
                sanaachlaga = new DailyLogSanaachlaga(sanal, currentDate, user, myPlan);
            }
            sanaachlaga.create();
            JsonArray filenames = weatherLog.get("filenames").getAsJsonArray();
            if (filenames.size() > 0) {
                for (int i = 0; i < filenames.size(); i++) {
                    JsonPrimitive ids = filenames.get(i).getAsJsonPrimitive();
                    String[] file = ids.getAsString().split("\\.");
                    String[] fileName = file[0].split("/");
                    DailyLogSanaachlagaAttach attach = new DailyLogSanaachlagaAttach(fileName[fileName.length - 1], file[0], file[1],null, sanaachlaga);
                    attach.create();
                }
            }
            if (!user.userTeam.contractor) {
                Post post = new Post();
                post.owner = user;
                post.seeAll = true;
                post.content = sanal;
                post.type = "Санаачилга гаргалаа";
                post.typeIconName = "idea.jpg";
                post.typeModelId = sanaachlaga.id;
                post.create();
                if (filenames.size() > 0) {
                    for (int i = 0; i < filenames.size(); i++) {
                        JsonPrimitive ids = filenames.get(i).getAsJsonPrimitive();
                        String[] file = ids.getAsString().split("\\.");
                        String[] fileName = file[0].split("/");
                        PostAttach attach = new PostAttach(fileName[fileName.length - 1], file[0], file[1],null, post);
                        attach.create();
                    }
                }
            }
            responseObj.addProperty("newId", sanaachlaga.id);
            responseObj.addProperty("date", currentDate.getTime());
            responseObj.addProperty("tempId", weatherLog.get("tempid").getAsLong());
            renderJSON(responseObj);
        }
    }

    public static void guestLogList(String body) {

        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            boolean contractorUser = user.userTeam.contractor;
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            JsonArray obj = new JsonArray();
            Date currentDate = null;
            try {
                currentDate = dateFormat.parse(json.get("date").getAsString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            List<DailyLogVisitor> visitors;
            //  if (contractorUser)
            visitors = DailyLogVisitor.find("date>=?1 AND owner.id=?2", currentDate, user.id).fetch();
            // else
            //   weathers = DailyLogWeather.find("SELECT DISTINCT t FROM tb_dailylog_weather t JOIN t.task.taskAssignRels AS r WHERE r.task = t.task AND t.task.completedPercent<100F AND t.task.startDate<=?1 AND t.date>=?2 AND r.user.id=?3 ORDER BY t.owner.userTeam.contractor desc,t.task.name", currentDate, currentDate, user.id).fetch();
            for (DailyLogVisitor visitor : visitors) {
                JsonObject object = new JsonObject();
                object.addProperty("id", visitor.id);
                object.addProperty("date", visitor.id);
                object.addProperty("jobSubId", visitor.task == null ? visitor.myPlan.id : visitor.task.id);
                object.addProperty("jobMainId", visitor.task == null ? 0 : visitor.task.projectObject.id);
                object.addProperty("whichDate", Consts.dateFormat2.format(visitor.date));
                object.addProperty("who", visitor.visitor);
                object.addProperty("fromWhere", visitor.fromVisitor);
                object.addProperty("cameTime", timeFormat.format(visitor.startHour));
                object.addProperty("goneTime", timeFormat.format(visitor.endHour));
                object.addProperty("desc", visitor.description);
                JsonArray files = new JsonArray();
                for (DailyLogVisitorAttach attach : visitor.attaches) {
                    files.add(new JsonPrimitive(attach.dir.replace("/", "-") + "." + attach.extension));
                }
                object.add("filenames", files);
                obj.add(object);
            }
            renderJSON(obj);
        }
    }

    public static void guestLogNew(String body) {
        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            JsonObject responseObj = new JsonObject();
            JsonObject weatherLog = json.get("GuestLog").getAsJsonObject();
            Date currentDate = null;
            try {
                currentDate = dateFormat.parse(weatherLog.get("whichdate").getAsString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            currentDate = Functions.convertHourNull(currentDate);
            String startHour = weatherLog.get("cametime").getAsString();
            String finishHour = weatherLog.get("gonetime").getAsString();
            Long projectId = weatherLog.get("jobmainid").getAsLong();
            Long taskId = weatherLog.get("jobsubid").getAsLong();
            Date startDate = Functions.convertHourNull(currentDate);
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


            String description = weatherLog.get("desc").getAsString();
            String title = weatherLog.get("who").getAsString();
            String from = weatherLog.get("fromwhere").getAsString();
            DailyLogVisitor visitor;
            if (projectId != 0) {
                Task task = Task.findById(taskId);
                visitor = new DailyLogVisitor(title, from, startDate, finishDate, description, currentDate, user, task);
            } else {
                DailyLogMyPlan myPlan = DailyLogMyPlan.findById(taskId);
                visitor = new DailyLogVisitor(title, from, startDate, finishDate, description, currentDate, user, myPlan);
            }
            visitor.create();
            JsonArray filenames = weatherLog.get("filenames").getAsJsonArray();
            if (filenames.size() > 0) {
                for (int i = 0; i < filenames.size(); i++) {
                    JsonPrimitive ids = filenames.get(i).getAsJsonPrimitive();
                    String[] file = ids.getAsString().split("\\.");
                    String[] fileName = file[0].split("/");
                    DailyLogVisitorAttach attach = new DailyLogVisitorAttach(fileName[fileName.length - 1], file[0], file[1],null, visitor);
                    attach.create();
                }
            }
            responseObj.addProperty("newId", visitor.id);
            responseObj.addProperty("date", startDate.getTime());
            responseObj.addProperty("tempId", weatherLog.get("tempid").getAsLong());
            renderJSON(responseObj);
        }
    }

    public static void wasteLogList(String body) {

        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            boolean contractorUser = user.userTeam.contractor;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            JsonArray obj = new JsonArray();
            Date currentDate = null;
            try {
                currentDate = dateFormat.parse(json.get("date").getAsString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            List<DailyLogWaste> wastes;
            //  if (contractorUser)
            wastes = DailyLogWaste.find("date>=?1 AND owner.id=?2", currentDate, user.id).fetch();
            // else
            //   weathers = DailyLogWeather.find("SELECT DISTINCT t FROM tb_dailylog_weather t JOIN t.task.taskAssignRels AS r WHERE r.task = t.task AND t.task.completedPercent<100F AND t.task.startDate<=?1 AND t.date>=?2 AND r.user.id=?3 ORDER BY t.owner.userTeam.contractor desc,t.task.name", currentDate, currentDate, user.id).fetch();
            for (DailyLogWaste waste : wastes) {
                JsonObject object = new JsonObject();
                object.addProperty("id", waste.id);
                object.addProperty("date", waste.id);
                object.addProperty("jobSubId", waste.task == null ? waste.myPlan.id : waste.task.id);
                object.addProperty("jobMainId", waste.task == null ? 0 : waste.task.projectObject.id);
                object.addProperty("whichDate", Consts.dateFormat2.format(waste.date));
                object.addProperty("desc", waste.comments);
                object.addProperty("whoCreated", waste.subject);
                object.addProperty("unit", waste.measure);
                object.addProperty("amount", waste.quantity == null ? 0 : waste.quantity);
                object.addProperty("userId", waste.owner.id);
                JsonArray files = new JsonArray();
                for (DailyLogWasteAttach attach : waste.attaches) {
                    files.add(new JsonPrimitive(attach.dir.replace("/", "-") + "." + attach.extension));
                }
                object.add("filenames", files);
                obj.add(object);
            }
            renderJSON(obj);
        }
    }

    public static void wasteLogNew(String body) {

        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            JsonObject responseObj = new JsonObject();
            JsonObject weatherLog = json.get("WasteLog").getAsJsonObject();
            Date currentDate = null;
            try {
                currentDate = dateFormat.parse(weatherLog.get("whichdate").getAsString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            currentDate = Functions.convertHourNull(currentDate);
            Long projectId = weatherLog.get("jobmainid").getAsLong();
            Long taskId = weatherLog.get("jobsubid").getAsLong();

            String comments = weatherLog.get("desc").getAsString();
            String subject = weatherLog.get("whocreated").getAsString();
            String measure = weatherLog.get("unit").getAsString();
            Long quantity = weatherLog.get("amount").getAsLong();
            DailyLogWaste waste;
            if (projectId != 0) {
                Task task = Task.findById(taskId);
                waste = new DailyLogWaste(currentDate, user, subject, measure, quantity, comments, task);
            } else {
                DailyLogMyPlan myPlan = DailyLogMyPlan.findById(taskId);
                waste = new DailyLogWaste(currentDate, user, subject, measure, quantity, comments, myPlan);
            }
            waste.create();
            JsonArray filenames = weatherLog.get("filenames").getAsJsonArray();
            if (filenames.size() > 0) {
                for (int i = 0; i < filenames.size(); i++) {
                    JsonPrimitive ids = filenames.get(i).getAsJsonPrimitive();
                    String[] file = ids.getAsString().split("\\.");
                    String[] fileName = file[0].split("/");
                    DailyLogWasteAttach attach = new DailyLogWasteAttach(fileName[fileName.length - 1], file[0], file[1],null, waste);
                    attach.create();
                }
            }
            responseObj.addProperty("newId", waste.id);
            responseObj.addProperty("date", currentDate.getTime());
            responseObj.addProperty("tempId", weatherLog.get("tempid").getAsLong());

            renderJSON(responseObj);
        }
    }

    public static void workProgressManHourLogList(String body) {

        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            boolean contractorUser = user.userTeam.contractor;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            JsonArray obj = new JsonArray();
            Long taskId = json.get("jobsubid").getAsLong();
            Long projectId = json.get("jobmainid").getAsLong();
            Date currentDate = null;
            try {
                currentDate = dateFormat.parse(json.get("whichdate").getAsString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            DailyLogScheduledWork scheduledWork;
            if (projectId != 0) {
                scheduledWork = DailyLogScheduledWork.find("date=?1 AND task.id=?2", currentDate, taskId).first();
            } else {
                scheduledWork = DailyLogScheduledWork.find("date=?1 AND myPlan.id=?2", currentDate, taskId).first();
            }
            List<DailyLogManpower> manpowers;
            if (scheduledWork != null) {
                //  if (contractorUser)
                manpowers = DailyLogManpower.find("scheduledWork.id=?1 ORDER BY owner.userTeam.contractor desc,owner.userPosition.rate", scheduledWork.id).fetch();
                // else
                //   weathers = DailyLogWeather.find("SELECT DISTINCT t FROM tb_dailylog_weather t JOIN t.task.taskAssignRels AS r WHERE r.task = t.task AND t.task.completedPercent<100F AND t.task.startDate<=?1 AND t.date>=?2 AND r.user.id=?3 ORDER BY t.owner.userTeam.contractor desc,t.task.name", currentDate, currentDate, user.id).fetch();
                for (DailyLogManpower manpower : manpowers) {
                    JsonObject object = new JsonObject();
                    object.addProperty("id", manpower.id);
                    object.addProperty("date", manpower.id);
                    object.addProperty("jobSubId", manpower.scheduledWork.task == null ? manpower.scheduledWork.myPlan.id : manpower.scheduledWork.task.id);
                    object.addProperty("jobMainId", manpower.scheduledWork.task == null ? 0 : manpower.scheduledWork.task.projectObject.id);
                    object.addProperty("whichDate", Consts.dateFormat2.format(manpower.scheduledWork.date));
                    object.addProperty("desc", manpower.note);
                    object.addProperty("location", manpower.location);
                    object.addProperty("hourDuration", manpower.manHours);
                    object.addProperty("workerCount", manpower.workers);
                    object.addProperty("occupationId", manpower.mergejil.id);
                    object.addProperty("departmentId", manpower.orgTeam.id);
                    object.addProperty("departmentName", manpower.orgTeam.name);
                    object.addProperty("userId", manpower.owner.id);
                    JsonArray files = new JsonArray();
                    for (DailyLogManpowerAttach attach : manpower.attaches) {
                        files.add(new JsonPrimitive(attach.dir.replace("/", "-") + "." + attach.extension));
                    }
                    object.add("filenames", files);
                    obj.add(object);
                }
            }
            renderJSON(obj);
        }
    }

    public static void workProgressManHourLogNew(String body) {
        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            JsonObject responseObj = new JsonObject();
            JsonObject weatherLog = json.get("WorkProgressManHourLog").getAsJsonObject();
            Date currentDate = null;
            try {
                currentDate = dateFormat.parse(weatherLog.get("whichdate").getAsString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            currentDate = Functions.convertHourNull(currentDate);
            Long projectId = weatherLog.get("jobmainid").getAsLong();
            Long taskId = weatherLog.get("jobsubid").getAsLong();
            Long tempId = weatherLog.get("tempid").getAsLong();
            DailyLogScheduledWork scheduledWork = null;
            if (projectId != 0) {
                scheduledWork = DailyLogScheduledWork.find("date=?1 AND task.id=?2", currentDate, taskId).first();
                if (scheduledWork == null) {
                    Task task = Task.findById(taskId);
                    scheduledWork = new DailyLogScheduledWork("task", task.completedPercent, currentDate, task);
                    scheduledWork.create();
                }
            } else {
                scheduledWork = DailyLogScheduledWork.find("date=?1 AND myPlan.id=?2", currentDate, taskId).first();
                if (scheduledWork == null) {
                    DailyLogMyPlan myPlan = DailyLogMyPlan.findById(taskId);
                    scheduledWork = new DailyLogScheduledWork("task", myPlan.completedPercent, currentDate, myPlan);
                    scheduledWork.create();
                }
            }
            ManPower mergejil = ManPower.findById(weatherLog.get("occupationid").getAsLong());
            OrganizationTeam userTeam = OrganizationTeam.findById(weatherLog.get("departmentid").getAsLong());
            Long worker = weatherLog.get("workercount").getAsLong();
            Float hour = weatherLog.get("hourduration").getAsFloat();
            Float totalHours = hour * worker;
            DailyLogManpower manpower = null;
            if (tempId != -1)
                manpower = DailyLogManpower.find("tempId=?1", tempId).first();
            if (manpower == null) {
                manpower = new DailyLogManpower(userTeam, mergejil, worker
                        , hour, totalHours,
                        weatherLog.get("location").getAsString(), weatherLog.get("notes").getAsString(), scheduledWork, user);
                manpower.tempId = tempId;
                manpower.create();
                if (!user.userTeam.contractor) {
                    if (scheduledWork.totalManHours != null) scheduledWork.totalManHours += totalHours;
                    else scheduledWork.totalManHours = totalHours;
                    if (scheduledWork.totalWorkers != null) scheduledWork.totalWorkers += worker;
                    else scheduledWork.totalWorkers = worker;
                    scheduledWork._save();
                }
            } else {
                manpower.setDailyLogManpower(userTeam, mergejil, worker, hour, totalHours,
                        weatherLog.get("location").getAsString(), weatherLog.get("notes").getAsString(), scheduledWork, user);
                manpower.save();
                for (DailyLogManpowerAttach attach : manpower.attaches)
                    attach._delete();
            }
            JsonArray filenames = weatherLog.get("filenames").getAsJsonArray();
            if (filenames.size() > 0) {
                for (int i = 0; i < filenames.size(); i++) {
                    JsonPrimitive ids = filenames.get(i).getAsJsonPrimitive();
                    String[] file = ids.getAsString().split("\\.");
                    String[] fileName = file[0].split("/");
                    DailyLogManpowerAttach attach = new DailyLogManpowerAttach(fileName[fileName.length - 1], file[0], file[1],null, manpower);
                    attach.create();
                }
            }
            responseObj.addProperty("newId", manpower.id);
            responseObj.addProperty("date", currentDate.getTime());
            responseObj.addProperty("tempId", weatherLog.get("tempid").getAsLong());
            renderJSON(responseObj);
        }
    }

    public static void workProgressTechnicalHourLogList(String body) {
        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            boolean contractorUser = user.userTeam.contractor;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            JsonArray obj = new JsonArray();
            Long taskId = json.get("jobsubid").getAsLong();
            Long projectId = json.get("jobmainid").getAsLong();
            Date currentDate = null;
            try {
                currentDate = dateFormat.parse(json.get("whichdate").getAsString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            DailyLogScheduledWork scheduledWork;
            if (projectId != 0) {
                scheduledWork = DailyLogScheduledWork.find("date=?1 AND task.id=?2", currentDate, taskId).first();
            } else {
                scheduledWork = DailyLogScheduledWork.find("date=?1 AND myPlan.id=?2", currentDate, taskId).first();
            }
            List<DailyLogEquipment> equipments;
            if (scheduledWork != null) {
                //  if (contractorUser)
                equipments = DailyLogEquipment.find("scheduledWork.id=?1 ORDER BY owner.userTeam.contractor desc,owner.userPosition.rate", scheduledWork.id).fetch();
                // else
                //   weathers = DailyLogWeather.find("SELECT DISTINCT t FROM tb_dailylog_weather t JOIN t.task.taskAssignRels AS r WHERE r.task = t.task AND t.task.completedPercent<100F AND t.task.startDate<=?1 AND t.date>=?2 AND r.user.id=?3 ORDER BY t.owner.userTeam.contractor desc,t.task.name", currentDate, currentDate, user.id).fetch();
                for (DailyLogEquipment equipment : equipments) {
                    JsonObject object = new JsonObject();
                    object.addProperty("id", equipment.id);
                    object.addProperty("date", equipment.id);
                    object.addProperty("jobSubId", equipment.scheduledWork.task == null ? equipment.scheduledWork.myPlan.id : equipment.scheduledWork.task.id);
                    object.addProperty("jobMainId", equipment.scheduledWork.task == null ? 0 : equipment.scheduledWork.task.projectObject.id);
                    object.addProperty("whichDate", Consts.dateFormat2.format(equipment.scheduledWork.date));
                    object.addProperty("technicalMachineId", equipment.equipmentType.id);
                    object.addProperty("operatorName", equipment.operator);
                    object.addProperty("location", equipment.location);
                    object.addProperty("startTime", timeFormat.format(equipment.startHour));
                    object.addProperty("endTime", timeFormat.format(equipment.finishHour));
                    object.addProperty("desc", equipment.comments);
                    object.addProperty("userId", equipment.owner.id);
                    JsonArray files = new JsonArray();
                    for (DailyLogEquipmentAttach attach : equipment.attaches) {
                        files.add(new JsonPrimitive(attach.dir.replace("/", "-") + "." + attach.extension));
                    }
                    object.add("filenames", files);
                    obj.add(object);
                }
            }
            renderJSON(obj);
        }
    }

    public static void workProgressTechnicalHourLogNew(String body) {
        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            JsonObject responseObj = new JsonObject();
            JsonObject weatherLog = json.get("WorkProgressTechnicalHourLog").getAsJsonObject();
            String startHour = weatherLog.get("starttime").getAsString();
            String finishHour = weatherLog.get("endtime").getAsString();
            Date currentDate = null;
            try {
                currentDate = dateFormat.parse(weatherLog.get("whichdate").getAsString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            currentDate = Functions.convertHourNull(currentDate);
            Long projectId = weatherLog.get("jobmainid").getAsLong();
            Long taskId = weatherLog.get("jobsubid").getAsLong();
            Long tempId = weatherLog.get("tempid").getAsLong();
            Date startDate = Functions.convertHourNull(currentDate);
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
            DailyLogScheduledWork scheduledWork = null;

            if (projectId != 0) {
                scheduledWork = DailyLogScheduledWork.find("date=?1 AND task.id=?2", currentDate, taskId).first();
                if (scheduledWork == null) {
                    Task task = Task.findById(taskId);
                    scheduledWork = new DailyLogScheduledWork("task", task.completedPercent, currentDate, task);
                    scheduledWork.create();
                }
            } else {
                scheduledWork = DailyLogScheduledWork.find("date=?1 AND myPlan.id=?2", currentDate, taskId).first();
                if (scheduledWork == null) {
                    DailyLogMyPlan myPlan = DailyLogMyPlan.findById(taskId);
                    scheduledWork = new DailyLogScheduledWork("myPlan", myPlan.completedPercent, currentDate, myPlan);
                    scheduledWork.create();
                }
            }
            Equipment equipmentType = Equipment.findById(weatherLog.get("technicalmachineid").getAsLong());
            DailyLogEquipment equipment = null;
            if (tempId != -1)
                equipment = DailyLogEquipment.find("tempId=?", tempId).first();
            if (equipment == null) {
                equipment = new DailyLogEquipment(equipmentType, weatherLog.get("operatorname").getAsString()
                        , weatherLog.get("location").getAsString(), startDate, finishDate,
                        weatherLog.get("desc").getAsString(), scheduledWork, user);
                equipment.tempId = tempId;
                equipment.create();
                if (!user.userTeam.contractor) {
                    scheduledWork.totalMotHours += equipment.motHours;
                    scheduledWork._save();
                }
            } else {
                equipment.setDailyLogEquipment(equipmentType, weatherLog.get("operatorname").getAsString()
                        , weatherLog.get("location").getAsString(), startDate, finishDate,
                        weatherLog.get("desc").getAsString(), scheduledWork, user);
                equipment.save();
                for (DailyLogEquipmentAttach attach : equipment.attaches)
                    attach._delete();
            }
            JsonArray filenames = weatherLog.get("filenames").getAsJsonArray();
            if (filenames.size() > 0) {
                for (int i = 0; i < filenames.size(); i++) {
                    JsonPrimitive ids = filenames.get(i).getAsJsonPrimitive();
                    String[] file = ids.getAsString().split("\\.");
                    String[] fileName = file[0].split("/");
                    DailyLogEquipmentAttach attach = new DailyLogEquipmentAttach(fileName[fileName.length - 1], file[0], file[1],null, equipment);
                    attach.create();
                }
            }
            responseObj.addProperty("newId", equipment.id);
            responseObj.addProperty("date", currentDate.getTime());
            responseObj.addProperty("tempId", weatherLog.get("tempid").getAsLong());
            renderJSON(responseObj);
        }
    }

    public static void workProgressMaterialSpentLogList(String body) {

        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            boolean contractorUser = user.userTeam.contractor;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            JsonArray obj = new JsonArray();
            Long taskId = json.get("jobsubid").getAsLong();
            Long projectId = json.get("jobmainid").getAsLong();
            Date currentDate = null;
            try {
                currentDate = dateFormat.parse(json.get("whichdate").getAsString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            DailyLogScheduledWork scheduledWork;
            if (projectId != 0) {
                scheduledWork = DailyLogScheduledWork.find("date=?1 AND task.id=?2", currentDate, taskId).first();
            } else {
                scheduledWork = DailyLogScheduledWork.find("date=?1 AND myPlan.id=?2", currentDate, taskId).first();
            }
            List<DailyLogMaterial> materials;
            if (scheduledWork != null) {
                //  if (contractorUser)
                materials = DailyLogMaterial.find("scheduledWork.id=?1 ORDER BY owner.userTeam.contractor desc,owner.userPosition.rate", scheduledWork.id).fetch();
                // else
                //   weathers = DailyLogWeather.find("SELECT DISTINCT t FROM tb_dailylog_weather t JOIN t.task.taskAssignRels AS r WHERE r.task = t.task AND t.task.completedPercent<100F AND t.task.startDate<=?1 AND t.date>=?2 AND r.user.id=?3 ORDER BY t.owner.userTeam.contractor desc,t.task.name", currentDate, currentDate, user.id).fetch();
                for (DailyLogMaterial material : materials) {
                    JsonObject object = new JsonObject();
                    object.addProperty("id", material.id);
                    object.addProperty("date", material.id);
                    object.addProperty("jobSubId", taskId);
                    object.addProperty("jobMainId", projectId);
                    object.addProperty("whichDate", Consts.dateFormat2.format(material.scheduledWork.date));
                    object.addProperty("materialId", material.material.id);
                    object.addProperty("amount", material.amount + "");
                    object.addProperty("unit", material.material.inventoryMeasure.measure);
                    object.addProperty("desc", material.note);
                    object.addProperty("userId", material.owner.id);
                    object.addProperty("lastEdited", material.lastEdited);
                    JsonArray files = new JsonArray();
                    for (DailyLogMaterialAttach attach : material.attaches) {
                        files.add(new JsonPrimitive(attach.dir.replace("/", "-") + "." + attach.extension));
                    }
                    object.add("filenames", files);
                    obj.add(object);
                }
            }
            renderJSON(obj);
        }
    }

    public static void workProgressMaterialSpentLogNew(String body) {
        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            JsonObject responseObj = new JsonObject();
            JsonObject weatherLog = json.get("WorkProgressMaterialSpentLog").getAsJsonObject();
            Date currentDate = null;
            try {
                currentDate = dateFormat.parse(weatherLog.get("whichdate").getAsString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            currentDate = Functions.convertHourNull(currentDate);
            Long projectId = weatherLog.get("jobmainid").getAsLong();
            Long taskId = weatherLog.get("jobsubid").getAsLong();
            Date startDate = Functions.convertHourNull(currentDate);
            Date finishDate = startDate;
            Long tempId = weatherLog.get("tempid").getAsLong();
            JsonElement dailyLogMaterialIdJson = weatherLog.get("id");
            DailyLogMaterial material = null;
            boolean isEdit = true;
            if (tempId != -1)
                material = DailyLogMaterial.find("tempId=?", tempId).first();
            if (dailyLogMaterialIdJson != null) {
                Long materialId = dailyLogMaterialIdJson.getAsLong();
                if (materialId != 0)
                    material = DailyLogMaterial.findById(materialId);
            }
            DailyLogScheduledWork scheduledWork = null;
            if (projectId != 0) {
                scheduledWork = DailyLogScheduledWork.find("date=?1 AND task.id=?2", currentDate, taskId).first();
                if (scheduledWork == null) {
                    Task task = Task.findById(taskId);
                    scheduledWork = new DailyLogScheduledWork("task", task.completedPercent, currentDate, task);
                    scheduledWork.create();
                }
            } else {
                scheduledWork = DailyLogScheduledWork.find("date=?1 AND myPlan.id=?2", currentDate, taskId).first();
                if (scheduledWork == null) {
                    DailyLogMyPlan myPlan = DailyLogMyPlan.findById(taskId);
                    scheduledWork = new DailyLogScheduledWork("task", myPlan.completedPercent, currentDate, myPlan);
                    scheduledWork.create();
                }
            }
            Inventory inventory = Inventory.findById(weatherLog.get("materialid").getAsLong());
            if (material == null) {
                material = new DailyLogMaterial(inventory, weatherLog.get("amount").getAsFloat(),
                        weatherLog.get("desc").getAsString(), user, scheduledWork);
                material.tempId = tempId;
                material.create();
            } else {
                JsonElement lastEditedEl = weatherLog.get("lastEdited");
                if (lastEditedEl != null) {
                    Long lastEdited = lastEditedEl.getAsLong();
                    if (lastEdited < material.lastEdited) {
                        isEdit = false;
                        material.setDailyLogMaterial(inventory, weatherLog.get("amount").getAsFloat(),
                                weatherLog.get("desc").getAsString(), user, scheduledWork);
                        material._save();
                        for (DailyLogMaterialAttach attach : material.attaches) {
                            if (DailyLogMaterialAttach.count("material.id!=?1 AND dir=?2 AND extension=?3", material.id, attach.dir, attach.extension) == 0)
                                Functions.deleteUploadFile(attach.dir, attach.extension);
                            attach._delete();
                        }
                    } else
                        material.lastEdited = lastEdited;
                }
            }
            if (isEdit) {
                JsonArray filenames = weatherLog.get("filenames").getAsJsonArray();
                if (filenames.size() > 0) {
                    for (int i = 0; i < filenames.size(); i++) {
                        JsonPrimitive ids = filenames.get(i).getAsJsonPrimitive();
                        String[] file = ids.getAsString().split("\\.");
                        String[] fileName = file[0].split("/");
                        DailyLogMaterialAttach attach = new DailyLogMaterialAttach(fileName[fileName.length - 1], file[0], file[1],null, material);
                        attach.create();
                    }
                }
            }
            responseObj.addProperty("newId", material.id);
            responseObj.addProperty("date", currentDate.getTime());
            responseObj.addProperty("tempId", weatherLog.get("tempid").getAsLong());
            renderJSON(responseObj);
        }
    }

    public static void orgTeamList(String body) {
        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            JsonArray obj = new JsonArray();
            List<OrganizationTeam> orgTeams = OrganizationTeam.findAll();
            for (OrganizationTeam team : orgTeams) {
                JsonObject object = new JsonObject();
                object.addProperty("id", team.id);
                object.addProperty("projectId", team.project.id);
                object.addProperty("title", team.name);
                obj.add(object);
            }
            renderJSON(obj);
        }
    }

    public static void manPowerList(String body) {
        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            JsonArray obj = new JsonArray();
            List<ManPower> manPowers = ManPower.findAll();
            for (ManPower manPower : manPowers) {
                JsonObject object = new JsonObject();
                object.addProperty("id", manPower.id);
                object.addProperty("projectId", manPower.project.id);
                object.addProperty("title", manPower.name);
                obj.add(object);
            }
            renderJSON(obj);
        }
    }

    public static void equipmentList(String body) {
        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            JsonArray obj = new JsonArray();
            List<Equipment> equipments = Equipment.findAll();
            for (Equipment equipment : equipments) {
                JsonObject object = new JsonObject();
                object.addProperty("id", equipment.id);
                object.addProperty("projectId", equipment.project.id);
                object.addProperty("title", equipment.name);
                obj.add(object);
            }
            renderJSON(obj);
        }
    }

    public static void materialList(String body) {
        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            JsonArray obj = new JsonArray();
            List<Inventory> inventories = Inventory.findAll();
            for (Inventory inventory : inventories) {
                JsonObject object = new JsonObject();
                object.addProperty("id", inventory.id);
                object.addProperty("title", inventory.item);
                object.addProperty("groupId", inventory.inventorySubCategory.category.id);
                object.addProperty("unit", inventory.inventoryMeasure.measure);
                obj.add(object);
            }
            renderJSON(obj);
        }
    }

    public static void weatherConditionList(String body) {
        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            JsonArray obj = new JsonArray();
            List<WeatherCondition> conditions = WeatherCondition.findAll();
            for (WeatherCondition condition : conditions) {
                JsonObject object = new JsonObject();
                object.addProperty("id", condition.id);
                object.addProperty("title", condition.conditionWeather);
                obj.add(object);
            }
            renderJSON(obj);
        }
    }

    public static void serverTime(String body) {
        if (Consts.defugLog) System.out.println("AppDailyLog serverTime body: " + body);
        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            JsonObject jsonObject = new JsonObject();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            Date date = new Date();
            jsonObject.addProperty("time", dateFormat.format(date));
            if (Consts.defugLog) System.out.println("replay: " + jsonObject);
            renderJSON(jsonObject);
        }
    }

}
