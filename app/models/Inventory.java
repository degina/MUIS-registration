package models;

import controllers.InventoryDeposits;
import controllers.Users;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.List;


/**
 * Created by Personal on 2015-01-11.
 */
@Entity(name = "tb_inventory")
public class Inventory extends Model {
    @Required
    public String item;

    @Column(length = 65535)
    public String description;

    @Required
    @ManyToOne
    public InventorySubCategory inventorySubCategory;

    @Required
    @OneToMany(mappedBy = "inventory", cascade = CascadeType.ALL)
    public List<InventoryRelation> locationList;


    @OneToMany(mappedBy = "inventory", cascade = CascadeType.ALL)
    public List<InventoryRelationPrice> priceList;

    @OneToMany(mappedBy = "inventory", cascade = CascadeType.ALL)
    public List<InventoryRelationOrder> inventoryOrderList;


    @OneToMany(mappedBy = "inventory", cascade = CascadeType.ALL)
    public List<InventoryTransferItem> transferItemList;

    @OneToMany(mappedBy = "inventory", cascade = CascadeType.ALL)
    public List<TaskInventoryRel> taskInventoryRels;

    @OneToMany(mappedBy = "inventory", cascade = CascadeType.ALL)
    public List<InventoryRelationSupplier> relationSupplierList;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
    public List<InventoryWaste> wasteList;

    @OneToMany(mappedBy = "material", cascade = CascadeType.ALL)
    public List<DailyLogMaterial> dailyLogMaterialList;

    @OneToMany(mappedBy = "material", cascade = CascadeType.ALL)
    public List<DailyLogDumpster> dailyLogDumpsterList;

    @Column(length = 65535)
    public String itemNote;

    public String website;

    @Required
    @ManyToOne
    public InventoryMeasure inventoryMeasure;

    @OneToMany(mappedBy = "inventory", cascade = CascadeType.ALL)
    public List<InventoryAttachment> inventoryAttachment;


    public Double quantity_sum(String id) {
        Double sum = new Double(0);

        Long ID = new Long(0);
        if (id != null && !id.equals("")) {
            ID = Long.parseLong(id);
        }
        if (id == null || ID == 0) {
            for (InventoryRelation inventoryRelation : locationList) {
                if (inventoryRelation.quantity != null)
                    sum = sum + inventoryRelation.quantity;
            }
            return sum;
        } else {
            InventoryRelation inventoryRelations = InventoryRelation.find("inventory.id=?1 AND location.id =?2", this.id, ID).first();
            if (inventoryRelations != null && inventoryRelations.quantity != null)
                return inventoryRelations.quantity;
            else
                return new Double(0);
        }
    }

    public Double quantity_sum_project(String id) {
        Double sum = new Double(0);

        Long ID = new Long(0);
        if (id != null && !id.equals("")) {
            ID = Long.parseLong(id);
        }
        if (id == null || ID == 0) {
            List<InventoryRelation> inventoryRelations = InventoryRelation.find("inventory.id=?1 AND location.project.id =?2", this.id, Users.pid()).fetch();
            for (InventoryRelation inventoryRelation : inventoryRelations) {
                if (inventoryRelation.quantity != null)
                    sum = sum + inventoryRelation.quantity;
            }
            return sum;
        } else {
            InventoryRelation inventoryRelations = InventoryRelation.find("inventory.id=?1 AND location.id =?2", this.id, ID).first();
            if (inventoryRelations != null && inventoryRelations.quantity != null)
                return inventoryRelations.quantity;
            else
                return new Double(0);
        }
    }

    public String quantityStatus(String id) {
        double sum = 0;
        Long ID = new Long(0);
        if (id.length() > 0)
            ID = Long.parseLong(id);
        InventoryRelation inventoryRelations = InventoryRelation.find("inventory.id=?1 AND location.id =?2", this.id, ID).first();

        if (inventoryRelations != null && inventoryRelations.idealQuantity != null)
            if (quantity_sum(id) > inventoryRelations.idealQuantity)
                return "success";
            else if (quantity_sum(id) > inventoryRelations.warningQuantity)
                return "warning";
            else
                return "danger";
        else
            return "primary";
    }

    public double idealPercent(String id) {
        double sum = 0;
        Long ID = new Long(0);
        if (id.length() > 0)
            ID = Long.parseLong(id);
        InventoryRelation inventoryRelations = InventoryRelation.find("inventory.id=?1 AND location.id =?2", this.id, ID).first();

        if (inventoryRelations != null && inventoryRelations.idealQuantity != null)
            if (quantity_sum(id) > inventoryRelations.idealQuantity)
                return 100;
            else
                return quantity_sum(id) / inventoryRelations.idealQuantity * 100;
        else
            return 100;
    }

    public String MRs(String id) {
        double sum = 0;
        Long ID = new Long(0);
        if (id != null && id.length() > 0) {
            ID = Long.parseLong(id);
            List<InventoryRelationOrder> InventoryRelationOrders = InventoryRelationOrder.find("inventory.id=?1 AND order.location.id =?2", this.id, ID).fetch();
            String s = "[";
            if (InventoryRelationOrders != null) {
                int k = 0;
                if (InventoryRelationOrders.size() < 15)
                    k = InventoryRelationOrders.size();
                else
                    k = 15;
                for (int i = InventoryRelationOrders.size() - k; i < InventoryRelationOrders.size(); i++) {
                    if (InventoryRelationOrders.get(i) != null)
                        if (i < InventoryRelationOrders.size() - 1)
                            s = s + String.valueOf(InventoryRelationOrders.get(i).quantity) + ",";
                        else
                            s = s + String.valueOf(InventoryRelationOrders.get(i).quantity);
                }
                return s + "]";
            } else
                return "[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,]";
        } else {
            List<InventoryRelationOrder> InventoryRelationOrders = InventoryRelationOrder.find("inventory.id=?1", this.id).fetch();
            String s = "[";
            if (InventoryRelationOrders != null) {
                int k = 0;
                if (InventoryRelationOrders.size() < 15)
                    k = InventoryRelationOrders.size();
                else
                    k = 15;
                for (int i = InventoryRelationOrders.size() - k; i < InventoryRelationOrders.size(); i++) {
                    if (InventoryRelationOrders.get(i) != null)
                        if (i < InventoryRelationOrders.size() - 1)
                            s = s + String.valueOf(InventoryRelationOrders.get(i).quantity) + ",";
                        else
                            s = s + String.valueOf(InventoryRelationOrders.get(i).quantity);
                }
                return s + "]";
            } else
                return "[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,]";
        }
    }


    public int ok() {
        int status = 0;
        for (InventoryRelation inventoryRelation : locationList) {
            if (inventoryRelation.idealQuantity != null && inventoryRelation.quantity != null && inventoryRelation.quantity > inventoryRelation.idealQuantity)
                status++;
        }
        return status;
    }

    public int normal() {
        int status = 0;
        for (InventoryRelation inventoryRelation : locationList) {
            if (inventoryRelation.idealQuantity != null && inventoryRelation.quantity != null && inventoryRelation.warningQuantity != null && inventoryRelation.quantity <= inventoryRelation.idealQuantity && inventoryRelation.quantity > inventoryRelation.warningQuantity)
                status++;
        }
        return status;
    }

    public int warning() {
        int status = 0;
        for (InventoryRelation inventoryRelation : locationList) {
            if (inventoryRelation.quantity != null && inventoryRelation.warningQuantity != null && inventoryRelation.quantity <= inventoryRelation.warningQuantity)
                status++;
        }
        return status;
    }

    public Double project_planned() {
        Long project = Users.pid();
        Double quantity = 0D;
        List<TaskInventoryRel> tasks = TaskInventoryRel.find("task.projectObject.project.id=?1 AND inventory.id=?2", project, this.id).fetch();
        for (int i = 0; i < tasks.size(); i++) {
            quantity = quantity + tasks.get(i).value;
        }
        return quantity;
    }

    public Double project_in(String id) {
        Long project = Users.pid();
        Double quantity = 0D;
        InventoryLocation location = InventoryLocation.findById(Long.parseLong(id));
        if (id == null || Long.parseLong(id) == 0L) {
            List<InventoryTransferItem> transfers = InventoryTransferItem.find("transfer.to.project.id=?1 AND inventory.id=?2 AND transfer.status.id=?3", project, this.id, 2L).fetch();
            for (int i = 0; i < transfers.size(); i++) {
                quantity = quantity + transfers.get(i).quantity;
            }
        } else if (location.is_main.equals("true")) {
            List<InventoryTransferItem> transfers = InventoryTransferItem.find("transfer.to.id=?1 AND inventory.id=?2 AND transfer.status.id=?3", 1L, this.id, 2L).fetch();
            for (int i = 0; i < transfers.size(); i++) {
                quantity = quantity + transfers.get(i).quantity;
            }
            List<InventoryRelationSupplier> relationSuppliers = InventoryRelationSupplier.find("inventory.id=?1 AND location.id=?2", this.id,location.id).fetch();
            for (int i = 0; i < relationSuppliers.size(); i++) {
                quantity = quantity + relationSuppliers.get(i).quantity;
            }
        } else {
            List<InventoryTransferItem> transfers = InventoryTransferItem.find("transfer.to.id=?1 AND inventory.id=?2 AND transfer.status.id=?3", Long.parseLong(id), this.id, 2L).fetch();
            for (int i = 0; i < transfers.size(); i++) {
                quantity = quantity + transfers.get(i).quantity;
            }
        }
        return quantity;
    }

    public Double project_out(String id) {
        Long project = Users.pid();
        Double quantity = 0D;
        if (id == null || Long.parseLong(id) == 0) {
            List<InventoryTransferItem> transfers = InventoryTransferItem.find("transfer.from.project.id=?1 AND inventory.id=?2", project, this.id).fetch();
            for (int i = 0; i < transfers.size(); i++) {
                quantity = quantity + transfers.get(i).quantity;
            }
            List<InventoryRelationOrder> orders = InventoryRelationOrder.find("inventory.id=?1 AND order.location.project.id=?2", this.id, project).fetch();
            for (int i = 0; i < orders.size(); i++) {
                if (orders.get(i).order.status.id == 2)
                    quantity = quantity + orders.get(i).approved;
                if (orders.get(i).order.status.id == 4 || orders.get(i).order.status.id == 5)
                    quantity = quantity + orders.get(i).issued;
            }
        } else {
            List<InventoryTransferItem> transfers = InventoryTransferItem.find("transfer.from.id=?1 AND inventory.id=?2 AND transfer.status.id=?3", Long.parseLong(id), this.id, 2L).fetch();
            for (int i = 0; i < transfers.size(); i++) {
                quantity = quantity + transfers.get(i).quantity;
            }
            List<InventoryRelationOrder> orders = InventoryRelationOrder.find("order.location.id=?1 AND inventory.id=?2", Long.parseLong(id), this.id).fetch();
            for (int i = 0; i < orders.size(); i++) {
                if (orders.get(i).order.status.id == 2)
                    quantity = quantity + orders.get(i).approved;
                if (orders.get(i).order.status.id == 4 || orders.get(i).order.status.id == 5)
                    quantity = quantity + orders.get(i).issued;
            }
        }
        return quantity;

    }

    public Double project_waste(String id) {
        Long project = Users.pid();
        Double quantity = 0D;
        if (id == null || Long.parseLong(id) == 0L) {
            List<InventoryWaste> wastes = InventoryWaste.find("location.project.id=?1 AND item.id=?2", project, this.id).fetch();
            for (int i = 0; i < wastes.size(); i++) {
                quantity = quantity + wastes.get(i).quantity;
            }
        } else {
            List<InventoryWaste> wastes = InventoryWaste.find("location.id=?1 AND item.id=?2", Long.parseLong(id), this.id).fetch();
            for (int i = 0; i < wastes.size(); i++) {
                quantity = quantity + wastes.get(i).quantity;
            }
        }
        return quantity;

    }

    public Double project_stock(String id) {
        Long project = Users.pid();
        Double quantity = 0D;
        if (id == null || Long.parseLong(id) == 0) {
            List<InventoryRelation> relations = InventoryRelation.find("location.project.id=?1 AND inventory.id=?2", project, this.id).fetch();
            for (int i = 0; i < relations.size(); i++) {
                quantity = quantity + relations.get(i).quantity;
            }
        } else {
            List<InventoryRelation> relations = InventoryRelation.find("location.id=?1 AND inventory.id=?2", Long.parseLong(id), this.id).fetch();
            for (int i = 0; i < relations.size(); i++) {
                quantity = quantity + relations.get(i).quantity;
            }
        }
        return quantity;

    }

}
