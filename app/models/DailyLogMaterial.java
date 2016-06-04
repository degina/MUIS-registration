package models;

import controllers.CRUD;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by Enkh-Amgalan on 4/12/15.
 */
@Entity(name = "tb_dailylog_material")
public class DailyLogMaterial extends Model {

    @Required
    @ManyToOne
    public Inventory material;

    public Float amount=0F;

    @Column(length = 65535)
    public String note;

    public Long tempId=0l;

    public Long lastEdited;

    @Required
    @ManyToOne
    public User owner;

    @Required
    @ManyToOne
    public DailyLogScheduledWork scheduledWork;

    @CRUD.Hidden
    @OneToMany(mappedBy = "material", cascade = CascadeType.ALL)
    public List<DailyLogMaterialAttach> attaches;

    public DailyLogMaterial(Inventory material, Float amount, String note, User owner, DailyLogScheduledWork scheduledWork) {
        this.material = material;
        this.amount = amount;
        this.note = note;
        this.owner = owner;
        this.scheduledWork = scheduledWork;
        this.lastEdited = (new Date()).getTime();
    }

    public DailyLogMaterial(Inventory material, User owner, DailyLogScheduledWork scheduledWork) {
        this.material = material;
        this.owner = owner;
        this.scheduledWork = scheduledWork;
    }

    public void setDailyLogMaterial(Inventory material, Float amount, String note, User owner, DailyLogScheduledWork scheduledWork) {
        this.material = material;
        this.amount = amount;
        this.note = note;
        this.owner = owner;
        this.scheduledWork = scheduledWork;
    }

    public Long countContractors(){
        return DailyLogMaterial.count("material.id=?1 AND scheduledWork.id=?2 AND owner.userTeam.contractor=1",this.material.id,this.scheduledWork.id);
    }
    public List<DailyLogMaterial> getContractors(){
        return DailyLogMaterial.find("material.id=?1 AND scheduledWork.id=?2 AND owner.userTeam.contractor=1",this.material.id,this.scheduledWork.id).fetch();
    }
    public boolean checkHaruitsagch(){
        return DailyLogMaterial.count("material.id=?1 AND scheduledWork.id=?2 AND owner.userTeam.contractor=0",this.material.id,this.scheduledWork.id) == 0;
    }
}

