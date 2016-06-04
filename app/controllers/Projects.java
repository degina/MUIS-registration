package controllers;

import controllers.MyClass.MyDay;
import controllers.MyClass.MyMonth;
import controllers.MyClass.MyWeek;
import controllers.MyClass.MyYear;
import models.*;
import play.data.binding.Binder;
import play.db.Model;
import play.exceptions.TemplateNotFoundException;
import play.mvc.With;

import java.lang.reflect.Constructor;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by enkhamgalan on 12/21/14.
 */
@With(Secure.class)
public class Projects extends CRUD {
    public static void list(int page, String search, String searchFields, String orderBy, String order) {
        ObjectType type = ObjectType.get(getControllerClass());
        notFoundIfNull(type);
        if (page < 1) {
            page = 1;
        }
        if (orderBy != null && orderBy.equals("ProjectObjects")) {
            orderBy = "projectObjects.size";
        }
        List<Model> objects = type.findPage(page, search, searchFields, orderBy, order, (String) request.args.get("where"));
        Long count = type.count(search, searchFields, (String) request.args.get("where"));
        Long totalCount = type.count(null, null, (String) request.args.get("where"));
        if (orderBy != null && orderBy.equals("projectObjects.size")) {
            orderBy = "ProjectObjects";
        }
        try {
            render(type, objects, count, totalCount, page, orderBy, order);
        } catch (TemplateNotFoundException e) {
            render("CRUD/list.html", type, objects, count, totalCount, page, orderBy, order);
        }
    }

    public static void create() throws Exception {
        ObjectType type = ObjectType.get(getControllerClass());
        notFoundIfNull(type);
        Constructor<?> constructor = type.entityClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        Project object = (Project) constructor.newInstance();
        Binder.bindBean(params.getRootParamNode(), "object", object);
        object.owner = Users.getUser();
        validation.valid(object);
        if (validation.hasErrors()) {
            renderArgs.put("error", play.i18n.Messages.get("crud.hasErrors"));
            try {
                render(request.controller.replace(".", "/") + "/blank.html", type, object);
            } catch (TemplateNotFoundException e) {
                render("CRUD/blank.html", type, object);
            }
        }
        object.projectCounter = new ProjectCounter();
        object._save();
        flash.success(play.i18n.Messages.get("crud.created", type.modelName));
        if (params.get("_save") != null) {
            redirect(request.controller + ".list");
        }
        if (params.get("_saveAndAddAnother") != null) {
            redirect(request.controller + ".blank");
        }
        redirect(request.controller + ".show", object._key());
    }

    public static void save(String id) throws Exception {
        ObjectType type = ObjectType.get(getControllerClass());
        notFoundIfNull(type);
        Project object = Project.findById(Long.parseLong(id));
        notFoundIfNull(object);
        User owner = object.owner;
        Binder.bindBean(params.getRootParamNode(), "object", object);
        object.owner = owner;
        validation.valid(object);
        if (validation.hasErrors()) {
            renderArgs.put("error", play.i18n.Messages.get("crud.hasErrors"));
            try {
                render(request.controller.replace(".", "/") + "/show.html", type, object);
            } catch (TemplateNotFoundException e) {
                render("CRUD/show.html", type, object);
            }
        }
        object._save();
        flash.success(play.i18n.Messages.get("crud.saved", type.modelName));
        if (params.get("_save") != null) {
            redirect(request.controller + ".list");
        }
        redirect(request.controller + ".show", object._key());
    }

    public static void showActivatedProjects(int page, String search, String searchFields, String orderBy, String order) {
        ObjectType type = ObjectType.get(getControllerClass());
        notFoundIfNull(type);
        if (page < 1) {
            page = 1;
        }
        String where = "completedPercent<100";
        List<Model> objects = type.findPage(page, search, searchFields, orderBy, order, where);
        Long count = type.count(search, searchFields, where);
        Long totalCount = type.count(null, null, where);
        if (orderBy != null && orderBy.equals("projectObjects.size")) {
            orderBy = "ProjectObjects";
        }
        render(type, objects, count, totalCount, page, orderBy, order);
    }

    public static void projectAdd(String name) {
        Project project = new Project();
        project.name = name;
        project.owner = Users.getUser();
        project.create();
        setDates(project.id);
    }

    public static void deleteProject(Long pid) {
        Project project = Project.findById(pid);
        project._delete();
        setDates(0L);
    }

    public static void changeName(String type, Long id, String name) {

    }

    public static void objectOperation(String type, Long id, Long pid, String name) {
        if (type.equals("add")) {
            ProjectObject projectObject = new ProjectObject();
            projectObject.name = name;
            projectObject.project = Project.findById(pid);
            projectObject.create();
        } else if (type.equals("save")) {
            ProjectObject projectObject = ProjectObject.findById(id);
            projectObject.name = name;
            projectObject._save();
        } else if (type.equals("delete")) {
            ProjectObject projectObject = ProjectObject.findById(id);
            projectObject._delete();
        }
    }

    public static void setDates(Long pid) {
        String navbar = "Dashboard";
        List<Project> projects = Project.find("order by startDate,name").fetch();
        if (pid > 0) {
            List<ProjectObject> projectObjects = ProjectObject.find("project.id=?1", pid).fetch();
            Project project = Project.findById(pid);
            List<MyDay> days = new ArrayList<MyDay>();
            List<MyMonth> months = new ArrayList<MyMonth>();
            List<MyWeek> weeks = new ArrayList<MyWeek>();
            List<MyYear> years = new ArrayList<MyYear>();
            int lastMonth, lastDate, lastYear;
            Calendar cal = Functions.setCalDay(Calendar.getInstance());

            String stype = "Day";
            if (stype.equals("Day")) {
                String dateFormat = "dd";
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);

                if (project.startDate != null) {
                    cal.setTime(project.startDate);
                    cal.setFirstDayOfWeek(Calendar.MONDAY);
                    lastMonth = cal.get(Calendar.MONTH);
                    months.add(new MyMonth(Functions.monthName[lastMonth], lastMonth, 1, null));
                    days.add(new MyDay(cal.getTime(), simpleDateFormat.format(cal.getTime()), Functions.dayNamesMin[cal.get(Calendar.DAY_OF_WEEK) - 1], cal.get(Calendar.DAY_OF_WEEK)));
                    for (int d = 1; d < project.duration; d++) {
                        cal.add(Calendar.DATE, 1);
                        days.add(new MyDay(cal.getTime(), simpleDateFormat.format(cal.getTime()), Functions.dayNamesMin[cal.get(Calendar.DAY_OF_WEEK) - 1], cal.get(Calendar.DAY_OF_WEEK)));
                        if (cal.get(Calendar.MONTH) == lastMonth) months.get(months.size() - 1).merge++;
                        else {
                            lastMonth = cal.get(Calendar.MONTH);
                            months.add(new MyMonth(Functions.monthName[lastMonth], lastMonth, 1, null));
                        }
                    }
                }
            } else if (stype.equals("Week")) {
                if (project.startDate != null) {
                    cal.setTime(project.startDate);
                    cal.setFirstDayOfWeek(Calendar.MONDAY);
                    lastMonth = cal.get(Calendar.MONTH);
                    lastDate = cal.get(Calendar.WEEK_OF_MONTH);
                    lastYear = cal.get(Calendar.YEAR);
                    months.add(new MyMonth(Functions.monthName[lastMonth], lastMonth, 1, null));
                    weeks.add(new MyWeek(lastDate + "", cal.getTime(), lastDate, cal.get(Calendar.YEAR)));
                    years.add(new MyYear(lastYear + "", lastYear, 1));
                    for (int d = 1; d < project.duration; d++) {
                        cal.add(Calendar.DATE, 1);
                        if (cal.get(Calendar.WEEK_OF_MONTH) != lastDate) {
                            lastDate = cal.get(Calendar.WEEK_OF_MONTH);
                            weeks.add(new MyWeek(lastDate + "", cal.getTime(), lastDate, cal.get(Calendar.YEAR)));

                            if (cal.get(Calendar.MONTH) == lastMonth) months.get(months.size() - 1).merge++;
                            else {
                                lastMonth = cal.get(Calendar.MONTH);
                                months.add(new MyMonth(Functions.monthName[lastMonth], lastMonth, 1, null));
                            }
                            if (cal.get(Calendar.YEAR) == lastYear) years.get(years.size() - 1).merge++;
                            else {
                                lastYear = cal.get(Calendar.YEAR);
                                years.add(new MyYear(lastYear + "", lastYear, 1));
                            }
                        }
                    }
                }
            } else if (stype.equals("Month")) {
                if (project.startDate != null) {
                    cal.setTime(project.startDate);
                    cal.setFirstDayOfWeek(Calendar.MONDAY);
                    lastMonth = cal.get(Calendar.MONTH);
                    lastYear = cal.get(Calendar.YEAR);
                    months.add(new MyMonth(Functions.monthName[lastMonth], lastMonth, cal.get(Calendar.YEAR), cal.getTime()));
                    years.add(new MyYear(lastYear + "", lastYear, 1));
                    for (int d = 1; d < project.duration; d++) {
                        cal.add(Calendar.DATE, 1);
                        if (cal.get(Calendar.MONTH) != lastMonth) {
                            lastMonth = cal.get(Calendar.MONTH);
                            months.add(new MyMonth(Functions.monthName[lastMonth], lastMonth, cal.get(Calendar.YEAR), cal.getTime()));
                            if (cal.get(Calendar.YEAR) == lastYear) years.get(years.size() - 1).merge++;
                            else {
                                lastYear = cal.get(Calendar.YEAR);
                                years.add(new MyYear(lastYear + "", lastYear, 1));
                            }
                        }
                    }
                }
            }
            render(pid, projects, projectObjects, project, stype, days, months, weeks, years, navbar);
        } else render(pid, projects, navbar);
    }

    public static void eventv() {
        render();
    }
}
