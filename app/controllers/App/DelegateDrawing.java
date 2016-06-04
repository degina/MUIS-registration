package controllers.App;

import com.google.gson.*;
import controllers.Drawings;
import controllers.Users;
import models.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Rina on 4/30/15.
 */
public class DelegateDrawing extends Delegate {

    public static void disciplineList(String body) {
        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            Long pid = json.get("projectid").getAsLong();
            JsonArray obj = new JsonArray();
            List<DrawingDiscipline> disciplines = new ArrayList<DrawingDiscipline>();
            String query = "";
            if (pid.equals(new Long(0))) {
                List<Project> projects = Project.find("portfolio.owner.id=?1", user.id).fetch();
                List<OrganizationChart> organizationCharts = OrganizationChart.find("user.id=?1", user.id).fetch();
                for (OrganizationChart rel : organizationCharts) {
                    if (!projects.contains(rel.project)) query += "project.id=" + rel.project.id + " OR ";
                }
                for (Project project : projects)
                    query += "project.id=" + project.id + " OR ";
                query += "1 = 0";
            } else {
                query = "project.id = " + pid;

            }
            disciplines = DrawingDiscipline.find(query).fetch();
            for (DrawingDiscipline discipline : disciplines) {
                if (discipline.drawings.size() > 0) {
                    JsonObject object = new JsonObject();
                    object.addProperty("id", discipline.id);
                    object.addProperty("date", new Date().getTime());
                    object.addProperty("imageCount", (discipline.drawings != null ? discipline.drawings.size() : 0));
                    object.addProperty("name", discipline.name);
                    object.addProperty("projectId", discipline.project.id);
                    obj.add(object);
                }
            }
            renderJSON(obj);
        }
    }

    public static void drawingList(String body) {
        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            Long did = json.get("groupid").getAsLong();
            List<Drawing> drawings = Drawing.find("discipline.id = " + did).fetch();
            JsonArray obj = new JsonArray();
            for (Drawing drawing : drawings) {
                JsonObject object = new JsonObject();
                object.addProperty("groupId", drawing.discipline.id);
                object.addProperty("id", drawing.id);
                object.addProperty("name", drawing.title);
                object.addProperty("number", drawing.Number);
                object.addProperty("cipher", drawing.cipher);
                DrawingRevision revision = drawing.lastRevision();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                object.addProperty("date", dateFormat.format(revision.createdDate));
                object.addProperty("revisionId", revision.id);
                object.addProperty("revisionNumber", revision.number);
                object.addProperty("imageUrl", revision.dir + "." + revision.extension);
                DrawingLayer layer = revision.getUserPersonalLayer(user.id);
                object.addProperty("updatedDate", (layer != null ? layer.updatedDate.getTime() : revision.createdDate.getTime()));
                JsonArray shapes = (JsonArray) new JsonParser().parse(layer != null ? layer.path : "[]");
                object.add("shapes", shapes);
                obj.add(object);
            }
            renderJSON(obj);
        }
    }

    public static void updateDrawingItem(String body) {
        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            JsonObject drawingJson = json.get("drawingitem").getAsJsonObject();
            Date updatedDate = new Date(drawingJson.get("updatedDate").getAsLong());
            DrawingRevision revision = DrawingRevision.findById(drawingJson.get("revisionId").getAsLong());
            DrawingLayer layer = revision.getUserPersonalLayer(user.id);
            JsonObject object = new JsonObject();
            JsonArray array = drawingJson.get("shapes").getAsJsonArray();
            if (layer != null) {
                if (layer.updatedDate.after(updatedDate)) {
                    layer.path = array.toString();
                    layer.updatedDate = new Date();
                    layer._save();
                }
            } else {
                layer = new DrawingLayer(user.id, array.toString(), revision.id);
                layer.create();
            }
            JsonArray shapes = (JsonArray) new JsonParser().parse(layer.path);
            object.addProperty("newId", revision.id);
            object.addProperty("date", layer.updatedDate.getTime());
            object.add("shapes", shapes);
            renderJSON(object);
        }
    }
}
