package controllers;

import models.Inventory;
import models.InventoryCategory;
import models.InventorySubCategory;
import models.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Personal on 2015-01-12.
 */
public class InventoryCategorys extends CRUD {
    public static void viewAll(String message) {
        list(1, new Long(0), "", message);
    }

    public static void list(int CurrentPageNumber, Long status, String keyword, String message) {
        String query;
        if (keyword == null || keyword.length() == 0) {
            query = "order by id";
        } else {
            if (status == 0) {
                query = "id like '%" + keyword + "%'";
            } else {
                query = "name like'%" + keyword + "%'";
            }
        }

        Long is = status;

        int pageLimit = 40;
        List<InventoryCategory> MaxSizer = InventoryCategory.find("order by id desc").fetch();
        int MaxPageNumber = MaxSizer.size() / pageLimit;
        if (MaxSizer.size() % pageLimit != 0) MaxPageNumber++;
        List<InventoryCategory> Categorys = InventoryCategory.find("order by id desc").fetch(CurrentPageNumber, pageLimit);

        List<InventoryCategory> Categories = InventoryCategory.findAll();

        render(Categorys, Categories, is, message, CurrentPageNumber, MaxPageNumber);

    }

    public static void deleteCategory(Long id) {
        InventoryCategory Categorys = InventoryCategory.findById(id);
        if (Categorys != null) {
            Inventory inventory = Inventory.find("inventorySubCategory.category.id=" + id + " ").first();
            if (inventory == null) {
                Categorys._delete();
                renderText("deletesuccess");
            } else
                renderText("persistent");
        } else
            renderText("deletefailed");

    }

    public static void inventoryCheckCategory(String category) {
        InventoryCategory cat = InventoryCategory.find("name = ?1", category).first();
        if (cat == null)
            renderText("true");
        else
            renderText("false");
    }

    public static void inventoryCheckCategoryEdit(String category, String id) {
        InventoryCategory cat = InventoryCategory.find("name = ?1", category).first();
        if (cat == null)
            renderText("true");
        else if (cat.id == Integer.parseInt(id))
            renderText("true");
        else
            renderText("false");
    }

    public static void inventoryCheckSubCategory(String category, String subcategory) {
        InventoryCategory cat = InventoryCategory.find("name = ?1", category).first();
        if (cat != null) {
            InventorySubCategory subCategory = InventorySubCategory.find("category_id=?1 and name =?2", cat.id, subcategory).first();
            if (subCategory == null)
                renderText("true");
            else
                renderText("false");
        }
    }

    public static void newCategory(String selectcat, String inputcat, String[] subcategorys, String[] ids, String is) {

        if (is.equals("0")) {
            InventoryCategory category = InventoryCategory.find("name =?1", selectcat).first();
            if (category != null && subcategorys != null) {
                for (int i = 0; i < subcategorys.length; i++) {
                    if (!ids[i].equals("0")) {
                        InventorySubCategory newSubCategory = InventorySubCategory.findById(Long.parseLong(ids[i]));
                        newSubCategory.name = subcategorys[i];
                        newSubCategory.category = category;
                        newSubCategory.save();
                    } else {
                        InventorySubCategory newSubCategory = InventorySubCategory.find("name=?1 AND category_id=?2", subcategorys[i], category.id).first();
                        if (newSubCategory == null) {
                            newSubCategory = new InventorySubCategory();
                            newSubCategory.name = subcategorys[i];
                            newSubCategory.category = category;
                            newSubCategory.create();
                        }
                    }
                }
            } else {
                viewAll("newsuccess");
            }
        } else {
            InventoryCategory check = InventoryCategory.find("name = ?1", inputcat).first();
            if (check == null) {
                check = new InventoryCategory();
                check.name = inputcat;
                check.create();
                if (check != null && subcategorys != null)
                    for (int i = 0; i < subcategorys.length; i++) {
                        InventorySubCategory newSubCategory = new InventorySubCategory();
                        newSubCategory.name = subcategorys[i];
                        newSubCategory.category = check;
                        newSubCategory.create();
                    }
            } else {
                viewAll("newfailed");
            }

        }

        viewAll("newsuccess");
    }

    public static void newCategoryModal(String selectcat, String inputcat, String[] subcategorys, String[] ids, String is) {
        ArrayList<String> ID = new ArrayList<String>();
        if (is.equals("0")) {
            InventoryCategory category = InventoryCategory.find("name =?1", selectcat).first();
            if (category != null && subcategorys != null) {
                ID.add(String.valueOf(category.id));
                for (int i = 0; i < subcategorys.length; i++) {
                    if (!ids[i].equals("0")) {
                        InventorySubCategory newSubCategory = InventorySubCategory.findById(Long.parseLong(ids[i]));
                        newSubCategory.name = subcategorys[i];
                        newSubCategory.category = category;
                        newSubCategory.save();
                        ID.add(String.valueOf(newSubCategory.id));
                    } else {
                        InventorySubCategory newSubCategory = InventorySubCategory.find("name=?1 AND category_id=?2", subcategorys[i], category.id).first();
                        if (newSubCategory == null) {
                            newSubCategory = new InventorySubCategory();
                            newSubCategory.name = subcategorys[i];
                            newSubCategory.category = category;
                            newSubCategory.create();
                        }
                        ID.add(String.valueOf(newSubCategory.id));
                    }
                }
            } else {
                ID.add("false");
                renderJSON(ID);
            }
        } else {
            InventoryCategory check = InventoryCategory.find("name = ?1", inputcat).first();
            if (check == null) {
                check = new InventoryCategory();
                check.name = inputcat;
                check.create();
                ID.add(String.valueOf(check.id));
                if (check != null && subcategorys != null) {
                    for (int i = 0; i < subcategorys.length; i++) {
                        InventorySubCategory newSubCategory = new InventorySubCategory();
                        newSubCategory.name = subcategorys[i];
                        newSubCategory.category = check;
                        newSubCategory.create();
                        ID.add(String.valueOf(newSubCategory.id));
                    }
                }
            } else {
                ID.add("false");
                renderJSON(ID);
            }

        }

        renderJSON(ID);
    }

    public static void saveCategory(Long id, String inputcat, String[] subcategorys, String[] ids) {
        InventoryCategory category = InventoryCategory.findById(id);
        if (category != null) {
            category.name = inputcat;
            category.save();

            category.save();

            if (subcategorys != null) {
                int is = 0;
                for (int i = 0; i < category.subCategories.size(); i++) {
                    is = 0;
                    for (int j = 0; j < ids.length; j++) {
                        if (category.subCategories.get(i).id == Integer.parseInt(ids[j]))
                            is++;
                    }
                    if (is == 0) {
                        Inventory inventory = Inventory.find("inventorySubCategory.id=" + category.subCategories.get(i).id + " ").first();
                        if (inventory == null) {
                            category.subCategories.get(i)._delete();
                        } else
                            viewAll("subpersistent");
                    }
                }

                for (int i = 0; i < subcategorys.length; i++) {
                    if (!ids[i].equals("0")) {
                        InventorySubCategory newSubCategory = InventorySubCategory.findById(Long.parseLong(ids[i]));
                        System.out.println(newSubCategory.name);
                        newSubCategory.name = subcategorys[i];
                        newSubCategory.category = category;
                        newSubCategory.save();
                    } else {
                        InventorySubCategory newSubCategory = new InventorySubCategory();
                        newSubCategory.name = subcategorys[i];
                        newSubCategory.category = category;
                        newSubCategory.create();

                    }
                }
                viewAll("savesuccess");
            } else viewAll("savefailed");
        } else
            viewAll("savefailed");
    }


    public static void editCategory(Long id) {
        InventoryCategory category = InventoryCategory.findById(id);

        List<String> strings = new ArrayList<String>();
        strings.add(category.name);

        for (int i = 0; i < category.subCategories.size(); i++) {
            strings.add(category.subCategories.get(i).name);
            strings.add(String.valueOf(category.subCategories.get(i).id));
        }
        renderJSON(strings);

    }

}
