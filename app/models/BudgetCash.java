package models;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.List;

/**
 * Created by Enkh-Amgalan on 3/6/15.
 */
@Entity(name = "tb_budget_cash")
public class BudgetCash extends Model {

    @Required
    public String name;

    public Long inflow=0L;

    public Long outflow=0L;

    public Long netflow=0L;

    @OneToMany(mappedBy = "cash", cascade = CascadeType.ALL)
    public List<BudgetSubContractor> subContractors;

    @Required
    @ManyToOne
    public Budget budget;

    public BudgetCash(Budget budget, String name) {
        this.budget = budget;
        this.name = name;
    }
}
