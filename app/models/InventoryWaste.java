package models;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by Personal on 5/3/2015.
 */
@Entity(name = "tb_inventory_waste")
public class InventoryWaste extends Model {
    @Required
    @ManyToOne
    public Inventory item;

    @Required
    public Double quantity;

    @Required
    @ManyToOne
    public User waster;

    @Column(length = 65535)
    public String note;

    @Required
    public Date date;

    @Required
    @ManyToOne
    public InventoryLocation location;

    @OneToMany(mappedBy = "waste", cascade = CascadeType.ALL)
    public List<InventoryWasteAttachment> attachments;


}
