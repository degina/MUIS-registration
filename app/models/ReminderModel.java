package models;

import controllers.CRUD;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by Enkh-Amgalan on 3/28/15.
 */
@Entity(name = "tb_remindermodel")
public class ReminderModel extends Model {

    @Required
    public Date reminderDate;

    @Required
    public String mainType;

    public String subType;

    @Required
    public String title;

    @OneToMany(mappedBy = "reminderModel", cascade = CascadeType.ALL)
    public List<ReminderUser> reminderUsers;

    @ManyToOne
    public Contract contract;

    public Long count; // contract honog

    @ManyToOne
    public Meeting meeting;

    @ManyToOne
    public RFI rfi;

    @OneToOne
    public InventoryOrder order;

    @OneToOne
    public Task task;

    @OneToOne
    public DailyLogMyPlan myPlan;

    public void addUser(User user) {
        ReminderUser reminderUser = new ReminderUser();
        reminderUser.reminderModel = this;
        reminderUser.user = user;
        reminderUsers.add(reminderUser);
    }
}
