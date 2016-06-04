package models;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Created by Personal on 3/7/2015.
 */
@Entity(name = "tb_inventory_relationprice")
public class InventoryRelationPrice extends Model {

    @Required
    @ManyToOne
    public InventorySupplier supplier;

    public Long price;


    @Required
    @ManyToOne
    public Inventory inventory;

    public String supplier_partnumber;
}
