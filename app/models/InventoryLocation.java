package models;

import controllers.CRUD;
import org.hibernate.annotations.ManyToAny;
import play.data.validation.Required;
import play.db.jpa.Model;
import javax.persistence.OneToOne;

import javax.persistence.*;
import java.util.List;

/**
 * Created by Personal on 1/17/2015.
 */
@Entity(name = "tb_inventory_location")
public class InventoryLocation extends Model {

    @Required
    @Column(length = 65535)
    public String address;

    @Required
    public String name;

    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL)
    public List<InventoryRelation> inventorys;

    @OneToMany(mappedBy = "to", cascade = CascadeType.ALL)
    public List<InventoryTransfer> tos;

    @OneToMany(mappedBy = "from", cascade = CascadeType.ALL)
    public List<InventoryTransfer> froms;

    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL)
    public List<InventoryOrder> orders;

    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL)
    public List<InventoryRelationSupplier> relationSupplierList;

    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL)
    public List<InventoryRelationWorker> relationWorkerList;

    public String is_main;

    @ManyToOne
    public Project project;

}
