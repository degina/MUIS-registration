package models;

import controllers.CRUD;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by Enkh-Amgalan on 3/27/15.
 */
@Entity(name = "tb_dailylog_technicaldelay")
public class DailyLogTechnicalDelay extends Model{

    @Required
    public Date startDate;

    @Required
    public Date finishDate;

    @Required
    public String typeWork;

    @Column(length = 65535)
    public String notes;

    @Required
    public String conditionTechnical;

    @Required
    public Date date;

    public Long tempId=0l;

    @Required
    @ManyToOne
    public User owner;

    @ManyToOne
    public Task task;

    @ManyToOne
    public DailyLogMyPlan myPlan;

    @CRUD.Hidden
    @OneToMany(mappedBy = "technicalDelay", cascade = CascadeType.ALL)
    public List<DailyLogTechnicalDelayAttach> attaches;

    public DailyLogTechnicalDelay(String conditionTechnical, Date startDate, Date finishDate, Date date,String notes, Task task ,User owner) {
        this.conditionTechnical = conditionTechnical;
        this.startDate = startDate;
        this.finishDate = finishDate;
        this.notes = notes;
        this.task = task;
        this.date=date;
        this.owner=owner;
    }
    public DailyLogTechnicalDelay(String conditionTechnical, Date startDate, Date finishDate, Date date,String notes, DailyLogMyPlan myPlan,User owner) {
        this.conditionTechnical = conditionTechnical;
        this.startDate = startDate;
        this.finishDate = finishDate;
        this.notes = notes;
        this.myPlan = myPlan;
        this.date=date;
        this.owner=owner;
    }
}