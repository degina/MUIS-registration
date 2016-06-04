package models;

import play.db.jpa.Model;
import play.data.validation.Required;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;

/**
 * Created by Personal on 3/7/2015.
 */
@Entity(name = "tb_inventory_relationorder")
public class InventoryRelationOrder extends Model {

    @Required
    @ManyToOne
    public InventoryOrder order;

    @Required
    public Double quantity;

    @Required
    public Double approved;

    @Required
    public Double issued;

    @Column(length = 65535)
    public String orderer_comment;

    @Column(length = 65535)
    public String approver_comment;

    @Column(length = 65535)
    public String issuer_comment;

    @Required
    @ManyToOne
    public Inventory inventory;
}
