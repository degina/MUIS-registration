package controllers.ReportClass;

import com.google.gson.JsonArray;
import models.Inventory;
import models.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by enkhamgalan on 4/13/15.
 */
public class ReportMaterial {
    public Inventory inventory;
    public Float value = 0F;
    public User owner;
    public List<JsonArray> charts = new ArrayList<JsonArray>();
}
