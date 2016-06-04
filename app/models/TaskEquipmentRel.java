package models;

import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Created by enkhamgalan on 4/11/15.
 */
@Entity(name = "tb_taskequipmentrel")
public class TaskEquipmentRel extends Model {
    @ManyToOne
    public Task task;

    @ManyToOne
    public Equipment equipment;

    public Float value;
}
