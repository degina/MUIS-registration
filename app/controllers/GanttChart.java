package controllers;

import com.google.gson.*;
import controllers.MyClass.GanttJson;
import controllers.MyClass.GanttJsonMain;
import controllers.MyClass.GanttJsonParseTask;
import models.*;
import play.mvc.With;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by enkhamgalan on 1/24/15.
 */
@With(Secure.class)
@Check(Consts.permissionGantt)
public class GanttChart extends CRUD {
    public static void showProject() {
        Long pid = 0L;
        if (Users.pid() == 0) forbidden();
        else pid = Users.pid();
        User user = Users.getUser();
        if (user.getPermissionType(Consts.permissionGantt) != 3) forbidden();

        Project project = Project.findById(pid);
        String prjData = getProjectJsonData("", project).toJsonString();
        String selectStyle = "styleNot";
        List<UserTeam> userTeams = getUserTeams(pid);
        render(prjData, project, selectStyle, pid, userTeams);
    }

    public static List<UserTeam> getUserTeams(Long pid) {
        List<UserTeam> userTeams = new ArrayList<UserTeam>();
        List<ProjectAssignRel> projectAssignRels = ProjectAssignRel.find("project.id=?1 ORDER BY user.userTeam.queue, user.userTeam.name, " +
                "user.userPosition.rate, user.firstName", pid).fetch();
        Long tid = 0L;
        for (ProjectAssignRel rel : projectAssignRels) {
            if (rel.user.userTeam.id.compareTo(tid) != 0) {
                UserTeam team = new UserTeam();
                team.name = rel.user.userTeam.name;
                team.userLocals = new ArrayList<User>();
                userTeams.add(team);
                tid = rel.user.userTeam.id;
            }
            if (!userTeams.get(userTeams.size() - 1).userLocals.contains(rel.user))
                userTeams.get(userTeams.size() - 1).userLocals.add(rel.user);
        }
        return userTeams;
    }

    public static void getAssignFilter() {
        List<UserTeam> userTeams = getUserTeams(Users.pid());
        render(userTeams);
    }

    public static GanttJsonMain getProjectJsonData(String response, Project project) {
        GanttJsonMain jsonMain = new GanttJsonMain();
        jsonMain.response = response;
        GanttJson json = new GanttJson();
        json.id = "p" + project.id.intValue();
        json.name = project.name;
        json.level = 0;
        json.code = project.code;
        json.status = project.status;
        json.startDate = Consts.myDateFormat.format(project.startDate);
        json.duration = project.duration.intValue();
        json.progress = project.completedPercent;
        json.scopePercent = 100f;
        json.endDate = Consts.myDateFormat.format(project.finishDate);
        json.startIsMilestone = project.startIsMilestone;
        json.endIsMilestone = project.startIsMilestone;
        json.workCount = project.workCount;
        if (project.projectObjects.size() > 0) json.hasChild = true;
        else json.hasChild = false;
        for (ProjectAssignRel rel : project.projectAssignRels) {
            json.assignIds.add(rel.user.id);
        }
        jsonMain.tasks.add(json);

        for (ProjectObject object : project.getProjectObjects()) {
            json = new GanttJson();
            json.id = "o" + object.id.intValue();
            json.name = object.name;
            json.level = 1;
            json.code = object.code;
            json.status = object.status;
            json.depends = object.depends;
            json.startDate = Consts.myDateFormat.format(object.startDate);
            json.duration = object.duration.intValue();
            json.progress = object.completedPercent;
            json.scopePercent = object.scopePercent;
            json.endDate = Consts.myDateFormat.format(object.finishDate);
            json.startIsMilestone = object.startIsMilestone;
            json.endIsMilestone = object.startIsMilestone;
            json.workCount = object.workCount;
            if (object.floors != null && object.floors.size() > 0) json.floor = object.floors.size() + "";
            if (object.tasks != null && object.tasks.size() > 0) json.hasChild = true;
            else json.hasChild = false;
            for (ProjectObAssignRel rel : object.projectObAssignRels) {
                json.assignIds.add(rel.user.id);
            }
            jsonMain.tasks.add(json);

            for (Task task : object.getTasks()) {
                json = new GanttJson();
                json.id = "t" + task.id.intValue();
                json.name = task.name;
                json.level = task.level.intValue();
                json.code = task.code;
                json.status = task.status;
                json.depends = task.depends;
                json.startDate = Consts.myDateFormat.format(task.startDate);
                json.duration = task.duration.intValue();
                json.progress = task.completedPercent;
                json.scopePercent = task.scopePercent;
                json.endDate = Consts.myDateFormat.format(task.finishDate);
                json.startIsMilestone = task.startIsMilestone;
                json.endIsMilestone = task.startIsMilestone;
                json.workCount = task.workCount;
                if (task.tasks.size() > 0) json.hasChild = true;
                else json.hasChild = false;
                if (task.floor != null) json.floor = task.floor.name;
                if (task.taskInventoryRels.size() > 0) json.material = task.taskInventoryRels.size() + "";
                if (task.taskEquipmentRels.size() > 0) json.technical = task.taskEquipmentRels.size() + "";
                if (task.taskManPowerRels.size() > 0) json.person = task.taskManPowerRels.size() + "";
                int c = 0;
                for (TaskAssignRel rel : task.taskAssignRels) {
                    json.assign += rel.user.toString();
                    if (c < task.taskAssignRels.size() - 1) json.assign += ", ";
                    c++;
                    json.assignIds.add(rel.user.id);
                }
                jsonMain.tasks.add(json);
            }
        }
//              System.out.println("JSON");
//            System.out.println(jsonMain.toJsonString());
        return jsonMain;
    }

//    public static void deleteProject(Long pid) {
//        Project project = Project.findById(pid);
//        project._delete();
//        showProject();
//    }

    public static Task checkLevelTask(List<GanttJsonParseTask> parseTasks, int level) {
        level--;
        for (GanttJsonParseTask parseTask : parseTasks) {
            if (parseTask.level == level) return parseTask.task;
        }
        return null;
    }

    public static List<GanttJsonParseTask> addLevelTask(List<GanttJsonParseTask> parseTasks, Task task, int level) {
        boolean insert = true;
        for (GanttJsonParseTask parseTask : parseTasks) {
            if (parseTask.level == level) {
                parseTask.task = task;
                insert = false;
            }
        }
        if (insert) {
            GanttJsonParseTask jsonParseTask = new GanttJsonParseTask();
            jsonParseTask.level = level;
            jsonParseTask.task = task;
            parseTasks.add(jsonParseTask);
        }
        return parseTasks;
    }

    public static Long getTaskId(String s) {
        Long id = null;
        try {
            if (s.charAt(0) == 't' || s.charAt(0) == 'o' || s.charAt(0) == 'p') {
                id = Long.parseLong(s.substring(1, s.length()));
            } else id = Long.parseLong(s);
        } catch (Exception e) {
            return id;
        }
        return id;
    }

    public static void saveProject(String prj, String deletedTasks) {
        JsonObject json = (JsonObject) new JsonParser().parse(prj);
//        System.out.println();
        String response = "success";
        JsonArray el = json.getAsJsonArray("tasks");
        JsonObject jo;
        List<GanttJsonParseTask> parseTasks = new ArrayList<GanttJsonParseTask>();
        Project project = null;
        ProjectObject object = null;
        Long lo;
        Long in;
        boolean save = false;
        int orderObject = 0, orderTask = 0;
        try {
            for (JsonElement element : el) {
                jo = element.getAsJsonObject();
                in = jo.get("level").getAsLong();
                if (in.intValue() == 0) {
                    lo = getTaskId(jo.get("id").getAsString());
                    if (lo == null || lo < 0L) project = new Project();
                    else {
                        project = Project.findById(lo);
                        String[] deletedTask = deletedTasks.split("#");
                        if (deletedTasks.length() > 1) {
                            for (int d = deletedTask.length - 1; d >= 0; d--) {
                                if (deletedTask[d].charAt(0) == 'p') {
                                    Project dproject = Project.findById(Long.parseLong(deletedTask[d].substring(1, deletedTask[d].length())));
                                    if (dproject != null) dproject._delete();
                                } else if (deletedTask[d].charAt(0) == 'o') {
                                    ProjectObject dobject = ProjectObject.findById(Long.parseLong(deletedTask[d].substring(1, deletedTask[d].length())));
                                    try {
                                        if (dobject != null) dobject._delete();
                                    } catch (Exception e) {
                                        response = dobject.name + " -г устгаж чадахгүй байна, Түүний ажлуудад түүх үүссэн байна!";
                                    }
                                } else if (deletedTask[d].charAt(0) == 't') {
                                    Task dtask = Task.findById(Long.parseLong(deletedTask[d].substring(1, deletedTask[d].length())));
                                    try {
                                        if (dtask != null) dtask._delete();
                                    } catch (Exception e) {
                                        response = dtask.name + " -г устгаж чадахгүй байна, Энэ ажилд түүх үүссэн байна!";
                                    }
                                }
                            }
                        }
                        save = true;
                    }
                    project.code = jo.get("code").getAsString();
                    project.name = jo.get("name").getAsString();
                    project.startDate = Consts.myDateFormat.parse(jo.get("startDate").getAsString());
                    project.finishDate = Consts.myDateFormat.parse(jo.get("endDate").getAsString());
                    if (project.actualFinish == null || project.actualFinish.before(project.finishDate))
                        project.actualFinish = project.finishDate;
                    project.duration = jo.get("duration").getAsLong();
                    project.status = jo.get("status").getAsString();
                    project.depends = jo.get("depends").getAsString();
                    project.startIsMilestone = jo.get("startIsMilestone").getAsBoolean();
                    project.workCount = jo.get("workCount").getAsString();
                    project.owner = Users.getUser();
                    if (save) {
                        project._save();
                    } else {
                        project.projectAssignRels = new ArrayList<ProjectAssignRel>();
                        List<FolderBasic> folderBasics = FolderBasic.findAll();
                        project.folderStructures = new ArrayList<FolderStructure>();
                        FolderStructure folderStructureParent = null;
                        FolderStructure folderStructureParent2 = null;
                        for (FolderBasic basic : folderBasics) {
                            FolderStructure folderStructure = new FolderStructure();
                            folderStructure.queue = basic.queue;
                            folderStructure.folderType = basic.folderType;
                            folderStructure.folderColor = basic.folderColor;
                            folderStructure.name = basic.name;
                            if (basic.parentFolder) {
                                if (basic.folderBasic == null) folderStructureParent = folderStructure;
                                else {
                                    folderStructure.folderStructure = folderStructureParent;
                                    if (basic.folderBasic != null && basic.folderBasic.parentFolder)
                                        folderStructureParent2 = folderStructure;
                                }
                            } else {
                                if (basic.folderBasic.folderBasic == null)
                                    folderStructure.folderStructure = folderStructureParent;
                                else folderStructure.folderStructure = folderStructureParent2;
                            }
                            folderStructure.project = project;
                            project.folderStructures.add(folderStructure);
                        }
                        project.create();
                    }
                } else if (in.intValue() == 1) {
                    lo = getTaskId(jo.get("id").getAsString());
                    if (lo == null || lo < 0L) {
                        object = new ProjectObject();
                        save = false;
                    } else {
                        object = ProjectObject.findById(lo);
                        save = true;
                    }
                    object.code = jo.get("code").getAsString();
                    object.name = jo.get("name").getAsString();
                    object.startDate = Consts.myDateFormat.parse(jo.get("startDate").getAsString());
                    object.finishDate = Consts.myDateFormat.parse(jo.get("endDate").getAsString());
                    if (object.actualFinish == null || object.actualFinish.before(object.finishDate)) object.actualFinish = object.finishDate;
                    object.duration = jo.get("duration").getAsLong();
                    object.scopePercent = jo.get("scopePercent").getAsFloat();
                    if (object.scopePercent == null) object.scopePercent = 0f;
                    object.status = jo.get("status").getAsString();
                    object.depends = jo.get("depends").getAsString();
                    object.startIsMilestone = jo.get("startIsMilestone").getAsBoolean();
                    object.owner = Users.getUser();
                    object.project = project;
                    object.workCount = jo.get("workCount").getAsString();
                    object.orderGantt = orderObject;
                    orderObject++;
                    if (save) {
                        object._save();
                    } else {
                        object.projectObAssignRels = new ArrayList<ProjectObAssignRel>();
                        object.create();
                    }
                } else {
                    Task task;
                    lo = getTaskId(jo.get("id").getAsString());
                    if (lo == null || lo < 0L) {
                        task = new Task();
                        save = false;
                    } else {
                        task = Task.findById(lo);
                        save = true;
                    }
                    task.code = jo.get("code").getAsString();
                    task.name = jo.get("name").getAsString();
                    task.startDate = Consts.myDateFormat.parse(jo.get("startDate").getAsString());
                    task.finishDate = Consts.myDateFormat.parse(jo.get("endDate").getAsString());
//                Calendar calendar = Calendar.getInstance();
//                calendar.setTime(task.finishDate);
//                calendar.set(Calendar.SECOND,58);
//                task.finishDate=calendar.getTime();
//                System.out.println(task.finishDate);
                    if (task.actualFinish == null || task.actualFinish.before(task.finishDate)) task.actualFinish = task.finishDate;
                    task.duration = jo.get("duration").getAsLong();
                    task.scopePercent = jo.get("scopePercent").getAsFloat();
                    if (task.scopePercent == null) task.scopePercent = 0f;
                    task.status = jo.get("status").getAsString();
                    task.depends = jo.get("depends").getAsString();
                    task.startIsMilestone = jo.get("startIsMilestone").getAsBoolean();
                    task.owner = Users.getUser();
                    task.level = in;
                    task.workCount = jo.get("workCount").getAsString();
                    task.orderGantt = orderTask;
                    orderTask++;
                    task.hasChild = jo.get("hasChild").getAsBoolean();
                    if (task.hasChild)
                        parseTasks = addLevelTask(parseTasks, task, in.intValue());
                    if (in.intValue() != 2) task.task = checkLevelTask(parseTasks, in.intValue());
                    task.projectObject = object;
                    if (save) {
                        task._save();
                    } else {
                        task.tasks = new ArrayList<Task>();
                        task.taskAssignRels = new ArrayList<TaskAssignRel>();
                        task.taskInventoryRels = new ArrayList<TaskInventoryRel>();
                        task.taskEquipmentRels = new ArrayList<TaskEquipmentRel>();
                        task.taskManPowerRels = new ArrayList<TaskManPowerRel>();
                        task.create();
                    }
                }
            }
            project.refresh();
            renderJSON(getProjectJsonData(response, project));
        } catch (Exception e) {
            System.out.println(e);
            GanttJsonMain jsonMain = new GanttJsonMain();
            jsonMain.response = response;
            renderJSON(jsonMain);
        }
    }

    public static void deleteCheck(String tid) {
        Long id = Long.parseLong(tid.substring(1, tid.length()));
        if (tid.charAt(0) == 'o') {
            ProjectObject object = ProjectObject.findById(id);
            for (Task task : object.tasks) {
                if (task.meetingTopics.size() > 0 ||
                        task.rfis.size() > 0 ||
                        task.punchLists.size() > 0 ||
                        task.dailyLogScheduledWorks.size() > 0 ||
                        task.weathers.size() > 0 ||
                        task.technicalDelays.size() > 0 ||
                        task.deliveries.size() > 0 ||
                        task.inspections.size() > 0 ||
                        task.notesLog.size() > 0 ||
                        task.sanaachlagas.size() > 0 ||
                        task.visitors.size() > 0 ||
                        task.myPlans.size() > 0)
                    renderText(object.name + " -г устгаж чадахгүй байна, Түүний ажлуудад түүх үүссэн байна!");
            }
        } else { //task
            Task task = Task.findById(id);
            if (task.meetingTopics.size() > 0 ||
                    task.rfis.size() > 0 ||
                    task.punchLists.size() > 0 ||
                    task.dailyLogScheduledWorks.size() > 0 ||
                    task.weathers.size() > 0 ||
                    task.technicalDelays.size() > 0 ||
                    task.deliveries.size() > 0 ||
                    task.inspections.size() > 0 ||
                    task.notesLog.size() > 0 ||
                    task.sanaachlagas.size() > 0 ||
                    task.visitors.size() > 0 ||
                    task.myPlans.size() > 0)
                renderText(task.name + " -д түүх үүссэн учир устгаж чадахгүй байна!");
        }
        renderText("success");
    }

    public static boolean checkProjectAssign(User us, Project project) {
        for (ProjectAssignRel assignRels : project.projectAssignRels) {
            if (assignRels.user.id.compareTo(us.id) == 0) return true;
        }
        return false;
    }

    public static boolean checkProjectObAssign(User us, ProjectObject projectObject) {
        for (ProjectObAssignRel assignRels : projectObject.projectObAssignRels) {
            if (assignRels.user.id.compareTo(us.id) == 0) return true;
        }
        return false;
    }

    public static void getFloors(Long oid) {
        ProjectObject projectObject = ProjectObject.findById(oid);
        render(projectObject);
    }

    public static void delFloors(Long floorId) {
        Floor floor = Floor.findById(floorId);
        if (floor != null) {
            for (Task task : floor.tasks) {
                task.floor = null;
                task._save();
            }
            floor.refresh();
            floor._delete();
        }
    }

    public static void uptFloors(Long oid, Long[] ids, String[] names) {
        ProjectObject projectObject = ProjectObject.findById(oid);
        String text = "";
        if (projectObject != null && ids != null && names != null) {
            for (int i = 0; i < ids.length; i++) {
                if (ids[i].intValue() == 0) {
                    Floor floor = new Floor();
                    floor.name = names[i];
                    floor.projectObject = projectObject;
                    floor.create();
                } else {
                    Floor floor = Floor.findById(ids[i]);
                    floor.name = names[i];
                    floor.projectObject = projectObject;
                    floor._save();
                }
            }
            if (projectObject.floors.size() > 0) text = projectObject.floors.size() + "";
        }
        renderText(text);
    }

    public static void getFloor(Long tid) {
        Task task = Task.findById(tid);
        render(task);
    }

    public static void setFloor(Long tid, Long fid) {
        Task task = Task.findById(tid);
        task.floor = Floor.findById(fid);
        task._save();
    }


    public static void ganttViewer() {
        if (Users.pid() == 0) forbidden();
        Long pid = Users.pid();
        Project project = Project.findById(pid);
        String prjData = getProjectJsonData("", project).toJsonString();
        String selectStyle = "styleNot";
        List<UserTeam> userTeams = getUserTeams(pid);
        render(prjData, project, selectStyle, userTeams);
    }

    public static void ganttViewInfo(String id) {
        int vtype;
        Long oid = Long.parseLong(id.substring(1, id.length()));
        if (id.charAt(0) == 'p') {
            vtype = 1;
            Project object = Project.findById(oid);
            render(object, vtype);
        } else if (id.charAt(0) == 'o') {
            vtype = 2;
            ProjectObject object = ProjectObject.findById(oid);
            render(object, vtype);
        } else if (id.charAt(0) == 't') {
            vtype = 3;
            Task object = Task.findById(oid);
            render(object, vtype);
        }
    }

    public static void getUsers(Long tid) {
        List<TaskAssignRel> assignRels = TaskAssignRel.find("task.id=?1", tid).fetch();
        render(assignRels);
    }

    public static void delUsers(Long aid) {
        TaskAssignRel rel = TaskAssignRel.findById(aid);
        Task task;
        if (rel != null) {
            task = rel.task;
            User user = rel.user;
            rel._delete();
            List<TaskAssignRel> taskAssignRels = TaskAssignRel.find("user.id=?1 AND task.projectObject.id=?2", user.id, task.projectObject.id).fetch();
            if (taskAssignRels.size() == 0) {
                ProjectObAssignRel obAssignRels = ProjectObAssignRel.find("user.id=?1 and projectObject.id=?2", user.id, task.projectObject.id).first();
                obAssignRels._delete();
            }
            taskAssignRels = TaskAssignRel.find("user.id=?1 AND task.projectObject.project.id=?2", user.id, task.projectObject.project.id).fetch();
            if (taskAssignRels.size() == 0) {
                ProjectAssignRel assignRels = ProjectAssignRel.find("user.id=?1 and project.id=?2", user.id, task.projectObject.project.id).first();
                assignRels._delete();
            }
            renderJSON(getAssignedJson(task));
        }
    }

    public static void addUser() {
        List<User> users = User.find("ORDER BY userTeam.name, userPosition.rate, firstName").fetch();
        render(users);
    }

    public static void uptUsers(Long tid, int level, Long[] ids, Long[] uids, Float[] vals) {
        JsonObject ganttAssigns = new JsonObject();
        List<User> users = new ArrayList<User>();
        int i;
        if (ids != null && uids != null) {
            for (i = 0; i < ids.length; i++) {
                User user = User.findById(uids[i]);
                users.add(user);
            }
        }
        if (users.size() > 0) {
            if (level > 1) {
                Task task = Task.findById(tid);
                if (task != null && !task.hasChild && ids != null && uids != null) {
                    i = 0;
                    for (User user : users) {
                        if (ids[i].intValue() == 0) {
                            TaskAssignRel rel = new TaskAssignRel();
                            rel.user = user;
                            rel.task = task;
                            rel.hours = vals[i];
                            task.taskAssignRels.add(rel);
                            rel.create();
                        } else {
                            TaskAssignRel rel = TaskAssignRel.findById(ids[i]);
                            rel.user = user;
                            rel.task = task;
                            rel.hours = vals[i];
                            rel._save();
                        }
                        if (!checkProjectObAssign(user, task.projectObject)) {
                            ProjectObAssignRel rel1 = new ProjectObAssignRel();
                            rel1.user = user;
                            rel1.projectObject = task.projectObject;
                            task.projectObject.projectObAssignRels.add(rel1);
                            rel1.create();
                        }
                        if (!checkProjectAssign(user, task.projectObject.project)) {
                            ProjectAssignRel rel2 = new ProjectAssignRel();
                            rel2.user = user;
                            rel2.project = task.projectObject.project;
                            task.projectObject.project.projectAssignRels.add(rel2);
                            rel2.create();
                        }
                        i++;
                    }
                    ganttAssigns = getAssignedJson(task);
                } else if (task.hasChild) {  //Task Is Group
                    Task first_task = null;
                    for (TaskAssignRel assignRel : task.taskAssignRels) assignRel._delete();
                    for (Task tchild : task.getChildTasks()) {
                        for (TaskAssignRel assignRel : tchild.taskAssignRels) assignRel._delete();
                        i = 0;
                        for (User user : users) {
                            TaskAssignRel rel = new TaskAssignRel();
                            rel.user = user;
                            rel.task = tchild;
                            rel.hours = vals[i];
                            tchild.taskAssignRels.add(rel);
                            rel.create();
                            i++;
                        }
                        if (first_task == null) first_task = tchild;
                    }
                    for (User user : users) {
                        if (!checkProjectObAssign(user, task.projectObject)) {
                            ProjectObAssignRel rel1 = new ProjectObAssignRel();
                            rel1.user = user;
                            rel1.projectObject = task.projectObject;
                            task.projectObject.projectObAssignRels.add(rel1);
                            rel1.create();
                        }
                        if (!checkProjectAssign(user, task.projectObject.project)) {
                            ProjectAssignRel rel2 = new ProjectAssignRel();
                            rel2.user = user;
                            rel2.project = task.projectObject.project;
                            task.projectObject.project.projectAssignRels.add(rel2);
                            rel2.create();
                        }
                    }
                    if (first_task != null) ganttAssigns = getAssignedJson(first_task);
                }
            } else if (level == 1) {
                ProjectObject projectObject = ProjectObject.findById(tid);
                Task first_task = null;
                for (Task task : projectObject.getChildTasks()) {
                    if (!task.hasChild && users.size() > 0) {
                        for (TaskAssignRel assignRel : task.taskAssignRels) assignRel._delete();
                        i = 0;
                        for (User user : users) {
                            TaskAssignRel rel = new TaskAssignRel();
                            rel.user = user;
                            rel.task = task;
                            rel.hours = vals[i];
                            task.taskAssignRels.add(rel);
                            rel.create();
                            i++;
                        }
                        if (first_task == null) first_task = task;
                    }
                }
                for (ProjectObAssignRel assignRel : projectObject.projectObAssignRels) assignRel._delete();
                projectObject.projectObAssignRels = new ArrayList<ProjectObAssignRel>();
                for (User user : users) {
                    ProjectObAssignRel rel1 = new ProjectObAssignRel();
                    rel1.user = user;
                    rel1.projectObject = projectObject;
                    projectObject.projectObAssignRels.add(rel1);
                    rel1.create();
                    if (!checkProjectAssign(user, projectObject.project)) {
                        ProjectAssignRel rel2 = new ProjectAssignRel();
                        rel2.user = user;
                        rel2.project = projectObject.project;
                        projectObject.project.projectAssignRels.add(rel2);
                        rel2.create();
                    }
                }
                if (first_task != null) ganttAssigns = getAssignedJson(first_task);
            }
        }
        renderJSON(ganttAssigns);
    }

    public static JsonObject getAssignedJson(Task task) {
        JsonObject ganttAssigns = new JsonObject();
        int c = 0;
        String assign = "";
        JsonArray array = new JsonArray();
        for (TaskAssignRel rel : task.taskAssignRels) {
            assign += rel.user.toString();
            if (c < task.taskAssignRels.size() - 1) assign += ", ";
            c++;
            array.add(new JsonPrimitive(rel.user.id));
        }
        ganttAssigns.addProperty("names", assign);
        ganttAssigns.add("ids", array);

        JsonArray proObjAss = new JsonArray();
        for (ProjectObAssignRel rel : task.projectObject.projectObAssignRels)
            proObjAss.add(new JsonPrimitive(rel.user.id));
        ganttAssigns.add("objectIds", proObjAss);

        JsonArray projectAss = new JsonArray();
        for (ProjectAssignRel rel : task.projectObject.project.projectAssignRels)
            projectAss.add(new JsonPrimitive(rel.user.id));
        ganttAssigns.add("projectIds", projectAss);
        return ganttAssigns;
    }

    public static void addResource(int rtype) {
        if (rtype == 1) {
            List<Inventory> inventories = Inventory.findAll();
            render(rtype, inventories);
        } else if (rtype == 2) {
            List<Equipment> equipments = Equipment.findAll();
            render(rtype, equipments);
        } else if (rtype == 3) {
            List<ManPower> manPowers = ManPower.findAll();
            render(rtype, manPowers);
        }
    }

    public static void getResource(int rtype, Long tid) {
        if (rtype == 1) {
            List<Inventory> inventories = Inventory.findAll();
            List<TaskInventoryRel> taskInventoryRels = TaskInventoryRel.find("task.id=?1", tid).fetch();
            render(rtype, taskInventoryRels, inventories);
        } else if (rtype == 2) { // equipment
            List<Equipment> equipments = Equipment.findAll();
            List<TaskEquipmentRel> taskEquipmentRels = TaskEquipmentRel.find("task.id=?1", tid).fetch();
            render(rtype, taskEquipmentRels, equipments);
        } else if (rtype == 3) { // manPower
            List<ManPower> manPowers = ManPower.findAll();
            List<TaskManPowerRel> taskManPowerRels = TaskManPowerRel.find("task.id=?1", tid).fetch();
            render(rtype, taskManPowerRels, manPowers);
        }
    }

    public static void delResource(int rtype, Long rid) {
        if (rtype == 1) {
            TaskInventoryRel rel = TaskInventoryRel.findById(rid);
            rel._delete();
        } else if (rtype == 2) {
            TaskEquipmentRel rel = TaskEquipmentRel.findById(rid);
            rel._delete();
        } else if (rtype == 3) {
            TaskManPowerRel rel = TaskManPowerRel.findById(rid);
            rel._delete();
        }
    }

    public static void uptResource(int rtype, Long tid, Long[] rids, Long[] ids, String[] vals) {
        Task task = Task.findById(tid);
        String text = "";
        if (task != null && ids != null && rids != null && vals != null) {
            if (rtype == 1) {
                for (int i = 0; i < rids.length; i++) {
                    if (rids[i].intValue() == 0) {
                        TaskInventoryRel rel = new TaskInventoryRel();
                        rel.task = task;
                        rel.inventory = Inventory.findById(ids[i]);
                        rel.value = Float.parseFloat(vals[i]);
                        rel.create();
                    } else {
                        TaskInventoryRel rel = TaskInventoryRel.findById(rids[i]);
                        rel.task = task;
                        rel.inventory = Inventory.findById(ids[i]);
                        rel.value = Float.parseFloat(vals[i]);
                        rel._save();
                    }
                }
                if (task.taskInventoryRels.size() > 0) text = task.taskInventoryRels.size() + "";
            } else if (rtype == 2) {
                for (int i = 0; i < rids.length; i++) {
                    TaskEquipmentRel rel;
                    if (rids[i].intValue() == 0) rel = new TaskEquipmentRel();
                    else rel = TaskEquipmentRel.findById(rids[i]);
                    rel.task = task;
                    rel.equipment = Equipment.findById(ids[i]);
                    rel.value = Float.parseFloat(vals[i]);
                    if (rids[i].intValue() == 0) rel.create();
                    else rel._save();
                }
                if (task.taskEquipmentRels.size() > 0) text = task.taskEquipmentRels.size() + "";
            } else if (rtype == 3) {
                for (int i = 0; i < rids.length; i++) {
                    TaskManPowerRel rel;
                    if (rids[i].intValue() == 0) rel = new TaskManPowerRel();
                    else rel = TaskManPowerRel.findById(rids[i]);
                    rel.task = task;
                    rel.manPower = ManPower.findById(ids[i]);
                    rel.amount = Float.parseFloat(vals[i]);
                    if (rids[i].intValue() == 0) rel.create();
                    else rel._save();
                }
                if (task.taskManPowerRels.size() > 0) text = task.taskManPowerRels.size() + "";
            }
        }
        renderText(text);
    }

    public static void saveTemplate(String name, String[] names, int[] levels, int[] durations, float[] scopePercents) {
        if (name != null && name.length() > 0) {
            TmpGantt tmpGantt = new TmpGantt();
            tmpGantt.name = name;
            tmpGantt.project = false;
            tmpGantt.tmpGanttTrees = new ArrayList<TmpGanttTree>();
            for (int i = 0; i < names.length; i++) {
                TmpGanttTree tree = new TmpGanttTree();
                tree.name = names[i];
                tree.duration = durations[i];
                tree.level = levels[i];
                tree.scopePercent = scopePercents[i];
                tree.tmpGantt = tmpGantt;
                tmpGantt.tmpGanttTrees.add(tree);
            }
            tmpGantt.create();
        }
    }

    public static void loadTemplates(boolean project) {
        List<TmpGantt> tmpGantts = TmpGantt.find("project=?1 ORDER BY name", project).fetch();
        render(tmpGantts);
    }

    public static void deleteTemplate(Long id) {
        TmpGantt tmpGantt = TmpGantt.findById(id);
        tmpGantt._delete();
    }

    public static void getTemplate(Long id) {
        List<TmpGanttTree> ganttTrees = TmpGanttTree.find("tmpGantt.id=?1", id).fetch();
        JsonArray ganttTreeVals = new JsonArray();
        Date date = new Date();
        for (TmpGanttTree tree : ganttTrees) {
            JsonObject obj = new JsonObject();
            obj.addProperty("name", tree.name);
            obj.addProperty("start", date.getTime());
            obj.addProperty("duration", tree.duration);
            obj.addProperty("level", tree.level);
            obj.addProperty("scopePercent", tree.scopePercent);
            ganttTreeVals.add(obj);
        }
        renderJSON(ganttTreeVals);
    }

    public static void saveTemplateProject(String name, String prj) {
        if (name != null && name.length() > 0) {
            TmpGantt tmpGantt = new TmpGantt();
            tmpGantt.name = name;
            tmpGantt.project = true;
            tmpGantt.tmpGanttTrees = new ArrayList<TmpGanttTree>();
            boolean save = false;
            JsonObject json = (JsonObject) new JsonParser().parse(prj);
            JsonArray el = json.getAsJsonArray("tasks");
            JsonObject jo;
            try {
                for (JsonElement element : el) {
                    jo = element.getAsJsonObject();
                    TmpGanttTree tree = new TmpGanttTree();
                    tree.level = jo.get("level").getAsInt();
                    if (tree.level > 0) save = true;
                    tree.name = jo.get("name").getAsString();
                    tree.startDate = Consts.myDateFormat.parse(jo.get("startDate").getAsString());
                    tree.duration = jo.get("duration").getAsInt();
                    tree.scopePercent = jo.get("scopePercent").getAsFloat();
                    tree.status = jo.get("status").getAsString();
                    tree.depends = jo.get("depends").getAsString();
                    tree.tmpGantt = tmpGantt;
                    tree.startIsMilestone = jo.get("startIsMilestone").getAsBoolean();
                    tree.workCount = jo.get("workCount").getAsString();
                    tmpGantt.tmpGanttTrees.add(tree);
                }
            } catch (Exception e) {
                System.out.println(e);
            }
            if (save) tmpGantt.create();
        }
    }

    public static void loadTemplateProject(Long id,Date projStart) {
        Project project = Project.findById(Users.pid());
        int count = 0, deff = 0;
        if (project.projectObjects.size() == 0) {
            GanttJsonMain jsonMain = new GanttJsonMain();
            TmpGantt tmpGantt = TmpGantt.findById(id);
            for (TmpGanttTree tree : tmpGantt.tmpGanttTrees) {
                GanttJson json = new GanttJson();
                if (tree.level == 0) {
                    json.id = "p" + project.id.intValue();
                    json.name = project.name;
                    if (tree.startDate.getTime() < projStart.getTime())
                        deff = Functions.getDifferenceDays(tree.startDate, projStart);
                    else {
                        deff = Functions.getDifferenceDays(projStart, tree.startDate);
                        deff *= -1;
                    }
                    json.startDate = Consts.myDateFormat.format(projStart);
                } else {
                    json.id = "tmp_1438" + count;
                    json.name = tree.name;
                    json.startDate = Consts.myDateFormat.format(Functions.computedDate(tree.startDate, deff));
                }
                json.level = tree.level;
                json.status = tree.status;
                json.scopePercent = tree.scopePercent;
                json.startIsMilestone = tree.startIsMilestone;
                json.endIsMilestone = tree.startIsMilestone;
                json.workCount = tree.workCount;
                json.depends = tree.depends;
                json.duration = tree.duration;
                json.endDate = Consts.myDateFormat.format(Functions.computedDateWorkDays(project, tree.startDate, deff));
                jsonMain.tasks.add(json);
                count++;
            }
            renderJSON(jsonMain);
        }
    }
}