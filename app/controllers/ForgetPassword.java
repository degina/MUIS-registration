package controllers;

import models.User;
import play.mvc.Controller;

/**
 * Created by enkhamgalan on 1/22/15.
 */
public class ForgetPassword extends Controller {
    public static void forget() {
        render();
    }

    public static void reset(String email) {
        User user = User.find("email=?1", email).first();
        if (user != null && user.email != null && user.email.length() > 0) {
            String newPassword = MailClient.randomPassword();
            user.password = Functions.getSha1String(newPassword);
            user._save();
            MailClient.message(user.email, "Нууц үг солигдсон", "Нэвтрэх нэр: " + user.username.toLowerCase() + "\nШинэ нууц үг: " + newPassword);
            flash.success("Таны и-мэйл хаягруу шинэ нууц үг илгээгдсэн");
        } else flash.error("Таны и-мэйл хаяг бүртгэлгүй байна!");
        redirect("/forgetpassword/forget");
    }
}
