package controllers.MyClass;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
public class GanttJsonMain {

    @Expose
    public List<GanttJson> tasks = new ArrayList<GanttJson>();
    @Expose
    public int selectedRow = 0;

    @Expose
    public String response = "";

    @Expose
    public boolean canWrite = true;
    @Expose
    public boolean canWriteOnParent = true;

    public String toJsonString() {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return gson.toJson(this);
    }
}
