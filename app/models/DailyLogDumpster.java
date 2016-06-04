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
@Entity(name = "tb_dailylog_dumpster")
public class DailyLogDumpster extends Model {

    @Required
    public Long quantity=0L;

    @Required
    public String location;

    @Column(length = 65535)
    public String notes;

    public Long tempId=0l;

    @Required
    @ManyToOne
    public Inventory material;

    @Required
    public Date date;

    @Required
    @ManyToOne
    public User owner;

    @Required
    @ManyToOne
    public Task task;

    @Required
    @ManyToOne
    public DailyLogMyPlan myPlan;

    @CRUD.Hidden
    @OneToMany(mappedBy = "dumpster", cascade = CascadeType.ALL)
    public List<DailyLogDumpsterAttach> attaches;

    public DailyLogDumpster(Long quantity, String location, String notes, Inventory material, Date date, User owner, Task task) {
        this.quantity = quantity;
        this.location = location;
        this.notes = notes;
        this.material = material;
        this.date = date;
        this.owner = owner;
        this.task = task;
    }
    public DailyLogDumpster(Long quantity, String location, String notes, Inventory material, Date date, User owner, DailyLogMyPlan myPlan) {
        this.quantity = quantity;
        this.location = location;
        this.notes = notes;
        this.material = material;
        this.date = date;
        this.owner = owner;
        this.myPlan = myPlan;
    }
}
