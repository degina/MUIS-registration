package models;

import controllers.CRUD;
import controllers.MyPlans;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by Enkh-Amgalan on 2/14/15.
 */
@Entity(name = "tb_dailylog_waste")
public class DailyLogWaste extends Model {

    @Required
    public Date date;

    @Required
    @ManyToOne
    public User owner;

    @Required
    public String subject;

    public String measure;

    public Long tempId=0l;

    public Long quantity;

    @Column(length = 65535)
    public String comments;

    @Required
    @ManyToOne
    public Task task;

    @Required
    @ManyToOne
    public DailyLogMyPlan myPlan;

    @CRUD.Hidden
    @OneToMany(mappedBy = "waste", cascade = CascadeType.ALL)
    public List<DailyLogWasteAttach> attaches;

    public DailyLogWaste(Date date, User owner, String subject, String measure, Long quantity, String comments, Task task) {
        this.date = date;
        this.owner = owner;
        this.subject = subject;
        this.measure = measure;
        this.quantity = quantity;
        this.comments = comments;
        this.task = task;

    }
    public DailyLogWaste(Date date, User owner, String subject, String measure, Long quantity, String comments, DailyLogMyPlan myPlan) {
        this.date = date;
        this.owner = owner;
        this.subject = subject;
        this.measure = measure;
        this.quantity = quantity;
        this.comments = comments;
        this.myPlan = myPlan;
    }
}
