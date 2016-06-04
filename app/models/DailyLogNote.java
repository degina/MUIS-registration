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
@Entity(name = "tb_dailylog_note")
public class DailyLogNote extends Model {

    @Required
    public Long issue;

    @Required
    @Column(length = 65535)
    public String comment;

    public Long tempId=0l;

    @Required
    public Date date;

    @Required
    @ManyToOne
    public User owner;

    @ManyToOne
    public Task task;

    @ManyToOne
    public DailyLogMyPlan myPlan;

    @CRUD.Hidden
    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL)
    public List<DailyLogNoteAttach> attaches;

    public DailyLogNote(Long issue, String comment, Date date, User owner, Task task) {
        this.issue = issue;
        this.comment = comment;
        this.date = date;
        this.owner = owner;
        this.task = task;
    }

    public DailyLogNote(Long issue, String comment, Date date, User owner, DailyLogMyPlan myPlan) {
        this.issue = issue;
        this.comment = comment;
        this.date = date;
        this.owner = owner;
        this.myPlan = myPlan;
    }
}
