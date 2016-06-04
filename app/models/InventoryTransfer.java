package models;

import controllers.CRUD;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by Personal on 3/15/2015.
 */
@Entity(name = "tb_inventory_transfer")
public class InventoryTransfer extends Model {


    @Required
    @OneToMany(mappedBy = "transfer", cascade = CascadeType.ALL)
    public List<InventoryTransferItem> inventorys;

    @Required
    @ManyToOne
    public InventoryLocation from;

    @Required
    @ManyToOne
    public InventoryLocation to;

    @Required
    @ManyToOne
    public InventoryTransferStatus status;

    @Required
    @ManyToOne
    public User transferer;

    @Required
    @ManyToOne
    public User reciever;

    @ManyToOne
    public User canceller;

    @Required
    public Date date;

    @Required
    public Date recieved_date;


    @Required
    public boolean isReturn;

    @CRUD.Hidden
    @OneToMany(mappedBy = "transfer", cascade = CascadeType.ALL)
    public List<NotificationMessage> notificationMessages;


    public boolean hasAttachment(){
        boolean IS = false;
        for(int i =0 ;i< inventorys.size();i++){
            if(inventorys.get(i).attachments.size()>0){
                IS=true;
            }
        }
        return  IS;
    }

}
