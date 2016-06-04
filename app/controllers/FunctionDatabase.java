package controllers;

import models.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by enkhamgalan on 8/6/15.
 */
public class FunctionDatabase {
    public static List<OrganizationChart> createOrgChartFromCompanyOrg(Project project) {
        List<OrganizationChart> organizationCharts = new ArrayList<OrganizationChart>();
        List<CompanyOrg> companyOrgs = CompanyOrg.find("order by user.userTeam.id").fetch();
        OrganizationTeam organizationTeam = null;
        long teamid = 0L;
        for (CompanyOrg org : companyOrgs) {
            if (org.user.userTeam.id.compareTo(teamid) != 0) {
                OrganizationTeam team = new OrganizationTeam();
                team.name = org.user.userTeam.name;
                team.project = project;
                team.create();
                organizationTeam = team;
                teamid = org.user.userTeam.id;
            }
            OrganizationChart chart = new OrganizationChart();
            chart.user = org.user;
            chart.userPosition = org.user.userPosition;
            chart.parentId = org.parentId;
            chart.project = project;
            chart.team = organizationTeam;
            chart.create();
            organizationCharts.add(chart);
        }
        return organizationCharts;
    }
    public static void createImg(User user, Task task, String name, String dir, String extension) {
        Calendar calendar = Calendar.getInstance();
        GalleryPicture galleryPicture = new GalleryPicture();
        galleryPicture.name = name;
        galleryPicture.date = new Date();
        galleryPicture.path = dir;
        galleryPicture.pathTumb = dir + "t";
        galleryPicture.description = "";
        galleryPicture.uploader = user;
        galleryPicture.extension = extension;
        galleryPicture.task = task;
        galleryPicture.create();

        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        String album_name = year + " оны " + month + "-р сар";

        GalleryAlbum album = GalleryAlbum.find("type=?1 and name=?2", 1, album_name).first();

        if (album == null) {
            Long pid = Users.pid();
            Project project = Project.findById(pid);
            album = new GalleryAlbum();
            album.name = album_name;
            album.type = 1;
            album.createdDate = new Date();
            album.project = project;
            album.uploader = user;
            album.create();
            galleryPicture.album = album;
            galleryPicture.save();
        } else {
            galleryPicture.album = album;
            galleryPicture.save();
        }
    }

    public static void checkImg(String dir) {
        GalleryPicture galleryPicture = GalleryPicture.find("path=?1", dir).first();
        if (galleryPicture != null) galleryPicture._delete();
    }
}
