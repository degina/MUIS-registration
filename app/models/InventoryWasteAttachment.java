package models;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Created by Personal on 2015-01-12.
 */
@Entity(name = "tb_inventory_waste_attachment")
public class InventoryWasteAttachment extends Model {
    @Required
    public String filename;

    @Required
    public String filedir;

    @Required
    public String extension;

    @Required
    public Float filesize;

    @ManyToOne
    public InventoryWaste waste;
}
