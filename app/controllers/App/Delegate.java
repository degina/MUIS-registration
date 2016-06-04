package controllers.App;

import com.google.gson.*;
import controllers.*;
import controllers.MobileApp.AppConsts;
import models.*;
import org.apache.commons.io.IOUtils;
import play.Play;
import play.mvc.Controller;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by enkhamgalan on 4/5/15.
 */
public class Delegate extends Controller {

    public static void login(String body) {
        if (body != null) {
            JsonObject json = (JsonObject) new JsonParser().parse(body);
            User us = User.find("username=?1 and password=?2", json.get("username").getAsString().toUpperCase(), Functions.getSha1String(json.get("password").getAsString())).first();
            if (us != null) {
                JsonObject obj = new JsonObject();
                obj.add("session", new JsonPrimitive(us.id + "-" + us.username + "-" + us.password));
                JsonObject usobj = new JsonObject();
                usobj.addProperty("id", us.id);
                usobj.addProperty("username", us.username);
                obj.add("user", usobj);
                renderJSON(obj);
            }
        }
    }

    public static void portfolios(String body) {
        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            String qr = "SELECT DISTINCT f FROM tb_portfolio f LEFT JOIN f.project AS p LEFT JOIN p.organizationCharts AS c" +
                    " WHERE (f.owner.id = "+user.id+" OR ( p.id=f.project.id AND p.id=c.project.id AND c.user.id=" + user.id+ ")) ";
            List<Portfolio> portfolios = Portfolio.find(qr).fetch();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            Date today = Functions.convertHourNull(new Date());
            Date nextWeek = Functions.addOrMinusDays(today, 7, true);
            JsonArray obj = new JsonArray();
            Project project=null;
            for (Portfolio portfolio : portfolios) {
                JsonObject object = new JsonObject();
                project=portfolio.project;
                object.addProperty("id", project.id);
                object.addProperty("name", project.name);
                object.addProperty("percent", project.completedPercent+"");
                object.addProperty("startDate", dateFormat.format(project.startDate));
                object.addProperty("finishDate", dateFormat.format(project.finishDate));
                object.addProperty("days", project.duration);
                object.addProperty("location", portfolio.address);
                object.addProperty("image", portfolio.imageUrl+"c.jpg");
                if(CompanyConf.name.equals("Hyundai")){
                    object.addProperty("category", portfolio.portfolioHyundai != null? (portfolio.portfolioHyundai.location != null ?(portfolio.portfolioHyundai.location.name):""):"");
                    object.addProperty("stage", portfolio.portfolioHyundai != null? (portfolio.portfolioHyundai.stage !=null ?(portfolio.portfolioHyundai.stage.name):""):"");
                }
                else{
                    object.addProperty("category", portfolio.category.name);
                    object.addProperty("stage", portfolio.stage.name);
                }
                object.addProperty("allMeetings", Meeting.count("status.id=1L AND project.id=?1", project.id));
                object.addProperty("overdueMeetings", Meeting.count("status.id=1L AND project.id=?1 AND finishDate <?2", project.id, today));
                object.addProperty("nextWeekMeetings", Meeting.count("status.id=1L AND project.id=?1 AND finishDate >=?2 AND finishDate <?3 ", project.id, today, nextWeek));
                object.addProperty("nNextWeekMeetings", Meeting.count("status.id=1L AND project.id=?1 AND finishDate >?2 ", project.id, nextWeek));

                object.addProperty("allRFIs",RFI.count("project.id =?1 AND status.status = 'Open'", project.id));
                object.addProperty("overdueRFIs",RFI.count("project.id =?1 AND status.status = 'Open' AND dueDate <?2 ", project.id, today));
                object.addProperty("nextWeekRFIs", RFI.count("project.id =?1 AND status.status = 'Open' AND dueDate >=?2 AND dueDate <?3", project.id, today, nextWeek));
                object.addProperty("nNextWeekRFIs",  RFI.count("project.id =?1 AND status.status = 'Open' AND dueDate >?2 ", project.id, nextWeek));

                object.addProperty("allPunchLists", PunchList.count("project.id =?1 AND status.status = 'NotResolved'", project.id));
                object.addProperty("overduePunchLists", PunchList.count("project.id =?1 AND status.status = 'NotResolved' AND dueDate <?2 ", project.id, today));
                object.addProperty("nextWeekPunchLists", PunchList.count("project.id =?1 AND status.status = 'NotResolved' AND dueDate >=?2 AND dueDate <?3", project.id, today, nextWeek));
                object.addProperty("nNextWeekPunchLists",PunchList.count("project.id =?1 AND status.status = 'NotResolved' AND dueDate >?2 ", project.id, nextWeek));

                object.addProperty("overdueSchedule",Task.count("projectObject.project.id=?1 AND hasChild=false AND completedPercent<100 AND actualFinish!=null AND finishDate < actualFinish", project.id));
                object.addProperty("nextWeekSchedule",Task.count("projectObject.project.id=?1 AND hasChild=false AND completedPercent<100 AND finishDate = actualFinish AND finishDate >=?2 AND finishDate <?3 ", project.id, today, nextWeek));
                object.addProperty("nNextWeekSchedule",Task.count("projectObject.project.id=?1 AND hasChild=false AND completedPercent<100 AND finishDate = actualFinish AND finishDate >?2 ", project.id, nextWeek));
                object.addProperty("allSchedule",Task.count("projectObject.project.id=?1 AND hasChild=false AND completedPercent<100 ", project.id));

                if (portfolio.isActive) object.addProperty("status", "Идэвхтэй");
                else object.addProperty("status", "Идэвхгүй");
                obj.add(object);
            }
            renderJSON(obj);
        }
    }

    public static void portfolioUsers(String body) {

        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            JsonArray obj = new JsonArray();
            JsonElement projectidEl = json.get("projectid");
            if(projectidEl != null) {
                List<OrganizationChart> orgChart = OrganizationChart.find("project.id=?1 ORDER BY team.id, team.name, userPosition.rate, user.firstName", projectidEl.getAsLong()).fetch();
                for (OrganizationChart rel : orgChart) {
                    JsonObject object = new JsonObject();
                    object.addProperty("id", rel.user.id);
                    object.addProperty("userid", rel.user.id);
                    object.addProperty("username", rel.user.toString());
                    object.addProperty("team", rel.user.userTeam.name);
                    object.addProperty("position", rel.user.userPosition.name);
                    object.addProperty("mobile", rel.user.phone1);
                    object.addProperty("mail", rel.user.email);
                    object.addProperty("projectid", rel.project.id);
                    obj.add(object);
                }
            }else {
              List<ProjectAssignRel>  rels = ProjectAssignRel.find(getProjectQuery(user.id)).fetch();
                for (ProjectAssignRel rel : rels) {
                    JsonObject object = new JsonObject();
                    object.addProperty("id", rel.user.id);
                    object.addProperty("userid", rel.user.id);
                    object.addProperty("username", rel.user.toString());
                    object.addProperty("team", rel.user.userTeam.name);
                    object.addProperty("position", rel.user.userPosition.name);
                    object.addProperty("mobile", rel.user.phone1);
                    object.addProperty("mail", rel.user.email);
                    object.addProperty("projectid", rel.project.id);
                    obj.add(object);
                }
            }

            renderJSON(obj);
        }
    }

    public static void users(String body){
        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            JsonArray obj = new JsonArray();
            List<User> users = User.find("active=true").fetch();
            for (User userRe : users) {
                JsonObject object = new JsonObject();
                object.addProperty("id", userRe.id);
                object.addProperty("name", userRe.toString());
                object.addProperty("team", userRe.userTeam.name);
                object.addProperty("position",userRe.userPosition.name);
                object.addProperty("mobile", userRe.phone1);
                object.addProperty("mail", userRe.email);
                object.addProperty("avatar", userRe.profilePicture);
                obj.add(object);
            }
            renderJSON(obj);
        }
    }

    public static User getUser(String session) {
        String[] usData = session.split("-");
        return User.find("id=?1 AND username=?2 AND password=?3", Long.parseLong(usData[0]), usData[1], usData[2]).first();
    }

    public static void upload() throws Exception {
        String uploadPath = request.headers.get("upload-path").toString();
        String extension = "jpg";
        String filename = "null";
        if (request.isNew) {
            FileOutputStream moveTo = null;
            try {
                uploadPath = uploadPath.replace("[", "").replace("]", "");
                String[] paths =uploadPath.split("/");
                filename = paths[paths.length-1];
                uploadPath="";
                for (int i = 1; i < (paths.length-1) ; i++) {
                    uploadPath=uploadPath+"/"+paths[i];
                }
                String[] filer = filename.split("\\.");
                extension = filer[filer.length - 1];
                InputStream data = request.body;
                String absolutePath = Functions.cleanUrl(Play.applicationPath.getAbsolutePath() + "/" + uploadPath);
                File f = new File(absolutePath);
                if (!f.exists()) {
                    if (!f.mkdirs()) throw new Exception("Unable to create upload dir");
                }
                absolutePath = Functions.cleanUrl(absolutePath + "/" + filer[0] + "." + extension);
                moveTo = new FileOutputStream(new File(absolutePath));
                IOUtils.copy(data, moveTo);
                moveTo.close();
                if (FileUploader.isImage(extension)) {        //to small
                    ConvertToImage convertToImage = new ConvertToImage();
                    if (!convertToImage.convert(uploadPath + "/" + filer[0], extension, 400, 400, "none", true)) ;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                if (moveTo != null)
                    moveTo.close();
            }
        }
    }

    static String getString(String str) {
        if (str == null)
            return "";
        return str;
    }

    private static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }


    public static void gcmRegistration(String body){
        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            String token = json.get("token").getAsString();
            String device = json.get("device").getAsString();
            System.out.println("token: "+token);
            user.gcmRegistrationId = token;
            user.device = device;
            user._save();
            JsonObject response = new JsonObject();
            response.addProperty("response","seccuss");
        }

    }

    public static String getProjectQuery(Long userId){
        String query=" ( ";
        List<Project> projects = Project.find("portfolio.owner.id=?1", userId).fetch();
        List<OrganizationChart> organizationCharts = OrganizationChart.find("user.id=?1", userId).fetch();
        for (OrganizationChart rel : organizationCharts) {
            if (!projects.contains(rel.project)) query+="project.id="+rel.project.id+" OR ";
        }
        for (Project project : projects)
            query += "project.id=" + project.id + " OR ";
        if (query.length() > 3)
            query = query.substring(0, query.length() - 3) +" )";
        else
            query = "";
        return query;
    }

    public static void AttachRedirect(String body) {
        String dir = body.replace("-", "/");
        redirect(dir);
    }
}
