package controllers;

import models.OrgPermissionRelation;
import models.User;
import models.UserPermissionRelation;

public class Security extends Secure.Security {

    static boolean authentify(String username, String password) {
        username = username.toUpperCase();
        User user = User.find("username=?1 AND active=true", username).first();
        if (user != null) {
            if (!user.username.equals(username)) return false;
            return user.password.compareTo(Functions.getSha1String(password)) == 0;
        }
        return false;
    }

    static boolean check(String type) {
        Long selectedProject = Users.pid();
        if (type.equals(Consts.permissionDashboard) ||
                type.equals(Consts.permissionMyPlan) ||
                type.equals(Consts.permissionDailyLog) ||
                type.equals(Consts.permissionBudget) ||
                type.equals(Consts.permissionContract) ||
                type.equals(Consts.permissionAdmin) ||
                type.equals(Consts.permissionAccount) || (selectedProject.intValue() == 0 && type.equals(Consts.permissionFileShare))) {
            UserPermissionRelation permissionRelation = UserPermissionRelation.find("user.username=?1 AND permissionType.permission.alias=?2", connected(), type).first();
            return (permissionRelation != null && permissionRelation.permissionType.value > 0);
        } else {
            if (selectedProject.intValue() == 0) return false;
            else {
                OrgPermissionRelation permissionRelation = OrgPermissionRelation.find("organizationChart.project.id=?1 AND" +
                        " organizationChart.user.username=?2 AND permissionType.permission.alias=?3", selectedProject, connected(), type).first();
                return (permissionRelation != null && permissionRelation.permissionType.value > 0);
            }
        }
//        if (type.equals(Consts.permissionContract)) {
//            return (permissionRelation != null);
//        } else if (type.equals(Consts.permissionBudget)) {
//            return (permissionRelation != null);
//        } else if (type.equals(Consts.permissionAccount)) {
//            if (permissionRelation != null) return (permissionRelation.permissionType.value == 3);
//        } else if (type.equals(Consts.permissionGantt)) {
//            if (permissionRelation != null) return (permissionRelation.permissionType.value == 3);
//        } else if (type.equals(Consts.permissionFileShare)) {
//            if (permissionRelation != null) return (permissionRelation.permissionType.value == 3);
//        } else if (type.equals(Consts.permissionGreatePunchList)) {
//            if (permissionRelation != null) return (permissionRelation.permissionType.value == 3);
//        } else if (type.equals(Consts.permissionMonitorDrawing)) {
//            if (permissionRelation != null) return (permissionRelation.permissionType.value == 3);
//        }
    }
}
