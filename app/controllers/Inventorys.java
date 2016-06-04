package controllers;

import models.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Personal on 2015-01-11.
 */
public class Inventorys extends CRUD {
    public static void viewAll(String message) {
        list(1, "", "", "", "", message);
    }

    public static void list(int CurrentPageNumber, String keyword, String supplier_filter, String category_filter, String subcategory_filter, String message) {
        User user = Users.getUser();
        UserTeam userTeam = Users.getUser().userTeam;

        String query = "id>0 ";

        List<InventoryCategory> Categories = InventoryCategory.findAll();
        List<InventoryLocation> Locations = InventoryLocation.findAll();
        List<InventorySupplier> Suppliers = InventorySupplier.findAll();
        List<InventoryMeasure> Measures = InventoryMeasure.findAll();
        List<InventorySubCategory> SubCategories = new ArrayList<InventorySubCategory>();
        List<Inventory> inventoryList = Inventory.findAll();

        if (!supplier_filter.equalsIgnoreCase("0") && !supplier_filter.equalsIgnoreCase("")) {
            List<InventoryRelationPrice> relationPrices = InventoryRelationPrice.find("supplier.id=?1", Long.parseLong(supplier_filter)).fetch();
            inventoryList.clear();
            for (int i = 0; i < relationPrices.size(); i++) {
                inventoryList.add(relationPrices.get(i).inventory);
            }

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
        List<Inventory> MaxSizer = Inventory.find(query + "order by item desc").fetch();
        MaxSizer.retainAll(inventoryList);
        int MaxPageNumber = MaxSizer.size() / pageLimit;
        if (MaxSizer.size() % pageLimit != 0) MaxPageNumber++;
        List<Inventory> Inventorys = Inventory.find(query + "order by item desc").fetch(CurrentPageNumber, pageLimit);
        Inventorys.retainAll(inventoryList);

        render(Inventorys, Categories, SubCategories, Locations, Suppliers, Measures, CurrentPageNumber, MaxPageNumber, message, supplier_filter, category_filter, subcategory_filter, keyword);
    }


    public static void inventoryShowSubCategory(String name) {
        InventoryCategory category = InventoryCategory.find("name = ?1", name).first();
        if (category != null) {
            List<InventorySubCategory> subCategories = InventorySubCategory.find("category_id=?1 order by name", category.id).fetch();
            if (subCategories != null) {
                List<String> subs = new ArrayList<String>();
                for (int i = 0; i < subCategories.size(); i++) {
                    subs.add(subCategories.get(i).name);
                    subs.add(String.valueOf(subCategories.get(i).id));
                }
                renderJSON(subs);
            }
        }

    }

    public static void inventoryShowSubCategoryFilter(String id) {
        InventoryCategory category = InventoryCategory.findById(Long.parseLong(id));
        System.out.println(id);
        if (category != null) {

            List<InventorySubCategory> subCategories = InventorySubCategory.find("category_id=?1 order by name", category.id).fetch();
            if (subCategories != null) {
                List<String> subs = new ArrayList<String>();
                for (int i = 0; i < subCategories.size(); i++) {
                    subs.add(subCategories.get(i).name);
                    subs.add(String.valueOf(subCategories.get(i).id));
                }
                renderJSON(subs);
            }
        }

    }

    public static void previewItem(Long id) {
        List<InventoryMeasure> Measures = InventoryMeasure.findAll();
        List<InventoryCategory> Categories = InventoryCategory.findAll();

        Inventory inventory = Inventory.findById(id);

        render(inventory, Measures, Categories);

    }

    public static void editItemGet(Long id) {

        Inventory inventory = Inventory.findById(id);

        List<String> strings = new ArrayList<String>();
        strings.add(inventory.item);
        strings.add(inventory.description);
        strings.add(String.valueOf(inventory.inventorySubCategory.id));
        strings.add(String.valueOf(inventory.inventoryMeasure.id));
        strings.add("");
        strings.add(inventory.itemNote);
        strings.add(inventory.website);

        for (int i = 0; i < inventory.inventoryAttachment.size(); i++) {
            strings.add(inventory.inventoryAttachment.get(i).filename);
            strings.add(inventory.inventoryAttachment.get(i).filedir);
            strings.add(inventory.inventoryAttachment.get(i).extension);
        }


        renderJSON(strings);

    }

    public static void deleteItem(Long id) {
        //
        Inventory inventory = Inventory.findById(id);
        if (inventory != null) {
            List<InventoryOrder> orders = InventoryOrder.findAll();
            for (int i = 0; i < orders.size(); i++) {
                if (orders.get(i).inventorys.size() == 0) {
                    orders.get(i)._delete();
                }
            }
            List<InventoryTransfer> transfers = InventoryTransfer.findAll();
            for (int i = 0; i < transfers.size(); i++) {
                if (transfers.get(i).inventorys.size() == 0) {
                    transfers.get(i)._delete();
                }
            }
            inventory._delete();
            renderText("deletesuccess");
        } else
            renderText("deletefailed");

    }

    public static void saveItem(Long id, String item, String description, Long subcategory,
                                String item_note, String item_website, Long measure, List<String> filename, List<String> filedir, List<String> extension, List<String> filesize) {
        Inventory inventory = Inventory.findById(id);
        if (inventory != null) {
            inventory.item = item;
            inventory.description = description;
            inventory.itemNote = item_note;
            inventory.website = item_website;
            inventory.inventoryMeasure = InventoryMeasure.findById(measure);
            inventory.description = description;
            InventorySubCategory subCategory = InventorySubCategory.findById(subcategory);
            inventory.inventorySubCategory = subCategory;
            inventory.save();

            for (InventoryAttachment attach : inventory.inventoryAttachment) {
                attach._delete();
            }
            inventory.inventoryAttachment = new ArrayList<InventoryAttachment>();
            inventory.save();
            if (filename != null && filename.size() > 0) {
                for (int i = 0; i < filename.size(); i++) {
                    InventoryAttachment newAttachment = new InventoryAttachment();
                    newAttachment.filename = filename.get(i);
                    newAttachment.filedir = filedir.get(i);
                    newAttachment.extension = extension.get(i);
                    newAttachment.filesize = Float.parseFloat(filesize.get(i));
                    newAttachment.inventory = inventory;
                    newAttachment.create();
                }
            }
            viewAll("true");
        } else
            viewAll("false");

    }

    public static void addItem() {

    }

    public static void newItem(String item, String description, Long subcategory,
                               String item_note, String item_website, Long measure, List<String> filename, List<String> filedir, List<String> extension, List<String> filesize) {
        Inventory inventory = new Inventory();

        inventory.item = item;
        inventory.description = description;
        inventory.itemNote = item_note;
        inventory.website = item_website;
        inventory.inventoryMeasure = InventoryMeasure.findById(measure);
        inventory.description = description;
        InventorySubCategory subCategory = InventorySubCategory.findById(subcategory);
        inventory.inventorySubCategory = subCategory;
        inventory.create();
        if (Inventory.findById(inventory.id) != null) {
            if (filename != null) {
                for (int i = 0; i < filename.size(); i++) {
                    InventoryAttachment newAttachment = new InventoryAttachment();
                    newAttachment.filename = filename.get(i);
                    newAttachment.filedir = filedir.get(i);
                    newAttachment.extension = extension.get(i);
                    newAttachment.filesize = Float.parseFloat(filesize.get(i));
                    newAttachment.inventory = inventory;
                    newAttachment.create();
                }
            }

            viewAll("true");
        } else
            viewAll("false");


    }

    public static void newItemModal(String item, String description, Long subcategory,
                                    String item_note, String item_website, Long measure, List<String> filename, List<String> filedir, List<String> extension, List<String> filesize) {
        Inventory inventory = new Inventory();

        inventory.item = item;
        inventory.description = description;
        inventory.itemNote = item_note;
        inventory.website = item_website;
        inventory.inventoryMeasure = InventoryMeasure.findById(measure);
        inventory.description = description;
        InventorySubCategory subCategory = InventorySubCategory.findById(subcategory);
        inventory.inventorySubCategory = subCategory;
        inventory.create();
        if (Inventory.findById(inventory.id) != null) {
            if (filename != null) {
                for (int i = 0; i < filename.size(); i++) {
                    InventoryAttachment newAttachment = new InventoryAttachment();
                    newAttachment.filename = filename.get(i);
                    newAttachment.filedir = filedir.get(i);
                    newAttachment.extension = extension.get(i);
                    newAttachment.filesize = Float.parseFloat(filesize.get(i));
                    newAttachment.inventory = inventory;
                    newAttachment.create();
                }
            }

            renderText("true");
        } else
            renderText("false");


    }


    public static void previewLocation(Long id) {
        List<InventoryRelation> Relations = InventoryRelation.find("inventory_id = ?1", id).fetch();

        render(Relations);

    }


    public static void transfer() {
        List<Inventory> inventorys = Inventory.findAll();
        int[] quantitys = new int[inventorys.size()];
        List<InventoryLocation> locations = InventoryLocation.findAll();
        render(inventorys, locations, quantitys);

    }

    public static void transferItem(Long item, String from, String to, String quantity) {
        InventoryRelation loc1 = InventoryRelation.find("inventory_id=" + item + " and location_id=" + Long.parseLong(from)).first();
        InventoryRelation loc2 = InventoryRelation.find("inventory_id=" + item + " and location_id=" + Long.parseLong(to)).first();

        if (loc2 == null) {
            loc2 = new InventoryRelation();
            loc2.create();
            Inventory inv = Inventory.findById(item);
            loc2.inventory = inv;
            InventoryLocation loc = InventoryLocation.findById(Long.parseLong(to));
            loc2.location = loc;
            loc2.quantity = Double.parseDouble(quantity);
            loc1.quantity = loc1.quantity - Long.parseLong(quantity);
        } else {
            loc2.quantity = loc2.quantity + Long.parseLong(quantity);
            loc1.quantity = loc1.quantity - Long.parseLong(quantity);
        }
        loc2.save();
        loc1.save();
        viewAll("");
    }

    public static void checkTransfer(Long item, Long location, Double quantity) {
        InventoryRelation relation = InventoryRelation.find("inventory_id=" + item + " and location_id=" + location).first();

        if (relation != null && quantity != null) {


            if (relation.quantity >= quantity) {
                renderText("true");
            } else {
                renderText("false");
            }
        } else {
            renderText("null");
        }
    }

    public static void relationList() {
        List<InventoryRelation> Relations = InventoryRelation.findAll();
        render(Relations);
    }

    public static void inventoryCheckInventory(String inventory) {
        Inventory inv = Inventory.find("item = ?1", inventory).first();
        if (inv == null)
            renderText("true");
        else
            renderText("false");

    }

    public static void inventoryLikeInventory(String inventory) {
        List<Inventory> inv = Inventory.find("item like '%" + inventory + "%'").fetch();
        List<String> names = new ArrayList<String>();
        for (int i = 0; i < inv.size(); i++)
            names.add(inv.get(i).item);
        if (inv == null)
            renderText("false");
        else
            renderJSON(names);

    }

    public static void inventoryCheckInventoryEdit(String inventory, String id) {
        Inventory inv = Inventory.find("item = ?1", inventory).first();
        if (inv == null)
            renderText("true");
        else if (inv.id == Integer.parseInt(id))
            renderText("true");
        else
            renderText("false");

    }

    public static void checkPermission() {
        renderText(String.valueOf(Users.getUser().getUserPermissionType("inventory")));
    }

    public static void supplierDetails(String inventory, String supplier) {
        List<String> strings = new ArrayList<String>();
        if (inventory == null || inventory.length() <= 0 || supplier == null || supplier.length() <= 0) {
            strings.add(" ");
            strings.add(" ");
            strings.add(" ");
        } else {
            InventorySupplier inventorySupplier = InventorySupplier.find("id=?1", Long.parseLong(supplier)).first();
            InventoryRelationPrice relationPrice = InventoryRelationPrice.find("inventory.id=?1 AND supplier.id=?2", Long.parseLong(inventory), Long.parseLong(supplier)).first();
            if (supplier != null && relationPrice != null) {
                strings.add(String.valueOf(relationPrice.price));
                if (inventorySupplier.phone_alternative > 0)
                    strings.add(String.valueOf(inventorySupplier.phone) + " " + String.valueOf(inventorySupplier.phone_alternative));
                else
                    strings.add(String.valueOf(inventorySupplier.phone));
                strings.add(String.valueOf(inventorySupplier.website));
            } else {
                strings.add(" ");
                strings.add(" ");
                strings.add(" ");
            }
        }
        renderJSON(strings);

    }

    public static void attachs(String item) {
        Inventory inventory = Inventory.findById(Long.parseLong(item));
        List<String> strings = new ArrayList<String>();

        for (int i = 0; i < inventory.inventoryAttachment.size(); i++) {
            strings.add(inventory.inventoryAttachment.get(i).filename);
            strings.add(inventory.inventoryAttachment.get(i).filedir);
            strings.add(inventory.inventoryAttachment.get(i).extension);
            strings.add(String.valueOf(inventory.inventoryAttachment.get(i).filesize));
        }

        renderJSON(strings);

    }
}
