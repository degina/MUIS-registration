package models;

/**
 * Created by Khuubuu on 1/13/2015.
 */

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;

@Entity(name = "tb_contract_financial")
public class ContractFinancial extends Model{
    @Required
    public Long amount;

    public Date date;

    public Float huvi;

    @ManyToOne
    public Contract contract;

    @Required
    public String financialType;

    public ContractFinancial(Long amount,Date date,Float huvi,String financialType, Contract contract ){
        this.amount=amount;
        this.date=date;
        this.huvi=huvi;
        this.financialType=financialType;
        this.contract=contract;
    }

}
