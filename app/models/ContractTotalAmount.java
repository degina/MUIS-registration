package models;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import java.util.Date;

@Entity(name = "tb_contract_totalamount")
public class ContractTotalAmount extends Model{

    @Required
    public Long amount;

    public Date date;

    public Float huvi;

    @OneToOne
    public Contract contract;

    public ContractTotalAmount(Long amount,Date date,Float huvi, Contract contract ){
        this.amount=amount;
        this.date=date;
        this.huvi=huvi;
        this.contract=contract;
    }
}
