package controllers;

import models.User;
import play.mvc.With;

/**
 * Created by enkhamgalan on 5/16/15.
 */
@With(Secure.class)
public class MainRoot extends CRUD {
    public static void root() {
//        User user = Users.getUser();
//        System.out.println("CompanyConf.name: "+CompanyConf.name);
        if (CompanyConf.name.equals("Hyundai")) PortfoliosHyundai.list();
        else Portfolios.list();
    }
}
