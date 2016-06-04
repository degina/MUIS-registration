package controllers.MyClass;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Enkh-Amgalan
 * Date: 1/24/15
 * Time: 12:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class GanttJson {

    @Expose
    public String id = "-1";
    @Expose
    public String name = "";
    @Expose
    public Float progress = 0f;
    @Expose
    public String description = "";
    @Expose
    public String code = "";
    @Expose
    public int level = 0;
    @Expose
    public String status = "";
    @Expose
    public String depends = "";
    @Expose
    public boolean canWrite = true;
    @Expose
    public String startDate;
    @Expose
    public int duration = 1;
    @Expose
    public Float scopePercent = 0F;
    @Expose
    public String endDate;
    @Expose
    public boolean startIsMilestone = false;
    @Expose
    public boolean endIsMilestone = false;
    @Expose
    public boolean collapsed = false;
    @Expose
    public String assign = "";
    @Expose
    public List<Long> assignIds = new ArrayList<Long>();
    @Expose
    public String floor = "";
    @Expose
    public boolean hasChild = false;
    @Expose
    public String workCount = "";
    @Expose
    public String person="";
    @Expose
    public String material="";
    @Expose
    public String technical="";

    public String toJsonString() {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return gson.toJson(this);
    }
}
