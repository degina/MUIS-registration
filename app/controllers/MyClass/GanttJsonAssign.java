package controllers.MyClass;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.annotations.Expose;

/**
 * Created with IntelliJ IDEA.
 * User: Enkh-Amgalan
 * Date: 1/24/15
 * Time: 12:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class GanttJsonAssign {

    @Expose
    public String id = "0";
    @Expose
    public String resourceId = "";
//    @Expose
//    public String roleId = "tmp_1";
//    @Expose
//    public int effort=0;

    public String toJsonString() {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return gson.toJson(this);
    }
}
