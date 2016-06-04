package controllers.App;

import com.google.gson.*;
import controllers.Consts;
import controllers.Functions;
import controllers.UserLiveRoom;
import controllers.Users;
import javafx.geometry.Pos;
import models.*;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Enkhbayar on 6/26/2015.
 */
public class DelegateMeeting extends Delegate {

    public static void meetingList(String body) {
        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            boolean contractorUser = user.userTeam.contractor;
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            JsonArray obj = new JsonArray();
            Date currentDate = null;
            try {
                currentDate = dateFormat.parse(json.get("date").getAsString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String projectQuery = "";
            JsonElement projectEliment = json.get("projectId");
            if (projectEliment != null) {
                projectQuery = "AND m.project.id=" + projectEliment.getAsLong();
            } else {
                projectQuery = getProjectQuery(user.id).replace("project.id", "m.project.id");
                if (projectQuery.length() > 3) projectQuery = "AND " + projectQuery;
            }
            List<Meeting> meetings = Meeting.find("SELECT DISTINCT m FROM tb_meeting m LEFT JOIN m.meetingUserRels AS r WHERE " +
                    "r.meeting.id = m.id AND m.status.id!=3L " + projectQuery + " AND m.meetingDate>=?1 AND (m.viewMeeting=true OR ( m.owner.id=?2 OR r.user.id=?3))", currentDate, user.id, user.id).fetch();
            for (Meeting meeting : meetings) {
                JsonObject object = new JsonObject();
                object.addProperty("id", meeting.id);
                object.addProperty("date", meeting.meetingDate.getTime());
                object.addProperty("tempId", 0);
                object.addProperty("number", meeting.id + "");
                object.addProperty("meetingType", meeting.status.status);
                object.addProperty("title", meeting.title);
                object.addProperty("dueDate", dateFormat.format(meeting.meetingDate));
                object.addProperty("startTime", timeFormat.format(meeting.meetingDate));
                object.addProperty("endTime", timeFormat.format(meeting.finishDate));
                object.addProperty("whoCreated", meeting.owner.toString());
                object.addProperty("whoCreatedId", meeting.owner.id);
                object.addProperty("allowedToCreateAgenda", meeting.privateMeeting ? "Нээлттэй" : "Хаалттай");
                object.addProperty("viewable", meeting.privateMeeting ? "Бүгдэд" : "Оролцогчдод");
                object.addProperty("location", meeting.location);
                object.addProperty("desc", meeting.overview);
                object.addProperty("projectId", meeting.project.id);
                JsonArray files = new JsonArray();
                for (MeetingAttachment attach : meeting.meetingAttachments) {
                    files.add(new JsonPrimitive(attach.dir.replace("/", "-") + "." + attach.extension));
                }
                object.add("filenames", files);
                JsonArray attendes = new JsonArray();
                for (MeetingUserRel rel : meeting.meetingUserRels) {
                    JsonObject attObject = new JsonObject();
                    attObject.addProperty("id", rel.user.id);
                    attObject.addProperty("name", rel.user.toString());
                    attObject.addProperty("profession", rel.user.userPosition.name);
                    attObject.addProperty("team", rel.user.userTeam.name);
                    Long irts = rel.irts == null ? 2 : rel.irts.id;
                    attObject.addProperty("came", irts == 1 ? true : false);
                    attObject.addProperty("notCame", irts == 2 ? true : false);
                    attObject.addProperty("freed", irts == 3 ? true : false);
                    attendes.add(attObject);
                }
                object.add("attendees", attendes);
                obj.add(object);
            }
            renderJSON(obj);
        }
    }

    public static void meetingTopicList(String body) {
        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            JsonElement mEliment = json.get("meetingid");
            List<MeetingTopic> meetingTopics;
            if (mEliment != null) {
                meetingTopics = MeetingTopic.find("meeting.id=?1", mEliment.getAsLong()).fetch();
            } else {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                Date currentDate = null;
                try {
                    currentDate = dateFormat.parse(json.get("date").getAsString());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                meetingTopics = MeetingTopic.find("SELECT DISTINCT t FROM tb_meeting_topic t LEFT JOIN t.meeting.meetingUserRels AS r WHERE " +
                        "t.meeting.status.id!=3L AND t.meeting.meetingDate>=?1 AND (t.meeting.viewMeeting=true OR ( t.meeting.owner.id=?2 OR (r.meeting.id = t.meeting.id AND r.user.id=?3)))", currentDate, user.id, user.id).fetch();
            }
            JsonArray obj = new JsonArray();
            for (MeetingTopic topic : meetingTopics) {
                JsonObject object = new JsonObject();
                object.addProperty("id", topic.id);
                object.addProperty("date", topic.id);
                object.addProperty("tempId", 0);
                object.addProperty("meetingId", topic.meeting.id);
                object.addProperty("title", topic.title);
                object.addProperty("proposedUser", topic.owner.toString());
                object.addProperty("proposedUserId", topic.owner.id);
                object.addProperty("correspondence", topic.task == null ? "" : topic.task.name);
                object.addProperty("correspondingJobId", topic.task == null ? 0 : topic.task.id);
                object.addProperty("significance", topic.priority.priority);
                object.addProperty("state", topic.status.status);
                object.addProperty("SHKH", topic.newOld.newOld);
                object.addProperty("agendaNote", topic.minutes == null ? "" : topic.minutes);
                object.addProperty("desc", topic.description == null ? "" : topic.description);
                JsonArray files = new JsonArray();
                for (MeetingTopicAttachment attach : topic.topicAttachments) {
                    files.add(new JsonPrimitive(attach.dir.replace("/", "-") + "." + attach.extension));
                }
                JsonArray owners = new JsonArray();
                JsonArray ids = new JsonArray();
                List<MeetingTopicUserRel> topicUserRels = MeetingTopicUserRel.find("topic.id=?1", topic.id).fetch();
                if (!topicUserRels.isEmpty())
                    for (MeetingTopicUserRel topicUserRel : topicUserRels) {
                        if(topicUserRel.user != null){
                        ids.add(new JsonPrimitive(topicUserRel.user.id + ""));
                        owners.add(new JsonPrimitive(topicUserRel.user.toString()));}
                    }
                object.add("filenames", files);
                object.add("owner", owners);
                object.add("ownerUserId", ids);
                obj.add(object);
            }
            renderJSON(obj);
        }

    }

    public static void meetingTopicNew(String body) {
        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            JsonObject topicObj = json.get("MeetingAgenda").getAsJsonObject();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            Long meetingId = topicObj.get("meetingid").getAsLong();
            JsonObject responseObj = new JsonObject();
            models.Meeting meeting = models.Meeting.findById(meetingId);
            MeetingTopic meetingTopic = new MeetingTopic();
            meetingTopic.title = topicObj.get("title").getAsString();
            meetingTopic.newOld = MeetingTopicNewOld.findById((long) 1);
            String priority = topicObj.get("significance").getAsString();
            meetingTopic.priority = MeetingTopicPriority.findById(priority.equals("?????") ? 1l : priority.equals("????") ? 2l : 3l);
            String status = topicObj.get("state").getAsString();
            meetingTopic.status = MeetingTopicStatus.findById(status.equals("????????") ? 1l : 2l);
            meetingTopic.description = topicObj.get("desc").getAsString();
            meetingTopic.meeting = meeting;
            meetingTopic.owner = user;
            Long tid = topicObj.get("jobsubid").getAsLong();
            if (tid != 0 && tid != null) {
                Task task = Task.findById(tid);
                meetingTopic.task = task;
            }
            meetingTopic.create();
            JsonArray userArray = topicObj.get("owneruserid").getAsJsonArray();
            for (JsonElement jsonElement : userArray) {
                String ownerUserId = jsonElement.getAsString();
                User userO = User.findById(Long.parseLong(ownerUserId));
                MeetingTopicUserRel userRel = new MeetingTopicUserRel(userO, meetingTopic);
                userRel.create();
            }
            JsonArray filenames = topicObj.get("filenames").getAsJsonArray();
            if (filenames.size() > 0) {
                for (int i = 0; i < filenames.size(); i++) {
                    JsonPrimitive ids = filenames.get(i).getAsJsonPrimitive();
                    String[] file = ids.getAsString().split("\\.");
                    String[] fileName = file[0].split("/");
                    MeetingTopicAttachment attach = new MeetingTopicAttachment(fileName[fileName.length - 1], file[0], file[1],null, meetingTopic);
                    attach.create();
                }
            }
            List<User> users = new ArrayList<User>();
            users.add(meeting.owner);
            UserLiveRoom.get().notification(user, "MeetingTopic", users, meeting.id);

            responseObj.addProperty("newId", meetingTopic.id);
            responseObj.addProperty("date", meetingTopic.id);
            responseObj.addProperty("tempId", topicObj.get("tempid").getAsLong());
            renderJSON(responseObj);
        }
    }

    public static void deleteModel(String body) {
        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            JsonArray modelArray = json.get("deleteModel").getAsJsonArray();
            for (JsonElement jsonElement : modelArray) {
                JsonObject modelObj = jsonElement.getAsJsonObject();
                int model = modelObj.get("model").getAsInt();
                Long modelId = modelObj.get("id").getAsLong();
                System.out.println("modelId:" +modelId+" model: "+model);
                switch (model) {
                    case Consts.MEETING_TOPIC:
                        MeetingTopic meetingTopic = MeetingTopic.findById(modelId);
                        if (meetingTopic != null && (meetingTopic.owner.id == user.id || meetingTopic.meeting.owner.id == user.id)) {
                            if(meetingTopic.owner.id != user.id){
                                List<User> users = new ArrayList<User>();
                                users.add(meetingTopic.owner);
                                UserLiveRoom.get().notification(user, "MeetingTopicDelete", users, meetingTopic.meeting.id);
                            }
                            List<MeetingTopicAttachment> topicAttachments = MeetingTopicAttachment.find("topic.id=?1", meetingTopic.id).fetch();
                            for (MeetingTopicAttachment topicAttachment : topicAttachments)
                                if (MeetingTopicAttachment.count("topic.id!=?1 AND dir=?2 AND extension=?3", meetingTopic.id, topicAttachment.dir, topicAttachment.extension) == 0)
                                    Functions.deleteUploadFile(topicAttachment.dir, topicAttachment.extension);
                            meetingTopic._delete();
                        }
                        break;
                    case Consts.DAILYLOG_WEATHER:
                        DailyLogWeather weather = DailyLogWeather.findById(modelId);
                        if (weather != null) {
                            for (DailyLogWeatherAttach attach : weather.attaches)
                                if (DailyLogWeatherAttach.count("weather.id!=?1 AND dir=?2 AND extension=?3", weather.id, attach.dir, attach.extension) == 0)
                                    Functions.deleteUploadFile(attach.dir, attach.extension);
                            weather._delete();
                        }
                        break;
                    case Consts.DAILYLOG_DELIVERY:
                        DailyLogDelivery delivery = DailyLogDelivery.findById(modelId);
                        if (delivery != null) {
                            for (DailyLogDeliveryAttach attach : delivery.attaches)
                                if (DailyLogDeliveryAttach.count("delivery.id!=?1 AND dir=?2 AND extension=?3", delivery.id, attach.dir, attach.extension) == 0)
                                    Functions.deleteUploadFile(attach.dir, attach.extension);
                            delivery._delete();
                        }
                        break;
                    case Consts.DAILYLOG_EQUIPMENT:
                        DailyLogEquipment equipment = DailyLogEquipment.findById(modelId);
                        if (equipment != null) {
                            for (DailyLogEquipmentAttach attach : equipment.attaches)
                                if (DailyLogEquipmentAttach.count("equipment.id!=?1 AND dir=?2 AND extension=?3", equipment.id, attach.dir, attach.extension) == 0)
                                    Functions.deleteUploadFile(attach.dir, attach.extension);
                            equipment._delete();
                        }
                        break;
                    case Consts.DAILYLOG_GUEST:
                        DailyLogVisitor visitor = DailyLogVisitor.findById(modelId);
                        if (visitor != null) {
                            for (DailyLogVisitorAttach attach : visitor.attaches)
                                if (DailyLogVisitorAttach.count("visitor.id!=?1 AND dir=?2 AND extension=?3", visitor.id, attach.dir, attach.extension) == 0)
                                    Functions.deleteUploadFile(attach.dir, attach.extension);
                            visitor._delete();
                        }
                        break;
                    case Consts.DAILYLOG_IDEA:
                        DailyLogSanaachlaga sanaachlaga = DailyLogSanaachlaga.findById(modelId);
                        if (sanaachlaga != null) {
                            for (DailyLogSanaachlagaAttach attach : sanaachlaga.attaches)
                                if (DailyLogSanaachlagaAttach.count("sanaachlaga.id!=?1 AND dir=?2 AND extension=?3", sanaachlaga.id, attach.dir, attach.extension) == 0)
                                    Functions.deleteUploadFile(attach.dir, attach.extension);
                            sanaachlaga._delete();
                        }
                        break;
                    case Consts.DAILYLOG_INSPECTION:
                        DailyLogInspection inspection = DailyLogInspection.findById(modelId);
                        if (inspection != null) {
                            for (DailyLogInspectionAttach attach : inspection.attaches)
                                if (DailyLogInspectionAttach.count("inspection.id!=?1 AND dir=?2 AND extension=?3", inspection.id, attach.dir, attach.extension) == 0)
                                    Functions.deleteUploadFile(attach.dir, attach.extension);
                            inspection._delete();
                        }
                        break;
                    case Consts.DAILYLOG_MANPOWER:
                        DailyLogManpower manpower = DailyLogManpower.findById(modelId);
                        if (manpower != null) {
                            for (DailyLogManpowerAttach attach : manpower.attaches)
                                if (DailyLogManpowerAttach.count("manpower.id!=?1 AND dir=?2 AND extension=?3", manpower.id, attach.dir, attach.extension) == 0)
                                    Functions.deleteUploadFile(attach.dir, attach.extension);
                            manpower._delete();
                        }
                        break;
                    case Consts.DAILYLOG_MATERIAL:
                        DailyLogMaterial material = DailyLogMaterial.findById(modelId);
                        if (material != null) {
                            for (DailyLogMaterialAttach attach : material.attaches)
                                if (DailyLogMaterialAttach.count("material.id!=?1 AND dir=?2 AND extension=?3", material.id, attach.dir, attach.extension) == 0)
                                    Functions.deleteUploadFile(attach.dir, attach.extension);
                            material._delete();
                        }
                        break;
                    case Consts.DAILYLOG_MATERIAL_LOSS:
                        DailyLogDumpster dumpster = DailyLogDumpster.findById(modelId);
                        if (dumpster != null) {
                            for (DailyLogDumpsterAttach attach : dumpster.attaches)
                                if (DailyLogDumpsterAttach.count("dumpster.id!=?1 AND dir=?2 AND extension=?3", dumpster.id, attach.dir, attach.extension) == 0)
                                    Functions.deleteUploadFile(attach.dir, attach.extension);
                            dumpster._delete();
                        }
                        break;
                    case Consts.DAILYLOG_PROBLEM_NOTE:
                        DailyLogNote note = DailyLogNote.findById(modelId);
                        if (note != null) {
                            for (DailyLogNoteAttach attach : note.attaches)
                                if (DailyLogNoteAttach.count("note.id!=?1 AND dir=?2 AND extension=?3", note.id, attach.dir, attach.extension) == 0)
                                    Functions.deleteUploadFile(attach.dir, attach.extension);
                            note._delete();
                        }
                        break;
                    case Consts.DAILYLOG_SAFETY:
                        DailyLogSafety safety = DailyLogSafety.findById(modelId);
                        if (safety != null) {
                            for (DailyLogSafetyAttach attach : safety.attaches)
                                if (DailyLogSafetyAttach.count("safety.id!=?1 AND dir=?2 AND extension=?3", safety.id, attach.dir, attach.extension) == 0)
                                    Functions.deleteUploadFile(attach.dir, attach.extension);
                            safety._delete();
                        }
                        break;
                    case Consts.DAILYLOG_TECHNICAL_PROBLEM:
                        DailyLogTechnicalDelay technicalDelay = DailyLogTechnicalDelay.findById(modelId);
                        if (technicalDelay != null) {
                            for (DailyLogTechnicalDelayAttach attach : technicalDelay.attaches)
                                if (DailyLogTechnicalDelayAttach.count("technicalDelay.id!=?1 AND dir=?2 AND extension=?3", technicalDelay.id, attach.dir, attach.extension) == 0)
                                    Functions.deleteUploadFile(attach.dir, attach.extension);
                            technicalDelay._delete();
                        }
                        break;
                    case Consts.DAILYLOG_WASTE:
                        DailyLogWaste waste = DailyLogWaste.findById(modelId);
                        if (waste != null) {
                            for (DailyLogWasteAttach attach : waste.attaches)
                                if (DailyLogWasteAttach.count("waste.id!=?1 AND dir=?2 AND extension=?3", waste.id, attach.dir, attach.extension) == 0)
                                    Functions.deleteUploadFile(attach.dir, attach.extension);
                            waste._delete();
                        }
                        break;
                    case Consts.DAILYLOG_WORK:
                        DailyLogWorkNote workNote = DailyLogWorkNote.findById(modelId);
                        if (workNote != null ) {
                            for (DailyLogWorkNoteAttach attach : workNote.attaches)
                                if (DailyLogWorkNoteAttach.count("workNote.id!=?1 AND dir=?2 AND extension=?3", workNote.id, attach.dir, attach.extension) == 0)
                                    Functions.deleteUploadFile(attach.dir, attach.extension);
                            workNote._delete();
                        }
                        break;
                    case Consts.POST:
                        Post post = Post.findById(modelId);
                        if (post != null && post.owner.id == user.id) {
                            for (PostAttach attach : post.attaches)
                                Functions.deleteUploadFile(attach.dir, attach.extension);
                            post._delete();
                        }
                        break;
                    case Consts.POST_COMMENT:
                        PostComment comment = PostComment.findById(modelId);
                        if (comment != null && comment.owner.id == user.id) comment._delete();
                        break;
                    case Consts.MY_PLAN:
                        DailyLogMyPlan myPlan = DailyLogMyPlan.findById(modelId);
                        if (myPlan != null) myPlan._delete();
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
