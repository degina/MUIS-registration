package controllers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import models.*;
import play.libs.WS;
import play.mvc.With;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@With(Secure.class)
@Check(Consts.permissionMeeting)
public class Meetings extends CRUD {

    public static void list() {
        Long projectId = Users.pid();
        Long userId = Users.getUser().id;
        List<Meeting> meetings;
        Long maxSize;
        Long agendaSize = 0l, minuteSize = 0l, draftSize = 0l, meetingsSize = 0l;
        if (Users.getUser().getPermissionType("meeting") == 3) {
            maxSize = Meeting.count("status.id=1L AND project.id=?1", projectId);
            meetings = Meeting.find("status.id=1L AND project.id=?1 ORDER BY meetingDate", projectId).fetch(1, 16);
            agendaSize = maxSize;
            minuteSize = Meeting.count("( status.id=2L OR status.id=4L ) AND project.id=?1", projectId);
        } else {
            maxSize = (long) Meeting.find("SELECT DISTINCT m FROM tb_meeting m LEFT JOIN m.meetingUserRels AS r WHERE m.status.id=1L AND m.project.id=?1 AND ( m.viewMeeting=true OR (m.owner.id=?2 OR (r.meeting.id = m.id AND r.user.id=?3 )))", projectId, userId, userId).fetch().size();
            meetings = Meeting.find("SELECT DISTINCT m FROM tb_meeting m LEFT JOIN m.meetingUserRels AS r WHERE m.status.id=1L AND m.project.id=?1 AND ( m.viewMeeting=true OR (m.owner.id=?2 OR (r.meeting.id = m.id AND r.user.id=?3 ))) ORDER BY meetingDate", projectId, userId, userId).fetch(1, 16);
            agendaSize = maxSize;
            minuteSize = new Long(Meeting.find("SELECT DISTINCT m FROM tb_meeting m LEFT JOIN m.meetingUserRels AS r WHERE ( m.status.id=2L OR m.status.id=4L ) AND m.project.id=?1 AND ( m.viewMeeting=true OR (m.owner.id=?2 OR (r.meeting.id = m.id AND r.user.id=?3 )))", projectId, userId, userId).fetch().size());
        }
        draftSize = Meeting.count("status.id=3L AND project.id=?1 AND owner.id =?2 ", projectId, userId);
        meetingsSize = agendaSize + minuteSize;
        List<UserTeam> userTeams = UserTeam.findAll();
        int permissionType = Users.getUser().getPermissionType(Consts.permissionMeeting);
        int CurrentPageNumber = 1;
        Long MaxPageNumber = maxSize / 16;
        if (maxSize % 16 != 0) MaxPageNumber++;

        render(meetings, meetingsSize, agendaSize, minuteSize, draftSize, userTeams, permissionType, CurrentPageNumber, MaxPageNumber);
    }

    public static void listData(int CurrentPageNumber, Long filterUserId, Long filterStatusId, String filterSearchName, Date filterStartDate, Date filterEndDate) {
        System.out.println("filterSearchName: " + filterSearchName);
        String qr = "";
        if (Users.getUser().getPermissionType(Consts.permissionMeeting) == 3) {
            qr = "SELECT DISTINCT m FROM tb_meeting m WHERE m.project.id=" + Users.pid();
        } else {
            qr = "SELECT DISTINCT m FROM tb_meeting m LEFT JOIN m.meetingUserRels AS r WHERE (m.viewMeeting=true OR (m.owner.id=" + Users.getUser().id + " OR ( r.meeting.id = m.id AND r.user.id=" + Users.getUser().id + " ))) AND m.project.id=" + Users.pid();
        }
        if (filterStatusId == 0L) {
            qr += " AND m.status.id != 3L ";
        } else if (filterStatusId == 2L) {
            qr += " AND ( m.status.id=2L OR m.status.id=4L )";
        } else if (filterStatusId == 1L) {
            qr += " AND m.status.id=1L ";
        } else if (filterStatusId == 3L) {
            qr = "SELECT DISTINCT m FROM tb_meeting m WHERE m.project.id=" + Users.pid() + " AND m.status.id=3L AND m.owner.id=" + Users.getUser().id;
        }
        if (filterUserId != 0L && filterStatusId != 3L) {
            qr += " AND m.owner.id=" + filterUserId;
        }
        if (filterSearchName != null && !filterSearchName.equals("")) {
            qr += " AND m.title LIKE '%" + filterSearchName + "%'";
        }
        if (filterStartDate != null) {
            qr += " AND " + "m.meetingDate >= '" + Consts.myDateFormat.format(filterStartDate) +
                    "' AND m.meetingDate <= '" + Consts.myDateFormat.format(Functions.addOrMinusDays(filterEndDate, 1, true)) + "'";
        }
        if (filterStatusId == 1L)
            qr += " ORDER BY meetingDate";
        else
            qr += " ORDER BY meetingDate desc";

        List<Meeting> meetings = Meeting.find(qr).fetch(CurrentPageNumber, 16);
        int permissionType = Users.getUser().getPermissionType(Consts.permissionMeeting);
        Long maxSize = (long) Meeting.find(qr).fetch().size();
        Long MaxPageNumber = maxSize / 16;
        if (maxSize % 16 != 0) MaxPageNumber++;
        render(meetings, permissionType, MaxPageNumber, CurrentPageNumber);
    }

    public static void blank(Long id, boolean urgeljlel) {
        List<User> users = User.findAll();
        if (id == null)
            render(users);
        else if (urgeljlel) {
            Meeting meeting = Meeting.findById(id);
            render(users, meeting, urgeljlel);
        } else {
            Meeting meeting = Meeting.findById(id);
            render(users, meeting);
        }
    }

    public static void create(String title, Long status, Date meetingDate, boolean privateMeeting, Long meetingEdit,
                              boolean viewMeeting, String startHour, String finishHour, String location,
                              String[] userId, String overview, String[] filename, String[] filedir, Float[] filesize,
                              String[] extension, boolean meetingUrgeljlel) {
        Date startDate = Functions.convertHourNull(meetingDate);
        Date finishDate = startDate;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.set(Calendar.HOUR, Integer.parseInt(startHour.substring(0, 2)));
        calendar.set(Calendar.MINUTE, Integer.parseInt(startHour.substring(3, 5)));
        meetingDate = calendar.getTime();
        calendar.setTime(finishDate);
        calendar.set(Calendar.HOUR, Integer.parseInt(finishHour.substring(0, 2)));
        calendar.set(Calendar.MINUTE, Integer.parseInt(finishHour.substring(3, 5)));
        finishDate = calendar.getTime();
        Meeting meeting;
        if (meetingEdit != null && !meetingEdit.equals("")) {
            if (meetingUrgeljlel) {
                meeting = new Meeting();
                meeting.urgeljlel = Meeting.findById(meetingEdit);
            } else {
                meeting = Meeting.findById(meetingEdit);
            }
        } else {
            meeting = new Meeting();
        }
        meeting.title = title;
        meeting.meetingDate = meetingDate;
        meeting.finishDate = finishDate;
        meeting.location = location;
        meeting.status = MeetingStatus.findById(status);
        meeting.privateMeeting = privateMeeting;
        meeting.overview = overview;
        meeting.viewMeeting = viewMeeting;
        meeting.owner = Users.getUser();
        meeting.project = Project.findById(Users.pid());

        if (meetingEdit != null && !meetingEdit.equals("")) {
            if (meetingUrgeljlel) {
                meeting.status = MeetingStatus.findById(1l);
                meeting.create();
                List<MeetingTopic> meetingTopics = MeetingTopic.find("meeting.id=?1 and status.id=?2", meetingEdit, 1L).fetch();
                for (MeetingTopic topic : meetingTopics) {
                    MeetingTopic newTopic = new MeetingTopic();
                    newTopic.title = topic.title;
                    newTopic.description = topic.description;
                    newTopic.minutes = topic.minutes;
                    newTopic.status = topic.status;
                    newTopic.newOld = MeetingTopicNewOld.findById(2L);
                    newTopic.priority = topic.priority;
                    newTopic.task = topic.task;
                    newTopic.owner = topic.owner;
                    newTopic.meeting = meeting;
                    newTopic.create();
                    newTopic.topicUserRels = new ArrayList<MeetingTopicUserRel>();
                    for (MeetingTopicUserRel userRel : topic.topicUserRels) {
                        MeetingTopicUserRel newUserRel = new MeetingTopicUserRel(userRel.user, newTopic);
                        newUserRel.create();
                        newTopic.topicUserRels.add(newUserRel);
                    }
                    newTopic.topicAttachments = new ArrayList<MeetingTopicAttachment>();
                    for (MeetingTopicAttachment attach : topic.topicAttachments) {
                        MeetingTopicAttachment newAttach = new MeetingTopicAttachment(attach.name, attach.dir, attach.extension,attach.filesize, newTopic);
                        newAttach.create();
                        newTopic.topicAttachments.add(newAttach);
                    }
                }
            } else {
                Calendar cal = Calendar.getInstance();
                cal.setTime(meetingDate);
                cal.add(Calendar.HOUR, -1);
                for (ReminderModel reminderModel : meeting.reminderModels) {
                    reminderModel.reminderDate = cal.getTime();
                    reminderModel._save();
                }
                for (MeetingUserRel meetingUserRel : meeting.meetingUserRels) meetingUserRel._delete();
                for (MeetingAttachment meetingAttachment : meeting.meetingAttachments) meetingAttachment._delete();
                meeting.meetingUserRels = new ArrayList<MeetingUserRel>();
                meeting.meetingAttachments = new ArrayList<MeetingAttachment>();
                meeting._save();
            }
        } else {
            meeting.meetingUserRels = new ArrayList<MeetingUserRel>();
            meeting.meetingAttachments = new ArrayList<MeetingAttachment>();
            meeting.meetingTopics = new ArrayList<MeetingTopic>();
            meeting.create();


        }
        if (userId != null && !userId.equals("")) {
            List<User> users = new ArrayList<User>();
            for (int i = 0; i < userId.length; i++) {
                if (userId[i].charAt(0) == 't') {
                    UserTeam userBag = UserTeam.findById(Long.parseLong(userId[i].substring(2)));
                    for (User us : userBag.users) {
                        MeetingUserRel userRel = new MeetingUserRel(us, meeting);
                        userRel.create();
                        users.add(us);
                    }
                } else {
                    User userO = User.findById(Long.parseLong(userId[i].substring(2)));
                    MeetingUserRel userRel = new MeetingUserRel(userO, meeting);
                    userRel.create();
                    users.add(userO);
                }
            }
            if (status != 3l) {
                UserLiveRoom.get().notification(Users.getUser(), "Meeting", users, meeting.id);
                Calendar cal = Calendar.getInstance();
                cal.setTime(meetingDate);
                cal.add(Calendar.HOUR, -1);
                meetingDate = cal.getTime();
                ReminderModel reminder = new ReminderModel();
                reminder.meeting = meeting;
                reminder.mainType = "meeting";
                reminder.title = title;
                reminder.reminderDate = meetingDate;
                reminder.reminderUsers = new ArrayList<ReminderUser>();
                for (User user : users) reminder.addUser(user);
                reminder.create();
            }
        }
        if (filename != null) {
            for (int i = 0; i < filename.length; i++) {
                if (filename[i] != null) {
                    MeetingAttachment attachment = new MeetingAttachment(filename[i], filedir[i],
                            extension[i],filesize[i], meeting);
                    attachment.create();
                }
            }
        }
        show(meeting.id);
    }

    public static void show(Long id) {
        boolean oroltsogchMon = false;
        Meeting meeting = Meeting.findById(id);
        MeetingUserRel oroltsogch = MeetingUserRel.find("meeting.id=?1 AND user.id=?2", id, Users.getUser().id).first();
        if (oroltsogch != null) {
            if (oroltsogch.seen == false) {
                oroltsogch.seen = true;
                oroltsogch.save();
            }
        }
        if (meeting.status.id == 2L) {
            minutes(id);
        } else if (meeting.status.id == 4L) {
            minutesClosed(id);
        } else {
            List<MeetingUserRel> userRels = MeetingUserRel.find("meeting.id=?1", id).fetch();
            List<MeetingTopic> meetingTopics = MeetingTopic.find("meeting.id=?1", id).fetch();
            if (oroltsogch != null) {
                oroltsogchMon = true;
            }
            int permissionType = Users.getUser().getPermissionType(Consts.permissionMeeting);
            render(meeting, userRels, meetingTopics, oroltsogchMon, permissionType);
        }
    }

    public static void createTopic(String title, Long tid, Long priority, Long status, String description,
                                   String[] filename, String[] filedir, String[] extension,Float[] filesize,
                                   String[] userIds, Long meetingId, Long editTopicID) {
        if (editTopicID != null) {
            MeetingTopic meetingTopic = MeetingTopic.findById(editTopicID);
            List<MeetingTopicAttachment> meetingTopicAttachments = MeetingTopicAttachment.find("topic.id=?1", editTopicID).fetch();
            for (MeetingTopicAttachment topicAttachment : meetingTopicAttachments) {
                topicAttachment._delete();
            }
            if (meetingTopic != null) {
                meetingTopic._delete();
            }
        }
        Meeting meeting = Meeting.findById(meetingId);
        MeetingTopic meetingTopic = new MeetingTopic();
        meetingTopic.title = title;
        meetingTopic.newOld = MeetingTopicNewOld.findById((long) 1);
        meetingTopic.priority = MeetingTopicPriority.findById(priority);
        meetingTopic.status = MeetingTopicStatus.findById(status);
        meetingTopic.description = description;
        meetingTopic.meeting = meeting;
        meetingTopic.owner = Users.getUser();
        if (tid != null && tid != -1) {
            Task task = Task.findById(tid);
            meetingTopic.task = task;
        }
        meetingTopic.topicUserRels = new ArrayList<MeetingTopicUserRel>();
        if (userIds != null && !userIds.equals("")) {
            for (int i = 0; i < userIds.length; i++) {
                if (userIds[i].charAt(0) == 't') {
                    UserTeam userBag = UserTeam.findById(Long.parseLong(userIds[i].substring(2)));
                    for (User us : userBag.users) {
                        MeetingTopicUserRel userRel = new MeetingTopicUserRel(us, meetingTopic);
                        meetingTopic.topicUserRels.add(userRel);
                    }
                } else {
                    User userO = User.findById(Long.parseLong(userIds[i].substring(2)));
                    MeetingTopicUserRel userRel = new MeetingTopicUserRel(userO, meetingTopic);
                    meetingTopic.topicUserRels.add(userRel);
                }
            }
        }
        meetingTopic.create();
        if (filename != null && !filename.equals("")) {
            meetingTopic.topicAttachments = new ArrayList<MeetingTopicAttachment>();
            for (int i = 0; i < filename.length; i++) {
                if (filename[i] != null) {
                    MeetingTopicAttachment attachment = new MeetingTopicAttachment(filename[i], filedir[i],
                            extension[i],filesize[i], meetingTopic);
                    attachment.create();
                    meetingTopic.topicAttachments.add(attachment);
                }
            }
        }
        if(editTopicID == null){
            List<User> users = new ArrayList<User>();
            users.add(meeting.owner);
            UserLiveRoom.get().notification(Users.getUser(), "MeetingTopic", users, meeting.id);
        }
        List<MeetingTopic> meetingTopics = MeetingTopic.find("meeting.id=?1", meetingId).fetch();
        int permissionType = Users.getUser().getPermissionType(Consts.permissionMeeting);
        render(meetingTopics, permissionType);
    }

    public static void deleteTopic(Long deleteTopicId) {
        MeetingTopic meetingTopic = MeetingTopic.findById(deleteTopicId);
        if (meetingTopic != null) {
            User user = Users.getUser();
            if(meetingTopic.owner.id != user.id){
                List<User> users = new ArrayList<User>();
                users.add(meetingTopic.owner);
                UserLiveRoom.get().notification(user, "MeetingTopicDelete", users, meetingTopic.meeting.id);
            }
            meetingTopic._delete();
        }
        List<MeetingTopicAttachment> topicAttachments = MeetingTopicAttachment.find("topic.id=?1", meetingTopic.id).fetch();
        for (MeetingTopicAttachment topicAttachment : topicAttachments)
            if (MeetingTopicAttachment.count("topic.id!=?1 AND dir=?2 AND extension=?3", meetingTopic.id, topicAttachment.dir, topicAttachment.extension) == 0)
                Functions.deleteUploadFile(topicAttachment.dir, topicAttachment.extension);
    }

    public static void minutesTopic(Long meetingId) {
        List<MeetingTopic> meetingTopics = MeetingTopic.find("meeting.id=?1", meetingId).fetch();
        render(meetingTopics);
    }

    public static void editTopic(Long editTopicId) {
        MeetingTopic editTopic = MeetingTopic.findById(editTopicId);
        render(editTopic);
    }

    public static void showModal(Long topicId) {
        MeetingTopic meetingTopic = MeetingTopic.findById(topicId);
        render(meetingTopic);
    }

    public static void agendaToMinutes(Long id) {
        Meeting meeting = Meeting.findById(id);
        meeting.status = MeetingStatus.findById(2L);
        meeting._save();
        minutes(id);
    }

    public static void minutes(Long id) {
        Meeting meeting = Meeting.findById(id);
        List<MeetingUserRel> userRels = MeetingUserRel.find("meeting.id=?1", id).fetch();
        List<MeetingTopic> meetingTopics = MeetingTopic.find("meeting.id=?1", id).fetch();
        int permissionType = Users.getUser().getPermissionType(Consts.permissionMeeting);
        render(meeting, userRels, meetingTopics, permissionType);
    }

    public static void minutesToClose(Long id, boolean urgeljlel) {
        Meeting meeting = Meeting.findById(id);
        meeting.status = MeetingStatus.findById(4L);
        meeting.closedUser = Users.getUser();
        meeting.minuteDate = new Date();
        meeting._save();
        if (urgeljlel) {
            blank(id, true);
        } else {
            minutesClosed(id);
        }
    }

    public static void minutesClosed(Long id) {
        Meeting meeting = Meeting.findById(id);
        List<MeetingUserRel> userRels = MeetingUserRel.find("meeting.id=?1", id).fetch();
        List<MeetingTopic> meetingTopics = MeetingTopic.find("meeting.id=?1", id).fetch();
        render(meeting, userRels, meetingTopics);
    }

    public static void deleteMeeting(Long id) {
        Meeting meeting = Meeting.findById(id);
        List<Notification> notifications = Notification.find("meeting.id=?1", meeting.id).fetch();
        for (Notification notification : notifications)
            notification._delete();
        List<MeetingUserRel> userRels = MeetingUserRel.find("meeting.id=?1", meeting.id).fetch();
        for (MeetingUserRel userRel : userRels)
            userRel._delete();
        for (MeetingAttachment meetingAttachment : meeting.meetingAttachments)
            if (MeetingAttachment.count("meeting.id!=?1 AND dir=?2 AND extension=?3", meeting.id, meetingAttachment.dir, meetingAttachment.extension) == 0)
                Functions.deleteUploadFile(meetingAttachment.dir, meetingAttachment.extension);
        try {
            meeting._delete();
        } catch (Exception e) {
            renderText("үргэлжлэл хуралуудыг эхлээд устгана уу!!!");
        }
        list();
    }

    public static void cancelMeeting(Long id) {
        Meeting meeting = Meeting.findById(id);
        long status = 3;
        meeting.status = MeetingStatus.findById(status);
        meeting._save();
        show(meeting.id);
    }

    public static void sergeehMeeting(Long id) {
        Meeting draftMeeting = Meeting.findById(id);
        Meeting meeting = new Meeting();
        meeting.title = draftMeeting.title;
        meeting.meetingDate = draftMeeting.meetingDate;
        meeting.finishDate = draftMeeting.finishDate;
        meeting.location = draftMeeting.location;
        meeting.status = MeetingStatus.findById(1l);
        meeting.privateMeeting = draftMeeting.privateMeeting;
        meeting.overview = draftMeeting.overview;
        meeting.viewMeeting = draftMeeting.viewMeeting;
        meeting.owner = Users.getUser();
        meeting.project = draftMeeting.project;
        meeting.meetingUserRels = new ArrayList<MeetingUserRel>();
        meeting.meetingAttachments = new ArrayList<MeetingAttachment>();
        meeting.meetingTopics = new ArrayList<MeetingTopic>();
        meeting.create();
        List<User> users = new ArrayList<User>();
        List<MeetingUserRel> meetingUserRels = MeetingUserRel.find("meeting.id=?1", draftMeeting.id).fetch();
        ReminderModel reminder = new ReminderModel();
        reminder.meeting = meeting;
        reminder.mainType = "meeting";
        reminder.title = meeting.title;
        reminder.reminderDate = meeting.meetingDate;
        reminder.reminderUsers = new ArrayList<ReminderUser>();
        for (MeetingUserRel rel : meetingUserRels) {
            MeetingUserRel userRel = new MeetingUserRel(rel.user, meeting);
            userRel.create();
            reminder.addUser(rel.user);
            users.add(rel.user);
        }
        reminder.create();

        List<MeetingAttachment> meetingAttachments = MeetingAttachment.find("meeting.id=?1", draftMeeting.id).fetch();
        for (MeetingAttachment meetingAttachment : meetingAttachments) {
            MeetingAttachment attachment = new MeetingAttachment(meetingAttachment.name, meetingAttachment.dir, meetingAttachment.extension,meetingAttachment.filesize, meeting);
            attachment.create();
        }
        List<MeetingTopic> meetingTopics = MeetingTopic.find("meeting.id=?1", draftMeeting.id).fetch();
        for (MeetingTopic topic : meetingTopics) {
            MeetingTopic meetingTopic = new MeetingTopic();
            meetingTopic.title = topic.title;
            meetingTopic.newOld = MeetingTopicNewOld.findById(1l);
            meetingTopic.priority = topic.priority;
            meetingTopic.status = topic.status;
            meetingTopic.description = topic.description;
            meetingTopic.meeting = meeting;
            meetingTopic.owner = topic.owner;
            meetingTopic.task = topic.task;
            meetingTopic.create();
            List<MeetingTopicUserRel> topicUserRels = MeetingTopicUserRel.find("topic.id=?1", topic.id).fetch();
            for (MeetingTopicUserRel rel : topicUserRels) {
                MeetingTopicUserRel userRel = new MeetingTopicUserRel(rel.user, meetingTopic);
                userRel.create();
            }
            List<MeetingTopicAttachment> topicAttachments = MeetingTopicAttachment.find("topic.id=?1", topic.id).fetch();
            for (MeetingTopicAttachment meetingAttachment : topicAttachments) {
                MeetingTopicAttachment attachment = new MeetingTopicAttachment(meetingAttachment.name, meetingAttachment.dir
                        , meetingAttachment.extension,meetingAttachment.filesize, topic);
                attachment.create();
            }
        }
        UserLiveRoom.get().notification(meeting.owner, "Meeting", users, meeting.id);
        show(meeting.id);
    }

    public static void attendeesIrts(Long userRelId, Long irtsId) {
        MeetingUserRel userRel = MeetingUserRel.findById(userRelId);
        userRel.irts = MeetingIrts.findById(irtsId);
        userRel._save();
    }

    public static void topicStatus(Long topicId, Long statusId, String topicMinutesData, String attaches) {
        MeetingTopic topic = MeetingTopic.findById(topicId);
        JsonParser parser = new JsonParser();
        JsonElement attachElement = parser.parse(attaches);
        JsonArray attachArray = attachElement.getAsJsonArray();
        if (statusId == 4l) {
            statusId = 2L;
            boolean isNew = false;
            Post post = Post.find("type='Хурлын шийдвэр' AND typeModelId=?1", topic.id).first();
            if (post == null) {
                isNew = true;
                post = new Post();
            }
            post.owner = Users.getUser();
            post.seeAll = true;
            post.content =
                    "<strong> " + topic.meeting.id + " <a href=/meetings/" + topic.meeting.id + " >" + topic.meeting.title + "</a></strong> </br>"
                            + "<strong>Асуудал: </strong> " + topic.title + "</br>"
                            + "<strong>Дэвшүүлсэн: </strong> " + topic.owner + "</br>"
                            + "<strong>Шийдвэр: </strong> " + topicMinutesData;
            post.type = "Хурлын шийдвэр";
            post.typeIconName = "idea.jpg";
            post.typeModelId = topic.id;
            if (isNew)
                post.create();
            else {
                post.save();
                List<PostAttach> postAttaches = PostAttach.find("post.id=?1", post.id).fetch();
                for (PostAttach postAttach : postAttaches)
                    postAttach._delete();
            }
            for (JsonElement jsonElement : attachArray) {
                JsonObject attachobj = jsonElement.getAsJsonObject();
                PostAttach attach = new PostAttach(attachobj.get("filename").getAsString(),
                        attachobj.get("filedir").getAsString(), attachobj.get("extension").getAsString(),
                        attachobj.get("filesize").getAsFloat(),post);
                attach.create();
            }
        } else if (statusId == 5)
            statusId = 2L;
        MeetingTopicStatus status = MeetingTopicStatus.findById(statusId);
        topic.status = status;
        topic.minutes = topicMinutesData;
        topic._save();
        for (MeetingTopicAttachment attach : topic.topicAttachments)
            attach._delete();
        for (JsonElement jsonElement : attachArray) {
            JsonObject attachobj = jsonElement.getAsJsonObject();
            MeetingTopicAttachment attach = new MeetingTopicAttachment(attachobj.get("filename").getAsString(),
                    attachobj.get("filedir").getAsString(), attachobj.get("extension").getAsString(),
                    attachobj.get("filesize").getAsFloat(),topic);
            attach.create();
        }
        JsonObject response = new JsonObject();
        response.addProperty("taskId", topic.task == null ? '0' : topic.task.id);
        response.addProperty("userId", (topic.topicUserRels == null || topic.topicUserRels.size() <= 0) ? '0' : topic.topicUserRels.get(0).user.id);
        renderJSON(response);
    }

    public static int checkNewMeeting() {
        int count = Meeting.find("SELECT DISTINCT m FROM tb_meeting m LEFT JOIN m.meetingUserRels AS r WHERE r.meeting.id=m.id AND m.status.id=1L AND r.seen=false AND r.user.id=?1 AND m.project.id=?2", Users.getUser().id, Users.pid()).fetch().size();
        return count;
    }
}
