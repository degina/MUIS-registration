package controllers.BackgroundModes;

import controllers.UserLiveRoom;
import models.MeetingUserRel;
import models.ReminderModel;
import models.User;
import play.jobs.Every;
import play.jobs.Job;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Enkh-Amgalan on 3/28/15.
 */
@Every("1mn")
public class ReminderJobs extends Job {
    public void doJob() {
        Date now = new Date();
        List<ReminderModel> reminders = ReminderModel.find("reminderDate <= ?1", now).fetch();
        for (ReminderModel reminder : reminders) {
//            System.out.println("reminder:" + reminder.title);
            if (reminder.reminderUsers.size() > 0) UserLiveRoom.get().reminderDialog(reminder);
            else reminder._delete();
        }
        for (UserLiveRoom.UserOnline userOnline : UserLiveRoom.get().userStates.values()) {
            if (!userOnline.pushed && userOnline.tabCount <= 1 && userOnline.date.after(now)) {
                System.out.println("userStates: "+userOnline.tabCount);
                userOnline.pushed = true;
                UserLiveRoom.get().userStates.put(userOnline.userId, userOnline);
                UserLiveRoom.get().plushUserState(userOnline.userId, userOnline.tabCount);
            }
        }
    }
}
