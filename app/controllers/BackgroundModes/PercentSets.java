package controllers.BackgroundModes;

import controllers.Functions;
import controllers.UserLiveRoom;
import controllers.Users;
import models.*;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.On;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by enkhamgalan on 5/1/15.
 */
@On("0 55 23 * * ?")
public class PercentSets extends Job {
    public void doJob() {
        Date today = Functions.convertHourNull(new Date());
        Date lastSecondDate = Functions.convertDayLastSecond(today);
        List<Project> projects = Project.find("completedPercent<=100").fetch();
        for (Project project : projects) {
            PercentHistoryProject historyProject = new PercentHistoryProject();
            historyProject.date = today;
            historyProject.project = project;
            historyProject.timeType = 2;
            historyProject.completedPercent = project.completedPercent;
            historyProject.create();
            if (project.actualFinish == null || project.actualFinish.before(lastSecondDate)) {
                project.actualFinish = lastSecondDate;
                project._save();
            }
        }
        List<ProjectObject> projectObjects = ProjectObject.find("completedPercent<=100").fetch();
        for (ProjectObject object : projectObjects) {
            PercentHistoryObject historyObject = new PercentHistoryObject();
            historyObject.date = today;
            historyObject.projectObject = object;
            historyObject.timeType = 2;
            historyObject.completedPercent = object.completedPercent;
            historyObject.create();
            if (object.actualFinish == null || object.actualFinish.before(lastSecondDate)) {
                object.actualFinish = lastSecondDate;
                object._save();
            }
        }
        List<Task> tasks = Task.find("completedPercent<=100").fetch();
        for (Task task : tasks) {
            PercentHistoryTask historyTask = new PercentHistoryTask();
            historyTask.date = today;
            historyTask.task = task;
            historyTask.timeType = 2;
            historyTask.completedPercent = task.completedPercent;
            historyTask.create();
        }
        // миний төлөвлөгөөний таск бодит дуусах өдөр буюу хоцолт бодох
        Date afterTomorrow = Functions.addOrMinusDays(today, 2, true);
        System.out.println("check afterTomorrow: " + afterTomorrow);
        List<DailyLogMyPlan> myPlans = DailyLogMyPlan.find("completedPercent <100F AND finishDate <?1 AND actualFinish <?2", afterTomorrow, afterTomorrow).fetch();
        for (DailyLogMyPlan myPlan : myPlans) {
            System.out.println("actualFinish day: " + myPlan.actualFinish);
            myPlan.actualFinish = afterTomorrow;
            myPlan._save();
        }
        // Гэрээ сануулах хэсэг
        List<ReminderModel> reminders = ReminderModel.find("mainType = 'contract' AND reminderDate <= ?1", today).fetch();
        for (ReminderModel reminder : reminders) {
//            System.out.println("reminder:" + reminder.title);
            UserLiveRoom.get().reminderDialog(reminder);
        }
        // гант чартын таск бодит дуусах өдөр буюу хоцолт бодох
        Date tomorrow = Functions.addOrMinusDays(today, 1, true);
        List<Task> taskDelays = Task.find("completedPercent <100F AND finishDate <?1 AND actualFinish <?2", tomorrow, tomorrow).fetch();
        if (taskDelays.size() > 0) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(tomorrow);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            tomorrow = cal.getTime();
            for (Task task : taskDelays) {
                System.out.println("actualFinish day: " + task.actualFinish);
                task.actualFinish = tomorrow;
                task._save();
            }
        }
        // төрсөн өдөр нь болж байгаа хүнд баяр хүргэх
        List<User> userBirt = User.find("birthday >?1 AND birthday <?2", today, afterTomorrow).fetch();
        for (User userb : userBirt) {
            Post post = new Post();
            post.owner = userb;
            post.seeAll = true;
            post.content = "";
            post.type = "Төрсөн өдрийн мэнд хүргье!!!";
            post.typeIconName = "birthday.png";
            post.create();
            PostAttach attach = new PostAttach("happy-birthday", "/public/images/image/happy-birthday", "jpg",null, post);
            attach.create();
            post.createdDate = userb.birthday;
            post._save();
        }
    }
}
