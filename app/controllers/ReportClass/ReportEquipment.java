package controllers.ReportClass;

import com.google.gson.JsonArray;
import models.Equipment;
import models.ManPower;
import models.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by enkhamgalan on 4/13/15.
 */
public class ReportEquipment {
    public Equipment equipment;
    public Float value = 0F;
    public User owner;
    public List<JsonArray> charts = new ArrayList<JsonArray>();
}
