package models;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import java.util.Date;

/**
 * Created by Personal on 1/20/2015.
 */
@Entity(name = "tb_inventory_relationsupplier")
public class InventoryRelationSupplier extends Model{

    @Required
    @ManyToOne
    public InventorySupplier supplier;

    @Required
    @ManyToOne
    public Inventory inventory;

    @Required
    @ManyToOne
    public User receiver;


    @Required
    public Double quantity;

    @Required
    public Long price;

    @Required
    public Date date;

    public String supplier_partnumber;

    @Required
    @ManyToOne
    public InventoryLocation location;



}
