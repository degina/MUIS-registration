package controllers;

import models.*;
import play.mvc.With;

import java.util.Date;
import java.util.List;

/**
 * Created by enkhamgalan on 2/20/15.
 */
@With(Secure.class)
public class TaskUsers {
    public static List<ProjectAssignRel> getMyProjectRels() {
        User us = Users.getUser();
        return ProjectAssignRel.find("user.id=?1 and project.completedPercent<?2 order by project.startDate", us.id, 100F).fetch();
    }

    public static List<ProjectObAssignRel> getMyProjectObRels(String uid, String percent) {
        if (uid == null || uid.length() == 0) {
            User us = Users.getUser();
            uid = us.id + "";
        }
        if (percent == null || percent.length() == 0) percent = "100";
        return ProjectObAssignRel.find("user.id=?1 and projectObject.project.id=?2 and projectObject.completedPercent<?3 order by projectObject.startDate", Long.parseLong(uid), Users.pid(), Float.parseFloat(percent)).fetch();
    }

    public static List<TaskAssignRel> getMyTaskRels() {
        User us = Users.getUser();
        return TaskAssignRel.find("user.id=?1 and task.projectObject.project.id=?2 and task.completedPercent<?3 order by task.startDate", us.id, Users.pid(), 100F).fetch();
    }

    public static List<TaskAssignRel> getMyTaskRels(Date date) {
        User us = Users.getUser();
        return TaskAssignRel.find("user.id=?1 and task.completedPercent<?2 and" +
                " task.startDate<=?3 order by task.startDate", us.id, 100F, date).fetch();
    }

    public static List<Task> getMyStartedTasks(Date date) {
        // return Task.find("SELECT t FROM tb_task t, tb_task_assign_rel r, tb_dailyLog_scheduledWork s WHERE r.task = t AND r.user.id=?1 AND t.startDate<=?2 AND (t.completedPercent<100F OR (s.task = t AND s.date=?2)) ORDER BY t.startDate", Users.getUser().id,date).fetch();
        return Task.find("SELECT DISTINCT t FROM tb_task t LEFT JOIN t.taskAssignRels AS r LEFT JOIN t.dailyLogScheduledWorks AS s " +
                "WHERE t.hasChild=false AND r.task.id = t.id AND t.startDate<=?1 AND r.user.id=?2 AND (t.completedPercent<100F OR (s.task.id = t.id AND s.date=?1)) ORDER BY t.startDate", date, Users.getUser().id).fetch();
    }

    public static List<DailyLogMyPlan> getMyUnScheduledTask(Date date) {
        User us = Users.getUser();
        Date nextDay = Functions.addOrMinusDays(date, 1, true);
        // return DailyLogMyPlan.find("owner.id=?1 and completedPercent<100F and startDate<?2 order by startDate", us.id, date).fetch();
        return DailyLogMyPlan.find("SELECT DISTINCT m FROM tb_dailylog_myplan m LEFT JOIN m.logScheduledWorks AS s WHERE m.owner.id=?1 AND m.startDate<?2 AND ( m.completedPercent<100f OR (s.myPlan.id = m.id AND s.date=?3))", us.id, nextDay, date).fetch();
    }
}
