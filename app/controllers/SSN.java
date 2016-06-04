package controllers;

import models.SSN_category;
import models.SSN_post;
import models.SSN_user;
import play.mvc.Before;
import java.util.Date;

import java.util.List;

/**
 * Created by Personal on 11/9/2015.
 */
public class SSN extends CRUD {
    @Before
    static void setConnectedUser() {
        if (session.contains("email")) {
            SSN_user user = SSN_user.find("email=?1", session.get("email")).first();
            renderArgs.put("user", user);
        }
    }
    public static void list() {
        List<SSN_post> postList= SSN_post.findAll();
        render(postList);
    }

    public static void profile() {
        render();
    }

    public static void single(Long id) {
        SSN_post post= SSN_post.findById(id);
        render(post);
    }

    public static void login(String message) {
        render(message);
    }

    public static void signup(String message) {
        render(message);
    }

    public static void postnew() {
        List<SSN_category> categoryList = SSN_category.findAll();
        render(categoryList);
    }

    public static void addpost(String title,String content,int category) {
        SSN_category cat= SSN_category.findById(new Long(category));
        SSN_post post = new SSN_post();
        post.title=title;
        post.content=content;
        post.category=cat;
        post.create_date= new Date();
        post.user=(SSN_user)renderArgs.get("user");
        post.create();
        list();
    }

    public static void newuser(String name,String email,String password) {
        SSN_user user = SSN_user.find("email=?1",email).first();
        if(user!=null){
            signup("E-mail already registered.");
        } else {
            user = new SSN_user();
            user.email=email;
            user.name=name;
            user.password=password;
            user.create();
            list();
        }
    }

    public static void checkuser(String email,String password) {
        SSN_user user = SSN_user.find("email=?1 AND password=?2",email,password).first();
        if(user==null){
            login("E-mail or password incorrect.");
        } else {
            session.put("email", email);
            list();
        }
    }
}
