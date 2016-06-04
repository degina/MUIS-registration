package controllers;

import models.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Personal on 5/3/2015.
 */
public class InventoryWastes extends CRUD {

    public static void viewAll(String message) {
        list(1, "", "", "", "", "", "", message);
    }

    public static void list(int CurrentPageNumber, String filterStartDate, String filterEndDate, String keyword, String inventory_filter, String location_filter, String waster_filter, String message) {
        List<Inventory> Inventorys = Inventory.findAll();
        List<InventoryLocation> Locations = new ArrayList<InventoryLocation>();
        if (Users.getUser().getUserPermissionType(Consts.permissionInventoryOther) == 4 || Users.getUser().getUserPermissionType(Consts.permissionInventoryOther) == 5) {
            List<InventoryLocation> locationList = InventoryLocation.find("is_main='true'").fetch();

            for (int i = 0; i < locationList.size(); i++) {
                if (InventoryLocations.isWorker(locationList.get(i).id, Users.getUser().id)) {
                    Locations.add(locationList.get(i));
                }
            }
        }
        List<OrgPermissionRelation> relations = OrgPermissionRelation.find("permissionType.permission.alias=?1 AND (permissionType.value=?2 OR permissionType.value=?3) AND organizationChart.user.id=?4", Consts.permissionInventory, 4, 5, Users.getUser().id).fetch();
        if (relations != null) {
            for (int i = 0; i < relations.size(); i++) {
                List<InventoryLocation> relationLocations = InventoryLocation.find("project.id=?1", relations.get(i).organizationChart.project.id).fetch();
                for (int j = 0; j < relationLocations.size(); j++) {
                    if (InventoryLocations.isWorker(relationLocations.get(j).id, Users.getUser().id) && !Locations.contains(relationLocations.get(j))) {
                        Locations.add(relationLocations.get(j));
                    }
                }
            }
        }
        List<InventoryTransfer> Transfers = InventoryTransfer.findAll();
        List<InventoryOrder> Orders = InventoryOrder.findAll();
        List<InventoryRelationSupplier> Deposits = InventoryRelationSupplier.findAll();
        List<User> Waters = User.findAll();
        User user = Users.getUser();
        String query = "id>0 ";

        if (filterStartDate != null && filterEndDate != null && !filterStartDate.equals("") && !filterEndDate.equals("")) {
            Date StartDate = new Date(filterStartDate);
            Date EndDate = new Date(filterEndDate);
            query = query + " AND " + "date >= '" + Consts.myDateFormat.format(StartDate) +
                    "' AND date <= '" + Consts.myDateFormat.format(EndDate) + "' ";
        }
        if (!location_filter.equals("0") && !location_filter.equals("")) {
            query = query + "AND location.id=" + location_filter + " ";

        }
        if (!inventory_filter.equalsIgnoreCase("0") && !inventory_filter.equalsIgnoreCase("")) {
            query = query + "AND item.id=" + inventory_filter + " ";
        }
        if (!waster_filter.equalsIgnoreCase("0") && !waster_filter.equalsIgnoreCase("")) {
            query = query + "AND waster.id=" + waster_filter + " ";
        }

        if (!keyword.trim().equalsIgnoreCase("")) {
            query += "and item.item like '%" + keyword + "%' OR id like '%" + keyword + "%' ";
        } else
            keyword = "";

        int pageLimit = 40;
        List<InventoryWaste> Wastes = InventoryWaste.find(query + "order by id desc").fetch(CurrentPageNumber, pageLimit);
        List<InventoryWaste> MaxSizer = InventoryWaste.find(" order by id desc").fetch();
        int MaxPageNumber = Wastes.size() / pageLimit;
        if (MaxSizer.size() % pageLimit != 0) MaxPageNumber++;


        render(Transfers, user, Waters, Wastes, Transfers, Locations, Inventorys, Orders, Deposits, CurrentPageNumber, MaxPageNumber, filterStartDate, filterEndDate, inventory_filter, location_filter, waster_filter, keyword);
    }

    public static void addWaste(String item, String location, String quantity, String note, List<String> filename, List<String> filedir, List<String> extension, List<String> filesize) {
        InventoryWaste waste = new InventoryWaste();
        Inventory inventory = Inventory.findById(Long.parseLong(item));
        InventoryLocation loc = InventoryLocation.findById(Long.parseLong(location));
        InventoryRelation relation = InventoryRelation.find("location.id=?1 AND inventory.id=?2", loc.id, inventory.id).first();

        if (inventory != null && relation != null && (Users.getUser().getPermissionType(controllers.Consts.permissionInventory) == 4 || Users.getUser().getPermissionType(controllers.Consts.permissionInventory) == 5)) {
            waste.item = inventory;
            waste.quantity = Double.parseDouble(quantity);
            waste.note = note;
            waste.location = loc;
            waste.waster = Users.getUser();
            waste.date = new Date();
            waste.create();

            relation.quantity = relation.quantity - Double.parseDouble(quantity);
            relation.save();

            if (filename != null) {
                for (int i = 0; i < filename.size(); i++) {
                    InventoryWasteAttachment newAttachment = new InventoryWasteAttachment();
                    newAttachment.filename = filename.get(i);
                    newAttachment.filedir = filedir.get(i);
                    newAttachment.extension = extension.get(i);
                    newAttachment.filesize = Float.parseFloat(filesize.get(i));
                    newAttachment.waste = waste;
                    newAttachment.create();
                }
            }
        }
        viewAll("");
    }

    public static void previewWaste(Long id) {
        InventoryWaste waste = InventoryWaste.findById(id);
        render(waste);
    }


    public static void getMeasure(String inv) {
        Inventory inventory = Inventory.findById(Long.parseLong(inv));
        renderText(inventory.inventoryMeasure.measure);
    }

    public static void getWaste(Long id) {

        InventoryWaste waste = InventoryWaste.findById(id);
        List<String> strings = new ArrayList<String>();

        if (waste != null) {
            strings.add(String.valueOf(waste.quantity));
            strings.add(String.valueOf(waste.note));
            for (int i = 0; i < waste.attachments.size(); i++) {
                strings.add(waste.attachments.get(i).filename);
                strings.add(waste.attachments.get(i).filedir);
                strings.add(waste.attachments.get(i).extension);
            }
        }

        renderJSON(strings);
    }

    public static void checkQuantity(Long location_id, Long inventory_id, String quantity) {
        InventoryRelation relation = InventoryRelation.find("location_id=?1 AND inventory_id=?2", location_id, inventory_id).first();
        if (relation == null) {
            renderText("false");
        } else if (relation.quantity < Double.parseDouble(quantity)) {
            renderText("false");
        } else {
            renderText("true");
        }
    }

}
