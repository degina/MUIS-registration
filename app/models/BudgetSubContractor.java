package models;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Created by Enkh-Amgalan on 3/6/15.
 */
@Entity(name = "tb_budget_sub_contractor")
public class BudgetSubContractor extends Model {

    @Required
    public String name;

    public Long inflow=0L;

    public Long outflow=0L;

    public Long netflow=0L;

    @Required
    @ManyToOne
    public BudgetCash cash;

    public BudgetSubContractor(String name, Long inflow, Long outflow, BudgetCash cash) {
        this.name = name;
        this.inflow = inflow;
        this.outflow = outflow;
        this.cash = cash;
    }
}
