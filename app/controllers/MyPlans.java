package controllers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import controllers.ReportClass.ReportFunction;
import models.*;
import play.mvc.With;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Enkh-Amgalan on 3/9/15.
 */
@With(Secure.class)
@Check(Consts.permissionMyPlan)
public class MyPlans extends CRUD {
    public static void blank() {
        Date today = new Date();
        today = Functions.convertHourNull(today);
        Long tod = today.getTime();
        render(tod);
    }

    public static void workersPlan() {
        User user = Users.getUser();
        if (user.getUserPermissionType(Consts.permissionMyPlan) < 2) forbidden();
        render();
    }

    public static void savePlan(String data, String type) {
        DailyLogMyPlan unTask = null;
        JsonParser parser = new JsonParser();
        JsonElement eventElement = parser.parse(data);
        JsonObject eventObj = eventElement.getAsJsonObject();
        Date start = new Date();
        Date end = new Date();
        Date reminderDate = null;
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Date today = Functions.convertHourNull(new Date());
        Date tom = Functions.convertHourNull(Functions.addOrMinusDays(today, 1, true));
        if (type.equals("new")) {
            if (eventObj.get("id") == null || eventObj.get("id").getAsLong() == 0) {
                reminderDate = null;
                try {
                    start = dateFormat.parse(eventObj.get("start").getAsString());
                    end = dateFormat.parse(eventObj.get("end").getAsString());
                    if (!eventObj.get("reminderDate").getAsString().equals("")) {
                        reminderDate = new SimpleDateFormat("yyyy/MM/dd HH:mm").parse(eventObj.get("reminderDate").getAsString());
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                unTask = new DailyLogMyPlan(eventObj.get("taskName").getAsString(), eventObj.get("description").getAsString()
                        , start, end, 0.0f, Users.getUser());
                Long taskId = eventObj.get("taskRelId").getAsLong();
                if (taskId > 0)
                    unTask.taskRel = Task.findById(taskId);
                unTask.create();
                if (reminderDate != null) {
                    ReminderModel reminderModel = new ReminderModel();
                    reminderModel.reminderDate = reminderDate;
                    reminderModel.myPlan = unTask;
                    reminderModel.mainType = "task";
                    reminderModel.title = unTask.name;
                    reminderModel.reminderUsers = new ArrayList<ReminderUser>();
                    reminderModel.addUser(Users.getUser());
                    reminderModel.create();
                }
            } else {
                unTask = DailyLogMyPlan.findById(eventObj.get("id").getAsLong());
                reminderDate = null;
                try {
                    start = dateFormat.parse(eventObj.get("start").getAsString());
                    end = dateFormat.parse(eventObj.get("end").getAsString());
                    if (!eventObj.get("reminderDate").getAsString().equals("")) {
                        reminderDate = new SimpleDateFormat("yyyy/MM/dd HH:mm").parse(eventObj.get("reminderDate").getAsString());
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                unTask.name = eventObj.get("taskName").getAsString();
                unTask.startDate = start;
                unTask.description = eventObj.get("description").getAsString();
                unTask.finishDate = end;
                unTask.actualFinish = end;
                Long taskId = eventObj.get("taskRelId").getAsLong();
                if (taskId > 0)
                    unTask.taskRel = Task.findById(taskId);
                unTask._save();
                if (reminderDate != null) {
                    if (unTask.reminderModel == null) {
                        ReminderModel reminderModel = new ReminderModel();
                        reminderModel.mainType = "task";
                        reminderModel.title = unTask.description;
                        reminderModel.reminderDate = reminderDate;
                        reminderModel.myPlan = unTask;
                        reminderModel.reminderUsers = new ArrayList<ReminderUser>();
                        reminderModel.addUser(Users.getUser());
                        reminderModel.create();
                    }
                } else if (unTask.reminderModel != null) unTask.reminderModel._delete();
            }
        } else if (type.equals("delete")) {
            unTask = DailyLogMyPlan.findById(eventObj.get("id").getAsLong());
            unTask._delete();
        }
        renderText(unTask.id);
    }

    public static void eventSources(String start, String end) {
        Date startDate = new Date();
        Date endDate = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            startDate = dateFormat.parse(start);
            endDate = dateFormat.parse(end);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        List<DailyLogMyPlan> unTasks = DailyLogMyPlan.find("((startDate>=?1 and startDate<?2) or (actualFinish>=?3 and actualFinish<?4)) and owner.id=?5",
                startDate, endDate, startDate, endDate, Users.getUser().id).fetch();
        JsonArray eventsArray = new JsonArray();
        JsonObject eventObj;
        DateFormat dateEventFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        for (DailyLogMyPlan unTask : unTasks) {
            eventObj = new JsonObject();
            eventObj.addProperty("type", "myTask");
            eventObj.addProperty("taskId", unTask.id);
            eventObj.addProperty("id", unTask.id);
            eventObj.addProperty("title", unTask.name + " (" + unTask.completedPercent + "%)");
            eventObj.addProperty("taskName", unTask.name);
            eventObj.addProperty("taskRelId", (unTask.taskRel != null) ? unTask.taskRel.id : -1);
            eventObj.addProperty("taskRelName", (unTask.taskRel != null) ? unTask.taskRel.name : "");
            eventObj.addProperty("start", dateEventFormat.format(unTask.startDate));
            eventObj.addProperty("end", dateEventFormat.format(unTask.actualFinish));
            if (unTask.completedPercent == 100L || unTask.finishDate.before(new Date()))
                eventObj.addProperty("delay", Functions.getDifferenceWorkDays(null, unTask.finishDate, unTask.actualFinish));
            else eventObj.addProperty("delay", "");
            if (unTask.reminderModel != null) {
                eventObj.addProperty("reminderDate", new SimpleDateFormat("yyyy/MM/dd HH:mm").format(unTask.reminderModel.reminderDate));
            } else
                eventObj.addProperty("reminderDate", "");
            if (unTask.completedPercent == 100) {
                eventObj.addProperty("className", "bg-green");
                eventObj.addProperty("editable", false);
            } else {
                eventObj.addProperty("className", "bg-bblue");
                eventObj.addProperty("editable", true);
            }
            if (Functions.getDifferenceWorkDays(null, unTask.startDate, unTask.actualFinish) >= 1)
                eventObj.addProperty("allDay", true);
            else eventObj.addProperty("allDay", false);
            eventObj.addProperty("description", unTask.description);
            eventsArray.add(eventObj);
        }
        List<TaskAssignRel> ganttTasks = ReportFunction.assignedTask(Users.getUser().id, startDate, endDate);
        for (TaskAssignRel ganttTask : ganttTasks) {
            eventObj = new JsonObject();
            eventObj.addProperty("type", "ganttTask");
            eventObj.addProperty("ganttTaskId", ganttTask.task.id);
            eventObj.addProperty("title", ganttTask.task.name + " (" + ganttTask.task.completedPercent + "%)");
            eventObj.addProperty("start", dateEventFormat.format(ganttTask.task.startDate));
            eventObj.addProperty("end", dateEventFormat.format(Functions.addOrMinusDays(Functions.convertHourNull(ganttTask.task.actualFinish), 1, true)));
            if (ganttTask.task.completedPercent == 100)
                eventObj.addProperty("className", "bg-green");
            else
                eventObj.addProperty("className", "bg-blue");
            if (ganttTask.task.reminderModel != null) {
                eventObj.addProperty("reminderDate", new SimpleDateFormat("yyyy/MM/dd HH:mm").format(ganttTask.task.reminderModel.reminderDate));
                eventObj.addProperty("reminderTitle", ganttTask.task.reminderModel.title);
            } else
                eventObj.addProperty("reminderDate", "");
            if (ganttTask.task.completedPercent == 100L || ganttTask.task.finishDate.before(new Date()))
                eventObj.addProperty("delay", Functions.getDifferenceWorkDays(ganttTask.task.projectObject.project, ganttTask.task.finishDate, ganttTask.task.actualFinish));
            else eventObj.addProperty("delay", "");
            Task curTask = ganttTask.task.task;
            String taskNames = "";
            while (curTask != null) {
                taskNames = " > " + curTask.name + taskNames;
                curTask = curTask.task;
            }
            eventObj.addProperty("description", ganttTask.task.projectObject.project.name + " > " + ganttTask.task.projectObject.name + taskNames + " > " + ganttTask.task.name);
            eventObj.addProperty("allDay", true);
            eventObj.addProperty("editable", false);
            eventsArray.add(eventObj);
        }
        List<Meeting> meetings = Meeting.find("meetingDate >=?1 and meetingDate<?2", startDate, endDate).fetch();
        for (Meeting meeting : meetings)
            for (MeetingUserRel userRel : meeting.meetingUserRels)
                if (userRel.user.id == Users.getUser().id) {
                    eventObj = new JsonObject();
                    eventObj.addProperty("title", meeting.title);
                    eventObj.addProperty("start", dateEventFormat.format(meeting.meetingDate));
                    eventObj.addProperty("end", dateEventFormat.format(meeting.finishDate));
                    eventObj.addProperty("className", "bg-purple");
                    eventObj.addProperty("meetingId", meeting.id);
                    eventObj.addProperty("description", meeting.title);
                    eventObj.addProperty("allDay", false);
                    eventObj.addProperty("editable", false);
                    eventsArray.add(eventObj);
                }
        List<DailyLogWeather> weathers = DailyLogWeather.find("date >=?1 AND date<?2 AND owner.id=?3 order by date", startDate, endDate, Users.getUser().id).fetch();
        Date currentDate;
        if (!weathers.isEmpty() && weathers.size() > 0) {
            currentDate = weathers.get(0).date;
            eventObj = new JsonObject();
            eventObj.addProperty("title", "Цаг агаарын саатал");
            eventObj.addProperty("start", dateEventFormat.format(currentDate));
            eventObj.addProperty("end", dateEventFormat.format(Functions.addOrMinusDays(currentDate, 1, true)));
            eventObj.addProperty("className", "bg-red");
            eventObj.addProperty("description", weathers.get(0).notes);
            eventObj.addProperty("date", weathers.get(0).date.getTime());
            eventObj.addProperty("isWeather", 1);
            eventObj.addProperty("allDay", true);
            eventObj.addProperty("editable", false);
            eventsArray.add(eventObj);
            for (DailyLogWeather weather : weathers) {
                if (currentDate.compareTo(weather.date) != 0) {
                    currentDate = weather.date;
                    eventObj = new JsonObject();
                    eventObj.addProperty("title", "Цаг агаарын саатал");
                    eventObj.addProperty("start", dateEventFormat.format(currentDate));
                    eventObj.addProperty("end", dateEventFormat.format(Functions.addOrMinusDays(currentDate, 1, true)));
                    eventObj.addProperty("className", "bg-red");
                    eventObj.addProperty("description", weather.notes);
                    eventObj.addProperty("date", weather.date.getTime());
                    eventObj.addProperty("isWeather", 1);
                    eventObj.addProperty("allDay", true);
                    eventObj.addProperty("editable", false);
                    eventsArray.add(eventObj);
                }
            }
        }
        List<DailyLogTechnicalDelay> delays = DailyLogTechnicalDelay.find("date >=?1 AND date<?2 AND owner=?3", startDate, endDate, Users.getUser()).fetch();
        if (!delays.isEmpty() && delays.size() > 0) {
            currentDate = delays.get(0).date;
            eventObj = new JsonObject();
            eventObj.addProperty("title", "Техникийн саатал");
            eventObj.addProperty("start", dateEventFormat.format(currentDate));
            eventObj.addProperty("end", dateEventFormat.format(Functions.addOrMinusDays(currentDate, 1, true)));
            eventObj.addProperty("className", "bg-red");
            eventObj.addProperty("description", delays.get(0).notes);
            eventObj.addProperty("date", delays.get(0).date.getTime());
            eventObj.addProperty("isWeather", 0);
            eventObj.addProperty("allDay", true);
            eventObj.addProperty("editable", false);
            eventsArray.add(eventObj);
            for (DailyLogTechnicalDelay delay : delays) {
                if (currentDate.compareTo(delay.date) != 0) {
                    currentDate = delay.date;
                    eventObj = new JsonObject();
                    eventObj.addProperty("title", "Техникийн саатал");
                    eventObj.addProperty("start", dateEventFormat.format(currentDate));
                    eventObj.addProperty("end", dateEventFormat.format(Functions.addOrMinusDays(currentDate, 1, true)));
                    eventObj.addProperty("className", "bg-red");
                    eventObj.addProperty("description", delay.notes);
                    eventObj.addProperty("date", delay.date.getTime());
                    eventObj.addProperty("isWeather", 0);
                    eventObj.addProperty("allDay", true);
                    eventObj.addProperty("editable", false);
                    eventsArray.add(eventObj);
                }
            }
        }
        renderJSON(eventsArray);
    }

    public static void resourcesWorker(String[] userIds) {
        String qr = "";
        List<UserTeam> userTeams = new ArrayList<UserTeam>();
        User user = Users.getUser();
        int permission = user.getUserPermissionType(Consts.permissionMyPlan);
        if (permission == 2) {
            qr = "userTeam.id=" + user.userTeam.id;
        } else if (userIds != null) {
            qr = "";
            for (String userId : userIds) {
                if (userId.startsWith("u-")) {
                    qr += " id=" + userId.substring(2) + " OR";
                } else if (userId.startsWith("t-")) {
                    UserTeam userTeam = UserTeam.find("id=?1", Long.parseLong(userId.substring(2))).first();
                    for (User currentUser : userTeam.users)
                        qr += " id=" + currentUser.id + " OR";
                }
            }
            qr = qr.substring(0, qr.length() - 3);
        }
        List<User> users = User.find(qr).fetch();
        JsonArray resourcesArray = new JsonArray();
        JsonObject resourceObj;
        JsonObject userObj;
        for (User userchirld : users) {
            userObj = new JsonObject();
            userObj.addProperty("id", userchirld.id);
            userObj.addProperty("title", userchirld.toString());
            userObj.addProperty("position", userchirld.userPosition.name);
            userObj.addProperty("parentId", "t" + userchirld.userTeam.id);
            resourcesArray.add(userObj);
            if (!userTeams.contains(userchirld.userTeam)) {
                userTeams.add(userchirld.userTeam);
            }
        }
        for (UserTeam userTeam : userTeams) {
            resourceObj = new JsonObject();
            resourceObj.addProperty("id", "t" + userTeam.id);
            resourceObj.addProperty("title", userTeam.name);
            resourcesArray.add(resourceObj);
        }
        renderJSON(resourcesArray);
    }

    public static void eventSourcesWorker(String start, String end, String[] userIds) {
        String qr = "", qr2 = "";
        User user = Users.getUser();
        int permission = user.getUserPermissionType(Consts.permissionMyPlan);
        if (permission < 2) forbidden();
        else if (permission == 2) {
            qr = " AND owner.userTeam.id=" + user.userTeam.id;
            qr2 = " AND user.userTeam.id=" + user.userTeam.id;
        } else if (userIds != null) {
            qr = " AND (";
            for (String userId : userIds) {
                if (userId.startsWith("u-")) {
                    qr += " owner.id=" + userId.substring(2) + " OR";
                } else if (userId.startsWith("t-")) {
                    UserTeam userTeam = UserTeam.find("id=?1", Long.parseLong(userId.substring(2))).first();
                    for (User currentUser : userTeam.users)
                        qr += " owner.id=" + currentUser.id + " OR";
                }
            }
            qr = qr.substring(0, qr.length() - 3) + " ) ";
            qr2 = qr.replace("owner", "user");
        }
        Date startDate = new Date();
        Date endDate = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            startDate = dateFormat.parse(start);
            endDate = dateFormat.parse(end);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        List<DailyLogMyPlan> unTasks = DailyLogMyPlan.find("((startDate>=?1 and startDate<?2) or (actualFinish>=?3 and actualFinish<?4))" + qr,
                startDate, endDate, startDate, endDate).fetch();
        JsonArray eventsArray = new JsonArray();
        JsonObject eventObj;
        DateFormat dateEventFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        DateFormat dateSimpleFormat = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat dateTimeFormat = new SimpleDateFormat("HH:mm");
        Calendar calendar = GregorianCalendar.getInstance();
        for (DailyLogMyPlan unTask : unTasks) {
            eventObj = new JsonObject();
            eventObj.addProperty("type", "myTask");
            eventObj.addProperty("id", unTask.id);
            eventObj.addProperty("title", unTask.name + " (" + unTask.completedPercent + "%)");
            eventObj.addProperty("taskName", unTask.name);
            eventObj.addProperty("taskRelId", (unTask.taskRel != null) ? unTask.taskRel.id : -1);
            eventObj.addProperty("taskRelName", (unTask.taskRel != null) ? unTask.taskRel.name : "");
            eventObj.addProperty("ganttTaskId", unTask.id);
            if ((unTask.actualFinish.getTime() - unTask.startDate.getTime()) / (24 * 60 * 60 * 1000) >= 1) {
                eventObj.addProperty("start", dateSimpleFormat.format(unTask.startDate));
                eventObj.addProperty("end", dateSimpleFormat.format(unTask.actualFinish));
            } else {
                eventObj.addProperty("start", dateEventFormat.format(unTask.startDate));
                eventObj.addProperty("end", dateEventFormat.format(unTask.actualFinish));
            }
            if (unTask.completedPercent == 100L || unTask.finishDate.before(new Date()))
                eventObj.addProperty("delay", Functions.getDifferenceWorkDays(null, unTask.finishDate, unTask.actualFinish));
            else eventObj.addProperty("delay", "");
            if (unTask.completedPercent == 100) {
                eventObj.addProperty("className", "bg-green");
                eventObj.addProperty("editable", false);
            } else {
                eventObj.addProperty("className", "bg-bblue");
                eventObj.addProperty("editable", false);
            }
            eventObj.addProperty("description", unTask.name);
            eventObj.addProperty("resourceId", unTask.owner.id);
            eventsArray.add(eventObj);
        }
        List<TaskAssignRel> ganttTasks = TaskAssignRel.find(
                "((task.startDate>=?1 AND task.startDate<?2) OR (task.actualFinish>=?1 AND task.actualFinish<?2) OR" +
                        " (task.startDate<=?1 AND task.actualFinish>?1) OR (task.startDate<=?2 AND task.actualFinish>?2)) " +
                " AND task.hasChild=false AND task.projectObject.project.portfolio.isActive=true " + qr2, startDate, endDate).fetch();
        Task task;
        for (TaskAssignRel ganttTask : ganttTasks) {
            task = ganttTask.task;
            eventObj = new JsonObject();
            eventObj.addProperty("type", "ganttTask");
            eventObj.addProperty("ganttTaskId", task.id);
            eventObj.addProperty("title", task.name + " (" + task.completedPercent + "%)");
            eventObj.addProperty("start", dateSimpleFormat.format(task.startDate));
            eventObj.addProperty("end", dateSimpleFormat.format(Functions.addOrMinusDays(Functions.convertHourNull(task.actualFinish), 1, true)));
            if (task.completedPercent == 100)
                eventObj.addProperty("className", "bg-green");
            else
                eventObj.addProperty("className", "bg-blue");
            if (task.completedPercent == 100L || task.finishDate.before(new Date()))
                eventObj.addProperty("delay", Functions.getDifferenceWorkDays(task.projectObject.project, task.finishDate, task.actualFinish));
            else eventObj.addProperty("delay", "");
            Task curTask = task.task;
            String taskNames = "";
            while (curTask != null) {
                taskNames = " > " + curTask.name + taskNames;
                curTask = curTask.task;
            }
            eventObj.addProperty("description", task.projectObject.project.name + " > " + task.projectObject.name + taskNames+" > "+task.name);
            eventObj.addProperty("resourceId", ganttTask.user.id);
            eventsArray.add(eventObj);
        }
        List<MeetingUserRel> meetingUserRels = MeetingUserRel.find("meeting.status.id!=4L AND meeting.meetingDate >=?1 AND meeting.meetingDate<?2" + qr2, startDate, endDate).fetch();
        for (MeetingUserRel rel : meetingUserRels) {
            Meeting meeting = rel.meeting;
            eventObj = new JsonObject();
            eventObj.addProperty("title", meeting.title);
            eventObj.addProperty("start", dateEventFormat.format(meeting.meetingDate));
            eventObj.addProperty("end", dateEventFormat.format(meeting.finishDate));
            eventObj.addProperty("className", "bg-purple");
            eventObj.addProperty("meetingId", meeting.id);
            eventObj.addProperty("description", dateTimeFormat.format(meeting.meetingDate)+" - "+dateTimeFormat.format(meeting.finishDate)+" "+meeting.title);
            eventObj.addProperty("resourceId", rel.user.id);
            eventsArray.add(eventObj);
        }

        List<DailyLogWeather> weathers = DailyLogWeather.find("date >=?1 AND date<?2" + qr, startDate, endDate).fetch();
        if (!weathers.isEmpty() && weathers.size() > 0) {
            for (DailyLogWeather weather : weathers) {
                eventObj = new JsonObject();
                eventObj.addProperty("title", "Цаг агаарын саатал");
                eventObj.addProperty("start", dateEventFormat.format(weather.startDate));
                eventObj.addProperty("end", dateEventFormat.format(weather.finishDate));
                eventObj.addProperty("className", "bg-red");
                eventObj.addProperty("description", dateTimeFormat.format(weather.startDate)+" - "+dateTimeFormat.format(weather.finishDate)+" Цаг агаарын саатал");
                eventObj.addProperty("date", weather.date.getTime());
                eventObj.addProperty("isWeather", 1);
                eventObj.addProperty("resourceId", weather.owner.id);
                eventsArray.add(eventObj);
            }
        }

        List<DailyLogTechnicalDelay> delays = DailyLogTechnicalDelay.find("date >=?1 AND date<?2" + qr, startDate, endDate).fetch();
        if (!delays.isEmpty() && delays.size() > 0) {
            for (DailyLogTechnicalDelay delay : delays) {
                eventObj = new JsonObject();
                eventObj.addProperty("title", "Техникийн саатал");
                eventObj.addProperty("start", dateEventFormat.format(delay.startDate));
                eventObj.addProperty("end", dateEventFormat.format(delay.finishDate));
                eventObj.addProperty("className", "bg-red");
                eventObj.addProperty("description", dateTimeFormat.format(delay.startDate)+" - "+dateTimeFormat.format(delay.finishDate)+" Техникийн саатал");
                eventObj.addProperty("date", delay.date.getTime());
                eventObj.addProperty("isWeather", 0);
                eventObj.addProperty("resourceId", delay.owner.id);
                eventsArray.add(eventObj);
            }
        }
        renderJSON(eventsArray);
    }

    public static void showWeatherModal(Long date, boolean isWeather) {
        Date date1 = new Date(date);
        if (isWeather) {
            List<DailyLogWeather> weathers = DailyLogWeather.find("owner.id=?1 AND date=?2", Users.getUser().id, date1).fetch();
            render(weathers, isWeather);
        } else {
            List<DailyLogTechnicalDelay> technicalDelays = DailyLogTechnicalDelay.find("owner.id=?1 AND date=?2", Users.getUser().id, date1).fetch();
            render(technicalDelays, isWeather);
        }
    }

    public static void showProjectTaskModal(Long projectTask, int type, String selectedDay) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date dateSelected = new Date();
        try {
            dateSelected = Functions.convertDayNull(dateFormat.parse(selectedDay));
        } catch (ParseException e) {
        }
        if (type == 2) showMyPlanTaskModal(projectTask, dateSelected);
        Task task = Task.findById(projectTask);
        int diffDays = Functions.getDifferenceWorkDays(task.projectObject.project, task.finishDate, task.actualFinish);
        render(task, diffDays, type, dateSelected);
    }

    public static void showMyPlanTaskModal(Long myPlanId, Date dateSelected) {
        DailyLogMyPlan task = DailyLogMyPlan.findById(myPlanId);
        int diffDays = Functions.getDifferenceWorkDays(null, task.finishDate, task.actualFinish);
        boolean oneDay =true;
        if ((task.finishDate.getTime() - task.startDate.getTime()) / (24 * 60 * 60 * 1000) >= 1)
            oneDay=false;
        render(task, diffDays, dateSelected,oneDay);
    }

    public static void reminderTask(Long taskId, String date, String title) {
        Task task = Task.findById(taskId);
        if (date.equals("") && date.isEmpty()) {
            if (task.reminderModel != null)
                task.reminderModel._delete();
        } else {
            Date reminderDate = null;
            try {
                reminderDate = new SimpleDateFormat("yyyy/MM/dd HH:mm").parse(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (task.reminderModel == null) {
                ReminderModel reminderModel = new ReminderModel();
                reminderModel.reminderDate = reminderDate;
                reminderModel.title = title;
                reminderModel.task = task;
                reminderModel.mainType = "ganttTask";
                reminderModel.reminderUsers = new ArrayList<ReminderUser>();
                reminderModel.addUser(Users.getUser());
                reminderModel.create();
            } else {
                task.reminderModel.reminderDate = reminderDate;
                task.reminderModel.title = title;
                task.reminderModel._save();
            }
        }
        renderText("Амжилттай хадгалагдлаа");
    }

    public static void gotoProjectPage(String type, Long id, String date, int ctype) {
        Project project = null;
        if (type.equals("Meeting")) {
            Meeting meeting = Meeting.findById(id);
            project = meeting.project;
        } else if (type.equals("Report")) {
            Task task = Task.findById(id);
            project = task.projectObject.project;
        } else if (type.equals("ReportMyPlan")) {
            User user = User.findById(id);
            ProjectAssignRel projectAssignRel = ProjectAssignRel.find("user.id=?", user.id).first();
            if (projectAssignRel != null) project = projectAssignRel.project;
        }
        if (project != null) {
            session.put("projectId", project.id);
            session.put("projectName", project.name);
            if (type.equals("Meeting")) Meetings.show(id);
            else if (type.equals("Report") || type.equals("ReportMyPlan")) Reports.reportMain(date, ctype, id);
        } else MyPlans.blank();
    }
}
