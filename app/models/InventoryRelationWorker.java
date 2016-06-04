package models;

import play.db.jpa.Model;
import play.data.validation.Required;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Created by Personal on 8/19/2015.
 */
@Entity(name = "tb_inventory_relationworker")
public class InventoryRelationWorker extends Model {

    @Required
    @ManyToOne
    public User worker;

    @Required
    @ManyToOne
    public InventoryLocation location;

}
