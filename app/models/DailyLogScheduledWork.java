package models;

import controllers.CRUD;
import controllers.Users;
import play.data.validation.Max;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by Enkh-Amgalan on 2/13/15.
 */
@Entity(name = "tb_dailyLog_scheduledWork")
public class DailyLogScheduledWork extends Model{

    public Date date;

    @Required
    public String typeWork;

    public Long totalWorkers=0L;

    public Float totalManHours=0F;

    public Float totalMotHours=0F;

    @Max(100)
    public Float completedPercent = 0f;

    @Max(100)
    public Float progressPercent = 0f;

    public Long tempId=0l;

    @ManyToOne
    public Task task;

    @ManyToOne
    public DailyLogMyPlan myPlan;

    @CRUD.Hidden
    @OneToMany(mappedBy = "scheduledWork", cascade = CascadeType.ALL)
    public List<DailyLogWorkNote> workNotes;

    @CRUD.Hidden
    @OneToMany(mappedBy = "scheduledWork", cascade = CascadeType.ALL)
    public List<DailyLogEquipment> equipments;

    @CRUD.Hidden
    @OneToMany(mappedBy = "scheduledWork", cascade = CascadeType.ALL)
    public List<DailyLogManpower> manpowers;

    @CRUD.Hidden
    @OneToMany(mappedBy = "scheduledWork", cascade = CascadeType.ALL)
    public List<DailyLogMaterial> materials;

    public DailyLogScheduledWork(String typeWork,Float completedPercent,Date date,Task task){
        this.completedPercent = completedPercent;
        this.task=task;
        this.date=date;
        this.typeWork = typeWork;
    }
    public DailyLogScheduledWork(String typeWork,Float completedPercent, Date date,DailyLogMyPlan myPlan){
        this.completedPercent=completedPercent;
        this.typeWork =typeWork;
        this.date=date;
        this.myPlan=myPlan;
    }

    public DailyLogWorkNote getMyWorkNote(){
        return DailyLogWorkNote.find("owner.id=?1 AND scheduledWork.id=?2", Users.getUser().id,this.id).first();
    }
    public List<DailyLogWorkNote> getContractorWorkNote(){
        return DailyLogWorkNote.find("owner.userTeam.contractor=1 AND scheduledWork.id=?1",this.id).fetch();
    }
    public Long countMaterial(){
        if (Users.getUser().userTeam.contractor)
            return DailyLogMaterial.count("owner.id=?1 AND scheduledWork.id=?2",Users.getUser().id,this.id);
        else
            return DailyLogMaterial.count("owner.userTeam.contractor=0 AND scheduledWork.id=?1",this.id);
    }
    public Long countManPower(){
        if (Users.getUser().userTeam.contractor)
            return DailyLogManpower.count("owner.id=?1 AND scheduledWork.id=?2",Users.getUser().id,this.id);
        else
            return DailyLogManpower.count("owner.userTeam.contractor=0 AND scheduledWork.id=?1",this.id);
    }
    public Long countEquipment(){
        if (Users.getUser().userTeam.contractor)
            return DailyLogEquipment.count("owner.id=?1 AND scheduledWork.id=?2",Users.getUser().id,this.id);
        else
            return DailyLogEquipment.count("owner.userTeam.contractor=0 AND scheduledWork.id=?1",this.id);
    }
}
