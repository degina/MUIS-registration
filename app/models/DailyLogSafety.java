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
@Entity(name = "tb_dailylog_safety")
public class DailyLogSafety extends Model {
    @Required
    public Date safetyDate;

    public String safetyNotice;

    public String issuedTo;

    public String complianceDue;

    public Long tempId=0l;

    @Column(length = 65535)
    public String comments;

    @Required
    public Date date;

    @Required
    @ManyToOne
    public User owner;

    @Required
    @ManyToOne
    public Task task;

    @ManyToOne
    public DailyLogMyPlan myPlan;

    @CRUD.Hidden
    @OneToMany(mappedBy = "safety", cascade = CascadeType.ALL)
    public List<DailyLogSafetyAttach> attaches;

    public DailyLogSafety(Date safetyDate, String safetyNotice, String issuedTo, String complianceDue, String comments, Date date, User owner, Task task) {
        this.safetyDate = safetyDate;
        this.safetyNotice = safetyNotice;
        this.issuedTo = issuedTo;
        this.complianceDue = complianceDue;
        this.comments = comments;
        this.date = date;
        this.owner = owner;
        this.task = task;
    }

    public DailyLogSafety(Date safetyDate, String safetyNotice, String issuedTo, String complianceDue, String comments, Date date, User owner, DailyLogMyPlan myPlan) {
        this.safetyDate = safetyDate;
        this.safetyNotice = safetyNotice;
        this.issuedTo = issuedTo;
        this.complianceDue = complianceDue;
        this.comments = comments;
        this.date = date;
        this.owner = owner;
        this.myPlan = myPlan;
    }
}
