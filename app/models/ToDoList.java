package models;

import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * Created by enkhamgalan on 5/3/15.
 */
@Entity(name = "tb_todo_list")
public class ToDoList extends Model {

    @ManyToOne
    public User user;

    public String todo;

    public Date date;

    public boolean done = false;

    public boolean reminder = false;

    public Date reminderDate;
}
