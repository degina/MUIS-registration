package controllers.ReportClass;

import com.google.gson.JsonArray;
import models.Inventory;
import models.ManPower;
import models.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by enkhamgalan on 4/13/15.
 */
public class ReportManPower {
    public ManPower manPower;
    public Float value = 0F;
    public int workers = 0;
    public User owner;
    public List<JsonArray> charts = new ArrayList<JsonArray>();
}
