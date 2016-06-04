package controllers;

import models.*;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by Personal on 1/17/2015.
 */

public class InventoryLocations extends CRUD {
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
            } else if (status == 1) {
                query = "name like'%" + keyword + "%'";
            } else {
                if (status == 2)
                    query = "address like'%" + keyword + "%'";
                else {
                    query = "order by id";
                }
            }
        }
        List<Inventory> Inventorys = Inventory.find(query).fetch();
        List<Project> Projects = Project.findAll();
        Long is = status;

        int pageLimit = 40;
        List<InventoryLocation> MaxSizer = InventoryLocation.find(" order by id desc").fetch();
        int MaxPageNumber = MaxSizer.size() / pageLimit;
        if (MaxSizer.size() % pageLimit != 0) MaxPageNumber++;
        List<InventoryLocation> Locations = InventoryLocation.find(" order by id desc").fetch(CurrentPageNumber, pageLimit);


        render(Projects, Locations, Inventorys, is, message, MaxPageNumber, CurrentPageNumber);
    }

    public static void previewLocation(Long id) {
        InventoryLocation location = InventoryLocation.findById(id);

        List<String> strings = new ArrayList<String>();
        strings.add(location.name);
        strings.add(location.address);
        renderJSON(strings);
    }

    public static void newLocation(String name, String address, String is_main, String project) {
        InventoryLocation is = InventoryLocation.find("name=?1", name).first();
        Project pro = Project.findById(Long.parseLong(project));
        if (is == null) {
            InventoryLocation location = new InventoryLocation();
            location.name = name;
            location.address = address;
            location.is_main = is_main;
            if (!is_main.equals("true") && pro != null) {
                location.project = pro;
            }
            location.create();

            viewAll("true");
        } else
            viewAll("false");
    }

    public static void saveLocation(Long id, String name, String address, String project, String[] worker) {
        InventoryLocation location = InventoryLocation.findById(id);
        Project pro = Project.findById(Long.parseLong(project));
        if (location != null) {
            location.name = name;
            location.address = address;
            if (pro != null) {
                location.project = pro;
            }
            if (location.is_main.equals("true"))
                location.project = null;
            location.save();

            List<InventoryRelationWorker> inventoryRelationWorkers = InventoryRelationWorker.find("location.id=?1", location.id).fetch();
            for (InventoryRelationWorker rels : inventoryRelationWorkers) {
                rels._delete();
            }
            if (worker != null) {
                for (int i = 0; i < worker.length; i++) {
                    User work = User.find("id=?1", Long.parseLong(worker[i])).first();
                    System.out.println(work.firstName);
                    InventoryRelationWorker newRel = new InventoryRelationWorker();
                    newRel.worker = work;
                    newRel.location = location;
                    newRel.create();
                }
            }

            viewAll("true");
        } else
            viewAll("false");
    }

    public static void deleteLocation(Long id) {
        InventoryLocation location = InventoryLocation.findById(id);
        if (location != null) {
            location._delete();
            viewAll("true");
        } else
            viewAll("false");
    }

    public static void editLocation(Long id) {
        InventoryLocation location = InventoryLocation.findById(id);
        List<User> Workers = new ArrayList<User>();
        if (location.is_main.equals("false")) {
            List<OrganizationChart> organizationCharts = OrganizationChart.find("project.id=?1", location.project.id).fetch();
            for(int i=0;i<organizationCharts.size();i++){
                if(!Workers.contains(organizationCharts.get(i).user)){
                    Workers.add(organizationCharts.get(i).user);
                }
            }
        } else {
            Workers = User.findAll();
        }
        List<Project> Projects = Project.findAll();

        render(location, Workers, Projects);
    }

    public static void editStatus(Long id) {
        List<InventoryRelation> relation = InventoryRelation.find("location_id = ?1", id).fetch();

        if (relation != null) {
            List<String> strings = new ArrayList<String>();
            for (int i = 0; i < relation.size(); i++) {
                if (relation.get(i).idealQuantity != null && relation.get(i).warningQuantity != null && (relation.get(i).idealQuantity > 0 || relation.get(i).warningQuantity > 0)) {
                    strings.add(relation.get(i).inventory.item);
                    strings.add(String.valueOf(relation.get(i).idealQuantity));
                    strings.add(String.valueOf(relation.get(i).warningQuantity));
                    strings.add(relation.get(i).inventory.inventoryMeasure.measure);
                }
            }
            renderJSON(strings);
        }
    }

    public static void saveStatus(Long id, String[] item, String[] ideal_quantity, String[] warning_quantity) {

        int is = 0;


        InventoryLocation location = InventoryLocation.findById(id);
        if (location != null) {
            is++;
            List<InventoryRelation> relationList = InventoryRelation.find("location.id=?1", location.id).fetch();
            for (int i = 0; i < relationList.size(); i++) {
                relationList.get(i).idealQuantity = 0D;
                relationList.get(i).warningQuantity = 0D;
                relationList.get(i).save();
            }
            for (int i = 0; i < item.length; i++) {
                Inventory inventory = Inventory.find("item=?1", item[i]).first();
                if (inventory == null)
                    is++;
                InventoryRelation relation = InventoryRelation.find("inventory_id = ?1 AND location_id=?2", inventory.id, location.id).first();
                if (relation == null) {
                    relation = new InventoryRelation();
                    relation.quantity = new Double(0);
                    relation.location = location;
                    relation.inventory = inventory;
                    if (ideal_quantity[i] != null)
                        relation.idealQuantity = Double.parseDouble(ideal_quantity[i]);
                    if (warning_quantity[i] != null)
                        relation.warningQuantity = Double.parseDouble(warning_quantity[i]);
                    relation.create();
                } else {
                    if (ideal_quantity[i] != null)
                        relation.idealQuantity = Double.parseDouble(ideal_quantity[i]);
                    if (warning_quantity[i] != null)
                        relation.warningQuantity = Double.parseDouble(warning_quantity[i]);
                    relation.save();
                }
                if (is == 0)
                    viewAll("true");
                else
                    viewAll("false");

            }
        } else
            viewAll("false");
    }

    public static void inventoryCheckLocation(String location) {
        InventoryLocation loc = InventoryLocation.find("name=?1", location).first();
        if (loc == null) {
            renderText("true");
        } else
            renderText("false");

    }

    public static void inventoryCheckLocationEdit(String location, String id) {
        InventoryLocation loc = InventoryLocation.find("name=?1", location).first();
        if (loc == null) {
            renderText("true");
        } else if (loc.id == Integer.parseInt(id)) {
            renderText("true");
        } else
            renderText("false");
    }

    public static int inventoryCheckLocationPermission(Long id, String alias) {
        UserPermission permission = UserPermission.find("alias = ?1", alias).first();
        if (permission == null) {
            return 0;
        } else {
            return 0;
//            return Users.getUser().getPermission(permission.id);
        }
    }

    public static boolean isWorker(Long loc, Long u) {
        if (InventoryRelationWorker.find("location.id=?1 AND worker.id=?2", loc, u).first() != null)
            return true;
        else
            return false;
    }
}

