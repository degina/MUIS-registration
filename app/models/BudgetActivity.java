package models;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Created by Enkh-Amgalan on 3/1/15.
 */
@Entity(name = "tb_budget_activity")
public class BudgetActivity extends Model {

    @Required
    public String name;

    @Required
    public Long salesActual=0L;

    @Required
    public Long salesBudget=0L;

    @Required
    public Long cogsActual=0L;

    @Required
    public Long cogsBudget=0L;

    @Required
    public Long profitActual=0L;

    @Required
    public Long profitBudget=0L;

    @Column(length = 65535)
    public String description;

    @Required
    @ManyToOne
    public Budget budget;

    public BudgetActivity(String name,Long salesActual,Long salesBudget,Long cogsActual,
                          Long cogsBudget,Long profitActual,Long profitBudget,String description,
                          Budget budget){
        this.name=name;
        this.salesActual=salesActual;
        this.salesBudget=salesBudget;
        this.cogsActual=cogsActual;
        this.cogsBudget=cogsBudget;
        this.profitActual=profitActual;
        this.profitBudget=profitBudget;
        this.description=description;
        this.budget=budget;
    }
}
