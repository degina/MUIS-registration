package controllers;

import models.AccountSetting;
import models.ThemeColor;
import models.User;
import play.mvc.With;

import java.util.List;

/**
 * Created by enkhamgalan on 2/25/15.
 */
@With(Secure.class)
public class AccountSettings extends CRUD {
    public static void panel(Long uid) {
        User user = Users.getUser();
        if (user.id.compareTo(uid) != 0) forbidden();
        List<ThemeColor> themeColors = ThemeColor.findAll();
        render(themeColors, uid);
    }

    public static void saveSettings(Long uid, Long theme) {
        User user = Users.getUser();
        if (user.id.compareTo(uid) != 0) forbidden();
        if (user.accountSetting == null) {
            user.accountSetting = new AccountSetting();
            user.accountSetting.user = user;
            user.accountSetting.themeColor = ThemeColor.findById(theme);
            user.accountSetting.create();
        } else {
            user.accountSetting.themeColor = ThemeColor.findById(theme);
            user.accountSetting._save();
        }
        panel(uid);
    }

    public static void saveMenu(String menu, String next) {
        User user = Users.getUser();
        if (user.accountSetting == null) {
            user.accountSetting = new AccountSetting();
            user.accountSetting.user = user;
            user.accountSetting.themeColor = ThemeColor.findById(1L);
            user.accountSetting.menu = menu;
            user.accountSetting.menuNext = next;
            user.accountSetting.create();
        } else {
            user.accountSetting.menu = menu;
            user.accountSetting.menuNext = next;
            user.accountSetting._save();
        }
    }
}
