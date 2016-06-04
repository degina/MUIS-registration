package models;

import controllers.CRUD;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.List;

/**
 * Created by Personal on 2015-01-12.
 */
@Entity(name = "tb_inventory_subcategory")
public class InventorySubCategory extends Model {
    @Required
    public String name;

    @CRUD.Hidden
    @Required
    @ManyToOne
    public InventoryCategory category;

    @CRUD.Hidden
    @OneToMany(mappedBy = "inventorySubCategory", cascade = CascadeType.PERSIST)
    public List<Inventory> inventories;
}
