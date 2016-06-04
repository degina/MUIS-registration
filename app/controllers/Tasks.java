package controllers;

import models.ProjectObject;
import models.Task;
import models.User;
import play.data.binding.Binder;
import play.db.Model;
import play.exceptions.TemplateNotFoundException;
import play.i18n.Messages;
import play.mvc.With;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by enkhamgalan on 12/21/14.
 */
@With(Secure.class)
public class Tasks extends CRUD {
    public static void list(Long oid, Long tid, int page, String search, String searchFields, String orderBy, String order) {
        ObjectType type = ObjectType.get(getControllerClass());
        notFoundIfNull(type);
        if (page < 1) {
            page = 1;
        }
        if (orderBy != null)
            if (orderBy.equals("tasks")) orderBy = "tasks.size";
        String where = "";
        if (oid != null) where = "projectObject.id=" + oid;
        else if (tid != null) where = "task.id=" + tid;
        List<Model> objects = type.findPage(page, search, searchFields, orderBy, order, where);
        Long count = type.count(search, searchFields, where);
        Long totalCount = type.count(null, null, where);
        ProjectObject projectObject = null;
        if (oid != null) projectObject = ProjectObject.findById(oid);
        Task task = null;
        if (tid != null) task = Task.findById(tid);
        List<String[]> navigations = null;
        if (projectObject != null) {
            navigations = Functions.buildNavigation(new String[][]{
                    {"/projects", Messages.get("Project")},
                    {"/projectobjects?id=" + projectObject.project.id, projectObject.project.name},
                    {"/tasks?oid=" + oid, projectObject.name},
            });
        } else if (task != null) {
            Task tsk = task;
            while (tsk.projectObject == null) {
                tsk = tsk.task;
            }
            projectObject = tsk.projectObject;
            navigations = Functions.buildNavigation(new String[][]{
                    {"/projects", Messages.get("Project")},
                    {"/projectobjects?id=" + projectObject.project.id, projectObject.project.name},
                    {"/tasks?oid=" + projectObject.id, projectObject.name},
            });
            tsk = task;
            List<String[]> naviPalend = new ArrayList<String[]>();
            while (tsk != null) {
                String[] strings = {"/tasks?tid=" + tsk.id, tsk.name};
                naviPalend.add(strings);
                tsk = tsk.task;
            }
            for (int n = naviPalend.size() - 1; n >= 0; n--)
                navigations.add(naviPalend.get(n));
        }
        if (orderBy != null) {
            if (orderBy.equals("tasks.size")) orderBy = "tasks";
        }
        try {
            render(type, objects, count, totalCount, page, orderBy, order, navigations, oid, tid);
        } catch (TemplateNotFoundException e) {
            render("CRUD/list.html", type, objects, count, totalCount, page, orderBy, order, navigations, oid, tid);
        }
    }

    public static void blank(Long oid, Long tid) throws Exception {
        ObjectType type = ObjectType.get(getControllerClass());
        notFoundIfNull(type);
        Constructor<?> constructor = type.entityClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        Task object = (Task) constructor.newInstance();
        ProjectObject projectObject = null;
        if (oid != null) projectObject = ProjectObject.findById(oid);
        Task task = null;
        if (tid != null) task = Task.findById(tid);
        try {
            render(type, object, oid, tid, task, projectObject);
        } catch (TemplateNotFoundException e) {
            render("CRUD/blank.html", type, object, oid, tid, task, projectObject);
        }
    }

    public static void create(Long oid, Long tid) throws Exception {
        ObjectType type = ObjectType.get(getControllerClass());
        notFoundIfNull(type);
        Constructor<?> constructor = type.entityClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        Task object = (Task) constructor.newInstance();
        Binder.bindBean(params.getRootParamNode(), "object", object);
        ProjectObject projectObject = null;
        if (oid != null) projectObject = ProjectObject.findById(oid);
        Task task = null;
        if (tid != null) task = Task.findById(tid);
        object.task = task;
        object.projectObject = projectObject;
        object.owner = Users.getUser();
        validation.valid(object);
        if (validation.hasErrors()) {
            renderArgs.put("error", Messages.get("crud.hasErrors"));
            render(request.controller.replace(".", "/") + "/blank.html", task, projectObject, type, object);
        }
        object._save();
        flash.success(Messages.get("crud.created", type.modelName));
        if (params.get("_save") != null) {
            list(oid, tid, 1, null, null, null, null);
        }
        if (params.get("_saveAndAddAnother") != null) {
            blank(oid, tid);
        }
        redirect(request.controller + ".show", object._key());
    }

    public static void save(String id) throws Exception {
        ObjectType type = ObjectType.get(getControllerClass());
        notFoundIfNull(type);
        Task object = Task.findById(Long.parseLong(id));
        ProjectObject projectObject = object.projectObject;
        Task task = object.task;
        User owner = object.owner;
        notFoundIfNull(object);
        Binder.bindBean(params.getRootParamNode(), "object", object);
        object.task = task;
        object.projectObject = projectObject;
        object.owner = owner;
        validation.valid(object);
        if (validation.hasErrors()) {
            renderArgs.put("error", Messages.get("crud.hasErrors"));
            try {
                render(request.controller.replace(".", "/") + "/show.html", type, object);
            } catch (TemplateNotFoundException e) {
                render("CRUD/show.html", type, object);
            }
        }
        object._save();
        flash.success(Messages.get("crud.saved", type.modelName));
        if (params.get("_save") != null) {
            list(projectObject.id, task.id, 1, null, null, null, null);
        }
        redirect(request.controller + ".show", object._key());
    }

}
