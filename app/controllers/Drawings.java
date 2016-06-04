package controllers;

import com.google.gson.*;
import models.*;
import play.Play;
import play.mvc.With;
import sun.misc.BASE64Decoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * Created by Rina on 3/28/2015.
 */
@With(Secure.class)
public class Drawings extends CRUD {
    @Check(Consts.permissionMonitorDrawing)
    public static void thumbnails() {
        List<DrawingDiscipline> disciplines = DrawingDiscipline.find("project.id  = " + Users.pid() + " order by name").fetch();
        List<Drawing> drawings = Drawing.find("discipline.project.id = " + Users.pid() + " order by Number").fetch();
        render(disciplines, drawings);
    }

    @Check(Consts.permissionMonitorDrawing)
    public static void viewType(Long disciplineId, String type) {
        List<DrawingDiscipline> disciplines = new ArrayList<DrawingDiscipline>();
        String query = "";
        if (disciplineId != null)
            query = "id  = " + disciplineId;
        else
            query = "project.id  = " + Users.pid() + " order by name";
        disciplines = DrawingDiscipline.find(query).fetch();
        render(disciplines, type);
    }

    @Check(Consts.permissionMonitorDrawing)
    public static void pdf() {
        DrawingPDF pdf = DrawingPDF.find("order by createdDate desc").first();
        render(pdf);
    }

    @Check(Consts.permissionMonitorDrawing)
    public static void uploader() {
        List<DrawingDiscipline> disciplines = DrawingDiscipline.find("project.id  = " + Users.pid()).fetch();
        Date today = new Date();
        render(disciplines, today);
    }

    @Check(Consts.permissionMonitorDrawing)
    public static void history() {
        List<DrawingPDF> pdfs = DrawingPDF.find("project.id = " + Users.pid()).fetch();
        render(pdfs);
    }

    @Check(Consts.permissionMonitorDrawing)
    public static void settings() {
        List<DrawingDiscipline> disciplines = DrawingDiscipline.find("project.id = " + Users.pid()).fetch();
        render(disciplines);
    }

    public static List<DrawingRevision> sortRevision(List<DrawingRevision> revisions) {
        Collections.sort(revisions, new Comparator<DrawingRevision>() {
            public int compare(DrawingRevision o2, DrawingRevision o1) {
                return o2.createdDate.compareTo(o1.createdDate);

            }
        });
        return revisions;
    }

    public static List<Drawing> sortDrawing(List<Drawing> drawings) {
        Collections.sort(drawings, new Comparator<Drawing>() {
            public int compare(Drawing o2, Drawing o1) {
                return o2.Number.compareTo(o1.Number);
            }
        });
        return drawings;
    }

    @Check(Consts.permissionMonitorDrawing)
    public static void extendField(Long id, boolean isClean, String path, int height, String action) {
        DrawingRevision revision = DrawingRevision.findById(id);
        if (isClean) {
        } else {
            if (action.equalsIgnoreCase("punchList")) {
                List<DrawingLayer> layers = DrawingLayer.find("revision.id = " + id + " and punchList IS NOT NULL").fetch();
                if (layers.size() > 0) {
                    path = "[";
                    for (DrawingLayer layer : layers) {
                        if (layer.punchList.isAllowReply(Users.getUser().id)) {
                            String pather = layer.path.replace("[", "").replace("]", "");
                            path += "{ \"punchlist\": " + layer.punchList.id + ", \"number\":\"" + layer.punchList.No + "\", \"status\":\"" + layer.punchList.status.status + "\", \"pin\":" + pather + "},";
                        }
                    }
                    path += "{}]";
                }
            }
            if (action.equalsIgnoreCase("userLayer")) {
                DrawingLayer layer = DrawingLayer.find("revision.id = " + id + " and user.id = " + Users.getUser().id).first();
                if (layer != null)
                    path = layer.path;
            }
        }
        render(revision, height, path, action);
    }

    @Check(Consts.permissionMonitorDrawing)
    public static void extendModal(Long id, String action) {
        DrawingRevision revision = DrawingRevision.findById(id);
        render(revision, action);
    }

    public static void extendShowingModal(Long id, int height) {
        DrawingLayer layer = DrawingLayer.findById(id);
        render(layer, height);
    }


    @Check(Consts.permissionMonitorDrawing)
    public static void updatePersonalLayer(String path, Long revisionId, Long userId) {
        if (userId == null) userId = Users.getUser().id;
        DrawingLayer layer = DrawingLayer.find("revision.id = " + revisionId + " and user.id = " + userId).first();
        if (layer != null) {
            layer.path = path;
            layer.updatedDate = new Date();
            layer._save();
        } else {
            layer = new DrawingLayer(Users.getUser().id, path, revisionId);
            layer.create();
        }

    }

    @Check(Consts.permissionMonitorDrawing)
    public static void createDrawingRevision(String imageBase64String, Long disciplineId, String disciplineName,
                                             Long pdfId, String pdfName, String pdfUrl, int pdfPageCount, int pageNumber,
                                             Long drawingId, String drawingTitle, String drawingCipher, String drawingNumber,
                                             String drawingDate) {
        DrawingDiscipline discipline = null;
        DrawingPDF pdf = null;
        Drawing drawing = null;
        JsonObject json = new JsonObject();
        if (pdfId != null) {
            pdf = DrawingPDF.findById(pdfId);
        } else {
            pdf = new DrawingPDF(pdfName, pdfUrl, Users.getUser().id, pdfPageCount, Users.pid());
            pdf.create();
        }
        if (disciplineId != null) {
            discipline = DrawingDiscipline.findById(disciplineId);
            json.addProperty("disciplineCreate", false);
        } else {
            discipline = new DrawingDiscipline(disciplineName, Users.pid());
            json.addProperty("disciplineCreate", true);
            discipline.create();
        }
        if (drawingId != null) {
            drawing = Drawing.findById(drawingId);
        } else {
            drawing = new Drawing(drawingTitle, discipline.id, drawingCipher, drawingNumber.toLowerCase());
            drawing.create();
        }
        String dir = decodeToImage(Consts.uploadDrawingImagePath, imageBase64String);
        DrawingRevision newRevision = new DrawingRevision(dir, "png", drawing.id, pdf.id, pageNumber, drawingDate);
        newRevision.create();
        json.addProperty("pdfId", pdf.id);
        json.addProperty("disciplineId", discipline.id);
        renderJSON(json);
    }

    @Check(Consts.permissionMonitorDrawing)
    public static void checkDrawingRevision(String number) {
        Drawing drawing = Drawing.find("discipline.project.id = " + Users.pid() + " and Number = '" + number.toLowerCase() + "'").first();
        JsonObject json = new JsonObject();
        if (drawing != null) {
            json.addProperty("isExist", true);
            json.addProperty("id", drawing.id);
            json.addProperty("title", drawing.title);
            json.addProperty("cipher", drawing.cipher);
            json.addProperty("disciplineId", drawing.discipline.id);
            json.addProperty("newRevision", drawing.newRevisionNumber());
        } else {
            json.addProperty("isExist", false);
        }
        renderJSON(json);
    }

    @Check(Consts.permissionMonitorDrawing)
    public static void extendPDFSlider(String pdfUrl, int pageNum) {
        render(pdfUrl, pageNum);
    }

    public static void handleDocumentAttachment(String fileDir, String fileName, String extension, Float filesize) {
        String img = Functions.handleDocumentAttachment(fileDir, fileName, extension, filesize);
        renderText(img);
    }

    @Check(Consts.permissionMonitorDrawing)
    public static String decodeToImage(String path, String imageString) {

        BufferedImage image;

        byte[] imageByte;

        try {

            BASE64Decoder decoder = new BASE64Decoder();
            imageByte = decoder.decodeBuffer(imageString.substring(23, imageString.length()));

            ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);

            image = ImageIO.read(bis);

            Calendar calendar = Calendar.getInstance();

            calendar.setTime(new Date());

            path = path + calendar.get(Calendar.YEAR) + "/" + (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.DATE) + "/";

            File f = new File(Play.applicationPath.getAbsolutePath() + "/" + path);

            if (!f.exists()) {

                if (!f.mkdirs()) throw new Exception("Unable to create upload dir");

            }

            path += calendar.getTimeInMillis();

            ImageIO.write(image, "png", new File(Play.applicationPath.getAbsolutePath() + "/" + path + ".png"));
            bis.close();

            ConvertToImage convertToImage = new ConvertToImage();
            convertToImage.convert(path, "png", 300, 300, "rect", true);

        } catch (Exception e) {

            e.printStackTrace();

        }

        return path;

    }

    public static String getSize(String path) {
        ConvertToImage image = new ConvertToImage();
        return image.getSizeForDrawing(path);
    }

    @Check(Consts.permissionMonitorDrawing)
    public static void getHistory() {
        User user = Users.getUser();
        if (user.getPermissionType(Consts.permissionMonitorDrawing) != 1) forbidden();

    }

}
