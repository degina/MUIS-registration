package models;

import controllers.CRUD;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.List;

/**
 * Created by Personal on 1/17/2015.
 */
@Entity(name = "tb_inventory_supplier")
public class InventorySupplier extends Model{

    @Required
    public String name;

    @Required
    public String person;

    @Required
    @Column(length = 65535)
    public String address;

    @Required
    public int phone;

    public int phone_alternative;

    public String email;

    public String website;

    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL)
    public List<InventoryRelationPrice> priceList;

    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL)
    public List<InventoryRelationSupplier> relationSupplierList;



}
