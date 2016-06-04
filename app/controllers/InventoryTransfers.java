package controllers;

import models.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by Personal on 3/15/2015.
 */
public class InventoryTransfers extends CRUD {
    public static void viewAll(String message) {
        transfer(1, "", "", "", "0", "0", "0", "0", message);
    }

    public static void transfer(int CurrentPageNumber, String filterStartDate, String filterEndDate, String keyword, String from_filter, String to_filter, String return_filter, String status, String message) {
        List<Inventory> Inventorys = Inventory.findAll();
        List<InventoryLocation> Locations = InventoryLocation.findAll();
        List<User> Recievers = User.findAll();
        User user = Users.getUser();

        String query = "id>0 ";

        if (filterStartDate != null && filterEndDate != null && !filterStartDate.equals("") && !filterEndDate.equals("")) {
            Date StartDate = new Date(filterStartDate);
            Date EndDate = new Date(filterEndDate);
            query = query + " AND " + "((transfer.date >= '" + Consts.myDateFormat.format(StartDate) +
                    "' AND transfer.date <= '" + Consts.myDateFormat.format(EndDate) + "') OR " + "(transfer.recieved_date >= '" + Consts.myDateFormat.format(StartDate) +
                    "' AND transfer.recieved_date <= '" + Consts.myDateFormat.format(EndDate) + "')" + ")";
        }
        if (status != null && !status.equals("0") && !status.equals("")) {
            query = query + "AND transfer.status.id=" + status + " ";
        }
        if (!from_filter.equals("0") && !from_filter.equals("")) {
            query = query + "AND transfer.from.id=" + from_filter + " ";
        }
        if (!to_filter.equals("0") && !to_filter.equals("")) {
            query = query + "AND transfer.to.id=" + to_filter + " ";
        }
        if (!return_filter.trim().equals("")) {
            if (return_filter.equals("1"))
                query = query + "AND transfer.isReturn = 0 ";
            if (return_filter.equals("2"))
                query = query + "AND transfer.isReturn = 1 ";
        }


        if (!keyword.trim().equals("")) {
            query += "and inventory.item like '%" + keyword + "%' ";
        } else
            keyword = "";


        int pageLimit = 40;
        List<InventoryTransfer> MaxSizer = InventoryTransfer.find("order by id desc").fetch();
        int MaxPageNumber = MaxSizer.size() / pageLimit;
        if (MaxSizer.size() % pageLimit != 0) MaxPageNumber++;

        List<InventoryTransferItem> Items = InventoryTransferItem.find(query + "order by transfer.id desc").fetch(CurrentPageNumber, pageLimit);

        int transfered = 0, recieved = 0, all = 0, cancelled = 0;
        transfered = InventoryTransfer.find("status.id = ?1", new Long(1)).fetch().size();
        recieved = InventoryTransfer.find("status.id = ?1", new Long(2)).fetch().size();
        cancelled = InventoryTransfer.find("status.id = ?1", new Long(3)).fetch().size();
        all = MaxSizer.size();

        List<InventoryTransfer> Transfers = new ArrayList<InventoryTransfer>();

        for (int i = 0; i < Items.size(); i++)
            if (!Transfers.contains(Items.get(i).transfer))
                Transfers.add(Items.get(i).transfer);


        render(Transfers, user, Recievers, Inventorys, Locations, CurrentPageNumber, MaxPageNumber, filterStartDate, filterEndDate, all, transfered, recieved, return_filter, cancelled, from_filter, message, to_filter, status, keyword);
    }


    public static void transferItem(String from, String to, String reciever, String[] item, String[] quantity, String[] note, String[] attach_number, String[] filename, String[] filedir, String[] extension, String[] filesize, boolean isReturn) {
        InventoryTransfer transfer = new InventoryTransfer();
        InventoryLocation f = InventoryLocation.find("name=?1", from).first();
        InventoryLocation t = InventoryLocation.find("name=?1", to).first();
        transfer.from = f;
        transfer.to = t;
        transfer.transferer = Users.getUser();
        transfer.reciever = User.findById(Long.parseLong(reciever));
        transfer.date = new Date();
        transfer.status = InventoryTransferStatus.findById(new Long(1));
        if (isReturn)
            transfer.isReturn = true;
        else
            transfer.isReturn = false;
        transfer.create();
        int attach_index = 0;
        for (int i = 0; i < item.length; i++) {
            InventoryTransferItem transferItem = new InventoryTransferItem();
            Inventory inventory = Inventory.find("item=?1", item[i]).first();
            transferItem.inventory = inventory;
            transferItem.transfer = transfer;
            transferItem.quantity = Double.parseDouble(quantity[i]);
            transferItem.note = note[i];
            transferItem.create();
            for (int j = attach_index; j < attach_index + Integer.parseInt(attach_number[i]); j++) {
                InventoryTransferItemAttachment attachment = new InventoryTransferItemAttachment();
                attachment.filename = filename[j];
                attachment.filedir = filedir[j];
                attachment.extension = extension[j];
                attachment.filesize = Float.parseFloat(filesize[j]);
                attachment.transferItem = transferItem;
                attachment.create();
            }
            attach_index = attach_index + Integer.parseInt(attach_number[i]);

            InventoryRelation relation = InventoryRelation.find("location_id = ?1 AND inventory_id=?2", f.id, inventory.id).first();
            relation.quantity = relation.quantity - Long.parseLong(quantity[i]);
            relation.save();
        }

        List<User> users = new ArrayList<User>();
        if (transfer.reciever != Users.getUser())
            users.add(transfer.reciever);
        UserLiveRoom.get().notification(Users.getUser(), "InventoryTransfer", users, transfer.id);

        viewAll("");
    }

    public static void editTransfer(Long id) {
        InventoryTransfer transfer = InventoryTransfer.findById(id);
        List<NotificationMessage> orderNot = NotificationMessage.find("transfer.id = " + transfer.id + " and acceptor.id = " + Users.getUser().id + " and seen <> true").fetch();
        for (NotificationMessage notf : orderNot)
            if (notf != null && !notf.seen) {
                notf.seen = true;
                notf._save();
            }
        render(transfer);
    }


    public static void recieveTransfer(Long id) {

        InventoryTransfer transfer = InventoryTransfer.findById(id);
        if (transfer == null) {
            renderText("failed");
        } else {
            if (transfer.status.id == 1L) {
                InventoryTransferStatus status = InventoryTransferStatus.findById(new Long(2));

                List<InventoryTransferItem> item = transfer.inventorys;

                for (int i = 0; i < item.size(); i++) {


                    InventoryRelation relation = InventoryRelation.find("location_id = ?1 AND inventory_id=?2", transfer.to.id, item.get(i).inventory.id).first();
                    if (relation == null) {
                        relation = new InventoryRelation();
                        relation.location = transfer.to;
                        relation.inventory = item.get(i).inventory;
                        relation.quantity = new Double(0);
                        relation.create();
                    }
                    relation.quantity = relation.quantity + item.get(i).quantity;
                    relation.save();


                }

                transfer.status = status;
                transfer.recieved_date = new Date();
                transfer.save();

                List<User> users = new ArrayList<User>();
                if (transfer.transferer != Users.getUser())
                    users.add(transfer.transferer);
                UserLiveRoom.get().notification(Users.getUser(), "InventoryTransfer", users, transfer.id);
                renderText("success");
            } else
                renderText("failed");
        }


    }

    public static void cancelTransfer(Long id) {
        InventoryTransfer transfer = InventoryTransfer.findById(id);
        InventoryTransferStatus status = InventoryTransferStatus.findById(new Long(3));
        if (transfer.status.id == 1L) {
            List<InventoryTransferItem> item = transfer.inventorys;

            for (int i = 0; i < item.size(); i++) {
                InventoryRelation relation = InventoryRelation.find("location_id = ?1 AND inventory_id=?2", transfer.from.id, item.get(i).inventory.id).first();
                relation.quantity = relation.quantity + item.get(i).quantity;
                relation.save();
            }
            transfer.canceller = Users.getUser();
            transfer.recieved_date = new Date();
            transfer.status = status;
            transfer.save();

            List<User> users = new ArrayList<User>();
            if (transfer.reciever != Users.getUser())
                users.add(transfer.reciever);
            if (transfer.transferer != Users.getUser())
                users.add(transfer.transferer);
            UserLiveRoom.get().notification(Users.getUser(), "InventoryTransfer", users, transfer.id);
            renderText("success");
        } else
            renderText("failed");
    }

    public static void getAdmins(String loc) {
        List<String> admins = new ArrayList<String>();
        InventoryLocation location = InventoryLocation.find("name=?1", loc).first();

        if (location.project != null) {
            List<OrgPermissionRelation> relation = OrgPermissionRelation.find("permissionType.permission.alias=?1 AND organizationChart.project.id=?2 AND (permissionType.value=?3 OR permissionType.value=?4)", Consts.permissionInventory, location.project.id, 4, 5).fetch();
            for (int i = 0; i < relation.size(); i++) {
                User user = User.findById(relation.get(i).organizationChart.user.id);
                if (!admins.contains(String.valueOf(user.id)) && InventoryLocations.isWorker(location.id, user.id)) {
                    admins.add(String.valueOf(user.id));
                    admins.add(user.getLastnameFirstCharacter());
                    admins.add(user.firstName);
                }
            }
        } else {
            List<UserPermissionRelation> permissionRelation = UserPermissionRelation.find("permissionType.permission.alias=?1 AND (permissionType.value=?2 OR permissionType.value=?3)", Consts.permissionInventoryOther, 4, 5).fetch();
            for (int i = 0; i < permissionRelation.size(); i++) {
                User user = User.findById(permissionRelation.get(i).user.id);
                if (!admins.contains(String.valueOf(user.id)) && InventoryLocations.isWorker(location.id, user.id)) {
                    admins.add(String.valueOf(user.id));
                    admins.add(user.getLastnameFirstCharacter());
                    admins.add(user.firstName);
                }
            }
        }


        renderJSON(admins);
    }

    public static void getMyLocations() {
        List<String> locations = new ArrayList<String>();
        if (Users.getUser().getUserPermissionType(Consts.permissionInventoryOther) == 4 || Users.getUser().getUserPermissionType(Consts.permissionInventoryOther) == 5) {
            List<InventoryLocation> locationList = InventoryLocation.find("is_main='true'").fetch();

            for (int i = 0; i < locationList.size(); i++) {
                if (InventoryLocations.isWorker(locationList.get(i).id, Users.getUser().id)) {
                    locations.add(String.valueOf(locationList.get(i).id));
                    locations.add(locationList.get(i).name);
                }
            }
        }

        List<OrgPermissionRelation> relations = OrgPermissionRelation.find("permissionType.permission.alias=?1 AND (permissionType.value=?2 OR permissionType.value=?3) AND organizationChart.user.id=?4", Consts.permissionInventory, 4, 5, Users.getUser().id).fetch();

        if (relations != null) {
            for (int i = 0; i < relations.size(); i++) {
                List<InventoryLocation> relationLocations = InventoryLocation.find("project.id=?1", relations.get(i).organizationChart.project.id).fetch();
                for (int j = 0; j < relationLocations.size(); j++) {
                    if (InventoryLocations.isWorker(relationLocations.get(j).id, Users.getUser().id)) {
                        locations.remove(String.valueOf(relationLocations.get(j).id));
                        locations.remove(relationLocations.get(j).name);
                        locations.add(String.valueOf(relationLocations.get(j).id));
                        locations.add(relationLocations.get(j).name);
                    }
                }
            }
        }

        renderJSON(locations);

    }
}
