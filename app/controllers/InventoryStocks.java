package controllers;

import models.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Personal on 4/9/2015.
 */
public class InventoryStocks extends CRUD {
    public static void viewAll(String message) {
        list(1, "", "0", "0", "", "", message);
    }

    public static void list(int CurrentPageNumber, String keyword, String inventory_filter, String location_filter, String category_filter, String subcategory_filter, String message) {
        User user = Users.getUser();
        UserTeam userTeam = Users.getUser().userTeam;

        String query = "id>0 ";
        Long project = Users.pid();

        List<Inventory> items = Inventory.findAll();
        List<InventoryLocation> Locations = InventoryLocation.find("project.id=?1 OR is_main='true'", project).fetch();
        List<InventoryCategory> Categories = InventoryCategory.findAll();
        List<InventorySupplier> Suppliers = InventorySupplier.findAll();
        List<InventoryMeasure> Measures = InventoryMeasure.findAll();
        List<InventorySubCategory> SubCategories = new ArrayList<InventorySubCategory>();

        if (!inventory_filter.equalsIgnoreCase("0") && !inventory_filter.equalsIgnoreCase("")) {
            query = query + "AND id=" + inventory_filter + " ";
        }
        if (!category_filter.equalsIgnoreCase("0") && !category_filter.equalsIgnoreCase("")) {
            query = query + "AND inventorySubCategory.category.id=" + category_filter + " ";
            SubCategories = InventorySubCategory.find("category_id=?1", Integer.parseInt(category_filter)).fetch();

        }
        if (!subcategory_filter.equalsIgnoreCase("0") && !subcategory_filter.equalsIgnoreCase("")) {
            query = query + "AND inventorySubCategory.id=" + subcategory_filter + " ";
        }

        if (!keyword.trim().equalsIgnoreCase("")) {
            query += "and item like '%" + keyword + "%' OR id like '%" + keyword + "%' ";
        } else
            keyword = "";


        int pageLimit = 40;
        List<Inventory> MaxSizer = Inventory.find(query + "order by id desc").fetch();
        for (int i = 0; i < MaxSizer.size(); i++) {
            if (location_filter.equalsIgnoreCase("0")) {
                if (InventoryRelation.find("inventory.id=" + MaxSizer.get(i).id + " AND location.project.id=" + Users.pid() + " ").first() == null) {
                    MaxSizer.remove(MaxSizer.get(i));
                    i = i - 1;
                }
            } else if (location_filter != null && !location_filter.equalsIgnoreCase("")) {
                if (InventoryRelation.find("inventory.id=" + MaxSizer.get(i).id + " AND location.id=" + location_filter + " ").first() == null) {
                    MaxSizer.remove(MaxSizer.get(i));
                    i = i - 1;
                }
            }
        }

        int MaxPageNumber = MaxSizer.size() / pageLimit;
        if (MaxSizer.size() % pageLimit != 0) MaxPageNumber++;
        List<Inventory> Inventorys = null;
        if (CurrentPageNumber * 40 <= MaxSizer.size())
            Inventorys = MaxSizer.subList((CurrentPageNumber - 1) * 40, CurrentPageNumber * 40);
        else
            Inventorys = MaxSizer.subList((CurrentPageNumber - 1) * 40, MaxSizer.size());

        render(items, Inventorys, Categories, SubCategories, Locations, Suppliers, Measures, CurrentPageNumber, MaxPageNumber, message,inventory_filter, location_filter, category_filter, subcategory_filter, keyword);
    }

}
