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
@Entity(name = "tb_inventory_attachment")
public class InventoryAttachment extends Model {
    @Required
    public String filename;

    @Required
    public String filedir;

    @Required
    public String extension;

    @Required
    public Float filesize;

    @ManyToOne
    public Inventory inventory;
}
