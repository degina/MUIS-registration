package models;

import controllers.CRUD;
import play.data.validation.Max;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by Enkh-Amgalan on 4/13/15.
 */
@Entity(name = "tb_dailyLog_workNote")
public class DailyLogWorkNote extends Model {

    @Required
    @ManyToOne
    public User owner;

    @Column(length = 65535)
    public String notes;

    public Float hours = 0f;

    public Long tempId=0l;

    public Long lastEdited;

    @Required
    @ManyToOne
    public DailyLogScheduledWork scheduledWork;

    @CRUD.Hidden
    @OneToMany(mappedBy = "workNote", cascade = CascadeType.ALL)
    public List<DailyLogWorkNoteAttach> attaches;

    public DailyLogWorkNote(User owner, DailyLogScheduledWork scheduledWork) {
        this.owner = owner;
        this.scheduledWork = scheduledWork;
        this.lastEdited= (new Date()).getTime();
    }
}
