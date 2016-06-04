package models;


import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

/**
 * Created by Personal on 2015-01-12.
 */
@Entity(name = "tb_inventory_measure")
public class InventoryMeasure extends Model {
    @Required
    public String measure;


    @OneToMany(mappedBy = "inventoryMeasure", cascade = CascadeType.PERSIST)
    public List<Inventory> inventories;

}
