package models;

import controllers.CRUD;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by Enkh-Amgalan on 2/25/15.
 */
@Entity(name = "tb_dailylog_sanaachlaga")
public class DailyLogSanaachlaga extends Model {

    @Required
    @Column(length = 65535)
    public String sanal;

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
    @OneToMany(mappedBy = "sanaachlaga", cascade = CascadeType.ALL)
    public List<DailyLogSanaachlagaAttach> attaches;

    public DailyLogSanaachlaga(String sanal, Date date, User owner, Task task) {
        this.sanal = sanal;
        this.date = date;
        this.owner = owner;
        this.task = task;
    }

    public DailyLogSanaachlaga(String sanal, Date date, User owner, DailyLogMyPlan myPlan) {
        this.sanal = sanal;
        this.date = date;
        this.owner = owner;
        this.myPlan = myPlan;
    }
}
