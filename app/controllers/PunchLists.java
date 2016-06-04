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
public class PunchLists extends CRUD {
    @Check(Consts.permissionGreatePunchList)
    public static void list(String status) {
        User user = Users.getUser();
        UserTeam userTeam = Users.getUser().userTeam;
        List<PunchList_Distribution> user_distributions = user.punchList_distributions;
        user_distributions.addAll(userTeam.punchList_distributions);
        String query = "project.id =" + Users.pid() + " and (";
        for (int i = 0; i < user_distributions.size(); i++) {
            if (user_distributions.get(i).punchList != null) {
                if (!user_distributions.get(i).punchList.status.status.equalsIgnoreCase("Draft"))
                    query += "id = " + user_distributions.get(i).punchList.id + " or ";
            }
        }
        query += "not(questionReceivedFrom.id <> " + Users.getUser().id + " and assignee.id <> " + Users.getUser().id + " and private_ = true))";
        int open, close, all, draft;
        open = PunchList.find(query + " and status.status = 'NotResolved'").fetch().size();
        close = PunchList.find(query + " and status.status = 'Resolved'").fetch().size();
        draft = PunchList.find("status.status = 'Draft' and questionReceivedFrom.id = " + Users.getUser().id).fetch().size();
        all = open + close;

        List<OrganizationTeam> teams = FunctionController.getOrganizationTeams();
        List<ProjectObject> projectObjects = ProjectObject.find("project.id = " + Users.pid()).fetch();
        render(teams, projectObjects, status, all, open, close, draft);
    }

    @Check(Consts.permissionGreatePunchList)
    public static void listAjax(String status, String sender, String assignee, String keyword, int CurrentPageNumber, String tasky) {
        boolean firstload = false;
        if (status.equals("Me")) {
            firstload = true;
        }
        int pageLimit = 40;
        //Selector Query create
        User user = Users.getUser();
        UserTeam userTeam = Users.getUser().userTeam;
        List<PunchList_Distribution> user_distributions = user.punchList_distributions;
        user_distributions.addAll(userTeam.punchList_distributions);
        String query = "project.id =" + Users.pid() + " and (";
        for (int i = 0; i < user_distributions.size(); i++) {
            if (user_distributions.get(i).punchList != null) {
                if (!user_distributions.get(i).punchList.status.status.equalsIgnoreCase("Draft"))
                    query += "id = " + user_distributions.get(i).punchList.id + " or ";
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
                    List<NotificationMessage> notificationMessages = NotificationMessage.find("reply.punchList.project.id = " + Users.pid() + " and reply.punchList.status.status = 'NotResolved' and acceptor.id = " + Users.getUser().id + " and seen = 0").fetch();
                    query = "id = 0";
                    if (notificationMessages != null) {
                        query = "(";
                        for (int i = 0; i < notificationMessages.size(); i++)
                            query += " id = " + notificationMessages.get(i).reply.punchList.id + " or";
                        query += " 1 = 0)";
                    }
                } else
                    query += " and status.status = '" + status + "'";
            } else {
                query += " and status.status <> 'Draft'";
            }
        } else {
            query += " and status.status = 'NotResolved'";
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
        //Add punchList filter
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
            query += " and ( name like '%" + keyword + "%' or assignee.firstName like '%" + keyword + "%' or questionReceivedFrom.firstName like '%" + keyword + "%' ) ";
        } else
            keyword = "";
        //--------------------------------------------

        //Page Number counter
        List<PunchList> punchListMaxSizer = PunchList.find(query + " order by id desc").fetch();
        MaxPageNumber = punchListMaxSizer.size() / pageLimit;
        if (punchListMaxSizer.size() % pageLimit != 0) MaxPageNumber++;

        //Overdue calculate
        Date now = new Date();
        now = new Date(now.getTime() - (1000 * 60 * 60 * 24));
        Date week = new Date(now.getTime() + (1000 * 60 * 60 * 24 * 7));
        Long weeker = 0L, overdue = 0L, nextWeeker = 0L;
        if (!status.equalsIgnoreCase("Draft"))
            for (int i = 0; i < punchListMaxSizer.size(); i++) {
                if (punchListMaxSizer.get(i).status.status.equalsIgnoreCase("NotResolved")) {
                    if (now.after(punchListMaxSizer.get(i).dueDate)) overdue++;
                    else if (week.after(punchListMaxSizer.get(i).dueDate)) weeker++;
                    else nextWeeker++;
                }
            }

        //--------------------------------------------

        //Page selector
        List<PunchList> punchLists = PunchList.find(query + " order by createDate desc").fetch(CurrentPageNumber, pageLimit);

        //--------------------------------------------
        render(punchLists, keyword, now, week, CurrentPageNumber, MaxPageNumber, status, sender, assignee, tasky, overdue, weeker, nextWeeker, firstload);

    }

    @Check(Consts.permissionGreatePunchList)
    public static void loadGraphic() {
        Date today = Functions.convertHourNull(new Date());
        Date nextWeek = Functions.addOrMinusDays(today, 7, true);
        List<PunchList> punchLists = PunchList.find("project.id =" + Users.pid() + " and status.status = 'NotResolved' order by assignee.userPosition.rate, assignee.firstName, assignee.id").fetch();
        List<UserGraphic> userGraphics = new ArrayList<UserGraphic>();
        Long uid = 0L, overdue = 0L, weeker = 0L, nextWeeker = 0L;
        for (PunchList punchList : punchLists) {
            if (punchList.assignee.id.compareTo(uid) != 0) {
                UserGraphic userGraphic = new UserGraphic();
                userGraphic.user = punchList.assignee;
                userGraphic.all = 0L;
                userGraphic.overdue = 0L;
                userGraphic.nextWeek = 0L;
                userGraphic.nNextWeek = 0L;
                userGraphics.add(userGraphic);
                uid = punchList.assignee.id;
            }
            userGraphics.get(userGraphics.size() - 1).all++;
            if (punchList.dueDate.before(today)) {
                userGraphics.get(userGraphics.size() - 1).overdue++;
                overdue++;
            } else if (punchList.dueDate.getTime() >= today.getTime() && punchList.dueDate.before(nextWeek)) {
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
        render(userGraphics, overdue, weeker, nextWeeker);
    }

    @Check(Consts.permissionGreatePunchList)
    public static void blank(Long id, Long taskId, Long assigneeId, String path, Long revision) {
        User user = Users.getUser();
        if (user.getPermissionType(Consts.permissionGreatePunchList) != 3) forbidden();
        Task task = Task.findById(taskId);
        List<OrganizationTeam> teams = FunctionController.getOrganizationTeams();
        PunchList punchList = PunchList.findById(id);
        render(punchList, teams, task, assigneeId, path, revision);
    }

    @Check(Consts.permissionGreatePunchList)
    public static void create(Long punchListId, String subject, Date dueDate, Long taskId, String location, String reference,
                              Long revisionId, String jsonString,
                              Long assignedTo, String distribution, String schedule, String cost,
                              String priority, String description,
                              boolean draft, boolean private_, String[] filename,
                              String[] filedir, String[] extension, Float[] filesize) {
        PunchList punchList;
        if (punchListId == null)
            punchList = new PunchList();
        else
            punchList = PunchList.findById(punchListId);
        punchList.name = subject;
        punchList.assignee = User.findById(assignedTo);
        punchList.project = Project.findById(Users.pid());
        Project project = Project.findById(Users.pid());
        punchList.createDate = new Date();
        punchList.dueDate = dueDate;
        punchList.private_ = private_;
        if (taskId != null)
            punchList.task = Task.findById(taskId);
        punchList.questionReceivedFrom = Users.getUser();
        if (!draft) {
            ProjectCounter counter = project.projectCounter;
            Long number = counter.lastPunchListNo;
            punchList.No = number + 1;
            counter.lastPunchListNo = number + 1;
            counter._save();
            punchList.status = PunchList_Status.find("status = ?1", "NotResolved").first();
        } else {
            punchList.No = 0l;
            punchList.status = PunchList_Status.find("status = ?1", "Draft").first();
        }
        punchList.location = location;
        punchList.reference = reference;
        punchList.scheduleImpact = PunchList_Impact.find("impact = ?1", schedule).first();
        punchList.costImpact = PunchList_Impact.find("impact = ?1", cost).first();
        punchList.priority = PunchList_Priority.find("priority = ?1", priority).first();
        punchList.description = description;
        if (punchListId == null)
            punchList.create();
        else {
            for (PunchList_Attach att : punchList.attaches) {
                att._delete();
            }
            punchList.attaches = new ArrayList<PunchList_Attach>();
            for (DrawingLayer pin : punchList.pins) {
                pin._delete();
            }
            punchList.pins = new ArrayList<DrawingLayer>();
            for (PunchList_Distribution dist : punchList.distributions) {
                dist._delete();
            }
            punchList.distributions = new ArrayList<PunchList_Distribution>();
            punchList._save();

        }
        if (filename != null) {
            for (int i = 0; i < filename.length; i++) {
                PunchList_Attach attach = new PunchList_Attach();
                attach.name = filename[i];
                attach.extension = extension[i];
                attach.dir = filedir[i];
                attach.filesize = filesize[i];
                attach.punchList = punchList;
                attach.create();
            }
        }
        if (revisionId != null) {
            DrawingLayer layer = new DrawingLayer(punchList, jsonString, revisionId, "drawing");
            layer.create();
        }
        if (distribution.length() > 0) {
            String[] distris = distribution.split(",");
            PunchList_Distribution[] punchList_distributions = new PunchList_Distribution[distris.length];
            for (int i = 0, j = 0; i < distris.length; i++) {
                String[] ids = distris[i].split("-");
                if (checkDist(punchList_distributions, j, ids)) {
                    punchList_distributions[j] = new PunchList_Distribution(ids[0], punchList, Long.parseLong(ids[1]));
                    punchList_distributions[j].create();
                    j++;
                }
            }
        }

        if (!draft) {
            List<User> users = new ArrayList<User>();
            users.add(punchList.assignee);
            if (punchList.distributions != null)
                for (int i = 0; i < punchList.distributions.size(); i++) {
                    PunchList_Distribution dist = punchList.distributions.get(i);
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
            UserLiveRoom.get().notification(Users.getUser(), "PunchList", users, punchList.id);
        }
        JsonObject data = new JsonObject();
        data.addProperty("status", punchList.status.status);
        data.addProperty("id", punchList.id);
        renderJSON(data);
    }

//    public static void show(String type, Long id) {
//        System.out.println(type + " " + id);
//        PunchList punchList;
//        if (type.equals("p"))
//            punchList = PunchList.findById(id);
//        else {
//            PunchList_Reply reply = PunchList_Reply.findById(id);
//            punchList = reply.punchList;
//        }
//        List<NotificationMessage> punchListNotf = NotificationMessage.find("punchList.id = " + punchList.id + " and acceptor.id = " + Users.getUser().id + " and seen <> true").fetch();
//        List<NotificationMessage> replyNotf = NotificationMessage.find("reply.punchList.id = " + punchList.id + " and acceptor.id = " + Users.getUser().id + " and seen <> true").fetch();
//        punchListNotf.addAll(replyNotf);
//        for (NotificationMessage notf : punchListNotf)
//            if (notf != null && !notf.seen) {
//                notf.seen = true;
//                notf._save();
//            }
//        render(punchList);
//    }

    public static void show(Long id, String stat) {
        PunchList punchList = PunchList.findById(id);
        List<PunchList_Reply> replies = PunchList_Reply.find("punchList.id = " + id + " order by createDate desc").fetch();
        List<NotificationMessage> punchListNotf = NotificationMessage.find("punchList.id = " + punchList.id + " and acceptor.id = " + Users.getUser().id + " and seen <> true").fetch();
        List<NotificationMessage> replyNotf = NotificationMessage.find("reply.punchList.id = " + punchList.id + " and acceptor.id = " + Users.getUser().id + " and seen <> true").fetch();
        punchListNotf.addAll(replyNotf);
        for (NotificationMessage notf : punchListNotf)
            if (notf != null && !notf.seen) {
                notf.seen = true;
                notf._save();
            }

        render(punchList, replies, stat);
    }

    public static void reply(Long id, String response, String[] filename, String[] filedir, String[] extension, Float[] filesize) {
        PunchList punchList = PunchList.findById(id);
        PunchList_Reply reply = new PunchList_Reply();
        reply.author = Users.getUser();
        reply.createDate = new Date();
        reply.response = response;
        reply.punchList = punchList;
        reply.create();

        if (filename != null) {
            for (int i = 0; i < filename.length; i++) {
                PunchList_Attach attach = new PunchList_Attach();
                attach.name = filename[i];
                attach.extension = extension[i];
                attach.dir = filedir[i];
                attach.filesize = filesize[i];
                attach.reply = reply;
                attach.create();
            }
        }
        List<User> users = new ArrayList<User>();
        if (punchList.assignee.id.equals(Users.getUser().id))
            users.add(punchList.questionReceivedFrom);
        else if (punchList.questionReceivedFrom.id.equals(Users.getUser().id))
            users.add(punchList.assignee);
        else {
            users.add(punchList.questionReceivedFrom);
            users.add(punchList.assignee);
        }

        if (punchList.distributions != null)
            for (int i = 0; i < punchList.distributions.size(); i++) {
                PunchList_Distribution dist = punchList.distributions.get(i);
                if (dist.getCode().equalsIgnoreCase("t")) {
                    List<User> team = User.find("userTeam.id = " + dist.getId()).fetch();
                    for (User us : team)
                        if (!us.id.equals(Users.getUser().id))
                            users.add(us);
                } else {
                    if (!dist.getId().equals(Users.getUser().id)) {
                        User dis = User.findById(dist.getId());
                        users.add(dis);
                    }
                }
            }
        UserLiveRoom.get().notification(Users.getUser(), "Reply", users, reply.id);
        show(id, "reply");
    }

    public static void close(Long id) {
        PunchList punchList = PunchList.findById(id);
        String stat = "";
        if (punchList.questionReceivedFrom.id == Users.getUser().id) {
            if (punchList.status.status.equalsIgnoreCase("NotResolved")) {
                punchList.status = PunchList_Status.find("status = ?1", "Resolved").first();
                punchList.closedDate = new Date();
                stat = "close";
            } else {
                punchList.status = PunchList_Status.find("status = ?1", "NotResolved").first();
                stat = "open";
            }
            punchList._save();
        }
        show(id, stat);
    }

    public static void deleteReply(Long replyId) {
        PunchList_Reply reply = PunchList_Reply.findById(replyId);
        Long id = reply.punchList.id;
        reply._delete();
        show(id, "replyDelete");
    }

    @Check(Consts.permissionGreatePunchList)
    public static void delete(Long id) {
        PunchList punchList = PunchList.findById(id);
        JsonObject data = new JsonObject();
        if (punchList.questionReceivedFrom.id == Users.getUser().id) {
            punchList._delete();
            data.addProperty("type", "success");
            data.addProperty("title", "Амжилттай устгагдлаа!");
        } else {
            data.addProperty("type", "error");
            data.addProperty("title", "Устгах боломжгүй!");
        }
        renderJSON(data);
    }

    public static void print(Long id) {
        PunchList punchList = PunchList.findById(id);
        if (punchList.isRelated(Users.getUser().id)) {
            List<PunchList> punchList_s = PunchList.find("punchList.id = " + id).fetch();
            render(punchList, punchList_s);
        } else
            list("All");
    }

    public static boolean checkDist(PunchList_Distribution[] punchList_distributions, int index, String[] ids) {
        for (int i = 0; i < index; i++) {
            if (ids[0].equalsIgnoreCase(punchList_distributions[i].code))
                if (ids[0].equalsIgnoreCase("u")) {
                    if (punchList_distributions[i].user.id == Long.parseLong(ids[1]))
                        return false;
                } else {
                    if (punchList_distributions[i].userTeam.id == Long.parseLong(ids[1]))
                        return false;
                }
        }
        return true;
    }

    public static void handleDocumentAttachment(String fileDir, String fileName, String extension, Float filesize) {
        String img = "<li><span>" + Functions.handleDocumentAttachment(fileDir, fileName, extension, filesize) + "</span></li>";
        renderText(img);
    }

    public static List<User> getUsers() {
        List<ProjectAssignRel> rels = ProjectAssignRel.find("project.id=?1", Users.pid()).fetch();
        List<User> users = new ArrayList<User>();
        for (ProjectAssignRel rel : rels) users.add(rel.user);
        return users;
    }

    public static int checkNewPunchList() {
        int countPunch = NotificationMessage.find("punchList <> null and punchList.project.id = " + Users.pid() + " and punchList.status.status = 'NotResolved' and acceptor.id = " + Users.getUser().id + " and seen <> true").fetch().size();
        int countReply = NotificationMessage.find("reply <> null and reply.punchList.project.id = " + Users.pid() + " and reply.punchList.status.status = 'NotResolved' and acceptor.id = " + Users.getUser().id + " and seen <> true").fetch().size();
        return countPunch + countReply;
    }

    @Check(Consts.permissionGreatePunchList)
    public static String checkNewPunchListandReply(Long id) {
        PunchList punchList = PunchList.findById(id);
        if (punchList.replies.size() > 0) {
            NotificationMessage message = NotificationMessage.find("reply.punchList.id = " + punchList.id + " and reply.punchList.status.status = 'NotResolved' and acceptor.id = " + Users.getUser().id + " and seen = 0").first();
            if (message != null)
                return "хариу";
        } else {
            NotificationMessage punch = NotificationMessage.find("punchList.id = " + punchList.id + " and punchList.status.status = 'NotResolved' and acceptor.id = " + Users.getUser().id + " and seen = 0").first();
            if (punch != null)
                return "шинэ";
        }
        return "";
    }
}