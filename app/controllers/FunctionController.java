package controllers;

import com.google.gson.JsonArray;
import models.*;
import play.Play;
import play.mvc.Controller;
import play.mvc.Http;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class FunctionController extends Controller {

    private static String controllerName = "FunctionController";

    public static List<User> getUsers() {
        return User.find("active=true ORDER BY userTeam.queue, userTeam.name, userPosition.rate, firstName").fetch();
    }
    public static List<UserTeam> getTeams() {
        return UserTeam.find("contractor=false ORDER BY queue, name").fetch();
    }
    public static List<OrganizationChart> getOrganizationUsers() {
        return OrganizationChart.find("project.id=?1 ORDER BY team.id, team.name, userPosition.rate, user.firstName", Users.pid()).fetch();
//        return User.find("SELECT DISTINCT t FROM tb_user t LEFT JOIN t.organizationCharts AS r WHERE r.user.id = t.id AND r.project.id=?1 ORDER BY t.userTeam.queue, t.userTeam.name, t.userPosition.rate, t.firstName", Users.pid()).fetch();
    }
    public static List<OrganizationTeam> getOrganizationTeams() {
        return OrganizationTeam.find("project.id=?1 ORDER BY name", Users.pid()).fetch();
//        return User.find("SELECT DISTINCT t FROM tb_user t LEFT JOIN t.organizationCharts AS r WHERE r.user.id = t.id AND r.project.id=?1 ORDER BY t.userTeam.queue, t.userTeam.name, t.userPosition.rate, t.firstName", Users.pid()).fetch();
    }

    public static void getSelectorInfo(Long id, int type) {
        if (type == 1) {
            OrganizationChart organizationChart = OrganizationChart.find("user.id=?1 AND project.id=?2", id, Users.pid()).first();
            render(controllerName + "/getSelectorInfo.html", organizationChart, type);
        }
    }

    public static void getUserInfo(String username, Long uid) {
        User us;
        if (uid != null) us = User.findById(uid);
        else {
            username = username.replace(" ", "");
            us = User.find("username=?1", username).first();
        }
        render(controllerName + "/userInfo.html", us);
    }

    public static void calendarList(String dateVal, int action) {
        String cals[] = Functions.CalendarList(dateVal, action);
        String day = cals[0];
        String month = cals[1];
        String year = cals[2];
        String dom = cals[3];
        String mprev = cals[4];
        String mnow = cals[5];
        String mnext = cals[6];
        render(day, month, year, dom, mprev, mnow, mnext);
    }

    public static void downloadFile(String fileDir, String fileName, String extension) throws IOException, GeneralSecurityException {
        String downloadUrl = fileDir + "." + extension;
        java.io.File file = new java.io.File(Play.applicationPath.getAbsoluteFile() + downloadUrl);
        Http.Response.current().contentType = "application/octet-stream";
        try {
            String des = fileName + "." + extension;
            renderBinary(new FileInputStream(file), des, "application/octet-stream", false);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            notFound();
        }
    }

    public static void getMyTaskRels(int type, String uid, Long tid, String percent) {
        if (percent == null || percent.length() == 0) percent = "100";
        if (type == 0) {
            List<ProjectObAssignRel> assignRels = ProjectObAssignRel.find("user.id=?1 and projectObject.project.id=?2 and projectObject.completedPercent<?3 order by projectObject.startDate", Long.parseLong(uid), tid, Float.parseFloat(percent)).fetch();
            render(assignRels, type, tid, uid, percent);
        } else {
            List<TaskAssignRel> assignRels = TaskAssignRel.find("user.id=?1 and task.projectObject.id=?2 and task.completedPercent<?3 order by task.startDate", Long.parseLong(uid), tid, Float.parseFloat(percent)).fetch();
            render(assignRels, type, tid, uid, percent);
        }
    }

    public static void selectTasksOpen(String uid, String percent) {
        List<ProjectObAssignRel> projectObRels = TaskUsers.getMyProjectObRels(uid, percent);
        render(projectObRels, uid, percent);
    }

    public static void getTaskUserObjects(String stype, int otype, Long oid) {
        if (oid == null) oid = 0L;
        if (stype.equals("Task")) {
            if (otype == 0) {
                List<Project> objects = Project.findAll();
                render(objects, stype, otype, oid);
            } else if (otype == 1) {
                List<ProjectObject> objects = ProjectObject.find("project.id=?1", oid).fetch();
                render(objects, stype, otype, oid);
            } else if (otype == 2) {
                List<Task> objects = Task.find("projectObject.id=?1", oid).fetch();
                render(objects, stype, otype, oid);
            } else if (otype == 3) {
                List<Task> objects = Task.find("task.id=?1", oid).fetch();
                render(objects, stype, otype, oid);
            }
        } else {
            if (otype == 0) {
                List<UserTeam> objects = UserTeam.find("order by queue, name").fetch();
                render(objects, stype, otype, oid);
            } else if (otype == 1) {
                List<User> objects = User.find("userTeam.id=?1 order by userPosition.rate, firstName", oid).fetch();
                render(objects, stype, otype, oid);
            }
        }
    }
}
