package controllers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import models.*;
import play.db.jpa.GenericModel;
import play.mvc.With;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Enkh-Amgalan on 4/26/15.
 */
@With(Secure.class)
public class Events extends CRUD {

    public static void blank(Long id) {
        User user = Users.getUser();
        if (user.getUserPermissionType(Consts.permissionDashboard) != 3) forbidden();
        List<EventCategory> categories = EventCategory.findAll();
        if (id == null)
            render(categories);
        else {
            Event event = Event.findById(id);
            render(categories, event);
        }
    }

    public static void edit(Long id) {
        User user = Users.getUser();
        if (user.getUserPermissionType(Consts.permissionDashboard) != 3) forbidden();
        Event event = Event.findById(id);
        render(event);
    }

    public static void list() {
        User user = Users.getUser();
        if (user.getUserPermissionType(Consts.permissionDashboard) == 0) forbidden();

        List<EventCategory> categories = EventCategory.findAll();


        List<Event> events = Event.findAll();
        render(events, categories);
    }

    public static void create(String title, Long status, String startDate, String endDate, Long eventEdit, Long category,
                              String location, String description, String filename, String filedir) {
        User user = Users.getUser();
        if (user.getUserPermissionType(Consts.permissionDashboard) != 3) forbidden();

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        Date eventStartDate = null, eventEndDate = null;
        Event event = null;
        try {
            eventStartDate = dateFormat.parse(startDate);
            eventEndDate = dateFormat.parse(endDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        EventCategory eventCategory = EventCategory.findById(category);
        if (eventEdit == null) {
            event = new Event();
            event.title = title;
            event.startDate = eventStartDate;
            event.endDate = eventEndDate;
            event.location = location;
            event.description = description;
            event.category = eventCategory;
            event.owner = Users.getUser();
            event.create();
            if (filename != null && !filename.equals("")) {
                EventAttach attach = new EventAttach(filename, filedir, "jpg", null, event);
                attach.create();
            }
        } else {
            event = Event.findById(eventEdit);
            event.title = title;
            event.startDate = eventStartDate;
            event.endDate = eventEndDate;
            event.location = location;
            event.description = description;
            event.category = eventCategory;
            EventAttach attach = event.getFirstAttach();
            if (attach != null) {
                if (!filename.equals(attach.name)) {
                    attach.name = filename;
                    attach.dir = filedir;
                    attach._save();
                }
            }
            event._save();
        }
        list();
    }

    public static void show(Long id) {
        User user = Users.getUser();
        if (user.getUserPermissionType(Consts.permissionDashboard) == 0) forbidden();
        Event event = Event.findById(id);
        List<Event> events = Event.find("ORDER BY startDate desc").fetch();
        List<Post> posts = Post.find("event.id=?1 ORDER BY activeDate desc", event.id).fetch();
        int total_groups = posts.size() / 10;
        List<EventPeople> eventPeoples = EventPeople.find("event.id=?1", event.id).fetch();
        render(events, event, posts, total_groups, eventPeoples);
    }

    public static void eventCome(int status, Long eventId) {
        EventPeople uer = EventPeople.find("event.id=?1 AND user.id=?2", eventId, Users.getUser().id).first();
        int temp = uer.status;
        uer.status = status;
        uer._save();
        renderText(temp);
    }

    public static void listData(int CurrentPageNumber, int flag) {
        if (flag == 0) {
            flag = 3;
        }
        int pageLimit = 8;
        long MaxPageNumber;
        Date today = Functions.convertHourNull(new Date());
        if (flag == 1) {
            Long totalPast = Event.count("startDate <=?1 ORDER BY startDate desc", today);
            MaxPageNumber = totalPast / pageLimit;
            if (totalPast % pageLimit != 0) MaxPageNumber++;
            List<Event> events = Event.find("startDate <=?1 ORDER BY startDate desc", today).fetch(CurrentPageNumber, pageLimit);
            render(events, CurrentPageNumber, MaxPageNumber);
        } else if (flag == 2) {
            long totalUpcoming = Event.count("startDate >=?1 ORDER BY startDate desc", today);
            MaxPageNumber = totalUpcoming / pageLimit;
            if (totalUpcoming % pageLimit != 0) MaxPageNumber++;
            List<Event> events = Event.find("startDate >=?1", today).fetch(CurrentPageNumber, pageLimit);
            render(events, CurrentPageNumber, MaxPageNumber);
        } else {
            List<Event> eventMaxSizer = Event.findAll();
            MaxPageNumber = eventMaxSizer.size() / pageLimit;
            if (eventMaxSizer.size() % pageLimit != 0) MaxPageNumber++;
            List<Event> events = Event.find("ORDER BY startDate desc").fetch(CurrentPageNumber, pageLimit);
            render(events, CurrentPageNumber, MaxPageNumber);
        }

    }

    public static void listDataThisWeek() {
        Date today = new Date();
        int b = 0, l = 0;
        int day = today.getDay() == 0 ? 7 : today.getDay();
        if (day <= 7)
            l = 7 - day;
        Date last = new Date(today.getTime() + l * 24 * 60 * 60 * 1000);
        last.setHours(23);
        last.setMinutes(59);
        last.setSeconds(59);
        GenericModel.JPAQuery query = Event.find("startDate >= :today and startDate <= :last").setParameter("today", today).setParameter("last", last);
        List<Event> events = query.fetch();
        render(events);
    }

    public static void eventDelete(Long id) {
        try {
            Event event = Event.findById(id);
            event._delete();
            renderText("Устсан");
        } catch (Exception e) {
            e.printStackTrace();
            renderText("Устсангүй");
        }
    }

    public static void saveAttach(Long eventId, String attach) {
        Event event = Event.findById(eventId);
        JsonParser parser = new JsonParser();
        JsonElement eventElement = parser.parse(attach);
        JsonArray attaches = eventElement.getAsJsonArray();
        for (JsonElement attachEvent : attaches) {
            JsonObject obj = attachEvent.getAsJsonObject();
            EventAttach eventAttach = new EventAttach(obj.get("filename").getAsString(), obj.get("filedir").getAsString(), obj.get("extension").getAsString(), null, event);
            eventAttach.create();
        }

    }

    public static void people(Long eventId, String[] userIds) {
        Event event = Event.findById(eventId);
        Long meId = Users.getUser().id;
        List<User> users = new ArrayList<User>();
        int i = 0;
        for (String user : userIds) {
            if (user.charAt(0) == 'u') {
                if (!checkUser(event, Long.parseLong(user.substring(2)))) {
                    User us = User.findById(Long.parseLong(user.substring(2)));
                    if (meId.intValue() != us.id.intValue()) users.add(us);
                    EventPeople eventPeople = new EventPeople(event, us, 0);
                    eventPeople.create();
                    i++;
                }
            } else if (user.charAt(0) == 't') {
                UserTeam userBag = UserTeam.findById(Long.parseLong(user.substring(2)));
                for (User us : userBag.users) {
                    if (!checkUser(event, us.id)) {
                        if (meId.intValue() != us.id.intValue()) users.add(us);
                        EventPeople eventPeople = new EventPeople(event, us, 0);
                        eventPeople.create();
                        i++;
                    }
                }
            }
        }
        UserLiveRoom.get().notification(Users.getUser(), "Event", users, event.id);
        event._save();
        renderText(i);
    }

    public static boolean checkUser(Event event, Long id) {
        for (EventPeople people : event.peoples) {
            if (people.user.id.compareTo(id) == 0) return true;
        }
        return false;
    }

    public static void createEvent(Long id, String title, String desc, String location, String startDate, String endDate) {
        Event event = new Event();
        event.title = title;
        event.description = desc;
        event.location = location;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        try {
            if (startDate != null && startDate.length() > 0) event.startDate = dateFormat.parse(startDate);
            if (endDate != null && endDate.length() > 0) event.endDate = dateFormat.parse(endDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        event.owner = Users.getUser();
        event.category = EventCategory.findById(1l);
        event.create();

        EventPeople eventPeople = new EventPeople(event, Users.getUser(), 2);
        eventPeople.create();

        Post post = new Post();
        post.owner = Users.getUser();
        boolean seeAll = true;
        post.seeAll = seeAll;
        post.content = event.owner + " Үйл ажиллагаа зарласан байна";
        post.event = event;
        post.create();
        renderText(event.id);
    }

    public static void saveEditedEvent(Long id, String title, String desc, String location, String startDate, String endDate) {


        Event event = Event.findById(id);
        event.title = title;
        event.description = desc;
        event.location = location;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        try {
            event.startDate = dateFormat.parse(startDate);
            event.endDate = dateFormat.parse(endDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        event.owner = Users.getUser();
        event.category = EventCategory.findById(1l);
        event._save();
    }

    public static void eventPicture(Long id, String fileData) {
        Event event = Event.findById(id);
        event.imageDir = FileUploader.decodeToImage(Consts.uploadEventPath, fileData.substring(22, fileData.length()));
        event._save();
    }

    public static void eventGuests(int status, Long eventId) {
        List<EventPeople> eventPeoples = EventPeople.find("status=?1 AND event.id=?2", status, eventId).fetch();
        List<EventPeople> eventInvite = EventPeople.find("event.id=?2", eventId).fetch();
        render(eventPeoples, status, eventInvite);
    }
}