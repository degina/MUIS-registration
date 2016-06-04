package controllers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import models.*;
import play.Logger;
import play.Play;
import play.mvc.With;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by enkhamgalan on 2/25/15.
 */
@With(Secure.class)
@Check(Consts.permissionFileShare)
public class FolderShare extends CRUD {
    public static void center() {
        User us = Users.getUser();
        boolean admin = (getPermission(us) == 3);
        Long pid = Users.pid();
        render(admin, pid);
    }

    public static void getTree(Long id) {
        User us = Users.getUser();
        boolean admin = (getPermission(us) == 3);
//        List<FolderStructure> folderStructures = FolderStructure.find("(allproject=true OR project.id=" + Users.pid() + ")" +
//                " AND (permission=NULL OR LOCATE('" + us.id + ",',permission)!=0) AND folderStructure.id=" + id + " order by queue, name").fetch();
        String qr;
        if (Users.pid() == 0) qr = "project.id=NULL";
        else qr = "project.id=" + Users.pid();
        List<FolderStructure> folderStructures = FolderStructure.find(qr + " AND " +
                "(permission=NULL OR LOCATE('," + us.id + ",',permission)!=0) AND folderStructure.id=" + id + " order by queue, name").fetch();
        JsonArray array = new JsonArray();
        for (FolderStructure structure : folderStructures) {
            array.add(getTreeJson(structure, admin));
        }
        renderJSON(array);
    }

    public static JsonObject getTreeJson(FolderStructure structure, boolean admin) {
        JsonObject treeJson = new JsonObject();
        treeJson.addProperty("id", structure.id);
        if (structure.name.length() > 30) {
            treeJson.addProperty("text", structure.name.substring(0, 30) + "...");
            treeJson.addProperty("tooltip", structure.name);
        } else {
            treeJson.addProperty("text", structure.name);
            treeJson.addProperty("tooltip", "");
        }
        if (structure.folderStructure != null) treeJson.addProperty("parent", structure.folderStructure.id);
        else treeJson.addProperty("parent", "#");
        if (structure.permission != null && admin)
            treeJson.addProperty("icon", "fa fa-lock " + structure.folderColor.color);
        else treeJson.addProperty("icon", "fa " + structure.folderType.val + " " + structure.folderColor.color);
        if (structure.folderStructures.size() > 0) treeJson.addProperty("children", true);
        else treeJson.addProperty("children", false);
        treeJson.addProperty("data", Functions.fileShareExtensionTypes[structure.extension]);
        return treeJson;
    }

    public static int getPermission(User user) {
        if (Users.pid().intValue() > 0) return user.getPermissionType(Consts.permissionFileShare);
        else return user.getUserPermissionType(Consts.permissionFileShare);
    }

    public static void fileAlreadyNamed(Long fid, String fileName) {
        FileShare file = FileShare.find("name=?1 and folderStructure.id=?2", fileName, fid).first();
        boolean exists = file != null;
//        String response = exists + "," + (exists ? file.uploader + "," + Consts.myDateFormat.format(file.uploadedAt) : ",");
        String response = exists + ",";
        renderText(response);
    }

    public static void saveFiles(Long fileSaveId, Long fid, String[] dir, String[] filename, String[] extension, String[] fileSize, Float[] fileSizeM, Long ftype, Long[] uid, String description) {
        if (fid != null && dir != null && filename != null && extension != null && dir.length > 0) {
            FolderStructure folderStructure = FolderStructure.findById(fid);
            String des1 = "";
            for (int c = 0; c < description.length(); c++) {
                if ((int) description.charAt(c) == 10) des1 += " ";
                else des1 += description.charAt(c);
            }
            description = des1;
            for (int i = 0; i < dir.length; i++) {
                FileShare file;
                if (fileSaveId.intValue() == 0) file = new FileShare();
                else file = FileShare.findById(fileSaveId);
                file.folderStructure = folderStructure;
                file.ftype = ftype;
                file.dir = dir[i];
                file.name = filename[i];
                file.extension = extension[i];
                file.fileSize = fileSize[i];
                file.fileSizeM = fileSizeM[i];
                file.description = description;
                file.sendUserList = "";
                if (ftype.intValue() == 2 && uid != null && uid.length > 0) {
                    file.sendUserList = ",";
                    for (int u = 0; u < uid.length; u++) {
                        file.sendUserList += uid[u] + ",";
                    }
                }
                if (fileSaveId.intValue() == 0) {
                    file.uploader = Users.getUser();
                    file.create();
                } else file.save();
            }
        }
    }

    public static void delete(Long id) {
        FileShare fileShare = FileShare.findById(id);
        try {
            File file = new File(Play.applicationPath.getAbsolutePath() + fileShare.dir + "." + fileShare.extension);
            if (file.exists()) file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        for (FileShareReceivedUser receivedUser:fileShare.s)
        fileShare._delete();
    }

    public static void editShow(Long id) {
        FileShare fileShare = FileShare.findById(id);
        List<User> users = new ArrayList<User>();
        if (fileShare.sendUserList.length() > 1) {
            String uid[] = fileShare.sendUserList.split(",");
            for (int u = 0; u < uid.length; u++) {
                if (uid[u].length() > 0) {
                    User user = User.findById(Long.parseLong(uid[u]));
                    users.add(user);
                }
            }
        }
        render(fileShare, users);
    }

    public static void loadFiles(Long fid, int ftype, int CurrentPageNumber) {
        User us = Users.getUser();
        String qr = "";
        if (ftype > 0) {
            String[] ext = Functions.fileShareExtensionTypes[ftype].split(",");
            qr = " AND (";
            for (int i = 0; i < ext.length; i++) {
                qr += "LOWER(extension) = '" + ext[i] + "'";
                if (i < ext.length - 1) qr += " OR ";
            }
            qr += ")";
        }
        Long maxSize = FileShare.count("folderStructure.id=?1" + qr + " AND (ftype=?2 OR uploader.id=?3 OR LOCATE('," + us.id + ",',sendUserList)!=0))", fid, 0L, us.id);
        List<FileShare> fileShares = FileShare.find("folderStructure.id=?1" + qr + " AND (ftype=?2 OR uploader.id=?3 OR LOCATE('," + us.id + ",',sendUserList)!=0))", fid, 0L, us.id).fetch(CurrentPageNumber, 18);
        int loadType = 1;
        Long MaxPageNumber = maxSize / 18;
        if (maxSize % 18 != 0) MaxPageNumber++;
        render(fileShares, CurrentPageNumber, MaxPageNumber, loadType);
    }

    public static void searchFiles(String search, int CurrentPageNumber) {
        if (search.length() > 1) {
            int loadType = 0;
            User user = Users.getUser();
            Long pid = Users.pid();
            String qr = "LOWER(name) LIKE '%" + search.toLowerCase() + "%' OR LOWER(description) LIKE '%" + search.toLowerCase() + "%'";
            if (pid.intValue() == 0) qr += " AND folderStructure.project.id=NULL";
            else qr += " AND folderStructure.project.id=" + pid;
            qr += " AND (ftype=0 OR uploader.id=" + user.id + " OR LOCATE('" + user.id + ",',sendUserList)!=0)";
            Long maxSize = FileShare.count(qr);
            List<FileShare> fileShares = FileShare.find(qr).fetch(CurrentPageNumber, 20);
            Long MaxPageNumber = maxSize / 18;
            if (maxSize % 18 != 0) MaxPageNumber++;
            render("FolderShare/loadFiles.html", fileShares, loadType, search, CurrentPageNumber, MaxPageNumber);
        }
    }

    public static void showMoreInfo(Long id, int ftype) {
        FileShare fileShare = FileShare.findById(id);
        if (ftype == 1) {
            List<User> users = new ArrayList<User>();
            if (fileShare.sendUserList.length() > 1) {
                String uid[] = fileShare.sendUserList.split(",");
                for (int u = 0; u < uid.length; u++) {
                    if (uid[u].length() > 0) {
                        User user = User.findById(Long.parseLong(uid[u]));
                        users.add(user);
                    }
                }
            }
            render(fileShare, users, ftype);
        } else {
            List<FileShareReceivedUser> receivedUsers = FileShareReceivedUser.find("fileShare.id=?1 order by date desc", fileShare.id).fetch();
            render(receivedUsers);
        }
    }

    public static void downloadHistorySave(Long id) {
        FileShare fileShare = FileShare.findById(id);
        fileShare.downloadCount++;
        fileShare._save();
        FileShareReceivedUser receivedUser = new FileShareReceivedUser();
        receivedUser.date = new Date();
        receivedUser.fileShare = fileShare;
        receivedUser.user = Users.getUser();
        receivedUser.create();
    }

    public static void folderRename(String id, String parent, String name) {
        User user = Users.getUser();
        if (getPermission(user) != 3) forbidden();
        FolderStructure folderStructure;
        String ext;
        if (id.indexOf('j') == 0) {
            folderStructure = new FolderStructure();
            folderStructure.queue = 1L;
            folderStructure.folderType = FolderType.findById(1L);
            folderStructure.folderColor = FolderColor.findById(5L);
            folderStructure.name = name;
            if (Users.pid() == 0) folderStructure.project = null;
            else folderStructure.project = Project.findById(Users.pid());
            if (!parent.equals("#")) {
                folderStructure.folderStructure = FolderStructure.findById(Long.parseLong(parent));
            }
            ext = Functions.fileShareExtensionTypes[0];
            folderStructure.create();
        } else {
            folderStructure = FolderStructure.findById(Long.parseLong(id));
            folderStructure.name = name;
            ext = Functions.fileShareExtensionTypes[folderStructure.extension];
            folderStructure._save();
        }
        folderStructure.refresh();
        renderText(folderStructure.id.toString() + "_" + ext);
    }

    public static void folderDelete(Long id) {
        User user = Users.getUser();
        if (getPermission(user) != 3) forbidden();
        FolderStructure folderStructure = FolderStructure.findById(id);
        if (folderStructure != null && !folderStructure.systemFolder) {
            List<FileShare> fileShares = FileShare.find("folderStructure.id=?1", id).fetch();
            for (FileShare fileShare : fileShares) {
                Functions.deleteFileSingle(fileShare.dir + "." + fileShare.extension);
                fileShare._delete();
            }
            List<FolderStructure> folderStructures = folderStructure.getFolderStructures();
            for (FolderStructure folderStructure1 : folderStructures) folderStructure1._delete();
            folderStructure._delete();
        }
    }

    public static void folderSettings(Long id) {
        User user = Users.getUser();
        if (getPermission(user) != 3) forbidden();
        FolderStructure folderStructure = FolderStructure.findById(id);
        List<FolderType> folderTypes = FolderType.findAll();
        List<FolderColor> folderColors = FolderColor.findAll();
        render(folderColors, folderTypes, folderStructure);
    }

    public static void folderSettingsSave(Long id, Long cid, Long tid, int eid, Long queue) {
        FolderStructure folderStructure = FolderStructure.findById(id);
        folderStructure.folderColor = FolderColor.findById(cid);
        folderStructure.folderType = FolderType.findById(tid);
        folderStructure.extension = eid;
        if (queue != null) folderStructure.queue = queue;
        folderStructure._save();
        if (folderStructure.permission != null) renderText("fa-lock " + folderStructure.folderColor.color);
        else renderText(folderStructure.folderType.val + " " + folderStructure.folderColor.color);
    }

    public static void folderPermission(Long id) {
        User owner = Users.getUser();
        if (getPermission(owner) != 3) forbidden();
        FolderStructure folderStructure = FolderStructure.findById(id);
        List<User> list = new ArrayList<User>();
        List<User> users = new ArrayList<User>();
        if (folderStructure.permission != null) {
            if (folderStructure.permission.length() > 0) {
                String[] permission = folderStructure.permission.split(",");
                for (int i = 0; i < permission.length; i++) {
                    if (permission[i].length() > 0) {
                        Long uid = Long.parseLong(permission[i]);
                        if (uid.compareTo(Users.getUser().id) != 0) {
                            User user = User.findById(uid);
                            list.add(user);
                        }
                    }
                }
                List<User> uAll = FunctionController.getUsers();
                for (User us : uAll) {
                    if (!containUser(list, us)) users.add(us);
                }
            } else {
                users = FunctionController.getUsers();
            }
        } else list = FunctionController.getUsers();
        render(users, list, folderStructure);
    }

    public static boolean containUser(List<User> users, User user) {
        for (User us : users) {
            if (us.id.compareTo(user.id) == 0) return true;
        }
        return false;
    }

    public static void folderPermissionSave(Long id, int unseens, String users) {
        User owner = Users.getUser();
        if (getPermission(owner) != 3) forbidden();
        FolderStructure folderStructure = FolderStructure.findById(id);
        if (unseens == 0) folderStructure.permission = null;
        else folderStructure.permission = users;
        folderStructure._save();
        if (folderStructure.permission != null) renderText("fa-lock " + folderStructure.folderColor.color);
        else renderText(folderStructure.folderType.val + " " + folderStructure.folderColor.color);
    }

    public static void getPermissionedUsers(Long id) {
        FolderStructure folderStructure = FolderStructure.findById(id);
        List<User> users = new ArrayList<User>();
        if (folderStructure.permission != null && folderStructure.permission.length() > 1) {
            String uid[] = folderStructure.permission.split(",");
            for (int u = 0; u < uid.length; u++) {
                if (uid[u].length() > 0) {
                    User user = User.findById(Long.parseLong(uid[u]));
                    users.add(user);
                }
            }
        }
        render(users);
    }

    public static void loadAktList(Long fid, int ftype, int CurrentPageNumber) {

        Long pid = Users.pid();
        // begin delete
        if (pid == 0L)
            pid = 1L;
        // end delete
        Long maxSize = Akt.count("folderStructure.id=?1", fid);
        List<Akt> akts = Akt.find("folderStructure.id=?1", fid).fetch(CurrentPageNumber, 18);
        List<AktStyle> aktStyles = AktStyle.find("project.id=?1", pid).fetch();
        int loadType = 1;
        Long MaxPageNumber = maxSize / 18;
        if (maxSize % 28 != 0) MaxPageNumber++;
        render(akts, CurrentPageNumber, MaxPageNumber, loadType, fid, aktStyles);
    }

    public static void aktStyleChoose(Long id, int ftype, Long fid) {
        Akt akt;
        if (ftype == 1)
            akt = Akt.findById(id);
        else {
            akt = null;
            id = 0L;
        }

        Long pid = Users.pid();
        // begin delete
        if (pid == 0L)
            pid = 1L;
        // end delete
        List<AktStyle> aktStyles;
        aktStyles = AktStyle.find("project.id=?1", pid).fetch();

        render(aktStyles, ftype, id, akt, fid);
    }

    public static void aktBlank(Long sid, int ftype, Long aid, Long fid) {
        Akt akt;
        if (ftype == 1)
            akt = Akt.findById(aid);
        else
            akt = null;

        Date currentDate = new Date();
        currentDate = Functions.convertHourNull(currentDate);

        AktStyle aktStyle = AktStyle.findById(sid);

        render(aktStyle, akt, currentDate, ftype, aid, fid);
    }

    public static void aktCreate(Long sid, Long fid, String queue, int ftype, Long aid, String name,
                                 String img1_date, String img1_txt, String img1_img,
                                 String img2_date, String img2_txt, String img2_img,
                                 String img3_date, String img3_txt, String img3_img,
                                 String img4_date, String img4_txt, String img4_img,
                                 String agentStaffName, String clientStaffName,
                                 String agentStaffPos, String clientStaffPos,
                                 String agentStaffName1, String clientStaffName1,
                                 String agentStaffPos1, String clientStaffPos1) {
        Akt object;
        AktStyle aktStyle = AktStyle.findById(sid);
        FolderStructure folderStructure = FolderStructure.findById(fid);
        if (ftype == 2)
            object = new Akt();
        else
            object = Akt.findById(aid);

        object.queue = queue;
        object.aktStyle = aktStyle;
        object.uploader = Users.getUser();
        object.folderStructure = folderStructure;

        if (ftype == 2) {
            object.create();

            AktImage image1 = new AktImage();
            image1.image = img1_img;
            image1.description = img1_txt;
            image1.date = img1_date;
            image1.create();
            object.aktImage1 = image1;

            if (object.aktStyle.aktType.id == 3 || object.aktStyle.aktType.id == 2) {
                AktImage image2 = new AktImage();
                image2.image = img2_img;
                image2.description = img2_txt;
                image2.date = img2_date;
                image2.create();
                object.aktImage2 = image2;
            }
            if (object.aktStyle.aktType.id == 3) {
                AktImage image3 = new AktImage();
                image3.image = img3_img;
                image3.description = img3_txt;
                image3.date = img3_date;
                image3.create();
                object.aktImage3 = image3;

                AktImage image4 = new AktImage();
                image4.image = img4_img;
                image4.description = img4_txt;
                image4.date = img4_date;
                image4.create();
                object.aktImage4 = image4;
            }
        } else {

            AktImage image1 = AktImage.findById(object.aktImage1.id);
            image1.image = img1_img;
            image1.description = img1_txt;
            image1.date = img1_date;
            image1.save();
            object.aktImage1 = image1;

            if (object.aktStyle.aktType.id == 3 || object.aktStyle.aktType.id == 2) {
                AktImage image2;
                if (object.aktImage2 == null) image2 = new AktImage();
                else image2 = AktImage.findById(object.aktImage2.id);
                image2.image = img2_img;
                image2.description = img2_txt;
                image2.date = img2_date;
                if (object.aktImage2 == null) image2.create();
                else image2.save();
                object.aktImage2 = image2;
            }
            if (object.aktStyle.aktType.id == 3) {
                AktImage image3;
                if (object.aktImage3 == null) image3 = new AktImage();
                else image3 = AktImage.findById(object.aktImage3.id);
                image3.image = img3_img;
                image3.description = img3_txt;
                image3.date = img3_date;
                if (object.aktImage3 == null) image3.create();
                else image3.save();
                object.aktImage3 = image3;

                AktImage image4;
                if (object.aktImage4 == null) image4 = new AktImage();
                else image4 = AktImage.findById(object.aktImage4.id);
                image4.image = img4_img;
                image4.description = img4_txt;
                image4.date = img4_date;
                if (object.aktImage4 == null) image4.create();
                else image4.save();
                object.aktImage4 = image4;
            }
        }
        object.save();

        String logoClient = FileUploader.encodeToBase64(object.aktStyle.logoClient);
        String logoAgent = FileUploader.encodeToBase64(object.aktStyle.logoAgent);

        String img1 = FileUploader.encodeToBase64(object.aktImage1.image);
        String img2 = "", img3 = "", img4 = "";
        if (object.aktStyle.aktType.id == 2L || object.aktStyle.aktType.id == 3L) {
            img2 = FileUploader.encodeToBase64(object.aktImage2.image);
        }
        if (object.aktStyle.aktType.id == 3L) {
            img3 = FileUploader.encodeToBase64(object.aktImage3.image);
            img4 = FileUploader.encodeToBase64(object.aktImage4.image);
        }

        Logger.info("aktCreate.java-ru orj bn");

        render(object, logoClient, logoAgent, img1, img2, img3, img4, ftype, name,
                agentStaffName, agentStaffPos, clientStaffName, clientStaffPos,
                agentStaffName1, agentStaffPos1, clientStaffName1, clientStaffPos1);
    }

    public static void aktInfo(Long sid, Long aid) {
        Akt akt = Akt.findById(aid);

        AktStyle aktStyle = AktStyle.findById(sid);
        Date currentDate = new Date();
        currentDate = Functions.convertHourNull(currentDate);

        render(aktStyle, currentDate, akt, aid);
    }

    public static void aktDelete(Long id) {
        Akt object = Akt.findById(id);

        try {
            File file = new File(Play.applicationPath.getAbsolutePath() + object.dir + "." + "pdf");
            if (file.exists()) file.delete();

        } catch (Exception e) {
            e.printStackTrace();
        }
        object.delete();
    }

    public static void loadAktStyleList(Long fid, int ftype, int CurrentPageNumber) {
        Long pid = Users.pid();
        // begin delete
        if (pid == 0L)
            pid = 1L;
        // end delete

        Long maxSize = AktStyle.count("project.id=?1", pid);
        List<AktStyle> aktStyles = AktStyle.find("project.id=?1", pid).fetch(CurrentPageNumber, 18);
        int loadType = 1;
        Long MaxPageNumber = maxSize / 18;
        if (maxSize % 28 != 0) MaxPageNumber++;
        render(aktStyles, CurrentPageNumber, MaxPageNumber, loadType, fid);
    }

    public static void aktStyleBlank(Long id, int ftype) {
        Long pid = Users.pid();
        // begin delete
        if (pid == 0L)
            pid = 1L;
        // end delete
        Project project = Project.findById(pid);
        String projectName = project.name;
        AktStyle aktStyle = null;
        if (ftype == 1 || ftype == 3) {
            aktStyle = AktStyle.findById(id);
        }
        render(aktStyle, projectName, ftype);
    }

    public static void aktStyleCreate(Long sid, String aktTypeSelect,
                                      String name, String agentLogo1, String clientLogo1,
                                      String agentStaffName, String clientStaffName,
                                      String agentStaffPos, String clientStaffPos,
                                      String agentStaffName1, String clientStaffName1,
                                      String agentStaffPos1, String clientStaffPos1) {

        AktStyle object;
        AktType aktType = AktType.findById(Long.parseLong(aktTypeSelect));
        if (sid == 0L) {
            object = new AktStyle();
            Long pid = Users.pid();
            // begin delete
            if (pid == 0L)
                pid = 1L;
            // end delete

            Project project = Project.findById(pid);
            object.project = project;
        } else {
            object = AktStyle.findById(sid);
            if (object.aktType.id < aktType.id && object.akts.size() > 0) {
                if (aktType.id == 2L) {
                    for (int i = 0; i < object.akts.size(); i++) {
                        if (object.akts.get(i).aktImage2 == null) {
                            AktImage img = new AktImage();
                            img.date = object.akts.get(i).aktImage1.date;
                            img.description = object.akts.get(i).aktImage1.description;
                            img.image = object.akts.get(i).aktImage1.image;
                            img.create();
                            object.akts.get(i).aktImage2 = img;
                        }
                    }
                } else if (aktType.id == 3L) {
                    for (int i = 0; i < object.akts.size(); i++) {
                        if (object.akts.get(i).aktImage2 == null) {
                            AktImage img = new AktImage();
                            img.date = object.akts.get(i).aktImage1.date;
                            img.description = object.akts.get(i).aktImage1.description;
                            img.image = object.akts.get(i).aktImage1.image;
                            img.create();
                            object.akts.get(i).aktImage2 = img;
                        }
                        if (object.akts.get(i).aktImage3 == null) {
                            AktImage img = new AktImage();
                            img.date = object.akts.get(i).aktImage1.date;
                            img.description = object.akts.get(i).aktImage1.description;
                            img.image = object.akts.get(i).aktImage1.image;
                            img.create();
                            object.akts.get(i).aktImage3 = img;
                        }
                        if (object.akts.get(i).aktImage4 == null) {
                            AktImage img = new AktImage();
                            img.date = object.akts.get(i).aktImage1.date;
                            img.description = object.akts.get(i).aktImage1.description;
                            img.image = object.akts.get(i).aktImage1.image;
                            img.create();
                            object.akts.get(i).aktImage4 = img;
                        }
                    }
                }
            }
        }

        object.name = name;
        object.logoAgent = agentLogo1;
        object.logoClient = clientLogo1;
        object.agentStaffName = agentStaffName;
        object.clientStaffName = clientStaffName;
        object.agentStaffPos = agentStaffPos;
        object.clientStaffPos = clientStaffPos;
        object.agentStaffName1 = agentStaffName1;
        object.clientStaffName1 = clientStaffName1;
        object.agentStaffPos1 = agentStaffPos1;
        object.clientStaffPos1 = clientStaffPos1;
        object.uploader = Users.getUser();
        object.aktType = aktType;


        if (sid == 0L) {
            object.create();
        } else {
            object.save();
        }
    }

    public static void aktStyleDelete(Long id) {
        AktStyle object = AktStyle.findById(id);
        try {
            for (int i = 0; i < object.akts.size(); i++) {
                File file = new File(Play.applicationPath.getAbsolutePath() + object.akts.get(i).dir + "." + "pdf");
                if (file.exists()) file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        object.delete();
    }

    public static void chooseGalleryImages(String dir){
        String gallery_menu="all";

        Long pid = Users.pid();
        // begin delete
        if(pid==0L)
            pid=1L;
        // end delete

        Date dateF = new Date();
        dateF = Functions.convertHourNull(dateF);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateF);
        calendar.add(Calendar.MONTH, -1);
        Date dateS = calendar.getTime();

        List<GalleryAlbum> dailyLogAlbums = GalleryAlbum.find("type=?1 and project.id=?2 ORDER BY createdDate DESC",1,pid).fetch();
        List<GalleryAlbum> generalAlbums = GalleryAlbum.find("type=?1 and project.id=?2 ORDER BY createdDate DESC",2,pid).fetch();

        List<OrganizationTeam> userTeams = OrganizationTeam.find("project.id=?1", pid).fetch();
        List<ProjectObject> projectObjects = ProjectObject.find("project.id = " + pid).fetch();

        render(dateS, dateF,dailyLogAlbums,generalAlbums, userTeams, projectObjects,gallery_menu);
    }

    public static void showAlbum(Long albumId){
        Long pid = Users.pid();
        // begin delete
        if(pid==0L)
            pid=1L;
        // end delete
        GalleryAlbum album = GalleryAlbum.findById(albumId);
        List<GalleryPicture> galleryPicture = GalleryPicture.find("album.id=?1 ORDER BY date DESC ",albumId).fetch();
//        List<ProjectObject> projectObjects = ProjectObject.find("project.id = " + pid).fetch();
        render(galleryPicture,album);
    }

    public static void picFilter(Long albumId,String menuActive,Long byUser,Long byTask,String startDate,String finishDate){
        String qr = "date>='"+startDate+"' AND date<='"+finishDate+" 23:59'";
        if (byUser.intValue() > 0) qr += " AND uploader.id=" + byUser;
        if (byTask.intValue() > 0) qr += " AND task.id=" + byTask;
        else  qr += " AND task=null ";
        if (albumId.intValue() > 0) qr += " AND album.id=" + albumId;
        if (menuActive.equals("dailylog")) qr += " AND album.type=1";
        if (menuActive.equals("general")) qr += " AND album.type=2";
        List<GalleryPicture> galleryPicture = GalleryPicture.find(qr+" order by date desc").fetch();

        render(galleryPicture, albumId,menuActive,byTask,byUser,startDate,finishDate);
    }
}
