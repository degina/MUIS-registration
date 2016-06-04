package controllers;

import models.*;

import java.util.Date;

/**
 * Created by Personal on 10/19/2015.
 */
public class Tests extends CRUD {
    public static void index() {
        for (int i = 0; i < 100; i++) {
            Test t = new Test();
            t.col1 = "a";
            t.col2 = new Date();
            t.col3 = new Float(0.1);
            for (int j = 0; j < i / 10; j++)
                t.col1 = t.col1 + "a";
            for (int j = 0; j < i / 20; j++)
                t.col2.setDate(t.col2.getDate() + 1);
            for (int j = 0; j < i / 30; j++)
                t.col3 = t.col3 + new Float(0.1);
            t.create();
        }
    }
    public static void index1() {
        for (int i = 0; i < 100; i++) {
            Test t = new Test();
            t.col1 = "a";
            t.col2 = new Date();
            t.col3 = new Float(0.1);
            for (int j = 0; j < i / 10; j++)
                t.col1 = t.col1 + "a";
            for (int j = 0; j < i / 20; j++)
                t.col2.setDate(t.col2.getDate()+1);
            for (int j = 0; j < i / 30; j++)
                t.col3 = t.col3 + new Float(0.1);
            t.create();
            if(i%2==0){
                t.delete();
            }
        }
    }


}
