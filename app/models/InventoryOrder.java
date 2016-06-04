package models;



import controllers.CRUD;
import controllers.Consts;
import controllers.Users;
import play.data.validation.Required;
import play.db.jpa.Model;


import java.util.Date;
import java.util.List;
import javax.persistence.*;

/**
 * Created by Personal on 1/31/2015.
 */
@Entity(name = "tb_inventory_order")
public class InventoryOrder extends Model {

    @Required
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    public List<InventoryRelationOrder> inventorys;

    @Required
    public Date date;

    @Required
    public Date approved_date;

    @Required
    public Date issued_date;

    public Date recieved_date;


    @Required
    public Date due;

    @Required
    public boolean edited;

    @Required
    public boolean urgent;

    @Required
    @ManyToOne
    public InventoryOrderStatus status;

    @Required
    @ManyToOne
    public InventoryLocation location;

    @Required
    @ManyToOne
    public User orderer;

    @Required
    @ManyToOne
    public User approver;

    @Required
    @ManyToOne
    public User issuer;

    @ManyToOne
    public User canceller;


    @CRUD.Hidden
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    public List<NotificationMessage> notificationMessages;

    @CRUD.Hidden
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    public ReminderModel reminderModel;

    @ManyToOne
    public Task task;

    public static boolean locationPermission(String loc) {
        InventoryLocation location = InventoryLocation.find("name = ?1 ", loc).first();
        if (location.project == null) {
                return false;
        } else {
            OrgPermissionRelation relation = OrgPermissionRelation.find("permissionType.permission.alias=?1 AND organizationChart.project.id=?2 AND organizationChart.user.id=?3", Consts.permissionInventory,location.project.id,Users.getUser().id).first();
            if(relation != null){
                if (relation.permissionType.value > 1)
                    return true;
                else
                    return false;
            }
            else
                return false;
        }
    }
}