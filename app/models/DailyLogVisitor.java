package models;

import controllers.CRUD;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by Enkh-Amgalan on 2/14/15.
 */
@Entity(name = "tb_dailylog_visitor")
public class DailyLogVisitor extends Model {
    @Required
    public String visitor;

    public String fromVisitor;

    @Required
    public Date startHour;

    @Required
    public Date endHour;

    @Column(length = 65535)
    public String description;

    @Required
    public Date date;

    @Required
    @ManyToOne
    public User owner;

    @ManyToOne
    public Task task;

    public Long tempId=0l;

    @ManyToOne
    public DailyLogMyPlan myPlan;

    @CRUD.Hidden
    @OneToMany(mappedBy = "visitor", cascade = CascadeType.ALL)
    public List<DailyLogVisitorAttach> attaches;

    public DailyLogVisitor(String visitor, String fromVisitor, Date startHour, Date endHour, String description, Date date, User owner, Task task) {
        this.visitor = visitor;
        this.fromVisitor = fromVisitor;
        this.startHour = startHour;
        this.endHour = endHour;
        this.description = description;
        this.date = date;
        this.owner = owner;
        this.task = task;
    }

    public DailyLogVisitor(String visitor, String fromVisitor, Date startHour, Date endHour, String description, Date date, User owner, DailyLogMyPlan myPlan) {
        this.visitor = visitor;
        this.fromVisitor = fromVisitor;
        this.startHour = startHour;
        this.endHour = endHour;
        this.description = description;
        this.date = date;
        this.owner = owner;
        this.myPlan = myPlan;
    }
}
