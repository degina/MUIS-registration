package models;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

/**
 * Created by Nyamjantsan on 2/1/2015.
 */
@Entity(name = "tb_inventory_orderstatus")
public class InventoryOrderStatus extends Model {
    @Required
    public String status;
}
