package controllers.App;

import com.google.gson.*;
import controllers.Consts;
import controllers.UserLiveRoom;
import controllers.Users;
import models.*;
import play.db.jpa.GenericModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Rina on 4/30/15.
 */
public class DelegatePunchList extends Delegate {

    public static void PunchListList(String body) {
        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());

        if (user != null) {
            UserTeam userTeam = user.userTeam;
            List<PunchList_Distribution> user_distributions = user.punchList_distributions;
            user_distributions.addAll(userTeam.punchList_distributions);
            Long pid = json.get("projectid").getAsLong();
            String query = "status.status <> 'Draft' and ";
            if (pid.equals(new Long(0))) {
                query += "(";
                List<Project> projects = Project.find("portfolio.owner.id=?1", user.id).fetch();
                List<OrganizationChart> organizationCharts = OrganizationChart.find("user.id=?1", user.id).fetch();
                for (OrganizationChart rel : organizationCharts) {
                    if (!projects.contains(rel.project)) query += "project.id=" + rel.project.id + " OR ";
                }
                for (Project project : projects)
                    query += "project.id=" + project.id + " OR ";
                query += "1 = 0) and (";
                for (int i = 0; i < user_distributions.size(); i++) {
                    if (user_distributions.get(i).punchList != null) {
                        if (!user_distributions.get(i).punchList.status.status.equalsIgnoreCase("Draft"))
                            query += "id = " + user_distributions.get(i).punchList.id + " or ";
                    }
                }
                query += "not(questionReceivedFrom.id <> " + user.id + " and assignee.id <> " + user.id + " and private_ = true)) and (status.status = 'NotResolved' or createDate >= :date)";
            } else {
                query += "project.id =" + pid + " and (";
                for (int i = 0; i < user_distributions.size(); i++) {
                    if (user_distributions.get(i).punchList != null) {
                        if (!user_distributions.get(i).punchList.status.status.equalsIgnoreCase("Draft"))
                            query += "id = " + user_distributions.get(i).punchList.id + " or ";
                    }
                }
                query += "not(questionReceivedFrom.id <> " + user.id + " and assignee.id <> " + user.id + " and private_ = true)) and (status.status = 'NotResolved' or createDate >= :date)";
            }
            Date currentDate = new Date();
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                currentDate = dateFormat.parse(json.get("date").getAsString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            GenericModel.JPAQuery q = PunchList.find(query).setParameter("date", currentDate);
            List<PunchList> punchLists = q.fetch();
            JsonArray obj = new JsonArray();
            for (PunchList punchList : punchLists) {
                JsonObject object = new JsonObject();
                object.addProperty("assignee", punchList.assignee.id);
                object.addProperty("balanceCost", punchList.costImpact.impact.equalsIgnoreCase("Yes") ? "Тийм" : (punchList.costImpact.impact.equalsIgnoreCase("No") ? "Үгүй" : "Мэдэхгүй"));
                object.addProperty("date", punchList.createDate.getTime());
                if (punchList.status.status.equalsIgnoreCase("Resolved"))
                    object.addProperty("closedDate", punchList.closedDate.getTime());
                object.addProperty("desc", punchList.description);
                JsonArray distrib = new JsonArray();
                String id;
                for (PunchList_Distribution dis : punchList.distributions) {
                    if (dis.code.equals("t")) {
                        for (User usr : dis.userTeam.users) {
                            id = String.valueOf(usr.id);
                            distrib.add(new JsonPrimitive(id));
                        }
                    } else {
                        id = String.valueOf(dis.user.id);
                        distrib.add(new JsonPrimitive(id));
                    }
                }
                object.add("distribution", distrib);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                object.addProperty("dueDate", dateFormat.format(punchList.dueDate));
                JsonArray files = new JsonArray();
                for (PunchList_Attach attach : punchList.attaches) {
                    files.add(new JsonPrimitive(attach.dir.replace("/", "-") + "." + attach.extension));
                }
                object.add("filenames", files);
                object.addProperty("fromUserId", punchList.questionReceivedFrom.id);
                object.addProperty("graphCost", punchList.scheduleImpact.impact.equalsIgnoreCase("Yes") ? "Тийм" : (punchList.scheduleImpact.impact.equalsIgnoreCase("No") ? "Үгүй" : "Мэдэхгүй"));
                object.addProperty("id", punchList.id);
                object.addProperty("isPrivate", punchList.private_ ? 1 : 0);
                object.addProperty("location", getString(punchList.location));
                object.addProperty("reason", getString(punchList.reference));
                object.addProperty("priority", punchList.priority.priority.equalsIgnoreCase("High") ? "Нэн даруй" : (punchList.scheduleImpact.impact.equalsIgnoreCase("Medium") ? "Яаралтай" : "Хэвийн"));
                object.addProperty("number", punchList.No);
                object.addProperty("open", punchList.status.status.equals("NotResolved") ? 0 : 1);
                object.addProperty("title", getString(punchList.name));
                object.addProperty("tempId", punchList.tempId != null ? punchList.tempId : 0);
                if (pid.equals(new Long(0)))
                    object.addProperty("projectId", punchList.project.id);
                else
                    object.addProperty("projectId", pid);
                object.addProperty("jobSubId", punchList.task != null ? punchList.task.id : 0);
                JsonObject names = new JsonObject();
                names.addProperty("assigneename", punchList.assignee.getLastnameFirstCharacter() + ". " + punchList.assignee.firstName);
                names.addProperty("fromusername", punchList.questionReceivedFrom.getLastnameFirstCharacter() + ". " + punchList.questionReceivedFrom.firstName);
                names.addProperty("jobname", punchList.task != null ? punchList.task.projectObject.name + ", " + punchList.task.name : "");
                object.add("names", names);
                obj.add(object);
            }
            renderJSON(obj);
        }

    }

    public static void PunchListNew(String body) throws ParseException {
        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            JsonObject punchListDet = json.get("mainPunchList").getAsJsonObject();

            PunchList punch = PunchList.find("id = " + punchListDet.get("id").getAsLong() + (punchListDet.get("tempid").getAsLong() != 0l ? " or tempId = " + punchListDet.get("tempid").getAsLong() : "")).first();
            if (punch == null) {
                Long pid = punchListDet.get("projectid").getAsLong();
                PunchList punchList = new PunchList();
                JsonObject object = new JsonObject();
                Project project = Project.findById(pid);
                ProjectCounter counter = project.projectCounter;
                Long number = counter.lastPunchListNo;
                punchList.No = number + 1;
                counter.lastPunchListNo = number + 1;
                counter._save();
                punchList.description = punchListDet.get("desc").getAsString();
                punchList.name = punchListDet.get("title").getAsString();
                punchList.questionReceivedFrom = User.findById(punchListDet.get("fromuserid").getAsLong());
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                String dater = punchListDet.get("duedate").getAsString();
                punchList.dueDate = dateFormat.parse(dater);
                punchList.createDate = new Date();
                punchList.tempId = punchListDet.get("tempid").getAsLong();
                punchList.project = Project.findById(pid);
                punchList.assignee = User.findById(punchListDet.get("assignee").getAsLong());
                punchList.location = punchListDet.get("location").getAsString();
                String cost = punchListDet.get("graphcost").getAsString();
                punchList.scheduleImpact = PunchList_Impact.find("impact = '" + (cost.equals("Тийм") ? "Yes" : (cost.equals("Үгүй") ? "No" : "Unknown")) + "'").first();
                cost = punchListDet.get("balancecost").getAsString();
                punchList.costImpact = PunchList_Impact.find("impact = '" + (cost.equals("Тийм") ? "Yes" : (cost.equals("Үгүй") ? "no" : "Unknown")) + "'").first();
                cost = punchListDet.get("priority").getAsString();
                punchList.priority = PunchList_Priority.find("priority = '" + (cost.equals("Нэн даруй") ? "High" : (cost.equals("Яаралтай") ? "Medium" : "Low")) + "'").first();
                punchList.private_ = (punchListDet.get("isprivate").getAsString().equalsIgnoreCase("0") ? false : true);
                punchList.status = PunchList_Status.find("status = '" + (punchListDet.get("open").getAsString().equalsIgnoreCase("0") ? "NotResolved" : "Resolved") + "'").first();
                if (!punchListDet.get("open").getAsString().equalsIgnoreCase("0")) {
                    punchList.closedDate = new Date();
                    object.addProperty("closedDate", punchList.closedDate.getTime());
                }
                if (!punchListDet.get("jobsubid").isJsonNull())
                    punchList.task = Task.findById(punchListDet.get("jobsubid").getAsLong());
                punchList.create();
                JsonArray filenames = punchListDet.get("filenames").getAsJsonArray();
                Date currentDate = new Date();
                if (filenames.size() > 0) {
                    for (int i = 0; i < filenames.size(); i++) {
                        JsonPrimitive ids = filenames.get(i).getAsJsonPrimitive();
                        PunchList_Attach attach = new PunchList_Attach();
                        String[] file = ids.getAsString().split("\\.");
                        String[] fileName = file[0].split("/");
                        attach.dir = file[0];
                        attach.name = fileName[fileName.length - 1];
                        attach.punchList = punchList;
                        attach.extension = file[1];
                        attach.create();
                    }
                }
                JsonArray distrib = punchListDet.get("distribution").getAsJsonArray();
                for (int i = 0; i < distrib.size(); i++) {
                    JsonPrimitive ids = distrib.get(i).getAsJsonPrimitive();
                    PunchList_Distribution distribution = new PunchList_Distribution("u", punchList, ids.getAsLong());
                    distribution.create();
                }
                object.addProperty("newId", punchList.id);
                object.addProperty("date", punchList.createDate.getTime());
                object.addProperty("tempId", punchList.tempId);
                object.addProperty("number", punchList.No);
                List<User> users = new ArrayList<User>();
                users.add(punchList.assignee);
                if (punchList.distributions != null)
                    for (int i = 0; i < punchList.distributions.size(); i++) {
                        PunchList_Distribution dist = punchList.distributions.get(i);
                        if (dist.getCode().equalsIgnoreCase("t")) {
                            List<User> team = User.find("userTeam.id = " + dist.getId()).fetch();
                            users.addAll(team);
                        } else {
                            User dis = User.findById(dist.getId());
                            users.add(dis);
                        }
                    }
                UserLiveRoom.get().notification(user, "PunchList", users, punchList.id);
                renderJSON(object);
            } else {
                PunchList punchList = punch;
                JsonObject object = new JsonObject();
                punchList.status = PunchList_Status.find("status = '" + (punchListDet.get("open").getAsString().equalsIgnoreCase("0") ? "NotResolved" : "Resolved") + "'").first();
                if (!punchListDet.get("open").getAsString().equalsIgnoreCase("0")) {
                    punchList.closedDate = new Date();
                    object.addProperty("closedDate", punchList.closedDate.getTime());
                }
                punchList._save();
                object.addProperty("newId", punchList.id);
                object.addProperty("date", punchList.createDate.getTime());
                object.addProperty("tempId", punchList.tempId);
                object.addProperty("number", punchList.No);
                renderJSON(object);
            }
        }
    }

    public static void PunchListReplyList(String body) {
        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());

        if (user != null) {
            Long punchid = json.get("punchlistid").getAsLong();
            String repQuery = "";
            if (punchid.equals(new Long(0))) {
                UserTeam userTeam = user.userTeam;
                List<PunchList_Distribution> user_distributions = user.punchList_distributions;
                user_distributions.addAll(userTeam.punchList_distributions);
                String query = "status.status <> 'Draft' and (";
                List<Project> projects = Project.find("portfolio.owner.id=?1", user.id).fetch();
                List<OrganizationChart> organizationCharts = OrganizationChart.find("user.id=?1", user.id).fetch();
                for (OrganizationChart rel : organizationCharts) {
                    if (!projects.contains(rel.project)) query += "project.id=" + rel.project.id + " OR ";
                }
                for (Project project : projects)
                    query += "project.id=" + project.id + " OR ";
                query += "1 = 0) and (";
                for (int i = 0; i < user_distributions.size(); i++) {
                    if (user_distributions.get(i).punchList != null) {
                        if (!user_distributions.get(i).punchList.status.status.equalsIgnoreCase("Draft"))
                            query += "id = " + user_distributions.get(i).punchList.id + " or ";
                    }
                }
                query += "not(questionReceivedFrom.id <> " + user.id + " and assignee.id <> " + user.id + " and private_ = true)) and (status.status = 'NotResolved' or createDate >= :date)";
                Date currentDate = new Date();
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                    currentDate = dateFormat.parse(json.get("date").getAsString());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                GenericModel.JPAQuery q = PunchList.find(query).setParameter("date", currentDate);
                List<PunchList> punchLists = q.fetch();
                repQuery = "";
                for (PunchList punchList : punchLists)
                    repQuery += "punchList.id = " + punchList.id + " or ";
                repQuery += "1 = 0";
            } else {
                repQuery += "punchList.id = " + punchid;
            }
            List<PunchList_Reply> replies = PunchList_Reply.find(repQuery).fetch();
            JsonArray obj = new JsonArray();
            for (PunchList_Reply reply : replies) {
                JsonObject object = new JsonObject();
                object.addProperty("id", reply.id);
                object.addProperty("text", reply.response);
                object.addProperty("userId", reply.author.id);
                object.addProperty("username", reply.author.getLastnameFirstCharacter() + ". " + reply.author.firstName);
                object.addProperty("date", reply.createDate.getTime());
                object.addProperty("tempId", reply.tempId != null ? reply.tempId : 0);
                object.addProperty("punchListId", reply.punchList.id);
                JsonArray files = new JsonArray();
                for (PunchList_Attach attach : reply.attaches) {
                    files.add(new JsonPrimitive(attach.dir.replace("/", "-") + "." + attach.extension));
                }
                object.add("filenames", files);
                obj.add(object);
            }
            renderJSON(obj);
        }

    }

    public static void PunchListReplyNew(String body) throws ParseException {
        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        JsonObject replyDet = json.get("punchListReply").getAsJsonObject();

        PunchList_Reply reply = new PunchList_Reply();
        reply.response = replyDet.get("text").getAsString();
        reply.author = user;
        reply.createDate = new Date();
        reply.tempId = replyDet.get("tempid").getAsLong();
        PunchList punchList = PunchList.findById(replyDet.get("punchlistid").getAsLong());
        reply.punchList = punchList;
        reply.create();
        JsonArray filenames = replyDet.get("filenames").getAsJsonArray();
        if (filenames.size() > 0) {
            for (int i = 0; i < filenames.size(); i++) {
                JsonPrimitive ids = filenames.get(i).getAsJsonPrimitive();
                PunchList_Attach attach = new PunchList_Attach();
                String[] file = ids.getAsString().split("\\.");
                String[] fileName = file[0].split("/");
                attach.dir = file[0];
                attach.name = fileName[fileName.length - 1];
                attach.extension = file[1];
                attach.reply = reply;
                attach.create();
            }
        }
        List<User> users = new ArrayList<User>();
        if (punchList.assignee.id.equals(user.id))
            users.add(punchList.questionReceivedFrom);
        else if (punchList.questionReceivedFrom.id.equals(user.id))
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
                        if (!us.id.equals(user.id))
                            users.add(us);
                } else {
                    if (!dist.getId().equals(user.id)) {
                        User dis = User.findById(dist.getId());
                        users.add(dis);
                    }

                }
            }
        UserLiveRoom.get().notification(user, "Reply", users, reply.id);

        JsonObject object = new JsonObject();
        object.addProperty("newId", reply.id);
        object.addProperty("date", reply.createDate.getTime());
        object.addProperty("tempId", reply.tempId);
        renderJSON(object);
    }
}
