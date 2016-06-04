package controllers;

import org.apache.commons.mail.SimpleEmail;
import play.libs.Mail;
import play.mvc.Mailer;

import java.util.Random;

/**
 * Created by enkhamgalan on 05/19/15.
 */
public class MailClient extends Mailer {
    public static void message(String address, String subject, String message) {
        try {

            SimpleEmail email = new SimpleEmail();
            email.setFrom("admin@progex.mn");
            email.addTo(address);
            email.setSubject(subject);
            email.setCharset("utf-8");
//            message += "\nНэвтрэх холбоос: " + CompanyConf.url;
            email.setMsg(message);
            Mail.send(email);
        } catch (Exception e) {
            System.out.println("Mail Error...");
            System.out.println(e);
        }
    }

    public static String randomPassword() {
        Random generator = new Random();
        int random;
        String value = "";
        for (int i = 0; i < 6; i++) {
            random = generator.nextInt(10);
            value += random + "";
        }
        return value;
    }
}
