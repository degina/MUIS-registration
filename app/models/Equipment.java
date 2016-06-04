package models;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.List;

/**
 * Created by enkhamgalan on 4/11/15.
 */
@Entity(name = "tb_equipment")
public class Equipment extends Model {

    @Required
    public String name;

    @ManyToOne
    public Project project;

    @OneToMany(mappedBy = "equipment", cascade = CascadeType.ALL)
    public List<TaskEquipmentRel> taskEquipmentRels;

    @OneToMany(mappedBy = "equipmentType", cascade = CascadeType.ALL)
    public List<DailyLogEquipment> dailyLogEquipments;

}
