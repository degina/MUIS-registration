package controllers.App;

import com.google.gson.*;
import controllers.*;
import models.*;
import org.apache.commons.io.IOUtils;
import play.Play;
import play.db.jpa.GenericModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Rina on 4/30/15.
 */
public class DelegateRFI extends Delegate {

    public static void RFIList(String body) {
        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());

        if (user != null) {
            UserTeam userTeam = user.userTeam;
            List<RFI_Distribution> user_distributions = user.rfi_distributions;
            user_distributions.addAll(userTeam.rfi_distributions);
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
                    if (user_distributions.get(i).rfi != null) {
                        if (!user_distributions.get(i).rfi.status.status.equalsIgnoreCase("Draft"))
                            query += "id = " + user_distributions.get(i).rfi.id + " or ";
                    }
                }
                query += "not(questionReceivedFrom.id <> " + user.id + " and assignee.id <> " + user.id + " and private_ = true)) and (status.status = 'Open' or createDate >= :date)";
            } else {
                query += "project.id = " + pid + " and (";
                for (int i = 0; i < user_distributions.size(); i++) {
                    if (user_distributions.get(i).rfi != null) {
                        if (!user_distributions.get(i).rfi.status.status.equalsIgnoreCase("Draft"))
                            query += "id = " + user_distributions.get(i).rfi.id + " or ";
                    }
                }
                query += "not(questionReceivedFrom.id <> " + user.id + " and assignee.id <> " + user.id + " and private_ = true)) and (status.status = 'Open' or createDate >= :date)";
            }
            Date currentDate = new Date();
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                currentDate = dateFormat.parse(json.get("date").getAsString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            GenericModel.JPAQuery q = RFI.find(query).setParameter("date", currentDate);
            List<RFI> rfis = q.fetch();
            JsonArray obj = new JsonArray();
            for (RFI rfi : rfis) {
                JsonObject object = new JsonObject();
                object.addProperty("assignee", rfi.assignee.id);
                object.addProperty("assigneeName", rfi.assignee.getLastnameFirstCharacter() + ". " + rfi.assignee.firstName);
                object.addProperty("balanceCost", rfi.costImpact.impact.equalsIgnoreCase("Yes") ? "Тийм" : (rfi.costImpact.impact.equalsIgnoreCase("No") ? "Үгүй" : "Мэдэхгүй"));
                object.addProperty("date", rfi.createDate.getTime());
                if (rfi.status.status.equalsIgnoreCase("Closed"))
                    object.addProperty("closedDate", rfi.closedDate.getTime());
                object.addProperty("desc", rfi.question);
                JsonArray distrib = new JsonArray();
                String id;
                for (RFI_Distribution dis : rfi.distributions) {
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
                object.addProperty("dueDate", dateFormat.format(rfi.dueDate));
                JsonArray files = new JsonArray();
                for (RFI_Attach attach : rfi.attaches) {
                    files.add(new JsonPrimitive(attach.dir.replace("/", "-") + "." + attach.extension));
                }
                object.add("filenames", files);
                object.addProperty("fromUserId", rfi.questionReceivedFrom.id);
                object.addProperty("fromUserName", rfi.questionReceivedFrom.getLastnameFirstCharacter() + ". " + rfi.questionReceivedFrom.firstName);
                object.addProperty("graphCost", rfi.scheduleImpact.impact.equalsIgnoreCase("Yes") ? "Тийм" : (rfi.scheduleImpact.impact.equalsIgnoreCase("No") ? "Үгүй" : "Мэдэхгүй"));
                object.addProperty("id", rfi.id);
                object.addProperty("imageNumber", "aaaa");
                object.addProperty("isPrivate", rfi.private_ ? 1 : 0);
                object.addProperty("location", getString(rfi.location));
                object.addProperty("number", rfi.No);
                object.addProperty("open", rfi.status.status.equals("Open") ? 0 : 1);
                object.addProperty("overdueNotification", rfi.overdueNotification ? 1 : 0);
                object.addProperty("question", getString(rfi.subject));
                object.addProperty("tempId", rfi.tempId != null ? rfi.tempId : 0);
                if(pid.equals(new Long(0)))
                    object.addProperty("projectId",rfi.project.id);
                else
                object.addProperty("projectId", pid);
                object.addProperty("jobSubId", rfi.task != null ? rfi.task.id : 0 );
//"jobsubid":735,"drawings":[{"shapes":[{"endX":857.5,"endY":605,"startX":719,
// "fillAlpha":0.3,"startY":482.5,"strokeWidth":1,"type":"Circle","color":"red"}],
// "imageurl":"\/FileCenter\/DrawingPath\/2015\/6\/12\/1434103319512.jpg","id":1}]
                JsonArray drawing = new JsonArray();
//                for (DrawingLayer pin : rfi.pins) {
//                    JsonObject jobj = new JsonObject();
//                    if (pin.drawingRevision != null) {
//                        jobj.addProperty("id", pin.drawingRevision.id);
//                        jobj.addProperty("imageurl", pin.drawingRevision.dir + "." + pin.drawingRevision.extension);
//                    } else {
//                        jobj.addProperty("id", pin.rfiAttach.id);
//                        jobj.addProperty("imageurl", pin.rfiAttach.dir + "." + pin.rfiAttach.extension);
//                    }
//                    JsonArray shapeArray = new JsonArray();
//                    for (DrawingShape shape : pin.shapes) {
//                        JsonObject jshape = (JsonObject) new JsonParser().parse(shape.json);
//                        shapeArray.add(jshape);
//                    }
//                    jobj.add("shapes", shapeArray);
//                    drawing.add(jobj);
//                }

                object.add("drawings", drawing);
                JsonObject names = new JsonObject();
                names.addProperty("assigneename", rfi.assignee.getLastnameFirstCharacter() + ". " + rfi.assignee.firstName);
                names.addProperty("fromusername", rfi.questionReceivedFrom.getLastnameFirstCharacter() + ". " + rfi.questionReceivedFrom.firstName);
                names.addProperty("jobname", rfi.task != null ? rfi.task.projectObject.name + ", " + rfi.task.name : "");
                object.add("names", names);

                obj.add(object);
            }
            renderJSON(obj);
        }

    }

    public static void RFIReplyList(String body) {
        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());

        if (user != null) {
            Long rfiid = json.get("rfiid").getAsLong();
            String traQuery = "";
            if(rfiid.equals(new Long(0))) {
                UserTeam userTeam = user.userTeam;
                List<RFI_Distribution> user_distributions = user.rfi_distributions;
                user_distributions.addAll(userTeam.rfi_distributions);
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
                    if (user_distributions.get(i).rfi != null) {
                        if (!user_distributions.get(i).rfi.status.status.equalsIgnoreCase("Draft"))
                            query += "id = " + user_distributions.get(i).rfi.id + " or ";
                    }
                }
                query += "not(questionReceivedFrom.id <> " + user.id + " and assignee.id <> " + user.id + " and private_ = true)) and (status.status = 'Open' or createDate >= :date)";
                Date currentDate = new Date();
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                    currentDate = dateFormat.parse(json.get("date").getAsString());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                GenericModel.JPAQuery q = RFI.find(query).setParameter("date", currentDate);
                List<RFI> rfis = q.fetch();
                traQuery = "";
                for (RFI rfi : rfis)
                    traQuery += "rfi.id = " + rfi.id + " or ";
                traQuery += "1 = 0";
            } else {
                traQuery += "rfi.id = " + rfiid;
            }
            List<RFI_Tracking> trackings = RFI_Tracking.find(traQuery).fetch();
            JsonArray obj = new JsonArray();
            for (RFI_Tracking track : trackings) {
                JsonObject object = new JsonObject();
                object.addProperty("id", track.id);
                object.addProperty("date", track.createDate.getTime());
                object.addProperty("rfiId", track.rfi.id);
                object.addProperty("userId", track.author.id);
                object.addProperty("username", track.author.getLastnameFirstCharacter() + "." + track.author.firstName);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                JsonArray files = new JsonArray();
                for (RFI_Attach attach : track.attaches) {
                    files.add(new JsonPrimitive(attach.dir.replace("/", "-") + "." + attach.extension));
                }
                object.add("filenames", files);
                object.addProperty("text", track.note);
                object.addProperty("tempId", track.tempId != null ? track.tempId : 0);
                obj.add(object);
            }
            renderJSON(obj);
        }
    }


    public static void RFINew(String body) throws ParseException {
        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            JsonObject rfiDetails = json.get("rfi").getAsJsonObject();
            RFI rfir = RFI.find("id = " + rfiDetails.get("id").getAsLong() + (rfiDetails.get("tempid").getAsLong() != 0l ? " or tempId = " + rfiDetails.get("tempid").getAsLong() : "")).first();
            if (rfir == null) {
                Long pid = rfiDetails.get("projectid").getAsLong();
                RFI rfi = new RFI();
                JsonObject object = new JsonObject();
                Project project = Project.findById(pid);
                rfi.question = rfiDetails.get("desc").getAsString();
                ProjectCounter counter = project.projectCounter;
                Long number = counter.lastRFINo;
                rfi.No = number + 1;
                counter.lastRFINo = number + 1;
                counter._save();
                rfi.subject = rfiDetails.get("question").getAsString();
                rfi.questionReceivedFrom = User.findById(rfiDetails.get("fromuserid").getAsLong());
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                String dater = rfiDetails.get("duedate").getAsString();
                rfi.dueDate = dateFormat.parse(dater);
                rfi.createDate = new Date();
                rfi.tempId = rfiDetails.get("tempid").getAsLong();
                rfi.project = Project.findById(pid);
                rfi.assignee = User.findById(rfiDetails.get("assignee").getAsLong());
                rfi.location = rfiDetails.get("location").getAsString();
                String cost = rfiDetails.get("graphcost").getAsString();
                rfi.scheduleImpact = RFI_Impact.find("impact = '" + (cost.equals("Тийм") ? "Yes" : (cost.equals("Үгүй") ? "No" : "Unknown")) + "'").first();
                cost = rfiDetails.get("balancecost").getAsString();
                rfi.costImpact = RFI_Impact.find("impact = '" + (cost.equals("Тийм") ? "Yes" : (cost.equals("Үгүй") ? "no" : "Unknown")) + "'").first();
                rfi.private_ = (rfiDetails.get("isprivate").getAsString().equalsIgnoreCase("0") ? false : true);
                rfi.overdueNotification = (rfiDetails.get("overduenotification").getAsString().equalsIgnoreCase("0") ? false : true);
                rfi.status = RFI_Status.find("status = '" + (rfiDetails.get("open").getAsString().equalsIgnoreCase("0") ? "Open" : "Closed") + "'").first();
                if(!rfiDetails.get("open").getAsString().equalsIgnoreCase("0")) {
                    rfi.closedDate = new Date();
                    object.addProperty("closedDate", rfi.closedDate.getTime());
                }
                if(!rfiDetails.get("jobsubid").isJsonNull())
                    if(rfiDetails.get("jobsubid").getAsLong() != 0l)
                        rfi.task = Task.findById(rfiDetails.get("jobsubid").getAsLong());
                rfi.create();
                JsonArray filenames = rfiDetails.get("filenames").getAsJsonArray();
                Date currentDate = new Date();
                if (filenames.size() > 0) {
                    for (int i = 0; i < filenames.size(); i++) {
                        JsonPrimitive ids = filenames.get(i).getAsJsonPrimitive();
                        RFI_Attach attach = new RFI_Attach();
                        String[] file = ids.getAsString().split("\\.");
                        String[] fileName = file[0].split("/");
                        attach.dir = file[0];
                        attach.name = fileName[fileName.length - 1];
                        attach.extension = file[1];
                        attach.rfi = rfi;
                        attach.create();
                    }
                }
//                JsonArray drawings = rfiDetails.get("drawings").getAsJsonArray();
//                for (JsonElement el : drawings) {
//                    JsonObject object = el.getAsJsonObject();
//                    DrawingLayer layer = new DrawingLayer();
//                    layer.drawingRevision = Drawing.findById(object.get("id").getAsLong());
//                    layer.rfi = rfi;
//                    layer.createdDate = new Date();
//                    layer.create();
//                    JsonArray shapes = object.get("shapes").getAsJsonArray();
//                    for (JsonElement element : shapes) {
//                        JsonObject jobj = element.getAsJsonObject();
//                        DrawingShape shape = new DrawingShape(jobj);
//                        shape.layer = layer;
//                        shape.create();
//                    }
//
//                }
                JsonArray distrib = rfiDetails.get("distribution").getAsJsonArray();
                for (int i = 0; i < distrib.size(); i++) {
                    JsonPrimitive ids = distrib.get(i).getAsJsonPrimitive();
                    RFI_Distribution distribution = new RFI_Distribution("u", rfi, ids.getAsLong());
                    distribution.create();
                }
                object.addProperty("newId", rfi.id);
                object.addProperty("date", rfi.createDate.getTime());
                object.addProperty("tempId", rfi.tempId != null ? rfi.tempId : 0);
                object.addProperty("number", rfi.id);
                List<User> users = new ArrayList<User>();
                users.add(rfi.assignee);
                if (rfi.distributions != null)
                    for (int i = 0; i < rfi.distributions.size(); i++) {
                        RFI_Distribution dist = rfi.distributions.get(i);
                        if (dist.getCode().equalsIgnoreCase("t")) {
                            List<User> team = User.find("userTeam.id = " + dist.getId()).fetch();
                            users.addAll(team);
                        } else {
                            User dis = User.findById(dist.getId());
                            users.add(dis);
                        }
                    }

                if(!rfiDetails.get("overduenotification").getAsString().equalsIgnoreCase("0")) {
                    ReminderModel reminderqRFI = new ReminderModel();
                    reminderqRFI.rfi = rfi;
                    reminderqRFI.mainType = "rfi";
                    reminderqRFI.title = rfi.subject;
                    reminderqRFI.reminderDate = rfi.dueDate;
                    reminderqRFI.reminderUsers = new ArrayList<ReminderUser>();
                    reminderqRFI.addUser(rfi.questionReceivedFrom);
                    for (User us : users) reminderqRFI.addUser(us);
                    reminderqRFI.create();
                }

                UserLiveRoom.get().notification(user, "RFI", users, rfi.id);
                renderJSON(object);
            } else {
                RFI rfi = rfir;
                JsonObject object = new JsonObject();
                rfi.status = RFI_Status.find("status = '" + (rfiDetails.get("open").getAsString().equalsIgnoreCase("0") ? "Open" : "Closed") + "'").first();
                if (!rfiDetails.get("open").getAsString().equalsIgnoreCase("0")) {
                    rfi.closedDate = new Date();
                    object.addProperty("closedDate", rfi.closedDate.getTime());
                    if(rfi.overdueNotification) {
                        List<ReminderModel> reminders = ReminderModel.find("mainType = 'rfi' and rfi.id = " + rfi.id).fetch();
                        for (ReminderModel rem : reminders) rem._delete();
                        rfi.reminderModels = new ArrayList<ReminderModel>();
                    }
                }
                rfi._save();
                object.addProperty("newId", rfi.id);
                object.addProperty("tempId", rfi.tempId);
                object.addProperty("date", rfi.createDate.getTime());
                object.addProperty("number", rfi.No);
                renderJSON(object);
            }
        }
    }

    public static void RFIReplyNew(String body) {
        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            JsonObject rfiReply = json.get("rfiReply").getAsJsonObject();
            RFI_Tracking track = new RFI_Tracking();
            track.note = rfiReply.get("text").getAsString();
            track.author = User.findById(rfiReply.get("userid").getAsLong());
            track.createDate = new Date();
            track.tempId = rfiReply.get("tempid").getAsLong();
            track.rfi = RFI.findById(rfiReply.get("rfiid").getAsLong());
            track.create();
            JsonArray filenames = rfiReply.get("filenames").getAsJsonArray();
            if (filenames.size() > 0) {
                for (int i = 0; i < filenames.size(); i++) {
                    JsonPrimitive ids = filenames.get(i).getAsJsonPrimitive();
                    RFI_Attach attach = new RFI_Attach();
                    String[] file = ids.getAsString().split("\\.");
                    String[] fileName = file[0].split("/");
                    attach.dir = file[0];
                    attach.name = fileName[fileName.length - 1];
                    attach.extension = file[1];
                    attach.tracking = track;
                    attach.create();
                }
            }

            JsonObject object = new JsonObject();
            object.addProperty("newId", track.id);
            object.addProperty("date", track.createDate.getTime());
            object.addProperty("tempId", track.tempId);
            renderJSON(object);
        }
    }

}
