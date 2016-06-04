package models;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

/**
 * Created by Personal on 1/20/2015.
 */
@Entity(name = "tb_inventory_relation")
public class InventoryRelation extends Model{

    @Required
    @ManyToOne
    public InventoryLocation location;

    @Required
    public Double quantity;

    @Required
    public Double idealQuantity;

    @Required
    public Double warningQuantity;

    @Required
    @ManyToOne
    public Inventory inventory;

}
