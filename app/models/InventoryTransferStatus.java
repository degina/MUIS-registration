package models;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Entity;

/**
 * Created by Personal on 3/15/2015.
 */
@Entity(name = "tb_inventory_transferstatus")
public class InventoryTransferStatus extends Model {
    @Required
    public String status;
}