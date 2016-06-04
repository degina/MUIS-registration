package controllers;

import models.*;
import java.util.Date;
import java.util.List;

/**
 * Created by enkhamgalan on 3/8/15.
 */
public class Notifications extends CRUD {
    public static int getNotifications(Long uid) {
        return Notification.find("acceptor.id=?1 and seen=?2", uid, false).fetch().size();
    }

    public static int getNotificationMessages(Long uid) {
        return NotificationMessage.find("acceptor.id=?1 and seen=?2", uid, false).fetch().size();
    }

    public static void notificationsHeader(Long uid, int type) {
        User user = Users.getUser();
        if (user.id.compareTo(uid) != 0) forbidden();
        List<Notification> notifications = getNotificationDatas(uid, type, 1);
        render(notifications, type);
    }

    public static List<Notification> getNotificationDatas(Long uid, int type, int page) {
        if (type == 0) return Notification.find("acceptor.id=?1 order by date desc", uid).fetch(page, 10);
        else return NotificationMessage.find("acceptor.id=?1 order by seen asc, date desc", uid).fetch(page, 10);
    }

    public static void checkMoreReminder(Long reminderId) {
        Long userId = Users.getUser().id;
        Date now = new Date();
        ReminderUser reminderUser = ReminderUser.find("reminderModel.id=?1 AND user.id=?2", reminderId, userId).first();
        if (reminderUser != null) {
            if (reminderUser.reminderModel.reminderUsers.size() == 1) reminderUser.reminderModel._delete();
            else reminderUser._delete();

            reminderUser = ReminderUser.find("reminderModel.reminderDate <= ?1 AND user.id=?2", now, userId).first();
            if (reminderUser != null) UserLiveRoom.get().reminderDialog(reminderUser.reminderModel);
        }
    }

    public static void showAllNotifications(int type) {
        render(type);
    }

    public static void getAllNotifications(int type, int CurrentPageNumber) {
        User user = Users.getUser();
        List<Notification> notifications = getNotificationDatas(user.id, type, CurrentPageNumber);
        Long maxSize;
        if (type == 0) maxSize = Notification.count("acceptor.id=?1", user.id);
        else maxSize = NotificationMessage.count("acceptor.id=?1", user.id);
        Long MaxPageNumber = maxSize / 10;
        if (maxSize % 10 != 0) MaxPageNumber++;

        render(notifications, type, CurrentPageNumber, MaxPageNumber);
    }

    public static void notificationSee(Long id) {
        Notification notification = Notification.findById(id);
        notification.seen = true;
        notification._save();
        if (notification.meeting != null) {
            session.put("projectId", notification.meeting.project.id);
            session.put("projectName", notification.meeting.project.name);
            Meetings.show(notification.meeting.id);
        } else if (notification.event != null) {
            Events.show(notification.event.id);
        }
    }

    public static void notificationMessageSee(Long id) {
        NotificationMessage notification = NotificationMessage.findById(id);
        notification.seen = true;
        notification._save();
        if (notification.rfi != null) {
            session.put("projectId", notification.rfi.project.id);
            session.put("projectName", notification.rfi.project.name);
//            RFIs.show(notification.rfi.id);
        } else {
            session.put("projectId", notification.punchList.project.id);
            session.put("projectName", notification.punchList.project.name);
//            PunchLists.show(notification.punchList.id,0);
        }
    }

}
