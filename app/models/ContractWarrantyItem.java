package models;

import controllers.CRUD;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * Created by Khuubuu on 1/13/2015.
 */
@Entity(name = "tb_contract_warranty_item")
public class ContractWarrantyItem extends Model {

    @Required
    public String name;

    public Date startDate;

    public Date endDate;

    public Long dugaar;

    @CRUD.Hidden
    @ManyToOne
    public Contract contract;

    public ContractWarrantyItem(String name,Long dugaar,Date startDate,Date endDate,Contract contract){
        this.name=name;
        this.dugaar=dugaar;
        this.startDate=startDate;
        this.endDate=endDate;
        this.contract=contract;
    }
    public Long getNotifyDay(){
        ReminderModel reminder = ReminderModel.find("contract.id=?1",this.id).first();
        if (reminder != null) return reminder.count;
        else return 0l;
    }
}
