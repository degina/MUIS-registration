package controllers;

import models.Inventory;
import models.InventoryMeasure;

import java.util.List;

/**
 * Created by Personal on 3/1/2015.
 */
public class InventoryMeasures extends CRUD {
    public static void viewAll(String message) {
        list(1, "", message);
    }

    public static void list(int CurrentPageNumber, String keyword, String message) {

        int pageLimit = 40;
        List<InventoryMeasure> MaxSizer = InventoryMeasure.find("order by id desc").fetch();
        int MaxPageNumber = MaxSizer.size() / pageLimit;
        if (MaxSizer.size() % pageLimit != 0) MaxPageNumber++;
        List<InventoryMeasure> Measures = InventoryMeasure.find("order by id desc").fetch(CurrentPageNumber, pageLimit);
        render(Measures, message, MaxPageNumber, CurrentPageNumber);
    }

    public static void newMeasure(String measure) {

        InventoryMeasure Measure = InventoryMeasure.find("measure = ?1", measure).first();
        if (Measure == null) {
            Measure = new InventoryMeasure();
            Measure.measure = measure;
            Measure.create();
            viewAll("addsuccess");
        } else {
            viewAll("addfailed");
        }
    }

    public static void newMeasureModal(String measure) {

        InventoryMeasure Measure = InventoryMeasure.find("measure = ?1", measure).first();
        if (Measure == null) {
            Measure = new InventoryMeasure();
            Measure.measure = measure;
            Measure.create();
            renderText(String.valueOf(Measure.id));
        } else {
            renderText("false");
        }
    }

    public static void editMeasure(Long id) {
        InventoryMeasure Measure = InventoryMeasure.findById(id);
        renderText(Measure.measure);
    }

    public static void saveMeasure(Long measure_id, String measure) {
        InventoryMeasure Measure = InventoryMeasure.findById(measure_id);
        if (measure != null) {

            Measure.measure = measure;
            Measure.save();
            viewAll("savesuccess");
        } else
            viewAll("savefailed");
    }

    public static void deleteMeasure(Long id) {
        System.out.println(id);
        InventoryMeasure measure = InventoryMeasure.findById(id);
        if (measure != null) {
            Inventory inventory = Inventory.find("inventoryMeasure.id=" + id + " ").first();
            if (inventory == null) {
                measure._delete();
                renderText("deletesuccess");
            } else
                renderText("persistent");

        } else
            renderText("deletefailed");
    }

    public static void inventoryCheckMeasure(String measure) {
        InventoryMeasure meas = InventoryMeasure.find("measure = ?1", measure).first();
        if (meas == null)
            renderText("true");
        else
            renderText("false");
    }

    public static void inventoryCheckMeasureEdit(String measure, String id) {
        InventoryMeasure meas = InventoryMeasure.find("measure = ?1", measure).first();
        if (meas == null)
            renderText("true");
        else if (meas.id == Integer.parseInt(id))
            renderText("true");
        else
            renderText("false");
    }
}
