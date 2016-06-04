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
@Entity(name = "tb_dailylog_inspection")
public class DailyLogInspection extends Model {

    @Required
    public Date startHour;

    @Required
    public Date finishHour;

    @Required
    public String inspectionType;

    public String inspectorName;

    public String area;

    @Column(length = 65535)
    public String comments;

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
    @OneToMany(mappedBy = "inspection", cascade = CascadeType.ALL)
    public List<DailyLogInspectionAttach> attaches;

    public DailyLogInspection(Date startHour, Date finishHour, String inspectionType, String inspectorName, String area, String comments, Date date, User owner, Task task) {
        this.startHour = startHour;
        this.finishHour = finishHour;
        this.inspectionType = inspectionType;
        this.inspectorName = inspectorName;
        this.area = area;
        this.comments = comments;
        this.date = date;
        this.owner = owner;
        this.task = task;
    }

    public DailyLogInspection(Date startHour, Date finishHour, String inspectionType, String inspectorName, String area, String comments, Date date, User owner, DailyLogMyPlan myPlan) {
        this.startHour = startHour;
        this.finishHour = finishHour;
        this.inspectionType = inspectionType;
        this.inspectorName = inspectorName;
        this.area = area;
        this.comments = comments;
        this.date = date;
        this.owner = owner;
        this.myPlan = myPlan;
    }
}
