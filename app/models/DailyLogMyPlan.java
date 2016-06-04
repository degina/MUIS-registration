package models;

import controllers.CRUD;
import controllers.Functions;
import controllers.Users;
import play.data.validation.Max;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by Enkh-Amgalan on 2/23/15.
 */
@Entity(name = "tb_dailylog_myplan")
public class DailyLogMyPlan extends Model {

    @Required
    public String name;

    public Date startDate;

    public Date finishDate;

    public Date actualFinish;

    @Column(length = 65535)
    public String description;

    @Max(100)
    public Float completedPercent = 0f;

    @Required
    @ManyToOne
    public User owner;

    @Required
    @ManyToOne
    public Task taskRel;

    @Required
    @OneToOne(mappedBy = "myPlan", cascade = CascadeType.ALL)
    public ReminderModel reminderModel;
    @CRUD.Hidden
    @OneToMany(mappedBy = "myPlan", cascade = CascadeType.ALL)
    public List<DailyLogScheduledWork> logScheduledWorks;

    @CRUD.Hidden
    @OneToMany(mappedBy = "myPlan", cascade = CascadeType.ALL)
    public List<DailyLogWeather> weathers;

    @CRUD.Hidden
    @OneToMany(mappedBy = "myPlan", cascade = CascadeType.ALL)
    public List<DailyLogTechnicalDelay> technicalDelays;

    @CRUD.Hidden
    @OneToMany(mappedBy = "myPlan", cascade = CascadeType.ALL)
    public List<DailyLogDelivery> deliveries;

    @CRUD.Hidden
    @OneToMany(mappedBy = "myPlan", cascade = CascadeType.ALL)
    public List<DailyLogInspection> inspections;

    @CRUD.Hidden
    @OneToMany(mappedBy = "myPlan", cascade = CascadeType.ALL)
    public List<DailyLogNote> notesLog;

    @CRUD.Hidden
    @OneToMany(mappedBy = "myPlan", cascade = CascadeType.ALL)
    public List<DailyLogSanaachlaga> sanaachlagas;

    @CRUD.Hidden
    @OneToMany(mappedBy = "myPlan", cascade = CascadeType.ALL)
    public List<DailyLogVisitor> visitors;

    @CRUD.Hidden
    @OneToMany(mappedBy = "myPlan", cascade = CascadeType.ALL)
    public List<DailyLogDumpster> dumpsters;

    @CRUD.Hidden
    @OneToMany(mappedBy = "myPlan", cascade = CascadeType.ALL)
    public List<DailyLogSafety> safeties;

    @CRUD.Hidden
    @OneToMany(mappedBy = "myPlan", cascade = CascadeType.ALL)
    public List<DailyLogWaste> wastes;

    @OneToMany(mappedBy = "myPlan", cascade = CascadeType.ALL)
    public List<ReportSummary> reportSummaries;


    public DailyLogMyPlan(String name,String description, Date startDate, Date finishDate, Float completedPercent, User owner){
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.finishDate = finishDate;
        this.actualFinish = finishDate;
        this.completedPercent = completedPercent;
        this.owner = owner;
    }

    public DailyLogScheduledWork getScheduledWork(Date date) {
        return DailyLogScheduledWork.find("date=?1 AND myPlan.id=?2", date, this.id).first();
    }

    public String continuedDuration(Date date) {
        String value;
        if (date.getTime() < this.finishDate.getTime()) {
            if (this.completedPercent >= 100)
                value = "<span class='c-green'>" + Functions.getDifferenceWorkDays(null, this.startDate, this.finishDate) + " - " + Functions.getDifferenceWorkDays(null, this.actualFinish, this.finishDate) + "</span>";
            else
                value = "<span class='c-green'>" + Functions.getDifferenceWorkDays(null, this.startDate, date) + "</span>";
        } else {
            value = "<span class='c-red'>" + Functions.getDifferenceWorkDays(null, this.startDate, this.finishDate) + " + " + Functions.getDifferenceWorkDays(null, this.finishDate, date) + "</span>";
        }
        return value;
    }

    public List<DailyLogWeather> getWeathers(Date date) {
        return DailyLogWeather.find("date=?1 AND myPlan.id=?2 AND owner.id=?3", date, this.id, Users.getUser().id).fetch();
    }

    public List<DailyLogTechnicalDelay> getTechnicalDelays(Date date) {
        return DailyLogTechnicalDelay.find("date=?1 AND myPlan.id=?2 AND owner.id=?3", date, this.id, Users.getUser().id).fetch();
    }

    public List<DailyLogDelivery> getDeliveries(Date date) {
        return DailyLogDelivery.find("date=?1 AND myPlan.id=?2 AND owner.id=?3", date, this.id, Users.getUser().id).fetch();
    }

    public List<DailyLogInspection> getInspections(Date date) {
        return DailyLogInspection.find("date=?1 AND myPlan.id=?2 AND owner.id=?3", date, this.id, Users.getUser().id).fetch();
    }

    public List<DailyLogSanaachlaga> getSanaachlagas(Date date) {
        return DailyLogSanaachlaga.find("date=?1 AND myPlan.id=?2 AND owner.id=?3", date, this.id, Users.getUser().id).fetch();
    }

    public List<DailyLogNote> getNotesLog(Date date) {
        return DailyLogNote.find("date=?1 AND myPlan.id=?2 AND owner.id=?3", date, this.id, Users.getUser().id).fetch();
    }

    public List<DailyLogVisitor> getVisitors(Date date) {
        return DailyLogVisitor.find("date=?1 AND myPlan.id=?2 AND owner.id=?3", date, this.id, Users.getUser().id).fetch();
    }

    public List<DailyLogSafety> getSafeties(Date date) {
        return DailyLogSafety.find("date=?1 AND myPlan.id=?2 AND owner.id=?3", date, this.id, Users.getUser().id).fetch();
    }

    public List<DailyLogDumpster> getDumpsters(Date date) {
        return DailyLogDumpster.find("date=?1 AND myPlan.id=?2 AND owner.id=?3", date, this.id, Users.getUser().id).fetch();
    }

    public List<DailyLogWaste> getWastes(Date date) {
        return DailyLogWaste.find("date=?1 AND myPlan.id=?2 AND owner.id=?3", date, this.id, Users.getUser().id).fetch();
    }
}
