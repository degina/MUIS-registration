package models;

import controllers.CRUD;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

/**
 * Created by Personal on 2015-01-11.
 */
@Entity(name = "tb_inventory_category")
public class InventoryCategory extends Model {

    @Required
    public String name;

    @CRUD.Hidden
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    public List<InventorySubCategory> subCategories;

}
