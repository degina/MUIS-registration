package controllers.App;

import com.google.gson.*;
import controllers.Consts;
import controllers.Functions;
import models.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Enkhbayar on 7/3/2015.
 */
public class DelegateTask extends Delegate {

    public static void taskList(String body) {
        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            Date currentDate = null;
            try {
                currentDate = dateFormat.parse(json.get("date").getAsString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            JsonArray eventsArray = new JsonArray();
            JsonObject eventObj;
            // suuliin neg sariin toloblogoog tatah eseh?     // grapick tolovlogoot ajiluud
            List<Task> tasks = Task.find("SELECT DISTINCT t FROM tb_task t LEFT JOIN t.taskAssignRels AS r WHERE t.hasChild=false AND t.id = r.task.id AND r.user.id=?1", user.id).fetch();
            Project project;
            ProjectObject projectObject;
            for (Task task : tasks) {
                eventObj = new JsonObject();
                eventObj.addProperty("type", "ganttTask");
                eventObj.addProperty("id", task.id);
                eventObj.addProperty("taskRelId", -1l);
                eventObj.addProperty("taskRelName", "");
                eventObj.addProperty("title", task.name);
                eventObj.addProperty("start", Consts.myDateFormat2.format(task.startDate));
                eventObj.addProperty("end", Consts.myDateFormat2.format(task.finishDate));
                eventObj.addProperty("actualFinish", Consts.myDateFormat2.format(task.actualFinish));
                eventObj.addProperty("completedPercent", Functions.getFloatFormat(task.completedPercent, 2));
                if (task.reminderModel != null) {
                    eventObj.addProperty("reminderDate", Consts.myDateFormat2.format(task.reminderModel.reminderDate));
                    eventObj.addProperty("rdTitle", task.reminderModel.title);
                } else {
                    eventObj.addProperty("reminderDate", "");
                    eventObj.addProperty("rdTitle", "");
                }
                Task curTask = task.task;
                String taskNames = "";
                while (curTask != null) {
                    taskNames += " > " + curTask.name + taskNames;
                    curTask = curTask.task;
                }
                projectObject = task.projectObject;
                project = projectObject.project;
                eventObj.addProperty("projectId", project.id);
                eventObj.addProperty("mainJobId", projectObject.id);
                eventObj.addProperty("resource", project.name + " > " + projectObject.name + taskNames);

                String assignUsers = "";
                for (TaskAssignRel taskAssignRel : task.taskAssignRels)
                    assignUsers += taskAssignRel.user.id + ",";
                eventObj.addProperty("assignUsers", assignUsers);
                eventsArray.add(eventObj);
            }
            // suuliin neg sariin toloblogoog tatah eseh?  // huviin tolovlogoot ajiluud
            List<DailyLogMyPlan> unTasks = DailyLogMyPlan.find("owner.id=?1", user.id).fetch();
            for (DailyLogMyPlan unTask : unTasks) {
                eventObj = new JsonObject();
                eventObj.addProperty("type", "myPlan");
                eventObj.addProperty("id", unTask.id);
                eventObj.addProperty("title", unTask.name);
                eventObj.addProperty("taskRelId", (unTask.taskRel != null) ? unTask.taskRel.id : -1);
                eventObj.addProperty("taskRelName", (unTask.taskRel != null) ? unTask.taskRel.name : "");
                eventObj.addProperty("start", Consts.myDateFormat2.format(unTask.startDate));
                eventObj.addProperty("end", Consts.myDateFormat2.format(unTask.finishDate));
                eventObj.addProperty("actualFinish", Consts.myDateFormat2.format(unTask.actualFinish));
                eventObj.addProperty("completedPercent", unTask.completedPercent);
                if (unTask.reminderModel != null) {
                    eventObj.addProperty("reminderDate", Consts.myDateFormat2.format(unTask.reminderModel.reminderDate));
                    eventObj.addProperty("rdTitle", unTask.description);
                } else {
                    eventObj.addProperty("reminderDate", "");
                    eventObj.addProperty("rdTitle", unTask.description);
                }
                eventObj.addProperty("projectId", 0);
                eventObj.addProperty("mainJobId", 0);
                eventObj.addProperty("resource", "Хувийн төлөвлөгөөт");
                eventObj.addProperty("assignUsers", "");
                eventsArray.add(eventObj);
            }
            renderJSON(eventsArray);
        }
    }

    public static void myPlanNew(String body) {
        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        if (user != null) {
            JsonObject myPlanObj = json.get("MyPlan").getAsJsonObject();
            Long myPlanId = myPlanObj.get("myPlanId").getAsLong();
            JsonObject responseObj = new JsonObject();
            DailyLogMyPlan myPlan;
            Date startDate = new Date();
            Date endDate = startDate;
            Date reminderDate = startDate;
            try {
                startDate = dateFormat.parse(myPlanObj.get("startDate").getAsString());
                endDate = dateFormat.parse(myPlanObj.get("endDate").getAsString());
                if (!myPlanObj.get("reminderDate").getAsString().equalsIgnoreCase(""))
                    reminderDate = dateFormat.parse(myPlanObj.get("reminderDate").getAsString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Long taskId = myPlanObj.get("taskRelId").getAsLong();
            if (myPlanId == 0) {
                myPlan = new DailyLogMyPlan(myPlanObj.get("myPlanName").getAsString()
                        , myPlanObj.get("description").getAsString()
                        , startDate, endDate, 0.0f, user);
                if (taskId != null && taskId > 0)
                    myPlan.taskRel = Task.findById(taskId);
                myPlan.create();
            } else {
                myPlan = DailyLogMyPlan.findById(myPlanId);
                myPlan.name = myPlanObj.get("myPlanName").getAsString();
                myPlan.description = myPlanObj.get("description").getAsString();
                myPlan.startDate = startDate;
                myPlan.finishDate = endDate;
                myPlan.actualFinish = endDate;
                if (taskId > 0)
                    myPlan.taskRel = Task.findById(taskId);
                myPlan._save();
            }
            responseObj.addProperty("newId", myPlan.id);
            renderJSON(responseObj);
        }
    }

    public static void projectList(String body) {
        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            JsonArray obj = new JsonArray();
            JsonObject jsonObject = new JsonObject();
            List<ProjectObAssignRel> assignRels = ProjectObAssignRel.find("user.id=?1 order by projectObject.startDate", user.id).fetch();
            for (ProjectObAssignRel rel : assignRels) {
                jsonObject = new JsonObject();
                jsonObject.addProperty("id", rel.projectObject.id);
                jsonObject.addProperty("title", rel.projectObject.project.name + " > " + rel.projectObject.name);
                jsonObject.addProperty("projectId", rel.projectObject.project.id);
                obj.add(jsonObject);
            }
            jsonObject = new JsonObject();
            jsonObject.addProperty("id", 0);
            jsonObject.addProperty("title", "Хувийн төлөвлөгөөт");
            jsonObject.addProperty("projectId", 0);
            obj.add(jsonObject);
            renderJSON(obj);
        }
    }

    public static void taskListUser(String body) {
        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            JsonObject response = new JsonObject();
            JsonArray obj = new JsonArray();
            JsonObject jsonObject;
            List<TaskAssignRel> assignRels = TaskAssignRel.find("task.hasChild=false order by task.startDate").fetch();
            for (TaskAssignRel rel : assignRels) {
                jsonObject = new JsonObject();
                jsonObject.addProperty("id", rel.id);
                jsonObject.addProperty("jobSubId", rel.task.id);
                jsonObject.addProperty("userId", rel.user.id);
                obj.add(jsonObject);
            }
            response.add("subJobToUsers", obj);
            obj = new JsonArray();
            List<Task> tasks = Task.find("hasChild=false").fetch();
            Task curTask;
            for (Task task : tasks) {
                String taskNames = "";
                curTask = task.task;
                while (curTask != null) {
                    taskNames += " > " + curTask.name + taskNames;
                    curTask = curTask.task;
                }
                curTask = null;
                jsonObject = new JsonObject();
                jsonObject.addProperty("id", task.id);
                jsonObject.addProperty("title", task.name);
                jsonObject.addProperty("mainJobId", task.projectObject.id);
                jsonObject.addProperty("mainJobName", task.projectObject.name + taskNames);
                jsonObject.addProperty("projectId", task.projectObject.project.id);
                obj.add(jsonObject);
            }
            List<DailyLogMyPlan> myPlans = DailyLogMyPlan.find("owner.id=?1 order by startDate", user.id).fetch();
            for (DailyLogMyPlan rel : myPlans) {
                jsonObject = new JsonObject();
                jsonObject.addProperty("id", rel.id);
                jsonObject.addProperty("title", rel.name);
                jsonObject.addProperty("mainJobId", 0);
                jsonObject.addProperty("mainJobName", "Хувийн төлөвлөгөөт");
                jsonObject.addProperty("projectId", 0);
                obj.add(jsonObject);
            }
            response.add("subJobs", obj);

            obj = new JsonArray();
            List<ProjectObject> projectObjects = ProjectObject.find("order by startDate ").fetch();
            for (ProjectObject projectObject : projectObjects) {
                jsonObject = new JsonObject();
                jsonObject.addProperty("id", projectObject.id);
                jsonObject.addProperty("title", projectObject.project.name + " > " + projectObject.name);
                jsonObject.addProperty("projectId", projectObject.project.id);
                obj.add(jsonObject);
            }
            jsonObject = new JsonObject();
            jsonObject.addProperty("id", 0);
            jsonObject.addProperty("title", "Хувийн төлөвлөгөөт");
            jsonObject.addProperty("projectId", 0);
            obj.add(jsonObject);
            response.add("mainJobs", obj);
            renderJSON(response);
        }
    }
}
