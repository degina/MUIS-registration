package controllers;

import com.google.gson.JsonObject;
import jdk.nashorn.internal.parser.JSONParser;
import models.*;
import play.libs.F;
import play.mvc.Controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by enkhamgalan on 3/7/15.
 */
public class UserLiveRoom extends Controller {
    // ~~~~~~~~~ Let's chat!

    final F.EventStream<UserLiveRoom.Event> chatEvents = new F.EventStream<Event>();
    public static final int offline = 0;
    public static final int online = 1;
    static final long ONE_MINUTE_IN_MILLIS = 60000;//millisecs
    public static final HashMap<Long, UserOnline> userStates = new HashMap<Long, UserOnline>();

    /**
     * For WebSocket, when a user join the room we return a continuous event stream
     * of ChatEvent
     */
    public F.EventStream<UserLiveRoom.Event> join(Long userId) {
        //System.out.println("JOIN: " + user.toString());
        onlineStateFn(userId, 1);
        return chatEvents;
    }

    public void notification(User owner, String type, List<User> users, Long id) {
        String uid = ",";
        Date date = new Date();
        users.remove(owner);
        String message = "", ph_title = "", ph_type = "";
        long ph_id = 0L;
        int status = 0;
        if (type.equals("PunchList") || type.equals("Reply")
                || type.equals("RFI") || type.equals("Tracking")
                || type.equals("InventoryOrder") || type.equals("InventoryTransfer")) {
            RFI rfi = null;
            PunchList punchList = null;
            RFI_Tracking tracking = null;
            PunchList_Reply reply = null;

            InventoryOrder order = null;
            InventoryTransfer transfer = null;
            int orderStatus = 0, transferStatus = 0;

            if (type.equals("PunchList")) {
                punchList = PunchList.findById(id);
                message = punchList.No.toString();
                ph_title = "Үүрэг даалгавар ирлээ";
                ph_type = "punchlist";
                ph_id = punchList.id;
            } else if (type.equals("Reply")) {
                reply = PunchList_Reply.findById(id);
                message = reply.punchList.No.toString();
                ph_title = "Үүрэг даалгаварт хариу ирлээ";
                ph_type = "punchlist";
                ph_id = reply.punchList.id;
            } else if (type.equals("RFI")) {
                rfi = RFI.findById(id);
                message = rfi.No.toString();
                ph_title = "Мэдээлэл хүсэлт ирлээ";
                ph_type = "rfi";
                ph_id = rfi.id;
            } else if (type.equals("Tracking")) {
                tracking = RFI_Tracking.findById(id);
                message = tracking.rfi.No.toString();
                ph_title = "Мэдээлэл хүсэлтэд хариу ирлээ";
                ph_type = "rfi";
                ph_id = tracking.rfi.id;
            } else if (type.equals("InventoryOrder")) {
                order = InventoryOrder.findById(id);
                orderStatus = order.status.id.intValue();
                message = order.id.toString();
                status = orderStatus;
            } else if (type.equals("InventoryTransfer")) {
                transfer = InventoryTransfer.findById(id);
                transferStatus = transfer.status.id.intValue();
                message = transfer.id.toString();
                status = transferStatus;
            }
            for (User us : users) {
                NotificationMessage notification = new NotificationMessage();
                notification.date = date;
                notification.sender = owner;
                notification.acceptor = us;
                notification.rfi = rfi;
                notification.punchList = punchList;
                notification.reply = reply;
                notification.tracking = tracking;
                notification.order = order;
                notification.orderStatus = orderStatus;
                notification.transfer = transfer;
                notification.transferStatus = transferStatus;
                uid += us.id + ",";
                notification.create();
            }
        } else if (type.equals("Meeting") || type.equals("MeetingTopic")
                || type.equals("MeetingTopicDelete") || type.equals("Event")) {
            Meeting meeting = null;
            models.Event event = null;
            if (type.equals("Meeting") || type.equals("MeetingTopic") || type.equals("MeetingTopicDelete")) {
                meeting = Meeting.findById(id);
                if (type.equals("MeetingTopic")) {
                    message = "Таны заралсан хуралд хэлэлцэх асуудал нэмлээ";
                    ph_title = "Хэлэлцэх асуудал нэмлээ";
                } else if (type.equals("Meeting")) {
                    message = meeting.title;
                    ph_title = "Хурал зараллаа";
                } else if (type.equals("MeetingTopicDelete")) {
                    message = "Таны нэмсэн хэлэлцэх асуудлыг устгалаа";
                    ph_title = "Хэлэлцэх асуудлыг устгалаа";
                }
                ph_type = "meeting";
                ph_id = meeting.id;
            } else if (type.equals("Event")) {
                event = models.Event.findById(id);
                message = event.title;
            }
            for (User us : users) {
                Notification notification = new Notification();
                notification.date = date;
                notification.message = message;
                notification.sender = owner;
                notification.acceptor = us;
                notification.meeting = meeting;
                notification.event = event;
                uid += us.id + ",";
                notification.create();
            }
        }
        if (uid.length() > 1) {
            JsonObject sendJson = new JsonObject();
            sendJson.addProperty("mainType", "notification");
            sendJson.addProperty("type", type);
            sendJson.addProperty("senderId", owner.id.intValue());
            sendJson.addProperty("sender", owner.toString());
            sendJson.addProperty("uid", uid);
            sendJson.addProperty("message", message);
            sendJson.addProperty("status", status);
            chatEvents.publish(new NotificationEvent(sendJson));
        }
        // To Phone notifications
        if (ph_id > 0) {
            JsonObject jsonPhone;
            JsonObject notiPhone;
            for (User us : users) {
                if (us.gcmRegistrationId != null && !us.gcmRegistrationId.equals("")) {
                    jsonPhone = new JsonObject();
                    jsonPhone.addProperty("to", us.gcmRegistrationId);
                    notiPhone = new JsonObject();
                    notiPhone.addProperty("type", ph_type);
                    notiPhone.addProperty("ownerId", owner.id);
                    notiPhone.addProperty("title", ph_title);
                    notiPhone.addProperty("id", ph_id);
                    jsonPhone.add("data", notiPhone);
                    Functions.sendNotificationPhone(jsonPhone, us.device);
                }
            }
        }
    }

    public void reminderDialog(ReminderModel reminderModel) {
        JsonObject sendJson = new JsonObject();
        sendJson.addProperty("mainType", "reminder");
        sendJson.addProperty("type", reminderModel.mainType);
        sendJson.addProperty("id", reminderModel.id);
        String uid = ",";
        if (reminderModel.mainType.equals("meeting")) {
            sendJson.addProperty("meetingId", reminderModel.meeting.id);
            sendJson.addProperty("message", reminderModel.title);
            sendJson.addProperty("date", new SimpleDateFormat("M сарын d ны HH:mm").format(reminderModel.meeting.meetingDate));
        } else if (reminderModel.mainType.equals("todo")) {
            sendJson.addProperty("message", reminderModel.title);
            sendJson.addProperty("date", new SimpleDateFormat("M сарын d ны HH:mm").format(reminderModel.reminderDate));
        } else if (reminderModel.mainType.equals("rfi")) {
            sendJson.addProperty("rfiId", reminderModel.rfi.id);
            sendJson.addProperty("rfiNo", reminderModel.rfi.No);
            sendJson.addProperty("project", reminderModel.rfi.project.name);
            sendJson.addProperty("date", new SimpleDateFormat("M сарын d ны HH:mm").format(reminderModel.reminderDate));
        } else if (reminderModel.mainType.equals("order")) {
            sendJson.addProperty("orderId", reminderModel.order.id);
            sendJson.addProperty("title", reminderModel.order.status.status);
            sendJson.addProperty("location", reminderModel.order.location.name);
            sendJson.addProperty("date", new SimpleDateFormat("M сарын d ны HH:mm").format(reminderModel.reminderDate));
        } else if (reminderModel.mainType.equals("contract")) {
            sendJson.addProperty("subType", reminderModel.subType);
            sendJson.addProperty("title", reminderModel.contract.title);
            sendJson.addProperty("message", reminderModel.title);
            sendJson.addProperty("contractId", reminderModel.contract.id);
        } else if (reminderModel.mainType.equals("ganttTask")) {
            sendJson.addProperty("project", reminderModel.task.projectObject.project.name);
            sendJson.addProperty("task", reminderModel.task.name);
            sendJson.addProperty("message", reminderModel.title);
        } else if (reminderModel.mainType.equals("task")) {
            sendJson.addProperty("project", "Хувийн төлөвлөгөөт");
            sendJson.addProperty("task", reminderModel.myPlan.name);
            sendJson.addProperty("message", reminderModel.title);
        }
        for (ReminderUser reminderUser : reminderModel.reminderUsers) uid += reminderUser.user.id + ",";
        sendJson.addProperty("uid", uid);
        chatEvents.publish(new NotificationReminder(sendJson));
    }

    public void webRTCData(String data, Long userId) {
        chatEvents.publish(new WebRTCEvents(data, userId));
    }

    public void plushUserState(Long userId, int state) {
        UserOnline userOnline = userStates.get(userId);
        JsonObject sendJson = new JsonObject();
        sendJson.addProperty("mainType", "chat");
        sendJson.addProperty("type", "userState");
        sendJson.addProperty("userId", userId);
        sendJson.addProperty("state", state);
        sendJson.addProperty("avatar", userOnline.avatar);
        sendJson.addProperty("name", userOnline.name);
        sendJson.addProperty("msgCount", userOnline.msgCount);
        sendJson.addProperty("team", userOnline.team);
        chatEvents.publish(new OnlineStateEvent(sendJson));
    }

    public void onlineStateFn(Long userId, int state) {
        UserOnline userOnline = userStates.get(userId);
        if (userOnline == null) {
            User user = User.findById(userId);
            userStates.put(userId, new UserOnline(userId, false, (state == online ? online : offline), new Date()
                    ,user.profilePicture,user.toString(),user.userTeam.name));

        }else if (state == online) {
            if (userOnline.tabCount == offline) {
                Date now = new Date();
                userOnline.pushed = !((now.getTime() - userOnline.date.getTime()) > (1 * ONE_MINUTE_IN_MILLIS));
                userOnline.date = new Date(now.getTime() + (1 * ONE_MINUTE_IN_MILLIS));
            }
            userOnline.tabCount++;
            userStates.put(userId, userOnline);
        } else if (state == offline) {
            if (userOnline.tabCount == online) {
                userOnline.date = new Date((new Date()).getTime() + (1 * ONE_MINUTE_IN_MILLIS));
                userOnline.pushed = false;
                userOnline.tabCount = offline;
            } else
                userOnline.tabCount--;
            userStates.put(userId, userOnline);

        }
        System.out.println(((state == 1) ? "JOIN: " : "LEAVE: ") + "user Id: " + userId + " tabCount: " + (userOnline != null ? userOnline.tabCount : "null"));
    }

    public static  List<UserOnline> getOnlineOfflineUsers(){
        return new ArrayList<UserOnline>(userStates.values());
    }
// ~~~~~~~~~ Chat room events

    public static abstract class Event {

        final public String type;
        final public Long timestamp;

        public Event(String type) {
            this.type = type;
            this.timestamp = System.currentTimeMillis();
        }

    }

    public static class NotificationEvent extends Event {
        final public JsonObject json;

        public NotificationEvent(JsonObject obj) {
            super("notification");
            this.json = obj;
        }
    }

    public static class NotificationReminder extends Event {
        final public JsonObject json;

        public NotificationReminder(JsonObject obj) {
            super("remindermodelClass");
            this.json = obj;
        }

    }

    public static class WebRTCEvents extends Event {
        final public String data;
        final public Long userId;

        public WebRTCEvents(String obj, Long userId) {
            super("webrcteventsClass");
            this.data = obj;
            this.userId = userId;
        }
    }

    public static class OnlineStateEvent extends Event {
        final public JsonObject json;

        public OnlineStateEvent(JsonObject obj) {
            super("onlinestateevent");
            this.json = obj;
        }
    }

    // ~~~~~~~~~ Chat room factory

    static UserLiveRoom instance = null;

    public static UserLiveRoom get() {
        if (instance == null) {
            instance = new UserLiveRoom();
        }
        return instance;
    }

    public class UserOnline {
        public int tabCount;
        public int msgCount=0;
        public boolean pushed;
        public String avatar;
        public String name;
        public String team;
        public Long userId;
        public Date date;

        public UserOnline(Long userId, boolean pushed, int tabCount, Date date
                ,String avatar,String name,String team) {
            this.userId = userId;
            this.date = date;
            this.pushed = pushed;
            this.tabCount = tabCount;
            this.avatar = avatar;
            this.name = name;
            this.team = team;
        }
    }
}


