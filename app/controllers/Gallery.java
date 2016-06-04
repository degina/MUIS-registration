package controllers;

import com.google.gson.JsonObject;
import models.*;
import play.mvc.With;

import java.util.*;

/**
 * Created by enkhamgalan on 3/6/15.
 */
@With(Secure.class)
@Check(Consts.permissionGallery)
public class Gallery extends CRUD {
    public static void center(String gallery_menu) {
        if (gallery_menu == null)
            gallery_menu = "all";
        Long pid = Users.pid();
        // begin delete
        if (pid == 0L)
            pid = 1L;
        // end delete

        Date dateF = new Date();
        dateF = Functions.convertHourNull(dateF);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateF);
        calendar.add(Calendar.MONTH, -1);
        Date dateS = calendar.getTime();

        List<GalleryAlbum> dailyLogAlbums = GalleryAlbum.find("type=?1 and project.id=?2 ORDER BY createdDate DESC", 1, pid).fetch();
        List<GalleryAlbum> generalAlbums = GalleryAlbum.find("type=?1 and project.id=?2 ORDER BY createdDate DESC", 2, pid).fetch();
        List<OrganizationTeam> userTeams = OrganizationTeam.find("project.id=?1", pid).fetch();
        List<ProjectObject> projectObjects = getSelectedObjects(pid);

        int all_count=0,d_count=0,g_count=0;

        for(int i=0;i<dailyLogAlbums.size();i++)
            d_count+=dailyLogAlbums.get(i).galleryPictures.size();
        for(int i=0;i<generalAlbums.size();i++)
            g_count+=generalAlbums.get(i).galleryPictures.size();
        all_count = d_count+g_count;

        render(dateS, dateF, dailyLogAlbums, generalAlbums, userTeams, projectObjects, gallery_menu,all_count,d_count,g_count);
    }

    public static List<ProjectObject> getSelectedObjects(Long pid) {
        return ProjectObject.find("SELECT DISTINCT o FROM tb_project_object o LEFT JOIN o.project AS p LEFT JOIN o.tasks AS t LEFT JOIN t.galleryPictures AS g" +
                " WHERE o.project.id=" + pid + " AND o.project.id=p.id AND t.projectObject.id=o.id AND t.id=g.task.id ORDER BY o.id").fetch();
    }

    public static void addAlbum(String name, String album_id) {
        Long pid = Users.pid();
        // begin delete
        if (pid == 0L)
            pid = 1L;
        // end delete
        GalleryAlbum galleryAlbum;

        if (album_id.equals("")) {
            galleryAlbum = new GalleryAlbum();
            galleryAlbum.name = name;
            galleryAlbum.createdDate = new Date();
            galleryAlbum.type = 2;
            galleryAlbum.galleryPictures = new ArrayList<GalleryPicture>();
            galleryAlbum.project = Project.findById(pid);
            galleryAlbum.uploader = Users.getUser();
            galleryAlbum.create();
        } else {
            galleryAlbum = GalleryAlbum.findById(Long.parseLong(album_id));
            galleryAlbum.name = name;
            galleryAlbum.save();
        }
        render(galleryAlbum, album_id);
    }

    public static void deleteAlbum(String album_id) {
        GalleryAlbum galleryAlbum = GalleryAlbum.findById(Long.parseLong(album_id));
        if (galleryAlbum.uploader.id == Users.getUser().id) {
            galleryAlbum.delete();
            renderText("Амжилттай устгалаа!");
        } else
            renderText("Та энэ цомгийг устгах эрхгүй байна!");
    }

    public static void deletePic(Long pic_id) {
        GalleryPicture picture = GalleryPicture.findById(pic_id);
        Functions.deleteUploadFile(picture.path, picture.extension);
        picture.delete();
    }

    public static void editPic(Long pic_id) {
        Long pid = Users.pid();
        GalleryPicture picture = GalleryPicture.findById(pic_id);
        List<ProjectObject> projectObjects = ProjectObject.find("project.id=?1", pid).fetch();
        render(picture, projectObjects);
    }

    public static void savePic(Long id, String name, Long byTask, String description) {
        Task task = null;
        if(byTask!=0)
            task = Task.findById(byTask);
        GalleryPicture picture = GalleryPicture.findById(id);
        picture.name = name;
        picture.task = task;
        picture.description = description;

        picture.save();

        JsonObject data = new JsonObject();
        data.addProperty("name_max", picture.getNameMax());
        data.addProperty("name", picture.name);
        data.addProperty("desc", picture.description);
        renderJSON(data);
    }

    public static void nameEqual(String name, String album_id) {
        //Logger.info("name:"+name);
        int b = 0;
        JsonObject data = new JsonObject();
        Long pid = Users.pid();
        List<GalleryAlbum> generalAlbums;
        if (album_id.equals(""))
            generalAlbums = GalleryAlbum.find("type=?1 and project.id=?2 ORDER BY createdDate DESC", 2, pid).fetch();
        else
            generalAlbums = GalleryAlbum.find("type=?1 and project.id=?2 and id!=?3 ORDER BY createdDate DESC", 2, pid, Long.parseLong(album_id)).fetch();

        for (int i = 0; i < generalAlbums.size(); i++) {
            if (generalAlbums.get(i).name.equals(name)) {
                b = 1;
                break;
            }
        }
        data.addProperty("b", b);
        renderJSON(data);
    }

    public static void showAlbum(Long albumId) {
        Long pid = Users.pid();
        GalleryAlbum album = GalleryAlbum.findById(albumId);
        List<GalleryPicture> galleryPicture = GalleryPicture.find("album.id=?1 ORDER BY date DESC ", albumId).fetch();
        List<ProjectObject> projectObjects = ProjectObject.find("project.id=?1", pid).fetch();
        render(galleryPicture, album, projectObjects);
    }

    public static void addImgAlbum(Long id, Long byTask, String[] filename, String[] filedir, String[] extension) {
        GalleryAlbum album = GalleryAlbum.findById(id);
        List<GalleryPicture> galleryPictures = new ArrayList<GalleryPicture>();
        Task task = null;
        if(byTask!=0)
            task = Task.findById(byTask);
        if (filename != null) {
            for (int i = 0; i < filename.length; i++) {
                GalleryPicture picture = new GalleryPicture();
                picture.album = album;
                picture.name = filename[i];
                picture.date = new Date();
                picture.path = filedir[i];
                picture.pathTumb = filedir[i] + "t";
                picture.extension = extension[i];
                picture.description = "";
                picture.uploader = Users.getUser();
                picture.task = task;

                picture.create();

                galleryPictures.add(picture);
            }
        }

        render(galleryPictures,album);
    }

    public static void filter(Long albumId, String menuActive, Long byUser, Long byTask, String startDate, String finishDate) {
        Long pid = Users.pid();
        // begin delete
        if (pid == 0L)
            pid = 1L;
        // end delete

        String qr = "album.project.id="+pid+" AND date>='"+startDate+"' AND date<='"+finishDate+" 23:59'";
        if (byUser.intValue() > 0) qr += " AND uploader.id=" + byUser;
        if (byTask.intValue() > 0) qr += " AND task.id=" + byTask;
        else  qr += " AND task=null ";
        if (albumId.intValue() > 0) qr += " AND album.id=" + albumId;
        if (menuActive.equals("dailylog")) qr += " AND album.type=1";
        if (menuActive.equals("general")) qr += " AND album.type=2";
        List<GalleryPicture> galleryPicture = GalleryPicture.find(qr+" order by date desc").fetch();

        render(galleryPicture, albumId,menuActive,byTask,byUser,startDate,finishDate);
    }

    public static List<Task> getSelectedTasks(Long oid) {
        return Task.find("SELECT DISTINCT t FROM tb_task t LEFT JOIN t.galleryPictures AS g" +
                " WHERE t.projectObject.id=" + oid + " AND t.id=g.task.id ORDER BY t.orderGantt").fetch();
    }
}
