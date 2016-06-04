package controllers;

import models.InventoryCategory;
import models.InventorySubCategory;
import play.data.binding.Binder;
import play.db.Model;
import play.exceptions.TemplateNotFoundException;
import play.i18n.Messages;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * Created by Personal on 2015-01-12.
 */
public class InventorySubCategorys extends CRUD {
    public static void list(Long id, int page, String search, String searchFields, String orderBy, String order) {
        ObjectType type = ObjectType.get(getControllerClass());
        notFoundIfNull(type);
        if (page < 1) {
            page = 1;
        }
        if (orderBy != null) {
            if (orderBy.equals("tasks")) orderBy = "tasks.size";
        }
        Long functionArguments = id;
        String where = "category.id=" + id;
        List<Model> objects = type.findPage(page, search, searchFields, orderBy, order, where);
        Long count = type.count(search, searchFields, where);
        Long totalCount = type.count(null, null, where);
        InventoryCategory inventoryCategory = InventoryCategory.findById(id);
        try {
            render(type, objects, count, totalCount, page, orderBy, order, functionArguments, inventoryCategory);
        } catch (TemplateNotFoundException e) {
            render("CRUD/list.html", type, objects, count, totalCount, page, orderBy, order, functionArguments, inventoryCategory);
        }
    }

    public static void blank(Long iid) throws Exception {
        ObjectType type = ObjectType.get(getControllerClass());
        notFoundIfNull(type);
        Constructor<?> constructor = type.entityClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        InventorySubCategory object = (InventorySubCategory) constructor.newInstance();
        InventoryCategory category = InventoryCategory.findById(iid);
        try {
            render(type, object, iid, category);
        } catch (TemplateNotFoundException e) {
            render("CRUD/blank.html", type, object, category);
        }
    }

    public static void create(Long iid) throws Exception {
        ObjectType type = ObjectType.get(getControllerClass());
        notFoundIfNull(type);
        Constructor<?> constructor = type.entityClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        InventorySubCategory object = (InventorySubCategory) constructor.newInstance();
        Binder.bindBean(params.getRootParamNode(), "object", object);
        InventoryCategory category = InventoryCategory.findById(iid);
        object.category = category;
        validation.valid(object);
        if (validation.hasErrors()) {
            renderArgs.put("error", Messages.get("crud.hasErrors"));
            render(request.controller.replace(".", "/") + "/blank.html", category, type, object);
        }
        object._save();
        flash.success(Messages.get("crud.created", type.modelName));
        if (params.get("_save") != null) {
            list(iid, 1, null, null, null, null);
        }
        if (params.get("_saveAndAddAnother") != null) {
            blank(iid);
        }
        redirect(request.controller + ".show", object._key());
    }

}
