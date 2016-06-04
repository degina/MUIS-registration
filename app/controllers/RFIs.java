package controllers;

import com.google.gson.JsonObject;
import controllers.MyClass.UserGraphic;
import models.*;
import play.mvc.With;
import java.util.*;

/**
 * Created by Rina on 1/11/2015.
 */
@With(Secure.class)
public class RFIs extends CRUD {
    @Check(Consts.permissionRFI)
    public static void list(String status) {
        User user = Users.getUser();
        UserTeam userTeam = Users.getUser().userTeam;
        List<RFI_Distribution> user_distributions = user.rfi_distributions;
        user_distributions.addAll(userTeam.rfi_distributions);
        String query = "project.id =" + Users.pid() + " and (";
        for (int i = 0; i < user_distributions.size(); i++) {
            if (user_distributions.get(i).rfi != null) {
                if (!user_distributions.get(i).rfi.status.status.equalsIgnoreCase("Draft"))
                    query += "id = " + user_distributions.get(i).rfi.id + " or ";
            }
        }
        query += "not(questionReceivedFrom.id <> " + Users.getUser().id + " and assignee.id <> " + Users.getUser().id + " and private_ = true))";
        int open, close, all, draft;
        open = RFI.find(query + " and status.status = 'Open'").fetch().size();
        close = RFI.find(query + " and status.status = 'Closed'").fetch().size();
        draft = RFI.find("status.status = 'Draft' and questionReceivedFrom.id = " + Users.getUser().id).fetch().size();
        all = open + close;

        List<OrganizationTeam> teams = FunctionController.getOrganizationTeams();
        List<ProjectObject> projectObjects = ProjectObject.find("project.id = " + Users.pid()).fetch();
        render(teams, projectObjects, status, all, open, close, draft);
    }

    @Check(Consts.permissionRFI)
    public static void listAjax(String status, String sender, String assignee, String keyword, int CurrentPageNumber, String tasky) {
        int pageLimit = 40;
        boolean firstload = false;
        if (status.equals("Me")) {
            firstload = true;
        }
        //Selector Query create
        User user = Users.getUser();
        UserTeam userTeam = Users.getUser().userTeam;
        List<RFI_Distribution> user_distributions = user.rfi_distributions;
        user_distributions.addAll(userTeam.rfi_distributions);
        String query = "project.id =" + Users.pid() + " and (";
        for (int i = 0; i < user_distributions.size(); i++) {
            if (user_distributions.get(i).rfi != null) {
                if (!user_distributions.get(i).rfi.status.status.equalsIgnoreCase("Draft"))
                    query += "id = " + user_distributions.get(i).rfi.id + " or ";
            }
        }
        query += "not(questionReceivedFrom.id <> " + Users.getUser().id + " and assignee.id <> " + Users.getUser().id + " and private_ = true))";
        int MaxPageNumber;
        //--------------------------------------------

        //Add status filter
        if (!status.equals("Me")) {
            if (!status.equalsIgnoreCase("All")) {
                if (status.equalsIgnoreCase("Draft"))
                    query = "status.status = 'Draft' and questionReceivedFrom.id = " + Users.getUser().id;
                else if (status.equalsIgnoreCase("Replied")) {
                    List<NotificationMessage> notificationMessages = NotificationMessage.find("tracking.rfi.project.id = " + Users.pid() + " and tracking.rfi.status.status = 'Open' and acceptor.id = " + Users.getUser().id + " and seen = 0").fetch();
                    query = "id = 0";
                    if (notificationMessages != null) {
                        query = "(";
                        for (int i = 0; i < notificationMessages.size(); i++)
                            query += " id = " + notificationMessages.get(i).tracking.rfi.id + " or";
                        query += " 1 = 0)";
                    }
                } else
                    query += " and status.status = '" + status + "'";
            } else {
                query += " and status.status <> 'Draft'";
            }
        } else {
            query += " and status.status = 'Open'";
        }
        //--------------------------------------------
        if (firstload) {
            query += " and ( assignee.id = " + assignee + " or questionReceivedFrom.id = " + assignee + ") ";
        } else {
            //Add sender filter
            if (!sender.equalsIgnoreCase("0")) query += " and questionReceivedFrom.id = " + sender;
            //--------------------------------------------

            //Add assignee filter
            if (!assignee.equalsIgnoreCase("0")) query += " and assignee.id = " + assignee;
            //--------------------------------------------
        }
        //Add rfi filter
        if (!tasky.equalsIgnoreCase("0")) {
            String[] tas;
            tas = tasky.split("-");
            if (tas[0].equalsIgnoreCase("o")) {
                query += " and task.projectObject.id = " + tas[1];
            } else {
                query += " and task.id = " + tas[1];
            }
        }
        //--------------------------------------------

        //Add search filter
        if (!keyword.trim().equalsIgnoreCase("")) {
            query += " and ( subject like '%" + keyword + "%' or assignee.firstName like '%" + keyword + "%' or questionReceivedFrom.firstName like '%" + keyword + "%' ) ";
        } else
            keyword = "";
        //--------------------------------------------

        //Page Number counter
        List<RFI> rfiMaxSizer = RFI.find(query + " order by id desc").fetch();
        MaxPageNumber = rfiMaxSizer.size() / pageLimit;
        if (rfiMaxSizer.size() % pageLimit != 0) MaxPageNumber++;

        //Overdue calculate
        Date now = new Date();
        now = new Date(now.getTime() - (1000 * 60 * 60 * 24));
        Date week = new Date(now.getTime() + (1000 * 60 * 60 * 24 * 7));
        Long weeker = 0L, overdue = 0L, nextWeeker = 0L;
        if (!status.equalsIgnoreCase("Draft"))
            for (int i = 0; i < rfiMaxSizer.size(); i++) {
                if (rfiMaxSizer.get(i).status.status.equalsIgnoreCase("Open")) {
                    if (now.after(rfiMaxSizer.get(i).dueDate)) overdue++;
                    else if (week.after(rfiMaxSizer.get(i).dueDate)) weeker++;
                    else nextWeeker++;
                }
            }
        //-------------------------------------
        //Page selector
        List<RFI> rfis = RFI.find(query + " order by createDate desc").fetch(CurrentPageNumber, pageLimit);
        //--------------------------------------------

        render(rfis, keyword, now, week, CurrentPageNumber, MaxPageNumber, status, sender, assignee, tasky, overdue, weeker, nextWeeker, firstload);
    }

    @Check(Consts.permissionRFI)
    public static void loadGraphic() {
        Date today = Functions.convertHourNull(new Date());
        Date nextWeek = Functions.addOrMinusDays(today, 7, true);
        List<RFI> rfis = RFI.find("project.id =" + Users.pid() + " and status.status = 'Open' order by assignee.userPosition.rate, assignee.firstName, assignee.id").fetch();
        List<UserGraphic> userGraphics = new ArrayList<UserGraphic>();
        Long uid = 0L, overdue = 0L, weeker = 0L, nextWeeker = 0L;
        for (RFI rfi : rfis) {
            if (rfi.assignee.id.compareTo(uid) != 0) {
                UserGraphic userGraphic = new UserGraphic();
                userGraphic.user = rfi.assignee;
                userGraphic.all = 0L;
                userGraphic.overdue = 0L;
                userGraphic.nextWeek = 0L;
                userGraphic.nNextWeek = 0L;
                userGraphics.add(userGraphic);
                uid = rfi.assignee.id;
            }
            userGraphics.get(userGraphics.size() - 1).all++;
            if (rfi.dueDate.before(today)) {
                userGraphics.get(userGraphics.size() - 1).overdue++;
                overdue++;
            } else if (rfi.dueDate.getTime() >= today.getTime() && rfi.dueDate.before(nextWeek)) {
                userGraphics.get(userGraphics.size() - 1).nextWeek++;
                weeker++;
            } else {
                userGraphics.get(userGraphics.size() - 1).nNextWeek++;
                nextWeeker++;
            }
        }
        Collections.sort(userGraphics, new Comparator<UserGraphic>() {
            public int compare(UserGraphic o2, UserGraphic o1) {
                return Long.compare(o1.overdue, o2.overdue);
            }
        });
        render("/PunchLists/loadGraphic.html", userGraphics, overdue, weeker, nextWeeker);
    }

    @Check(Consts.permissionRFI)
    public static void blank(Long id, Long taskId, Long assigneeId) {
        User user = Users.getUser();
        if (user.getPermissionType(Consts.permissionRFI) != 1) forbidden();
        List<OrganizationTeam> teams = FunctionController.getOrganizationTeams();
        Task task = Task.findById(taskId);
        RFI rfi = RFI.findById(id);
        render(rfi, teams, task, assigneeId);
    }

    @Check(Consts.permissionRFI)
    public static void create(Long rfiId, String subject, Date dueDate, Long taskId, String location,
                              Long[] revisionId, String[] jsonString,
                              Long assignedTo, String distribution, String schedule, String cost, String question,
                              boolean draft, boolean overdueNotification, boolean private_, String[] filename,
                              String[] filedir, String[] extension, Float[] filesize) {
        System.out.println("begin: " + (new Date()));
        RFI rfi;
        if (rfiId == null)
            rfi = new RFI();
        else
            rfi = RFI.findById(rfiId);
        rfi.subject = subject;
        rfi.assignee = User.findById(assignedTo);
        rfi.createDate = new Date();

            rfi.dueDate = dueDate;
        rfi.question = question;
        rfi.project = Project.findById(Users.pid());
        Project project = Project.findById(Users.pid());
        if (taskId != null)
            rfi.task = Task.findById(taskId);
        rfi.location = location;
        rfi.private_ = private_;
        rfi.scheduleImpact = RFI_Impact.find("impact = ?1", schedule).first();
        rfi.costImpact = RFI_Impact.find("impact = ?1", cost).first();
        rfi.overdueNotification = overdueNotification;
        rfi.questionReceivedFrom = Users.getUser();
        if (!draft) {
            ProjectCounter counter = project.projectCounter;
            Long number = counter.lastRFINo;
            rfi.No = number + 1;
            counter.lastRFINo = number + 1;
            counter._save();
            rfi.status = RFI_Status.find("status = ?1", "Open").first();
        } else {
            rfi.No = 0l;
            rfi.status = RFI_Status.find("status = ?1", "Draft").first();
        }
        if (rfiId == null){
            rfi.create();
            System.out.println("end: " + (new Date()));
        }
        else {
            for (RFI_Attach att : rfi.attaches) {
                att._delete();
            }
            rfi.attaches = new ArrayList<RFI_Attach>();
            for (DrawingLayer pin : rfi.pins) {
                pin._delete();
            }
            rfi.pins = new ArrayList<DrawingLayer>();
            for (RFI_Distribution dist : rfi.distributions) {
                dist._delete();
            }
            rfi.distributions = new ArrayList<RFI_Distribution>();
            rfi._save();
        }
        if (distribution.length() > 0) {
            String[] distris = distribution.split(",");
            RFI_Distribution[] rfi_distributions = new RFI_Distribution[distris.length];
            for (int i = 0, j = 0; i < distris.length; i++) {
                String[] ids = distris[i].split("-");
                if (checkDist(rfi_distributions, j, ids)) {
                    rfi_distributions[j] = new RFI_Distribution(ids[0].trim(), rfi, Long.parseLong(ids[1]));
                    rfi_distributions[j].create();
                    j++;
                }
            }
        }
        if (filename != null) {
            for (int i = 0; i < filename.length; i++) {
                RFI_Attach attach = new RFI_Attach();
                attach.name = filename[i];
                attach.extension = extension[i];
                attach.dir = filedir[i];
                attach.filesize = filesize[i];
                attach.rfi = rfi;
                attach.create();
            }
        }
        if (revisionId != null) {
            for (int i = 0; i < revisionId.length; i++) {
                DrawingLayer layer = new DrawingLayer(rfi, jsonString[i], revisionId[i], "drawing");
                layer.create();
            }
        }

        if (!draft) {
            List<User> users = new ArrayList<User>();
            users.add(rfi.assignee);
            if (rfi.distributions != null)
                for (int i = 0; i < rfi.distributions.size(); i++) {
                    RFI_Distribution dist = rfi.distributions.get(i);
                    if (dist.getCode().equalsIgnoreCase("t")) {
                        List<User> team = User.find("userTeam.id = " + dist.getId()).fetch();
                        for (User us : team)
                            if (!us.id.equals(Users.getUser().id))
                                users.addAll(team);
                    } else {
                        if (!dist.getId().equals(Users.getUser().id)) {
                            User dis = User.findById(dist.getId());
                            users.add(dis);
                        }
                    }
                }

//            if (overdueNotification) {
//                ReminderModel reminderqRFI = new ReminderModel();
//                reminderqRFI.rfi = rfi;
//                reminderqRFI.mainType = "rfi";
//                reminderqRFI.title = rfi.subject;
//                reminderqRFI.reminderDate = rfi.dueDate;
//                reminderqRFI.reminderUsers = new ArrayList<ReminderUser>();
//                reminderqRFI.addUser(rfi.questionReceivedFrom);
//                for (User us : users) reminderqRFI.addUser(us);
//                reminderqRFI.create();
//            }
            UserLiveRoom.get().notification(Users.getUser(), "RFI", users, rfi.id);
        }
        JsonObject data = new JsonObject();
        data.addProperty("status", rfi.status.status);
        data.addProperty("id", rfi.id);
        renderJSON(data);
    }

    @Check(Consts.permissionRFI)
    public static void drawingAttach(String filename, String filedir, String extension) {
        RFI_Attach attach = new RFI_Attach();
        attach.name = filename;
        attach.extension = extension;
        attach.dir = filedir;
        attach.create();
        renderText(attach.id);
    }

    public static void track(Long id, String note, String[] filename, String[] filedir, String[] extension, Float[] filesize) {
        RFI rfi = RFI.findById(id);
        RFI_Tracking tracking = new RFI_Tracking();
        tracking.author = Users.getUser();
        tracking.createDate = new Date();
        tracking.note = note;
        tracking.rfi = rfi;
        tracking.create();

        if (filename != null) {
            for (int i = 0; i < filename.length; i++) {
                RFI_Attach attach = new RFI_Attach();
                attach.name = filename[i];
                attach.extension = extension[i];
                attach.dir = filedir[i];
                attach.filesize = filesize[i];
                attach.tracking = tracking;
                attach.create();
            }
        }
        List<User> users = new ArrayList<User>();
        if (rfi.assignee.id.equals(Users.getUser().id))
            users.add(rfi.questionReceivedFrom);
        else if (rfi.questionReceivedFrom.id.equals(Users.getUser().id))
            users.add(rfi.assignee);
        else {
            users.add(rfi.questionReceivedFrom);
            users.add(rfi.assignee);
        }
        if (rfi.distributions != null)
            for (int i = 0; i < rfi.distributions.size(); i++) {
                RFI_Distribution dist = rfi.distributions.get(i);
                if (dist.getCode().equalsIgnoreCase("t")) {
                    List<User> team = User.find("userTeam.id = " + dist.getId()).fetch();
                    for (User us : team)
                        if (!us.id.equals(Users.getUser().id))
                            users.addAll(team);
                } else {
                    if (!dist.getId().equals(Users.getUser().id)) {
                        User dis = User.findById(dist.getId());
                        users.add(dis);
                    }
                }
            }
        UserLiveRoom.get().notification(Users.getUser(), "Tracking", users, tracking.id);
        show(id, "track");
    }

    public static void close(Long id) {
        RFI rfi = RFI.findById(id);
        JsonObject data = new JsonObject();
        String stat = "";
        if (rfi.questionReceivedFrom.id == Users.getUser().id) {
            if (rfi.status.status.equalsIgnoreCase("Open")) {
                rfi.status = RFI_Status.find("status = ?1", "Closed").first();
                rfi.closedDate = new Date();
                stat = "close";
            } else {
                rfi.status = RFI_Status.find("status = ?1", "Open").first();
                stat = "open";
            }
            rfi._save();
            if (rfi.overdueNotification) {
                List<ReminderModel> reminders = ReminderModel.find("mainType = 'rfi' and rfi.id = " + rfi.id).fetch();
                for (ReminderModel rem : reminders) rem._delete();
                rfi.reminderModels = new ArrayList<ReminderModel>();
            }
        }
        show(id, stat);
    }

    @Check(Consts.permissionRFI)
    public static void delete(Long id) {
        RFI rfi = RFI.findById(id);
        JsonObject data = new JsonObject();
        if (rfi.questionReceivedFrom.id == Users.getUser().id) {
            rfi._delete();
            data.addProperty("type", "success");
            data.addProperty("title", "Амжилттай устгагдлаа!");
        } else {
            data.addProperty("type", "error");
            data.addProperty("title", "Устгах боломжгүй!");
        }
        renderJSON(data);
    }

    public static void show(Long id, String stat) {
        RFI rfi = RFI.findById(id);
        List<NotificationMessage> rfiNotf = NotificationMessage.find("rfi.id = " + rfi.id + " and acceptor.id = " + Users.getUser().id + " and seen <> true").fetch();
        List<NotificationMessage> trackingNotf = NotificationMessage.find("tracking.rfi.id = " + rfi.id + " and acceptor.id = " + Users.getUser().id + " and seen <> true").fetch();
        rfiNotf.addAll(trackingNotf);
        for (NotificationMessage notf : rfiNotf)
            if (notf != null && !notf.seen) {
                notf.seen = true;
                notf._save();
            }
        render(rfi, stat);
    }

    public static void print(Long id) {
        RFI rfi = RFI.findById(id);
        if (rfi.isRelated(Users.getUser().id)) {
            List<RFI_Tracking> trackings = RFI_Tracking.find("rfi.id = " + id + " order by createDate desc").fetch();
            render(rfi, trackings);
        }
    }

    public static boolean checkDist(RFI_Distribution[] rfi_distributions, int index, String[] ids) {
        for (int i = 0; i < index; i++) {
            if (ids[0].equalsIgnoreCase(rfi_distributions[i].code))
                if (ids[0].equalsIgnoreCase("u")) {
                    if (rfi_distributions[i].user.id == Long.parseLong(ids[1]))
                        return false;
                } else {
                    if (rfi_distributions[i].userTeam.id == Long.parseLong(ids[1]))
                        return false;
                }
        }
        return true;
    }

    public static List<User> getUsers() {
        List<ProjectAssignRel> rels = ProjectAssignRel.find("project.id=?1", Users.pid()).fetch();
        List<User> users = new ArrayList<User>();
        for (ProjectAssignRel rel : rels) users.add(rel.user);
        return users;
    }

    public static int checkNewRFI() {
        int countRFI = NotificationMessage.find("rfi <> null and rfi.project.id = " + Users.pid() + " and rfi.status.status = 'Open' and acceptor.id = " + Users.getUser().id + " and seen <> true").fetch().size();
        int countTrack = NotificationMessage.find("tracking <> null and tracking.rfi.project.id = " + Users.pid() + " and tracking.rfi.status.status = 'Open' and acceptor.id = " + Users.getUser().id + " and seen <> true").fetch().size();
        return countRFI + countTrack;
    }

    @Check(Consts.permissionRFI)
    public static String checkNewRFIandTracking(Long id) {
        RFI rfi = RFI.findById(id);
        if (rfi.trackings.size() > 0) {
            NotificationMessage message = NotificationMessage.find("tracking.rfi.id = " + rfi.id + " and tracking.rfi.status.status = 'Open' and acceptor.id = " + Users.getUser().id + " and seen = 0").first();
            if (message != null)
                return "хариу";
        } else {
            NotificationMessage punch = NotificationMessage.find("rfi.id = " + rfi.id + " and rfi.status.status = 'Open' and acceptor.id = " + Users.getUser().id + " and seen = 0").first();
            if (punch != null)
                return "шинэ";
        }
        return "";
    }
}