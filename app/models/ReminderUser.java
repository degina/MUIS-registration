package models;

import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Created by enkhamgalan on 8/8/15.
 */
@Entity(name = "tb_reminder_user")
public class ReminderUser extends Model {

    @ManyToOne
    public User user;

    @ManyToOne
    public ReminderModel reminderModel;

}
