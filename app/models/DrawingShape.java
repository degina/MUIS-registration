package models;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import controllers.CRUD;
import play.db.jpa.Model;

import javax.persistence.*;

/**
 * Created by Rina on 3/28/2015.
 */
@Entity(name = "tb_drawingShape")
public class DrawingShape extends Model {

    @CRUD.Hidden
    @ManyToOne
    public DrawingShapeType type;

    @Column(length = 131070)
    public String json;

    public DrawingShape() {

    }

    public DrawingShape(String jsonString) {
        JsonObject jsonObject = (JsonObject) new JsonParser().parse(jsonString);
        type = DrawingShapeType.find("type = '" + jsonObject.get("type").getAsString() + "'").first();
        json = jsonString;
        this.create();
    }
    public DrawingShape(JsonObject jsonObject) {
        type = DrawingShapeType.find("type = '" + jsonObject.get("type").getAsString() + "'").first();
        json = jsonObject.toString();
        this.create();
    }
//    public DrawingShape(JsonObject object){
//        String type_ = object.get("type").getAsString();
//        this.type = DrawingShapeType.find("type = '" + type_ + "'").first();
//        this.color = object.get("color").getAsString();
//        if (type_.equalsIgnoreCase("Path")) {
//            JsonArray paths = object.get("path").getAsJsonArray();
//            String path = "";
//            for (int i = 0; i < paths.size() - 1; i++) {
//                JsonObject pathObj = paths.get(i).getAsJsonObject();
//                path += pathObj.get("x").getAsFloat() + "," + pathObj.get("y").getAsFloat() + ",";
//            }
//            JsonObject pathObj = paths.get(paths.size() - 1).getAsJsonObject();
//            path += pathObj.get("x").getAsFloat() + "," + pathObj.get("y").getAsFloat();
//            this.paths = path;
//            this.width = object.get("strokeWidth").getAsFloat();
//        }
//        if (type_.equalsIgnoreCase("Rectangle") || type_.equalsIgnoreCase("Circle")) {
//            ShapePoint start = new ShapePoint();
//            start.x = object.get("startX").getAsFloat();
//            start.y = object.get("startY").getAsFloat();
//            start.create();
//            this.startPoint = start;
//            ShapePoint end = new ShapePoint();
//            end.x = object.get("endX").getAsFloat();
//            end.y = object.get("endY").getAsFloat();
//            end.create();
//            this.endPoint = end;
//            this.width = object.get("strokeWidth").getAsFloat();
//            this.fillAlpha = object.get("fillAlpha").getAsFloat();
//        }
//        if (type_.equalsIgnoreCase("Line") || type_.equalsIgnoreCase("ArrowLine") || type_.equalsIgnoreCase("DoubleArrowLine")) {
//            ShapePoint start = new ShapePoint();
//            start.x = object.get("startX").getAsFloat();
//            start.y = object.get("startY").getAsFloat();
//            start.create();
//            this.startPoint = start;
//            ShapePoint end = new ShapePoint();
//            end.x = object.get("endX").getAsFloat();
//            end.y = object.get("endY").getAsFloat();
//            end.create();
//            this.endPoint = end;
//            this.width = object.get("strokeWidth").getAsFloat();
//        }
//        if (type_.equalsIgnoreCase("Text")) {
//            ShapePoint start = new ShapePoint();
//            start.x = object.get("startX").getAsFloat();
//            start.y = object.get("startY").getAsFloat();
//            start.create();
//            this.startPoint = start;
//            ShapePoint end = new ShapePoint();
//            end.x = object.get("endX").getAsFloat();
//            end.y = object.get("endY").getAsFloat();
//            end.create();
//            this.endPoint = end;
//            this.text = object.get("text").getAsString();
//            this.fontSize = object.get("fontSize").getAsInt();
//        }
//        if (type_.equalsIgnoreCase("Punch")) {
//            ShapePoint start = new ShapePoint();
//            start.x = object.get("startX").getAsFloat();
//            start.y = object.get("startY").getAsFloat();
//            start.create();
//            this.startPoint = start;
//        }
//    }

}
