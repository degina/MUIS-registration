package controllers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import models.*;
import play.i18n.Messages;
import play.mvc.With;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by enkhamgalan on 5/30/15.
 */
@With(Secure.class)
public class OrganizationCharts extends CRUD {
    public static void organization() {
        User user = Users.getUser();
        int permission = user.getPermissionType(Consts.permissionOrganization);
        boolean owner = user.isProjectOwner();
        if (permission == 0 && !owner) forbidden();
        boolean admin = (owner || permission == 2);
        Long selectedProject = Users.pid();
        if (selectedProject.intValue() == 0) forbidden();
        Project project = Project.findById(selectedProject);

        JsonObject obj = new JsonObject();
        obj.addProperty("class", "go.TreeModel");
        JsonArray array = new JsonArray();
        User us = project.portfolio.owner;
        if (us == null) forbidden();
        if (!CompanyConf.copy_orgChart) {
            if (project.organizationCharts.size() == 0) {
                OrganizationTeam team = new OrganizationTeam();
                team.name = "Баг";
                team.project = project;
                team.create();

                OrganizationChart chart = new OrganizationChart();
                chart.user = us;
                chart.userPosition = us.userPosition;
                chart.parentId = 0L;
                chart.project = project;
                chart.team = team;
                chart.create();
                project.organizationCharts.add(chart);
            } else {
                OrganizationChart chartRe = project.organizationCharts.get(0);
                if (chartRe.user.id.compareTo(us.id) != 0) {
                    long old_us = chartRe.user.id;
                    chartRe.user = us;
                    chartRe._save();
                    for (OrganizationChart chart : project.organizationCharts) {
                        if (chart.parentId.compareTo(old_us) == 0) {
                            chart.parentId = us.id;
                            chart._save();
                        }
                    }
                }
            }
        } else {
            if (project.organizationCharts.size() == 0)
                project.organizationCharts = FunctionDatabase.createOrgChartFromCompanyOrg(project);
        }
        for (OrganizationChart chart : project.organizationCharts) {
            JsonObject uobj = new JsonObject();
            uobj.addProperty("key", chart.user.id);
            uobj.addProperty("source", chart.user.profilePicture);
            uobj.addProperty("name", chart.user.toString());
            uobj.addProperty("positionId", chart.userPosition.id);
            uobj.addProperty("position", chart.userPosition.name);
            uobj.addProperty("teamId", chart.team.id);
            uobj.addProperty("team", chart.team.name);
            if (chart.parentId.intValue() > 0) uobj.addProperty("parent", chart.parentId);
            array.add(uobj);
        }
        obj.add("nodeDataArray", array);
        render(obj, admin);
    }

    public static void orgGetUsers(Long selectedId, Long selectedPos, String selectedData, boolean changePermission, Long selectedTeam) {
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
        Long first_userposition = null;
        for (UserTeam userTeam : userTeamAll) {
            UserTeam team = new UserTeam();
            team.id = userTeam.id;
            team.name = userTeam.name;
            team.userLocals = new ArrayList<User>();
            for (User user : userTeam.getUsers()) {
                if (selectedId.compareTo(user.id) == 0 || !selectedUsers.contains(user.id)) {
                    team.userLocals.add(user);
                    if (first_userposition == null) first_userposition = user.userPosition.id;
                }
            }
            if (team.userLocals.size() > 0) userTeams.add(team);
        }
        List<UserPosition> userPositions = UserPosition.find("order by rate,name").fetch();
        List<OrganizationTeam> organizationTeams = OrganizationTeam.find("project.id=?1", Users.pid()).fetch();
        if (first_userposition != null && selectedPos.intValue() == 0) selectedPos = first_userposition;
        render(userTeams, userPositions, selectedId, selectedPos, changePermission, organizationTeams, selectedTeam, first_userposition);
    }

    public static void orgSaveUsers(String selectedData) {
        Project project = Project.findById(Users.pid());
        JsonObject json = (JsonObject) new JsonParser().parse(selectedData);
        JsonArray el = json.getAsJsonArray("nodeDataArray");
        JsonObject jo;
        List<Long> users = new ArrayList<Long>();
        for (JsonElement element : el) {
            jo = element.getAsJsonObject();
            Long userId = jo.get("key").getAsLong();
            Long positionId = jo.get("positionId").getAsLong();
            Long teamId = jo.get("teamId").getAsLong();
            Long parentId = 0L;
            if (jo.get("parent") != null) parentId = jo.get("parent").getAsLong();
            if (!checkUser(users, userId)) {
                if (!checkUser(project.organizationCharts, userId, positionId, teamId, parentId)) {
                    OrganizationChart chart = new OrganizationChart();
                    chart.user = User.findById(userId);
                    chart.userPosition = UserPosition.findById(positionId);
                    chart.parentId = parentId;
                    chart.project = project;
                    chart.team = OrganizationTeam.findById(teamId);
                    chart.create();
                }
                users.add(userId);
            }
        }
        for (OrganizationChart chart : project.organizationCharts) {
            if (!users.contains(chart.user.id)) chart._delete();
        }
    }

    public static boolean checkUser(List<Long> users, Long uid) {
        for (Long us : users) {
            if (us.compareTo(uid) == 0) return true;
        }
        return false;
    }

    public static boolean checkUser(List<OrganizationChart> organizationCharts, Long userId, Long positionId, Long teamId, Long parentId) {
        boolean modified = false;
        for (OrganizationChart chart : organizationCharts) {
            if (chart.user.id.compareTo(userId) == 0) {
                if (chart.userPosition.id.compareTo(positionId) != 0) {
                    chart.userPosition = UserPosition.findById(positionId);
                    modified = true;
                }
                if (chart.team.id.compareTo(teamId) != 0) {
                    chart.team = OrganizationTeam.findById(teamId);
                    modified = true;
                }
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

    public static void orgGetTeams() {
        Long selectedProject = Users.pid();
        if (selectedProject.intValue() == 0) forbidden();
        Project project = Project.findById(selectedProject);
        List<OrganizationTeam> teams = project.organizationTeams;
        render(teams);
    }

    public static void orgSaveTeams(String deletedTeams, Long[] ids, String[] values) {
        Project project = Project.findById(Users.pid());
        String[] delTeams = deletedTeams.split("#");
        if (deletedTeams.length() > 0) {
            for (int i = 0; i < delTeams.length; i++) {
                OrganizationTeam team = OrganizationTeam.findById(Long.parseLong(delTeams[i]));
                team._delete();
            }
        }
        for (int i = 0; i < ids.length; i++) {
            if (ids[i].intValue() == 0) {
                OrganizationTeam team = new OrganizationTeam();
                team.name = values[i];
                team.project = project;
                team.create();
            } else checkTeam(project.organizationTeams, ids[i], values[i]);
        }
    }

    public static boolean checkTeam(List<OrganizationTeam> organizationTeams, Long teamId, String value) {
        for (OrganizationTeam organizationTeam : organizationTeams) {
            if (organizationTeam.id.compareTo(teamId) == 0) {
                if (!organizationTeam.name.equals(value)) {
                    organizationTeam.name = value;
                    organizationTeam._save();
                }
                return true;
            }
        }
        return false;
    }

    public static void permission(boolean success) {
        User user = Users.getUser();
        int permission = user.getPermissionType(Consts.permissionOrganization);
        if (!(permission == 2 || user.isProjectOwner())) forbidden();
        Long selectedProject = Users.pid();
        if (selectedProject.intValue() == 0) forbidden();
        Project project = Project.findById(selectedProject);
        List<OrganizationTeam> organizationTeams = project.organizationTeams;
        List<OrgPermission> permissions = OrgPermission.find("active=true ORDER BY queue,name").fetch();
        if (success) flash.success("Амжилттай хадгаллаа");
        render(organizationTeams, permissions);
    }

    public static void permissionSave() {
        User user = Users.getUser();
        int perm = user.getPermissionType(Consts.permissionOrganization);
        if (!(perm == 2 || user.isProjectOwner())) forbidden();
        Project project = Project.findById(Users.pid());
        List<OrgPermission> permissions = OrgPermission.find("active=true ORDER BY queue,name").fetch();
        int val;
        OrgPermissionRelation relation;
        for (OrgPermission permission : permissions) {
            for (OrganizationTeam team : project.organizationTeams) {
                for (OrganizationChart chart : team.organizationCharts) {
                    val = Integer.parseInt(params.get("permission-" + permission.id + "-" + chart.id));
                    relation = chart.getPermissionRelation(permission.id);
                    if (relation == null && val > 0) {
                        OrgPermissionRelation permissionRelation = new OrgPermissionRelation();
                        permissionRelation.organizationChart = chart;
                        permissionRelation.permissionType = permission.getPermissionType(val);
                        permissionRelation.create();
                    } else if (relation != null && val == 0) {
                        relation._delete();
                    } else if (relation != null && val > 0) {
                        relation.permissionType = permission.getPermissionType(val);
                        relation._save();
                    }
                }
            }
        }
        permission(true);
    }

    public static List<OrganizationChart> getProjectOrganizations() {
        return OrganizationChart.find("project.id=?1" +
                " ORDER BY team.id, user.userPosition.rate, user.firstName", Users.pid()).fetch();
    }
}
