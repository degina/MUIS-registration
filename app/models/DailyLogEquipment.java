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
@Entity(name = "tb_dailylog_equipment")
public class DailyLogEquipment extends Model {

    @Required
    @ManyToOne
    public Equipment equipmentType;

    @Required
    public String operator;
    @Required
    public String location;

    @Required
    public Date startHour;

    @Required
    public Date finishHour;

    public Float motHours=0F;

    @Column(length = 65535)
    public String comments;

    public Long tempId=0l;

    @Required
    @ManyToOne
    public User owner;

    @Required
    @ManyToOne
    public DailyLogScheduledWork scheduledWork;

    @CRUD.Hidden
    @OneToMany(mappedBy = "equipment", cascade = CascadeType.ALL)
    public List<DailyLogEquipmentAttach> attaches;

    public DailyLogEquipment(Equipment equipmentType,String operator,String location,Date startHour,
                             Date finishHour,String comments,DailyLogScheduledWork scheduledWork,User owner){
        this.equipmentType = equipmentType;
        this.location=location;
        this.operator=operator;
        this.startHour=startHour;
        this.finishHour = finishHour;
        this.comments=comments;
        this.scheduledWork=scheduledWork;
        this.owner = owner;
        this.motHours= ((float)(finishHour.getTime() - startHour.getTime()))/(1000 * 60 * 60);
    }
    public void setDailyLogEquipment(Equipment equipmentType,String operator,String location,Date startHour,
                                     Date finishHour,String comments, DailyLogScheduledWork scheduledWork,User owner){
        this.equipmentType = equipmentType;
        this.location=location;
        this.operator=operator;
        this.startHour=startHour;
        this.finishHour = finishHour;
        this.comments=comments;
        this.scheduledWork=scheduledWork;
        this.owner = owner;
        this.motHours= ((float)(finishHour.getTime() - startHour.getTime()))/(1000 * 60 * 60);
    }
}
