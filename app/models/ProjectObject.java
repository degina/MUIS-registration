package models;

import controllers.CRUD;
import controllers.Functions;
import play.data.validation.Max;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.xml.crypto.Data;
import java.util.Date;
import java.util.List;

/**
 * Created by enkhamgalan on 12/20/14.
 */
@Entity(name = "tb_project_object")
public class ProjectObject extends Model {

    public String code;

    @Required
    public String name;

    @CRUD.Hidden
    public Date startDate;

    @CRUD.Hidden
    public Long duration;

    @CRUD.Hidden
    public Date finishDate;

    public Date actualFinish;

    public String status;

    @Max(100)
    public Float scopePercent;

    public String depends = "";

    public boolean startIsMilestone = false;

    @Max(100)
    @CRUD.Hidden
    public Float completedPercent = 0.0f;

    @CRUD.Hidden
    @Required
    @ManyToOne
    public User owner;

    @CRUD.Hidden
    public int orderGantt;

    @CRUD.Hidden
    public String workCount;

    @Required
    @ManyToOne
    public Project project;

    @CRUD.Hidden
    @OneToMany(mappedBy = "projectObject", cascade = CascadeType.ALL)
    public List<Task> tasks;

    @CRUD.Hidden
    @OneToMany(mappedBy = "projectObject", cascade = CascadeType.ALL)
    public List<Floor> floors;

    @CRUD.Hidden
    @OneToMany(mappedBy = "projectObject", cascade = CascadeType.ALL)
    public List<ProjectObAssignRel> projectObAssignRels;

    @CRUD.Hidden
    @OneToMany(mappedBy = "projectObject", cascade = CascadeType.ALL)
    public List<PercentHistoryObject> percentHistoryObjects;

    public void setCompletedPercent() {
        float percent = 0;
        for (Task task1 : this.getChildTasks()) {
            percent += (task1.completedPercent * task1.scopePercent / 100);
        }
        this.completedPercent = percent;
        this._save();
    }

    public List<Task> getTasks() {
        return Task.find("projectObject.id=?1 order by orderGantt", this.id).fetch();
    }

    public List<Task> getChildTasks() {
        return Task.find("projectObject.id=?1 AND task=null order by orderGantt", this.id).fetch();
    }

    public String continuedDuration(Date date) {
        String value;
        if (this.actualFinish.getTime() <= this.finishDate.getTime()) { // хоцроогүй
            date = new Date();
            if (this.completedPercent >= 100 || date.getTime() <= this.startDate.getTime())
                value = "<span class='c-green'>" + this.duration + "</span>";
            else
                value = "<span class='c-green'>" + this.duration + " - " + Functions.getDifferenceWorkDays(this.project, this.startDate, date) + "</span>";
        } else
            value = "<span class='c-red'>" + this.duration + " + " + Functions.getDifferenceWorkDays(this.project, this.finishDate, this.actualFinish) + "</span>";
        return value;
    }

    public float getProjectObjectPercent(Date date) {
        PercentHistoryObject historyObject = PercentHistoryObject.find("date<?1 and projectObject.id=?2 order by date desc", date, this.id).first();
        if (historyObject != null) return historyObject.completedPercent;
        return -1;
    }
}
