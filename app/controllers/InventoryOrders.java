package controllers;

import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import models.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Personal on 3/7/2015.
 */
public class InventoryOrders extends CRUD {

    public static void viewAll(String message) {
        list(1, "", "", "", "0", "0", "0", message);
    }

    public static void list(int CurrentPageNumber, String filterStartDate, String filterEndDate, String keyword, String location_filter, String orderer_filter, String status, String message) {
        List<Inventory> Inventorys = Inventory.findAll();
        List<InventoryLocation> Locations = InventoryLocation.findAll();
        List<TaskAssignRel> Plans = TaskAssignRel.find("user.id=?1 AND task.projectObject.project.id=?2 AND task.completedPercent<100", Users.getUser().id, Users.pid()).fetch();
        List<String> permissions = new ArrayList<String>();
        List<InventoryRelationWorker> inv = InventoryRelationWorker.find("worker.id=?1", Users.getUser().id).fetch();
        for (int i = 0; i < inv.size(); i++) {
            if (!permissions.contains(inv.get(i).location.name)) {
                if (inv.get(i).location.project != null) {
                    if (getProjectInventoryOrderPermissionType(inv.get(i).location.project.id) == 1 || getProjectInventoryOrderPermissionType(inv.get(i).location.project.id) == 3)
                        permissions.add(inv.get(i).location.name);
                } else {
                    if (Users.getUser().getUserPermissionType(Consts.permissionInventoryOther) == 1 || Users.getUser().getUserPermissionType(Consts.permissionInventoryOther) == 3)
                        permissions.add(inv.get(i).location.name);
                }
            }
        }
        User user = Users.getUser();

        List<User> Orderer = new ArrayList<User>();
        List<InventoryOrder> orders = InventoryOrder.findAll();
        for (int i = 0; i < orders.size(); i++)
            if (!Orderer.contains(orders.get(i).orderer))
                Orderer.add(orders.get(i).orderer);

        String query = "id>0 ";

        if (filterStartDate != null && filterEndDate != null && !filterStartDate.equals("") && !filterEndDate.equals("")) {
            Date StartDate = new Date(filterStartDate);
            Date EndDate = new Date(filterEndDate);
            EndDate = Functions.PrevNextDay(EndDate, 1);
            query = query + " AND " + "((date >= '" + Consts.myDateFormat.format(StartDate) +
                    "' AND date <= '" + Consts.myDateFormat.format(EndDate) + "') OR (approved_date >= '" + Consts.myDateFormat.format(StartDate) +
                    "' AND approved_date <= '" + Consts.myDateFormat.format(EndDate) + "') OR (issued_date >= '" + Consts.myDateFormat.format(StartDate) +
                    "' AND issued_date <= '" + Consts.myDateFormat.format(EndDate) + "') OR (due >= '" + Consts.myDateFormat.format(StartDate) +
                    "' AND due <= '" + Consts.myDateFormat.format(EndDate) + "'))";
        }

        if (status != null && !status.equals("0") && !status.equals("")) {
            query = query + " AND order.status.id=" + status + " ";
        }
        if (!location_filter.equals("0") && !location_filter.equals("")) {
            query = query + " AND order.location.id=" + location_filter + " ";

        }
        if (!orderer_filter.equals("0") && !orderer_filter.equals("")) {
            query = query + "AND order.orderer.id=" + orderer_filter + " ";
        }

        if (!keyword.trim().equals("")) {
            query += "and inventory.item like '%" + keyword + "%' OR order.id like '%" + keyword + "%' ";
        } else
            keyword = "";

        List<InventoryRelationOrder> Items = InventoryRelationOrder.find(query + "order by order.id desc").fetch();

        List<InventoryOrder> MaxSizer = new ArrayList<InventoryOrder>();
        for (int i = 0; i < Items.size(); i++)
            if (!MaxSizer.contains(Items.get(i).order))
                MaxSizer.add(Items.get(i).order);
        int pageLimit = 40;
        int MaxPageNumber = MaxSizer.size() / pageLimit;
        if (MaxSizer.size() % pageLimit != 0) MaxPageNumber++;

        List<InventoryOrder> Orders = new ArrayList<InventoryOrder>();
        if (CurrentPageNumber * pageLimit < MaxSizer.size())
            Orders = MaxSizer.subList((CurrentPageNumber - 1) * pageLimit, CurrentPageNumber * pageLimit);
        else
            Orders = MaxSizer.subList((CurrentPageNumber - 1) * pageLimit, MaxSizer.size());


        int ordered = 0, approved = 0, ready = 0, recieved = 0, all = 0, cancelled = 0;
        if (Users.getUser().getPermissionType(controllers.Consts.permissionInventory) > 1) {
            ordered = InventoryOrder.find("status.id = ?1 AND location.project.id=?2 ", new Long(1), Users.pid()).fetch().size();
            approved = InventoryOrder.find("status.id = ?1  AND location.project.id=?2 ", new Long(2), Users.pid()).fetch().size();
            ready = InventoryOrder.find("status.id = ?1 AND location.project.id=?2 ", new Long(4), Users.pid()).fetch().size();
            recieved = InventoryOrder.find("status.id = ?1 AND location.project.id=?2 ", new Long(5), Users.pid()).fetch().size();
            cancelled = InventoryOrder.find("status.id = ?1 AND location.project.id=?2 ", new Long(6), Users.pid()).fetch().size();
            all = InventoryOrder.find("location.project.id=?1 ", Users.pid()).fetch().size();
        } else {
            ordered = InventoryOrder.find("status.id = ?1 AND location.project.id=?2 AND orderer.id=?3", new Long(1), Users.pid(), Users.getUser().id).fetch().size();
            approved = InventoryOrder.find("status.id = ?1  AND location.project.id=?2 AND orderer.id=?3", new Long(2), Users.pid(), Users.getUser().id).fetch().size();
            ready = InventoryOrder.find("status.id = ?1 AND location.project.id=?2 AND orderer.id=?3", new Long(4), Users.pid(), Users.getUser().id).fetch().size();
            recieved = InventoryOrder.find("status.id = ?1 AND location.project.id=?2 AND orderer.id=?3", new Long(5), Users.pid(), Users.getUser().id).fetch().size();
            cancelled = InventoryOrder.find("status.id = ?1 AND location.project.id=?2 AND orderer.id=?3", new Long(6), Users.pid(), Users.getUser().id).fetch().size();
            all = InventoryOrder.find("location.project.id=?1 AND orderer.id=?2", Users.pid(), Users.getUser().id).fetch().size();
        }

        render(MaxSizer, Orders, Orderer, permissions, Plans, user, filterStartDate, filterEndDate, Inventorys, Locations, MaxPageNumber, CurrentPageNumber, location_filter, orderer_filter, ordered, approved, ready, recieved, cancelled, all, status, keyword);
    }

    public static void newOrder(String[] item, String[] quantity, String[] note, Date due, String location, String plan) {

        InventoryOrder order = new InventoryOrder();
        Task newPlan = null;
        if (plan != null && !plan.equals(""))
            newPlan = Task.findById(Long.parseLong(plan));
        User orderer = Users.getUser();
        InventoryOrderStatus orderStatus = InventoryOrderStatus.findById(new Long(1));
        InventoryLocation loc = InventoryLocation.find("name=?1", location).first();
        Date date = new Date();
        order.orderer = orderer;
        order.location = loc;
        order.date = date;
        order.due = due;
        order.edited = false;
        order.status = orderStatus;
        order.task = newPlan;
        order.create();
        for (int i = 0; i < item.length; i++) {
            InventoryRelationOrder relationOrder = new InventoryRelationOrder();
            Inventory inv = Inventory.find("item = ?1", item[i]).first();
            relationOrder.inventory = inv;
            relationOrder.orderer_comment = note[i];
            relationOrder.quantity = Double.parseDouble(quantity[i]);
            relationOrder.order = order;
            relationOrder.create();

        }


        List<User> users = new ArrayList<User>();

        if (loc.project != null) {
            List<OrgPermissionRelation> relation = OrgPermissionRelation.find("permissionType.permission.alias=?1 AND (permissionType.value=?2 OR permissionType.value=?3 OR permissionType.value=?4)", Consts.permissionInventory, 2, 3, 5).fetch();
            for (int i = 0; i < relation.size(); i++) {
                if (!users.contains(relation.get(i).organizationChart.user) && relation.get(i).organizationChart.user != Users.getUser() && InventoryRelationWorker.find("location.id=?1", loc.id).first() != null)
                    users.add(relation.get(i).organizationChart.user);
            }
            UserLiveRoom.get().notification(Users.getUser(), "InventoryOrder", users, order.id);
            if (due != null) {
                ReminderModel reminder = new ReminderModel();
                reminder.order = order;
                reminder.mainType = "meeting";
                reminder.title = "";
                reminder.reminderDate = due;
                reminder.reminderUsers = new ArrayList<ReminderUser>();
                for (User user : users) reminder.addUser(user);
                reminder.create();
            }
        } else {
            List<UserPermissionRelation> userPermissionRelations = UserPermissionRelation.find("permissionType.permission.alias=?1 AND (permissionType.value=?2 OR permissionType.value=?3 OR permissionType.value=?4)", Consts.permissionInventoryOther, 2, 3, 5).fetch();
            for (int i = 0; i < userPermissionRelations.size(); i++) {
                if (InventoryLocations.isWorker(order.location.id, userPermissionRelations.get(i).user.id) && !users.contains(userPermissionRelations.get(i).user) && userPermissionRelations.get(i).user != Users.getUser())
                    users.add(userPermissionRelations.get(i).user);
            }
            UserLiveRoom.get().notification(Users.getUser(), "InventoryOrder", users, order.id);
            if (due != null) {
                ReminderModel reminder = new ReminderModel();
                reminder.order = order;
                reminder.mainType = "meeting";
                reminder.title = "";
                reminder.reminderDate = due;
                reminder.mainType = "order";
                reminder.reminderUsers = new ArrayList<ReminderUser>();
                for (User user : users) reminder.addUser(user);
                reminder.create();
            }
        }


        viewAll("");

    }

    public static void saveOrder(Long id, String ids[], String[] item, String[] quantity, Date due, String location) {
        InventoryOrder order = InventoryOrder.findById(id);
        InventoryOrderStatus orderStatus;
        if (Users.getUser().getPermissionType(location) == 2 || Users.getUser().getPermissionType(location) == 3) {
            order.edited = true;
            orderStatus = InventoryOrderStatus.findById(new Long(2));
        } else {
            orderStatus = InventoryOrderStatus.findById(new Long(1));
            InventoryLocation loc = InventoryLocation.find("name=?1", location).first();
            order.location = loc;
            order.due = due;
        }

        Date date = new Date();

        order.status = orderStatus;
        order.save();
        for (int i = 0; i < order.inventorys.size(); i++) {
            order.inventorys.get(i).delete();
        }
        for (int i = 0; i < item.length; i++) {
            if (ids[i].equals("0")) {
                InventoryRelationOrder relationOrder = new InventoryRelationOrder();
                Inventory inv = Inventory.find("item = ?1", item[i]).first();
                relationOrder.inventory = inv;
                relationOrder.quantity = Double.parseDouble(quantity[i]);
                relationOrder.order = order;
                relationOrder.create();
            } else {
                InventoryRelationOrder relationOrder = InventoryRelationOrder.find("id=?1", Long.parseLong(ids[i])).first();
                relationOrder.inventory = Inventory.find("item = ?1", item[i]).first();
                relationOrder.quantity = Double.parseDouble(quantity[i]);
                relationOrder.order = order;
                relationOrder.save();
            }

        }

        viewAll("");

    }


    public static void editOrder(Long id) {
        InventoryOrder order = InventoryOrder.findById(id);
        List<InventoryLocation> Locations = InventoryLocation.findAll();
        List<NotificationMessage> orderNot = NotificationMessage.find("order.id = " + order.id + " and acceptor.id = " + Users.getUser().id + " and seen <> true").fetch();
        for (NotificationMessage notf : orderNot)
            if (notf != null && !notf.seen) {
                notf.seen = true;
                notf._save();
            }
        render(order, Locations);

    }

    public static void deleteOrder(Long id) {
        InventoryOrder order = InventoryOrder.findById(id);
        order.delete();
        viewAll("");

    }

    public static void cancelOrder(Long id) {
        InventoryOrderStatus status = InventoryOrderStatus.findById(new Long(6));
        InventoryOrder order = InventoryOrder.findById(id);
        if (order.status.id == 2) {
            for (int i = 0; i < order.inventorys.size(); i++) {
                InventoryRelationOrder relationOrder = InventoryRelationOrder.findById(order.inventorys.get(i).id);
                InventoryRelation relation = InventoryRelation.find("location_id = ?1 AND inventory_id=?2", order.location.id, relationOrder.inventory.id).first();
                if (relation != null) {
                    relation.quantity = relation.quantity + relationOrder.approved;
                    relation.save();
                }
            }
        }
        if (order.status.id == 4) {
            for (int i = 0; i < order.inventorys.size(); i++) {
                InventoryRelationOrder relationOrder = InventoryRelationOrder.findById(order.inventorys.get(i).id);
                InventoryRelation relation = InventoryRelation.find("location_id = ?1 AND inventory_id=?2", order.location.id, relationOrder.inventory.id).first();
                if (relation != null) {
                    relation.quantity = relation.quantity + relationOrder.issued;
                    relation.save();
                }
            }
        }

        order.canceller = Users.getUser();
        order.status = status;
        order.save();


        List<User> users = new ArrayList<User>();
        if (order.orderer != Users.getUser())
            users.add(order.orderer);
        UserLiveRoom.get().notification(Users.getUser(), "InventoryOrder", users, order.id);

        viewAll("");

    }

    public static void approveOrder(Long id, String[] approved, String[] ids, String[] approver_comment, String location) {
        InventoryOrderStatus status = InventoryOrderStatus.findById(new Long(2));
        Date date = new Date();
        InventoryOrder order = InventoryOrder.findById(id);
        InventoryLocation loc = InventoryLocation.find("name = ?1", location).first();
        if ((orderPermissionType(order.id) == 2 || orderPermissionType(order.id) == 3 || orderPermissionType(order.id) == 5) && loc != null) {
            for (int i = 0; i < ids.length; i++) {
                InventoryRelationOrder relationOrder = InventoryRelationOrder.findById(Long.parseLong(ids[i]));
                relationOrder.approved = Double.parseDouble(approved[i]);
                relationOrder.approver_comment = approver_comment[i];
                relationOrder.save();
                InventoryRelation relation = InventoryRelation.find("location_id = ?1 AND inventory_id=?2", loc.id, relationOrder.inventory.id).first();
                if (relation != null) {
                    relation.quantity = relation.quantity - Double.parseDouble(approved[i]);
                    relation.save();
                }
            }
            order.status = status;
            order.approver = Users.getUser();
            order.approved_date = date;
            order.save();

            List<User> users = new ArrayList<User>();
            if (order.orderer != Users.getUser())
                users.add(order.orderer);
            if (order.location.project != null) {
                List<OrgPermissionRelation> orgPermissionRelations = OrgPermissionRelation.find("permissionType.permission.alias=?1 AND (permissionType.value=?2 OR permissionType.value=?3)", Consts.permissionInventory, 4, 5).fetch();
                for (int i = 0; i < orgPermissionRelations.size(); i++) {
                    if (InventoryLocations.isWorker(order.location.id, orgPermissionRelations.get(i).organizationChart.user.id) && !users.contains(orgPermissionRelations.get(i).organizationChart.user) && orgPermissionRelations.get(i).organizationChart.user != Users.getUser())
                        users.add(orgPermissionRelations.get(i).organizationChart.user);
                }
            } else {
                List<UserPermissionRelation> userPermissionRelations = UserPermissionRelation.find("permissionType.permission.alias=?1 AND (permissionType.value=?2 OR permissionType.value=?3)", Consts.permissionInventoryOther, 4, 5).fetch();
                for (int i = 0; i < userPermissionRelations.size(); i++) {
                    if (InventoryLocations.isWorker(order.location.id, userPermissionRelations.get(i).user.id) && !users.contains(userPermissionRelations.get(i).user) && userPermissionRelations.get(i).user != Users.getUser())
                        users.add(userPermissionRelations.get(i).user);
                }
            }

            UserLiveRoom.get().notification(Users.getUser(), "InventoryOrder", users, order.id);


        }

        viewAll("");
    }

    public static void readyOrder(Long id, String[] issued, String[] ids, String[] issuer_comment) {
        InventoryOrderStatus status = InventoryOrderStatus.findById(new Long(4));
        Date date = new Date();
        InventoryOrder order = InventoryOrder.findById(id);
        if ((orderPermissionType(order.id) == 2 || orderPermissionType(order.id) == 3 || orderPermissionType(order.id) == 5)) {
            for (int i = 0; i < ids.length; i++) {
                InventoryRelationOrder relationOrder = InventoryRelationOrder.findById(Long.parseLong(ids[i]));
                relationOrder.issued = Double.parseDouble(issued[i]);
                relationOrder.issuer_comment = issuer_comment[i];
                relationOrder.save();
                InventoryRelation relation = InventoryRelation.find("location_id = ?1 AND inventory_id=?2", order.location.id, relationOrder.inventory.id).first();
                if (relation != null) {
                    relation.quantity = relation.quantity + relationOrder.approved - Double.parseDouble(issued[i]);
                    relation.save();
                }
            }
            order.issued_date = date;
            order.issuer = Users.getUser();
            order.status = status;
            order.save();

            List<User> users = new ArrayList<User>();
            if (order.orderer != Users.getUser())
                users.add(order.orderer);
            UserLiveRoom.get().notification(Users.getUser(), "InventoryOrder", users, order.id);
        }
        viewAll("");

    }

    public static void recieveOrder(Long id) {
        InventoryOrderStatus status = InventoryOrderStatus.findById(new Long(5));
        InventoryOrder order = InventoryOrder.findById(id);
        if (order.orderer == Users.getUser()) {
            order.status = status;
            order.recieved_date = new Date();
            order.save();

            List<User> users = new ArrayList<User>();
            List<OrgPermissionRelation> relation = OrgPermissionRelation.find("permissionType.permission.alias=?1 AND (permissionType.value=?2 OR permissionType.value=?3)", Consts.permissionInventory, 4, 5).fetch();
            for (int i = 0; i < relation.size(); i++) {
                if (!users.contains(relation.get(i).organizationChart.user) && relation.get(i).organizationChart.user != Users.getUser())
                    users.add(relation.get(i).organizationChart.user);
            }
            UserLiveRoom.get().notification(Users.getUser(), "InventoryOrder", users, order.id);

        }
        viewAll("");

    }


    public static void getMeasure(String inv) {
        Inventory inventory = Inventory.find("item = ?1", inv).first();
        renderText(inventory.inventoryMeasure.measure);
    }

    public static void getSupplier(String inv) {
        Inventory inventory = Inventory.find("item = ?1", inv).first();
        List<String> suppliers = new ArrayList<String>();
        if (inventory != null) {
            List<InventoryRelationPrice> relationPrices = InventoryRelationPrice.find("inventory.id=?1", inventory.id).fetch();
            for (int i = 0; i < relationPrices.size(); i++) {
                suppliers.add(String.valueOf(relationPrices.get(i).supplier.id));
                suppliers.add(relationPrices.get(i).supplier.name);
            }
        }
        renderJSON(suppliers);
    }



    public static void getLocationQuantity(String inv, String loc) {
        if (inv != null && loc != null) {
            Inventory inventory = Inventory.find("item = ?1", inv).first();
            InventoryLocation location = InventoryLocation.find("name=?1", loc).first();
            InventoryRelation relation = null;
            if (inventory != null && location != null) {
                relation = InventoryRelation.find("inventory_id =?1 AND location_id=?2", inventory.id, location.id).first();
                if (relation == null)
                    renderText("0.0");
                else
                    renderText(relation.quantity);
            } else
                renderText("0.0");
        } else {
            renderText("0.0");
        }
    }

    public static Double getLocationQuantityReturn(String inv, String loc) {
        if (inv != null && loc != null) {
            Inventory inventory = Inventory.find("item = ?1", inv).first();
            InventoryLocation location = InventoryLocation.find("name=?1", loc).first();
            InventoryRelation relation = null;
            if (inventory != null && location != null) {
                relation = InventoryRelation.find("inventory_id =?1 AND location_id=?2", inventory.id, location.id).first();
                if (relation == null)
                    return 0D;
                else
                    return relation.quantity;
            } else
                return 0D;
        } else {
            return 0D;
        }
    }

    public static List<String> getLocationPermission() {
        List<String> permissions = new ArrayList<String>();
        List<InventoryLocation> inv = null;
        if (Users.getUser().getPermissionType("inventory") == 1 || Users.getUser().getPermissionType("inventory") == 3) {
            inv = InventoryLocation.find("project.id=?1", Users.pid()).fetch();
        }
        if (inv != null)
            for (int i = 0; i < inv.size(); i++) {
                permissions.add(inv.get(i).name);
            }
        if (permissions.size() == 0)
            return null;
        else
            return permissions;
    }

    public static void getLocationInventorys(String loc) {
        List<String> inventoryList = new ArrayList<String>();
        InventoryLocation location = InventoryLocation.find("name=?1",loc).first();
        List<InventoryRelation> relations = InventoryRelation.find("location.id=?1 AND quantity>0",location.id).fetch();
        for(int i=0;i<relations.size();i++){
            if(!inventoryList.contains(relations.get(i).inventory.item)){
                inventoryList.add(relations.get(i).inventory.item);
            }
        }
        renderJSON(inventoryList);
    }

    public static int getProjectInventoryOrderPermissionType(Long id) {
        if (id.intValue() == 0) return 0;
        OrgPermissionRelation relation = OrgPermissionRelation.find("organizationChart.project.id=?1 AND organizationChart.user.id=?2 AND permissionType.permission.alias=?3", id, Users.getUser().id, controllers.Consts.permissionInventory).first();
        if (relation != null) {
            return relation.permissionType.value;
        }
        return 0;
    }

    public static int orderPermissionType(Long id) {
        InventoryOrder order = InventoryOrder.findById(id);
        if (order.location.project == null) {
            UserPermissionRelation relation = UserPermissionRelation.find("user.id=?1 AND permissionType.permission.alias=?2", Users.getUser().id, Consts.permissionInventoryOther).first();
            if (relation != null && InventoryLocations.isWorker(order.location.id, Users.getUser().id)) {
                return relation.permissionType.value;
            }
        } else {
            OrgPermissionRelation relation = OrgPermissionRelation.find("organizationChart.project.id=?1 AND organizationChart.user.id=?2 AND permissionType.permission.alias=?3", order.location.project.id, Users.getUser().id, controllers.Consts.permissionInventory).first();
            if (relation != null && InventoryLocations.isWorker(order.location.id, Users.getUser().id)) {
                return relation.permissionType.value;
            }
        }

        return 0;
    }

}
