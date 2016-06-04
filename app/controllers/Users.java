package controllers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import models.*;
import play.Play;
import play.data.binding.Binder;
import play.db.Model;
import play.exceptions.TemplateNotFoundException;
import play.i18n.Messages;
import play.mvc.With;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@With(Secure.class)
public class Users extends CRUD {
    public static User getUser() {
        return (User) renderArgs.get("user");
    }

    public static Long pid() {
        if (session.get("projectId") != null) return Long.parseLong(session.get("projectId"));
        else return 0L;
    }

    public static Project selectedProject() {
        if (session.get("projectId") != null) return Project.findById(Long.parseLong(session.get("projectId")));
        else return null;
    }

    public static void list(int page, String search, String searchFields, String orderBy, String order,
                            String nameSearch, Long userTeam, Date startDate, Date endDate, Long position, String isActive, String nonActive) {
        User user = Users.getUser();
        int admin = user.getUserPermissionType(Consts.permissionAccount);
        if (admin == 0) forbidden();
        ObjectType type = ObjectType.get(getControllerClass());
        notFoundIfNull(type);
        if (page < 1) {
            page = 1;
        }
        if (orderBy == null) {
            orderBy = "userTeam.queue,userTeam.name,userPosition.rate,userPosition.name,firstName";
            order = "ASC";
        }
        search = null;
        searchFields = null;
        String where = "id!=0L";
        if (nameSearch != null && nameSearch.length() > 0) {
            where += " AND firstName LIKE '%" + nameSearch + "%'";
        }
        if (userTeam != null && userTeam != 0L) {
            where += " AND userTeam.id=" + userTeam;
        }
        if (position != null && position != 0L) {
            where += " AND userPosition.id=" + position;
        }
        if (startDate != null && endDate != null) {
            where += " AND " + "employmentDate >= '" + Consts.myDateFormat.format(startDate) +
                    "' AND employmentDate < '" + Consts.myDateFormat.format(endDate) + "'";
        }
        if (isActive == null && nonActive == null) {
            isActive = "active";
            where += " AND active=true";
        } else if ((isActive != null && isActive.equals("active")) && (nonActive == null || !nonActive.equals("nonActive"))) {
            where += " AND active=true";
        } else if (nonActive != null && isActive == null) {
            where += " AND active=false";
        }

        List<Model> objects = type.findPage(page, search, searchFields, orderBy, order, where);
        Long count = type.count(search, searchFields, where);
        Long totalCount = type.count(null, null, null);
        List<UserTeam> userTeams = UserTeam.findAll();
        List<UserPosition> userPositions = UserPosition.findAll();
        try {
            render(type, objects, count, totalCount, page, orderBy, order, search, admin
                    , userTeams, userPositions, position, userTeam, nameSearch, startDate, endDate, isActive, nonActive);
        } catch (TemplateNotFoundException e) {
            render("CRUD/list.html", type, objects, count, totalCount, page, orderBy, order
                    , userTeams, userPositions, position, userTeam, nameSearch, startDate, endDate, nonActive, isActive);
        }
    }

    public static void deleteUser(Long id) {
        User user = Users.getUser();
        if (user.getUserPermissionType(Consts.permissionAccount) != 3) forbidden();
        ObjectType type = ObjectType.get(getControllerClass());
        notFoundIfNull(type);
        User object = User.findById(id);
        notFoundIfNull(object);
        try {
            Date now = new Date();
            if (Functions.getDifferenceDays(object.createdDate, now) <= 3) {
                if (!object.profilePicture.startsWith("/assets")) {
                    try {
                        File file = new File(Play.applicationPath.getAbsolutePath() + object.profilePicture);
                        if (file.exists()) file.delete();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                object._delete();
            } else {
                object.active = false;
                object._save();
            }
        } catch (Exception e) {
            flash.error(play.i18n.Messages.get("crud.delete.error", type.modelName));
            redirect(request.controller + ".show", object._key());
        }
        flash.success(play.i18n.Messages.get("crud.deleted", type.modelName));
        redirect(request.controller + ".list");
    }

    public static void blank() throws Exception {
        User user = Users.getUser();
        if (user.getUserPermissionType(Consts.permissionAccount) != 3) forbidden();
        ObjectType type = ObjectType.get(getControllerClass());
        notFoundIfNull(type);
        Constructor<?> constructor = type.entityClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        Model object = (Model) constructor.newInstance();
        try {
            List<UserPermission> permissions = UserPermission.find("active=true ORDER BY queue,name").fetch();
            List<UserPosition> userPositions = UserPosition.find("ORDER BY rate,name").fetch();
            List<UserTeam> userTeams = UserTeam.find("ORDER BY name").fetch();
            List<UserRole> userRoles = UserRole.findAll();
            render(type, object, userPositions, userTeams, permissions, userRoles);
        } catch (TemplateNotFoundException e) {
            render("CRUD/blank.html", type, object);
        }
    }

    public static void create() throws Exception {
        User user = Users.getUser();
        if (user.getUserPermissionType(Consts.permissionAccount) != 3) forbidden();
        ObjectType type = ObjectType.get(getControllerClass());
        notFoundIfNull(type);
        Constructor<?> constructor = type.entityClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        User object = (User) constructor.newInstance();
        Binder.bind(object, "object", params.all());
        validation.valid(object);
        object.username = object.username.toUpperCase();
        long count = User.find("username=?1 AND active=true", object.username).fetch().size();
        List<UserPermission> permissions = UserPermission.find("active=true ORDER BY queue,name").fetch();
        if (validation.hasErrors() || count > 0 || object.profilePicture.length() == 0) {
            if (object.profilePicture.length() == 0) renderArgs.put("error", "Зураг заавал оруулах ёстой!");
            else if (count > 0) renderArgs.put("error", "Нэвтрэх нэр давхардаж байна!");
            else renderArgs.put("error", Messages.get("crud.hasErrors"));

            Boolean uniqueError = null, timeIduniqueError = null;
            if (count > 0) uniqueError = true;
            try {
                List<UserPosition> userPositions = UserPosition.find("ORDER BY rate,name").fetch();
                List<UserRole> userRoles = UserRole.findAll();
                List<UserTeam> userTeams = UserTeam.find("ORDER BY name").fetch();
                render(request.controller.replace(".", "/") + "/blank.html", type, object, userPositions,
                        userTeams, uniqueError, timeIduniqueError, permissions, userRoles);
            } catch (TemplateNotFoundException e) {
                render("CRUD/blank.html", type, object);
            }
        }
        object.password = Functions.getSha1String(object.password);
        object.permissionRelations = new ArrayList<UserPermissionRelation>();
        object.active = true;
        if (!object.profilePicture.startsWith("/assets")) {
            ConvertToImage convertToImage = new ConvertToImage();
            String path = object.profilePicture.substring(0, object.profilePicture.length() - 4);
            String ext = object.profilePicture.substring(object.profilePicture.length() - 3, object.profilePicture.length());
            convertToImage.convertRectEllipse(path, ext, object.x * 3, object.y * 3, object.w * 3, object.h * 3);
            if (!ext.toLowerCase().equals("png")) Functions.deleteFileSingle(object.profilePicture);
            object.profilePicture = path + ".png";
        }
        for (UserPermission permission : permissions) {
            int perId = Integer.parseInt(params.get("permission-" + permission.id));
            if (perId > 0) {
                UserPermissionRelation permissionRelation = new UserPermissionRelation();
                permissionRelation.user = object;
                permissionRelation.permissionType = permission.getPermissionType(perId);
                object.permissionRelations.add(permissionRelation);
            }
        }
        object.lastNameLength = lastNameWordLength(true, object);
        object._save();
        flash.success(Messages.get("crud.created", type.modelName));
        if (params.get("_save") != null) {
            redirect(request.controller + ".list");
        }
        if (params.get("_saveAndAddAnother") != null) {
            redirect(request.controller + ".blank");
        }
        redirect(request.controller + ".show", object._key());
    }

    public static void show(String id) throws Exception {
        User usercheck = User.findById(Long.parseLong(id));
        User owner = getUser();
        int admin = owner.getUserPermissionType(Consts.permissionAccount);
        if (!(admin == 2 || admin == 3 || usercheck.id.compareTo(owner.id) == 0)) forbidden();
        ObjectType type = ObjectType.get(getControllerClass());
        notFoundIfNull(type);
        User object = (User) type.findById(id);
        notFoundIfNull(object);
        try {
            List<UserPermission> permissions = UserPermission.find("active=true ORDER BY queue,name").fetch();
            List<UserPosition> userPositions = UserPosition.find("ORDER BY rate,name").fetch();
            List<UserTeam> userTeams = UserTeam.find("ORDER BY name").fetch();
            List<UserRole> userRoles = UserRole.findAll();
            render(usercheck, type, object, userPositions, userTeams, permissions, admin, userRoles);
        } catch (TemplateNotFoundException e) {
            render("CRUD/show.html", type, object);
        }
    }

    public static void save(String id) throws Exception {
        User owner = getUser();
        int admin = owner.getUserPermissionType(Consts.permissionAccount);

        ObjectType type = ObjectType.get(getControllerClass());
        notFoundIfNull(type);
        User object = User.findById(Long.parseLong(id));
        if (!(admin == 2 || admin == 3 || object.id.compareTo(owner.id) == 0)) forbidden();
        notFoundIfNull(object);
        String beforePassword = object.password;
        String proPic = object.profilePicture;
        String username = object.username;
        UserTeam userTeam = object.userTeam;
        UserPosition userPosition = object.userPosition;
        Binder.bind(object, "object", params.all());
        if (admin != 3) {
            object.username = username;
            object.userTeam = userTeam;
            object.userPosition = userPosition;
        }
        validation.valid(object);
        List<UserPermission> permissions = UserPermission.find("active=true ORDER BY queue,name").fetch();
        object.password = beforePassword;
        object.username = object.username.toUpperCase();
        long count = User.find("id!=?1 AND username=?2 AND active=true", object.id, object.username).fetch().size();
        if (validation.hasErrors() || count > 0 || object.profilePicture.length() == 0) {
            if (object.profilePicture.length() == 0) renderArgs.put("error", "Зураг заавал оруулах ёстой!");
            else if (count > 0) renderArgs.put("error", "Нэвтрэх нэр давхардаж байна!");
            else renderArgs.put("error", Messages.get("crud.hasErrors"));

            List<UserPosition> userPositions = UserPosition.find("ORDER BY rate,name").fetch();
            List<UserTeam> userTeams = UserTeam.find("ORDER BY name").fetch();
            List<UserRole> userRoles = UserRole.findAll();
            try {
                render(request.controller.replace(".", "/") + "/show.html", type, object, userPositions, userTeams, permissions, userRoles);
            } catch (TemplateNotFoundException e) {
                render("CRUD/show.html", type, object, userTeams);
            }
        }
        if (!proPic.equals(object.profilePicture) && !object.profilePicture.startsWith("/assets")) {
            Functions.deleteFileSingle(proPic);
            ConvertToImage convertToImage = new ConvertToImage();
            String path = object.profilePicture.substring(0, object.profilePicture.length() - 4);
            String ext = object.profilePicture.substring(object.profilePicture.length() - 3, object.profilePicture.length());
            convertToImage.convertRectEllipse(path, ext, object.x * 3, object.y * 3, object.w * 3, object.h * 3);
            if (!ext.toLowerCase().equals("png")) Functions.deleteFileSingle(object.profilePicture);
            object.profilePicture = path + ".png";
        }
        object.lastNameLength = lastNameWordLength(false, object);
        String email_fullname = params.get("email_fullname");
        String email_username = params.get("email_username");
        String email_password = params.get("email_password");
        if (email_fullname != null && email_username != null && email_password != null
                && email_fullname.length() > 0 && email_username.length() > 0 && email_password.length() > 0) {
            if (object.emailAccount == null) object.emailAccount = new Email();
            object.emailAccount.fullname = email_fullname;
            object.emailAccount.username = email_username;
            object.emailAccount.password = email_password;
            object.emailAccount.user = object;
        }
        object._save();
        if (admin == 3) {
            for (UserPermission permission : permissions) {
                int perId = Integer.parseInt(params.get("permission-" + permission.id));
                checkPermission(object, permission, perId);
            }
        }
        if (!username.equals(object.username) && owner.id.compareTo(object.id) == 0) {
            redirect("/logout");
        } else flash.success(Messages.get("crud.saved", object));
        if (params.get("_save") != null) {
            if (admin > 1) redirect(request.controller + ".list");
            else redirect(request.controller + ".show", object._key());
        }
        redirect(request.controller + ".show", object._key());
    }

    public static boolean checkPermission(User user, UserPermission permission, int perId) {
        boolean cont = false;
        for (UserPermissionRelation rel : user.permissionRelations) {
            if (rel.permissionType.permission.id.compareTo(permission.id) == 0) {
                cont = true;
                if (perId == 0) rel._delete();
                else {
                    rel.permissionType = permission.getPermissionType(perId);
                    rel._save();
                }
            }
        }
        if (!cont) {
            if (perId > 0) {
                UserPermissionRelation permissionRelation = new UserPermissionRelation();
                permissionRelation.user = user;
                permissionRelation.permissionType = permission.getPermissionType(perId);
                permissionRelation.create();
            }
        }
        return cont;
    }

    public static int lastNameWordLength(boolean isnew, User user) {
        if (user.lastName == null) return 0;
        List<User> users;
        if (isnew) users = User.findAll();
        else users = User.find("id!=?1", user.id).fetch();
        int count = 1;
        while (count <= user.lastName.length()) {
            if (lastNameMatch(users, user, count)) count++;
            else break;
        }
        return count;
    }

    public static boolean lastNameMatch(List<User> users, User own, int count) {
        for (User user : users) {
            if (user.firstName.toLowerCase().equals(own.firstName.toLowerCase())
                    && user.lastName.toLowerCase().startsWith(own.lastName.toLowerCase().substring(0, count))) {
                if (user.lastNameLength < count) {
                    user.lastNameLength = count;
                    user._save();
                }
                return true;
            }
        }
        return false;
    }

    public static void savePassword(String oldPass, String newPass, String newRepeatPass) {
        User user = Users.getUser();
        if (!user.password.equals(Functions.getSha1String(oldPass))) {
            renderText("Хуучин нууц үг буруу байна!");
        } else if (newPass == null || newPass.length() == 0) {
            renderText("Шинэ нүүц үгээ бичнэ үү!");
        } else if (!newPass.equals(newRepeatPass)) {
            renderText("Дахин бичсэн нууц үг зөрж байна!");
        } else {
            user.password = Functions.getSha1String(newPass);
            user.save();
            renderText("success");
        }
    }

    public static void companyOrg() {
        User user = Users.getUser();
        int adminRes = user.getUserPermissionType(Consts.permissionAccount);
        if (adminRes == 0) forbidden();
        boolean admin = (adminRes == 3);
        JsonObject obj = new JsonObject();
        obj.addProperty("class", "go.TreeModel");
        JsonArray array = new JsonArray();
        List<CompanyOrg> companyOrgs = CompanyOrg.findAll();
        if (companyOrgs.size() == 0) {
            User us = User.find("order by userPosition.rate").first();
            CompanyOrg org = new CompanyOrg();
            org.user = us;
            org.parentId = 0L;
            org.create();
            companyOrgs.add(org);
        }
        for (CompanyOrg chart : companyOrgs) {
            JsonObject uobj = new JsonObject();
            uobj.addProperty("key", chart.user.id);
            uobj.addProperty("source", chart.user.profilePicture);
            uobj.addProperty("name", chart.user.toString());
            uobj.addProperty("position", chart.user.userPosition.name);
            if (chart.parentId.intValue() > 0) uobj.addProperty("parent", chart.parentId);
            array.add(uobj);
        }
        obj.add("nodeDataArray", array);
        render(obj, admin);
    }

    public static void orgCompGetUsers(String selectedData) {
        JsonObject json = (JsonObject) new JsonParser().parse(selectedData);
        JsonArray el = json.getAsJsonArray("nodeDataArray");
        JsonObject jo;
        List<Long> selectedUsers = new ArrayList<Long>();
        for (JsonElement element : el) {
            jo = element.getAsJsonObject();
            selectedUsers.add(jo.get("key").getAsLong());
        }

        List<UserTeam> userTeams = new ArrayList<UserTeam>();
        List<UserTeam> userTeamAll = UserTeam.find("contractor=?1 order by queue,name", false).fetch();
        for (UserTeam userTeam : userTeamAll) {
            UserTeam team = new UserTeam();
            team.id = userTeam.id;
            team.name = userTeam.name;
            team.userLocals = new ArrayList<User>();
            for (User user : userTeam.getUsers()) {
                if (!selectedUsers.contains(user.id)) team.userLocals.add(user);
            }
            if (team.userLocals.size() > 0) userTeams.add(team);
        }
        render(userTeams);
    }

    public static void orgCompSave(String selectedData) {
        JsonObject json = (JsonObject) new JsonParser().parse(selectedData);
        JsonArray el = json.getAsJsonArray("nodeDataArray");
        JsonObject jo;
        List<Long> users = new ArrayList<Long>();
        List<CompanyOrg> companyOrgs = CompanyOrg.findAll();
        for (JsonElement element : el) {
            jo = element.getAsJsonObject();
            Long userId = jo.get("key").getAsLong();
            Long parentId = 0L;
            if (jo.get("parent") != null) parentId = jo.get("parent").getAsLong();
            users.add(userId);
            if (!checkUser(companyOrgs, userId, parentId)) {
                CompanyOrg chart = new CompanyOrg();
                chart.user = User.findById(userId);
                chart.parentId = parentId;
                chart.create();
            }
        }
        for (CompanyOrg chart : companyOrgs) {
            if (!users.contains(chart.user.id)) chart._delete();
        }
    }

    public static boolean checkUser(List<CompanyOrg> companyOrgs, Long userId, Long parentId) {
        boolean modified = false;
        for (CompanyOrg chart : companyOrgs) {
            if (chart.user.id.compareTo(userId) == 0) {
                if (chart.parentId.compareTo(parentId) != 0) {
                    chart.parentId = parentId;
                    modified = true;
                }
                if (modified) chart._save();
                return true;
            }
        }
        return false;
    }
}
