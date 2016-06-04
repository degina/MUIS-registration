package controllers;

import models.Inventory;
import models.InventoryRelationPrice;
import models.InventorySupplier;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by Personal on 1/17/2015.
 */

public class InventorySuppliers extends CRUD {
    public static void viewAll(String message) {
        list(1, new Long(0), "", message);
    }

    public static void list(int CurrentPageNumber, Long status, String keyword, String message) {
        List<Inventory> Inventorys = Inventory.findAll();
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
        Long is = status;


        int pageLimit = 40;
        List<InventorySupplier> MaxSizer = InventorySupplier.find(" order by id desc").fetch();
        int MaxPageNumber = MaxSizer.size() / pageLimit;
        if (MaxSizer.size() % pageLimit != 0) MaxPageNumber++;
        List<InventorySupplier> Suppliers = InventorySupplier.find(" order by id desc").fetch(CurrentPageNumber, pageLimit);


        render(Suppliers, is, Inventorys, message, MaxPageNumber, CurrentPageNumber);

    }

    public static void newSupplier(String name, String person, String address, int phone, int phone_alternative, String email, String website, String item[], String price[], String partnumber[]) {
        InventorySupplier is = InventorySupplier.find("name=?1", name).first();
        if (is == null) {
            InventorySupplier supplier = new InventorySupplier();
            supplier.name = name;
            supplier.address = address;
            supplier.person = person;
            supplier.phone = phone;
            supplier.phone_alternative = phone_alternative;
            supplier.email = email;
            supplier.website = website;
            supplier.create();
            if (item != null && item.length > 0) {
                for (int i = 0; i < item.length; i++) {
                    InventoryRelationPrice relationPrice = new InventoryRelationPrice();
                    Inventory inventory = Inventory.find("item=?1", item[i]).first();
                    relationPrice.inventory = inventory;
                    relationPrice.supplier = supplier;
                    if (price[i] != null && !price[i].equals(""))
                        relationPrice.price = StoSplitLong(price[i]);
                    else
                        relationPrice.price = 0L;
                    if (partnumber[i] != null && !partnumber[i].equals(""))
                        relationPrice.supplier_partnumber = partnumber[i];
                    else
                        relationPrice.supplier_partnumber = "";
                    relationPrice.create();
                }
            }
            viewAll("true");
        } else
            viewAll("false");
    }

    public static void newSupplierModal(String name, String person, String address, int phone, int phone_alternative, String email, String website, String item[], String price[], String partnumber[]) {
        InventorySupplier is = InventorySupplier.find("name=?1", name).first();
        if (is == null) {
            InventorySupplier supplier = new InventorySupplier();
            supplier.name = name;
            supplier.address = address;
            supplier.person = person;
            supplier.phone = phone;
            supplier.phone_alternative = phone_alternative;
            supplier.email = email;
            supplier.website = website;
            supplier.create();
            System.out.println("1");
            if (item != null && item.length > 0) {
                System.out.println("2");
                for (int i = 0; i < item.length; i++) {
                    InventoryRelationPrice relationPrice = new InventoryRelationPrice();
                    Inventory inventory = Inventory.find("item=?1", item[i]).first();
                    relationPrice.inventory = inventory;
                    relationPrice.supplier = supplier;
                    if (price[i] != null && !price[i].equals(""))
                        relationPrice.price = StoSplitLong(price[i]);
                    else
                        relationPrice.price = 0L;
                    if (partnumber[i] != null && !partnumber[i].equals(""))
                        relationPrice.supplier_partnumber = partnumber[i];
                    else
                        relationPrice.supplier_partnumber = "";
                    relationPrice.create();
                    System.out.println("3");
                }
            }
            renderText("true");
        } else
            renderText("false");
    }

    public static void saveSupplier(Long id, String name, String person, String address, String phone, String phone_alternative, String email, String website, String item[], String price[], String partnumber[]) {
        InventorySupplier supplier = InventorySupplier.findById(id);
        if (supplier != null) {
            supplier.name = name;
            supplier.address = address;
            supplier.person = person;
            supplier.phone = Integer.parseInt(phone);
            supplier.phone_alternative = Integer.parseInt(phone_alternative);
            supplier.email = email;
            supplier.website = website;
            supplier.save();
            for (int i = 0; i < supplier.priceList.size(); i++) {
                supplier.priceList.get(i)._delete();
            }
            if (item != null && item.length > 0) {
                for (int i = 0; i < item.length; i++) {
                    InventoryRelationPrice relationPrice = new InventoryRelationPrice();
                    Inventory inventory = Inventory.find("item=?1", item[i]).first();
                    relationPrice.inventory = inventory;
                    if (price[i] != null && !price[i].equals(""))
                        relationPrice.price = StoSplitLong(price[i]);
                    else
                        relationPrice.price = 0L;
                    if (partnumber[i] != null && !partnumber[i].equals(""))
                        relationPrice.supplier_partnumber = partnumber[i];
                    else
                        relationPrice.supplier_partnumber = "";
                    relationPrice.supplier = supplier;
                    relationPrice.create();
                }
            }
            viewAll("true");
        } else
            viewAll("false");
    }

    public static void deleteSupplier(Long id) {
        InventorySupplier supplier = InventorySupplier.findById(id);
        if (supplier != null) {
            supplier._delete();
            viewAll("true");
        } else
            viewAll("false");
    }

    public static void editSupplier(Long id) {
        InventorySupplier supplier = InventorySupplier.findById(id);

        List<String> strings = new ArrayList<String>();
        strings.add(supplier.name);
        strings.add(supplier.person);
        strings.add(supplier.address);
        strings.add(String.valueOf(supplier.phone));
        strings.add(String.valueOf(supplier.phone_alternative));
        strings.add(supplier.email);
        strings.add(supplier.website);

        for (int i = 0; i < supplier.priceList.size(); i++) {
            strings.add(supplier.priceList.get(i).inventory.item);
            strings.add(String.valueOf(supplier.priceList.get(i).price));
            if (supplier.priceList.get(i).supplier_partnumber != null)
                strings.add(supplier.priceList.get(i).supplier_partnumber);
            else
                strings.add("");

        }


        renderJSON(strings);

    }

    public static void previewSupplier(Long id) {
        InventorySupplier supplier = InventorySupplier.findById(id);

        List<String> strings = new ArrayList<String>();
        strings.add(supplier.name);
        strings.add(supplier.person);
        strings.add(supplier.address);
        strings.add(String.valueOf(supplier.phone));
        strings.add(String.valueOf(supplier.phone_alternative));
        strings.add(supplier.email);
        strings.add(supplier.website);


        renderJSON(strings);
    }

    public static Long StoSplitLong(String str) {
        if (str != null && !str.equals(""))
            return Long.parseLong(str.replaceAll("'", ""));
        else
            return null;
    }
}

