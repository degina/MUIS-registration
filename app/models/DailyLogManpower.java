package models;

import controllers.CRUD;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.List;

/**
 * Created by Enkh-Amgalan on 2/13/15.
 */
@Entity(name = "tb_dailylog_manpower")
public class DailyLogManpower extends Model {

    public Long workers = 0L;

    public Float hours = 0F;

    public Float manHours = 0F;

    public String location;

    @Column(length = 65535)
    public String note;

    public Long tempId = 0l;

    @Required
    @ManyToOne
    public OrganizationTeam orgTeam;

    @Required
    @ManyToOne
    public DailyLogScheduledWork scheduledWork;

    @Required
    @ManyToOne
    public ManPower mergejil;

    @Required
    @ManyToOne
    public User owner;

    @CRUD.Hidden
    @OneToMany(mappedBy = "manpower", cascade = CascadeType.ALL)
    public List<DailyLogManpowerAttach> attaches;

    @Transient
    public float c_workers = 0;
    public float c_manHours = 0;

    public DailyLogManpower(OrganizationTeam orgTeam, ManPower mergejil, Long workers, Float hours, Float manHours,
                            String location, String note, DailyLogScheduledWork scheduledWork, User owner) {
        this.orgTeam = orgTeam;
        this.mergejil = mergejil;
        this.workers = workers;
        this.hours = hours;
        this.manHours = manHours;
        this.location = location;
        this.note = note;
        this.owner = owner;
        this.scheduledWork = scheduledWork;
    }

    public void setDailyLogManpower(OrganizationTeam orgTeam, ManPower mergejil, Long workers, Float hours, Float manHours,
                                    String location, String note, DailyLogScheduledWork scheduledWork, User owner) {
        this.orgTeam = orgTeam;
        this.mergejil = mergejil;
        this.workers = workers;
        this.hours = hours;
        this.manHours = manHours;
        this.location = location;
        this.note = note;
        this.owner = owner;
        this.scheduledWork = scheduledWork;
    }
}
