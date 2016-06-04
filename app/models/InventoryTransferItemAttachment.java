package models;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Created by Personal on 2015-01-12.
 */
@Entity(name = "tb_inventory_transfer_item_attachment")
public class InventoryTransferItemAttachment extends Model {
    @Required
    public String filename;

    @Required
    public String filedir;

    @Required
    public String extension;

    @Required
    public Float filesize;

    @ManyToOne
    public InventoryTransferItem transferItem;
}
