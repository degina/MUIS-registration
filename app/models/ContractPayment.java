package models;

import controllers.CRUD;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import java.util.List;

/**
 * Created by Khuubuu on 1/12/2015.
 */
@Entity(name = "tb_contract_payment")
public class ContractPayment extends Model{
    @Required
    public String payment;

    @CRUD.Hidden
    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL)
    public List<Contract> contracts;

    @Transient
    public float percent;
}
