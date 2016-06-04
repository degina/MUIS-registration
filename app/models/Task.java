package models;

import controllers.CRUD;
import controllers.Functions;
import controllers.Users;
import org.joda.time.DateTime;
import play.data.validation.Max;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by enkhamgalan on 12/20/14.
 */
@Entity(name = "tb_task")
public class Task extends Model {

    public String code;

    @Required
    public String name;

    public Date startDate;

    @CRUD.Hidden
    public Long duration;

    @Max(100)
    public Float scopePercent;

    public Date actualFinish;

    public Date finishDate;

    public String status;

    public String depends = "";

    @CRUD.Hidden
    public Long level;

    public boolean hasChild = false;

    public boolean startIsMilestone = false;

    @Max(100)
    @CRUD.Hidden
    public Float completedPercent = 0.0f;

    @CRUD.Hidden
    @Required
    @ManyToOne
    public User owner;

    @CRUD.Hidden
    public String workCount;

    @ManyToOne
    @CRUD.Hidden
    public ProjectObject projectObject;

    @ManyToOne
    @CRUD.Hidden
    public Task task;

    @CRUD.Hidden
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    public List<Task> tasks;

    @CRUD.Hidden
    @ManyToOne
    public Floor floor;

    @CRUD.Hidden
    public int orderGantt;

    @CRUD.Hidden
    @OneToOne(mappedBy = "task", cascade = CascadeType.ALL)
    public ReminderModel reminderModel;

    @CRUD.Hidden
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    public List<TaskAssignRel> taskAssignRels;

    @CRUD.Hidden
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    public List<MeetingTopic> meetingTopics;

    @CRUD.Hidden
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    public List<RFI> rfis;

    @CRUD.Hidden
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    public List<PunchList> punchLists;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    public List<TaskInventoryRel> taskInventoryRels;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    public List<TaskManPowerRel> taskManPowerRels;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    public List<TaskEquipmentRel> taskEquipmentRels;

    @CRUD.Hidden
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    public List<DailyLogScheduledWork> dailyLogScheduledWorks;

    @CRUD.Hidden
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    public List<DailyLogWeather> weathers;

    @CRUD.Hidden
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    public List<DailyLogTechnicalDelay> technicalDelays;

    @CRUD.Hidden
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    public List<DailyLogDelivery> deliveries;

    @CRUD.Hidden
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    public List<DailyLogDumpster> dailyLogDumpsters;

    @CRUD.Hidden
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    public List<DailyLogInspection> inspections;

    @CRUD.Hidden
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    public List<DailyLogNote> notesLog;

    @CRUD.Hidden
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    public List<DailyLogSanaachlaga> sanaachlagas;

    @CRUD.Hidden
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    public List<DailyLogSafety> dailyLogSafeties;

    @CRUD.Hidden
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    public List<DailyLogVisitor> visitors;

    @CRUD.Hidden
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    public List<DailyLogWaste> dailyLogWastes;

    @CRUD.Hidden
    @OneToMany(mappedBy = "taskRel", cascade = CascadeType.ALL)
    public List<DailyLogMyPlan> myPlans;

    @CRUD.Hidden
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    public List<PercentHistoryTask> percentHistoryTasks;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    public List<ReportSummary> reportSummaries;

    @CRUD.Hidden
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    public List<GalleryPicture> galleryPictures;

    @CRUD.Hidden
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    public List<InventoryOrder> inventoryOrders;

    public List<Task> getChildTasks() {
        return Task.find("task.id=?1 AND hasChild=false order by orderGantt", this.id).fetch();
    }

    public void setValueCompletedPercent(float newPercent) {
        Task currentTask = this;
        currentTask.completedPercent = newPercent;
        float percent;
        while (currentTask.task != null) {
            percent = 0;
            for (Task task1 : currentTask.task.tasks) {
                percent += (task1.completedPercent * task1.scopePercent / 100);
            }
            currentTask.task.completedPercent = percent;
            currentTask.task._save();
            currentTask = currentTask.task;
        }
        this.projectObject.setCompletedPercent();
        this.projectObject.project.setCompletedPercent();
    }

    public String continuedDuration(Date date) {
        String value;
        if (this.actualFinish.getTime() <= this.finishDate.getTime()) { // хоцроогүй
            date = new Date();
            if (this.completedPercent >= 100 || date.getTime() <= this.startDate.getTime())
                value = "<span class='c-green'>" + this.duration + "</span>";
            else
                value = "<span class='c-green'>" + this.duration + " - " + Functions.getDifferenceWorkDays(this.projectObject.project, this.startDate, date) + "</span>";
        } else
            value = "<span class='c-red'>" + this.duration + " + " + Functions.getDifferenceWorkDays(this.projectObject.project, this.finishDate, this.actualFinish) + "</span>";
        return value;
    }

    public String getParentsName(String spec, boolean before) {
        Task curTask = this.task;
        String taskNames = "";
        while (curTask != null) {
            if (before) taskNames = spec + curTask.name + taskNames;
            else taskNames = curTask.name + spec + taskNames;
            curTask = curTask.task;
        }
        return taskNames;
    }

    public List<DailyLogWeather> getWeathers(Date date) {
        return DailyLogWeather.find("date=?1 AND task.id=?2 AND owner.id=?3", date, this.id, Users.getUser().id).fetch();
    }

    public List<DailyLogTechnicalDelay> getTechnicalDelays(Date date) {
        return DailyLogTechnicalDelay.find("date=?1 AND task.id=?2 AND owner.id=?3", date, this.id, Users.getUser().id).fetch();
    }

    public List<DailyLogDelivery> getDeliveries(Date date) {
        return DailyLogDelivery.find("date=?1 AND task.id=?2 AND owner.id=?3", date, this.id, Users.getUser().id).fetch();
    }

    public List<DailyLogInspection> getInspections(Date date) {
        return DailyLogInspection.find("date=?1 AND task.id=?2 AND owner.id=?3", date, this.id, Users.getUser().id).fetch();
    }

    public List<DailyLogSanaachlaga> getSanaachlagas(Date date) {
        return DailyLogSanaachlaga.find("date=?1 AND task.id=?2 AND owner.id=?3", date, this.id, Users.getUser().id).fetch();
    }

    public List<DailyLogNote> getNotesLog(Date date) {
        return DailyLogNote.find("date=?1 AND task.id=?2 AND owner.id=?3", date, this.id, Users.getUser().id).fetch();
    }

    public List<DailyLogVisitor> getVisitors(Date date) {
        return DailyLogVisitor.find("date=?1 AND task.id=?2 AND owner.id=?3", date, this.id, Users.getUser().id).fetch();
    }

    public List<DailyLogSafety> getSafeties(Date date) {
        return DailyLogSafety.find("date=?1 AND task.id=?2 AND owner.id=?3", date, this.id, Users.getUser().id).fetch();
    }

    public List<DailyLogDumpster> getDumpsters(Date date) {
        return DailyLogDumpster.find("date=?1 AND task.id=?2 AND owner.id=?3", date, this.id, Users.getUser().id).fetch();
    }

    public List<DailyLogWaste> getWastes(Date date) {
        return DailyLogWaste.find("date=?1 AND task.id=?2 AND owner.id=?3", date, this.id, Users.getUser().id).fetch();
    }

    public DailyLogScheduledWork getScheduledWork(Date date) {
        return DailyLogScheduledWork.find("date=?1 AND task.id=?2", date, this.id).first();
    }

    public void forseDelete() {
        /*
        for (TaskAssignRel taskAssignRel : this.taskAssignRels) taskAssignRel._delete();
        for (MeetingTopic meetingTopic : this.meetingTopics) meetingTopic._delete();
        for (RFI rfi : this.rfis) rfi._delete();
        for (PunchList punchList : this.punchLists) punchList._delete();
        for (TaskInventoryRel taskInventoryRel : this.taskInventoryRels) taskInventoryRel._delete();
        for (TaskManPowerRel taskManPowerRel : this.taskManPowerRels) taskManPowerRel._delete();
        for (TaskEquipmentRel taskEquipmentRel : this.taskEquipmentRels) taskEquipmentRel._delete();
        for (DailyLogScheduledWork dailyLogScheduledWork : this.dailyLogScheduledWorks) dailyLogScheduledWork._delete();
        public List<DailyLogWeather> weathers;
        public List<DailyLogTechnicalDelay> technicalDelays;
        public List<DailyLogDelivery> deliveries;
        public List<DailyLogInspection> inspections;
        public List<DailyLogNote> notesLog;
        public List<DailyLogSanaachlaga> sanaachlagas;
        public List<DailyLogVisitor> visitors;
        public List<DailyLogMyPlan> myPlans;
        public List<PercentHistoryTask> percentHistoryTasks;
        */
    }
}
