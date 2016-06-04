package controllers;

import controllers.CRUD;
import play.mvc.With;

/**
 * Created by enkhamgalan on 3/22/15.
 */
@With(Secure.class)
@Check(Consts.permissionMap)
public class Maps extends CRUD {
    public static void show(){
        render();
    }
}
