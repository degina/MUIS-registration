package controllers;

import play.mvc.With;

/**
 * Created by enkhamgalan on 3/22/15.
 */
@With(Secure.class)
@Check(Consts.permissionForum)
public class Forums extends CRUD {
    public static void list(){
        render();
    }
    public static void answer(){
        render();
    }
}
