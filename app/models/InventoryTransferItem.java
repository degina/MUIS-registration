package models;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by Personal on 3/15/2015.
 */
@Entity(name = "tb_inventory_transferitem")
public class InventoryTransferItem extends Model {

    @Required
    @ManyToOne
    public InventoryTransfer transfer;

    @Required
    public Double quantity;

    @Required
    @ManyToOne
    public Inventory inventory;

    @Column(length = 65535)
    public String note;

    @OneToMany(mappedBy = "transferItem", cascade = CascadeType.ALL)
    public List<InventoryTransferItemAttachment> attachments;

}
