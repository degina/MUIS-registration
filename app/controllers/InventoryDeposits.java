package controllers;

import models.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Personal on 3/4/2015.
 */
public class InventoryDeposits extends CRUD {


    public static void viewAll(String message) {
        list(1, "", "", "", "", "", "", message);
    }

    public static void list(int CurrentPageNumber, String filterStartDate, String filterEndDate, String receiver_filter, String supplier_filter, String location_filter, String keyword, String message) {
        List<Inventory> Inventorys = Inventory.findAll();
        List<InventorySupplier> Suppliers = InventorySupplier.findAll();

        UserPermission permission = UserPermission.find("alias='inventory_other'").first();

        List<UserPermissionRelation> userPermissions = UserPermissionRelation.find("permission_id = ?1 and permissionType.value=?2", permission, 3).fetch();
        List<User> Receiver = new ArrayList<User>();
        for (int i = 0; i < userPermissions.size(); i++)
            if (!Receiver.contains(userPermissions.get(i).user))
                Receiver.add(userPermissions.get(i).user);


        List<InventoryCategory> Categories = InventoryCategory.findAll();
        List<InventoryLocation> Locations = InventoryLocation.findAll();
        List<InventoryMeasure> Measures = InventoryMeasure.findAll();
        List<InventorySubCategory> SubCategories = new ArrayList<InventorySubCategory>();
        List<InventoryLocation> Mains = new ArrayList<InventoryLocation>();
        if (Users.getUser().getUserPermissionType(Consts.permissionInventoryOther) == 4 || Users.getUser().getUserPermissionType(Consts.permissionInventoryOther) == 5) {
            List<InventoryLocation> locationList = InventoryLocation.find("is_main='true'").fetch();

            for (int i = 0; i < locationList.size(); i++) {
                if (InventoryLocations.isWorker(locationList.get(i).id, Users.getUser().id)) {
                    Mains.add(locationList.get(i));
                }
            }
        }


        String query = "id>0 ";

        if (filterStartDate != null && filterEndDate != null && !filterStartDate.equals("0") && !filterEndDate.equals("0") && !filterStartDate.equals("") && !filterEndDate.equals("")) {
            Date StartDate = new Date(filterStartDate);
            Date EndDate = new Date(filterEndDate);
            query = query + " AND " + "date >= '" + Consts.myDateFormat.format(StartDate) +
                    "' AND date <= '" + Consts.myDateFormat.format(EndDate) + "'";
        }
        if (!receiver_filter.equals("0") && !receiver_filter.equals("")) {
            query = query + "AND receiver.id=" + receiver_filter + " ";
        }
        if (!supplier_filter.equals("0") && !supplier_filter.equals("")) {
            query = query + "AND supplier.id=" + supplier_filter + " ";
        }
        if (!location_filter.equals("0") && !location_filter.equals("")) {
            query = query + "AND location.id=" + location_filter + " ";
        }

        if (!keyword.trim().equals("")) {
            query += "and inventory.item like '%" + keyword + "%' OR id like '%" + keyword + "%' ";
        } else
            keyword = "";

        int pageLimit = 40;
        List<InventoryRelationSupplier> MaxSizer = InventoryRelationSupplier.find(query + "order by id desc").fetch();
        int MaxPageNumber = MaxSizer.size() / pageLimit;
        if (MaxSizer.size() % pageLimit != 0) MaxPageNumber++;
        List<InventoryRelationSupplier> Deposits = InventoryRelationSupplier.find(query + "order by id desc").fetch(CurrentPageNumber, pageLimit);


        render(Categories, Locations, Measures, SubCategories, Deposits, Receiver, Inventorys, Mains, Suppliers, MaxPageNumber, CurrentPageNumber, filterStartDate, filterEndDate, receiver_filter, supplier_filter, location_filter, keyword, message);
    }

    public static void newDeposit() {

        List<Inventory> Inventorys = Inventory.findAll();
        List<InventorySupplier> Suppliers = InventorySupplier.findAll();
        render(Inventorys, Suppliers);

    }


    public static void previewDeposit(Long id) {
        InventoryRelationSupplier relationSupplier = InventoryRelationSupplier.findById(id);

        List<String> strings = new ArrayList<String>();
        if (relationSupplier != null) {
            strings.add(relationSupplier.inventory.item);
            strings.add(relationSupplier.inventory.inventorySubCategory.category.name + " / " + relationSupplier.inventory.inventorySubCategory.name);
            strings.add(relationSupplier.inventory.description);
            strings.add(relationSupplier.inventory.itemNote);
            strings.add(relationSupplier.inventory.website);

            strings.add(relationSupplier.supplier.name);
            strings.add(relationSupplier.supplier.address);
            strings.add(relationSupplier.supplier.person);
            strings.add(String.valueOf(relationSupplier.supplier.phone));
            strings.add(String.valueOf(relationSupplier.supplier.phone_alternative));
            strings.add(relationSupplier.supplier.email);
            strings.add(relationSupplier.supplier.website);

            strings.add(String.valueOf(relationSupplier.quantity));
            strings.add(String.valueOf(relationSupplier.price));

            String month;
            String day;
            if (1 + relationSupplier.date.getMonth() < 10) {
                month = "0" + String.valueOf(1 + relationSupplier.date.getMonth());
            } else {
                month = String.valueOf(1 + relationSupplier.date.getMonth());
            }
            if (relationSupplier.date.getDate() < 10) {
                day = "0" + String.valueOf(relationSupplier.date.getDate());
            } else {
                day = String.valueOf(relationSupplier.date.getDate());
            }
            strings.add(String.valueOf(1900 + relationSupplier.date.getYear()) + "-" + month + "-" + day + " " + relationSupplier.date.getHours() + ":" + relationSupplier.date.getMinutes());
            strings.add(relationSupplier.supplier_partnumber);
            strings.add(relationSupplier.receiver.toString());
            strings.add(relationSupplier.location.name);
        }
        if (strings != null)
            renderJSON(strings);
    }

    public static void saveDeposit(String[] item, String[] supplier, String[] quantity, String[] price, String[] partnumber, String location) {

        if (Users.getUser().getUserPermissionType(Consts.permissionInventoryOther) == 4 || Users.getUser().getUserPermissionType(Consts.permissionInventoryOther) == 5) {

            for (int i = 0; i < item.length; i++) {
                Inventory inventory = Inventory.find("item ='" + item[i] + "'").first();
                InventorySupplier supp = InventorySupplier.findById(Long.parseLong(supplier[i]));
                InventoryLocation loc = InventoryLocation.findById(Long.parseLong(location));
                InventoryRelationSupplier relationSupplier = new InventoryRelationSupplier();
                relationSupplier.inventory = inventory;

                relationSupplier.supplier = supp;

                relationSupplier.receiver = Users.getUser();

                relationSupplier.supplier_partnumber = partnumber[i];
                relationSupplier.quantity = Double.parseDouble(quantity[i]);
                relationSupplier.price = Long.parseLong(price[i]);
                relationSupplier.date = new Date();
                relationSupplier.location = loc;
                relationSupplier.create();


                InventoryRelation is = InventoryRelation.find("inventory_id=?1 and location.id=?2", inventory.id, loc.id).first();
                if (is == null) {
                    InventoryRelation inventoryRelation = new InventoryRelation();
                    inventoryRelation.inventory = inventory;
                    inventoryRelation.location = loc;
                    inventoryRelation.quantity = Double.parseDouble(quantity[i]);
                    inventoryRelation.create();
                } else {
                    is.quantity = is.quantity + Double.parseDouble(quantity[i]);
                    is.save();
                }
            }

            viewAll("addsuccess");
        } else
            viewAll("");
    }

    public static Long StoSplitLong(String str) {
        if (str != null && !str.equals(""))
            return Long.parseLong(str.replaceAll("'", ""));
        else
            return null;
    }

    public static void supplierPrice(String sup, String inv) {
        List<String> strings = new ArrayList<String>();
        InventorySupplier supplier = InventorySupplier.find("id=?1", Long.parseLong(sup)).first();
        Inventory inventory = Inventory.find("item=?1", inv).first();
        if (inventory != null && supplier != null) {
            InventoryRelationPrice relationPrice = InventoryRelationPrice.find("inventory.id=?1 AND supplier.id=?2", inventory.id, supplier.id).first();
            strings.add(String.valueOf(relationPrice.price));
            strings.add(relationPrice.supplier_partnumber);
        }

        renderJSON(strings);
    }

    public static void printDeposit(int CurrentPageNumber, String filterStartDate, String filterEndDate, String receiver_filter, String supplier_filter, String location_filter, String keyword) {
        String query = "id>0 ";

        if (filterStartDate != null && filterEndDate != null && !filterStartDate.equals("0") && !filterEndDate.equals("0") && !filterStartDate.equals("") && !filterEndDate.equals("")) {
            Date StartDate = new Date(filterStartDate);
            Date EndDate = new Date(filterEndDate);
            query = query + " AND " + "date >= '" + Consts.myDateFormat.format(StartDate) +
                    "' AND date <= '" + Consts.myDateFormat.format(EndDate) + "'";
        }
        if (!receiver_filter.equals("0") && !receiver_filter.equals("")) {
            query = query + "AND receiver.id=" + receiver_filter + " ";
        }
        if (!supplier_filter.equals("0") && !supplier_filter.equals("")) {
            query = query + "AND supplier.id=" + supplier_filter + " ";
        }
        if (!location_filter.equals("0") && !location_filter.equals("")) {
            query = query + "AND location.id=" + location_filter + " ";
        }

        if (!keyword.trim().equals("")) {
            query += "and inventory.item like '%" + keyword + "%' OR id like '%" + keyword + "%' ";
        } else
            keyword = "";

        int pageLimit = 40;
        List<InventoryRelationSupplier> MaxSizer = InventoryRelationSupplier.find(query + "order by id desc").fetch();
        int MaxPageNumber = MaxSizer.size() / pageLimit;
        if (MaxSizer.size() % pageLimit != 0) MaxPageNumber++;
        List<InventoryRelationSupplier> Deposits = InventoryRelationSupplier.find(query + "order by id desc").fetch(CurrentPageNumber, pageLimit);

        Date today = new Date();

        render(Deposits,today);
    }

}
