package models;

import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Created by enkhamgalan on 4/10/15.
 */
@Entity(name = "tb_taskinventoryrel")
public class TaskInventoryRel extends Model {

    @ManyToOne
    public Task task;

    @ManyToOne
    public Inventory inventory;

    public Float value;
}
