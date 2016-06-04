package models;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Created by Enkh-Amgalan on 3/2/15.
 */
@Entity(name = "tb_budget_opex_detail")
public class BudgetOpexDetail extends Model {
    @Required
    public String name;

    @Required
    public boolean isEmpRelated;

    @Required
    public Long actual=0L;

    @Required
    public Long budget=0L;

    @Column(length = 65535)
    public String description;

    @ManyToOne
    public BudgetOpex opex;

    public BudgetOpexDetail(String name,boolean isEmpRelated,Long actual,Long budget, String description,BudgetOpex opex){
        this.name=name;
        this.isEmpRelated=isEmpRelated;
        this.actual=actual;
        this.budget=budget;
        this.description=description;
        this.opex=opex;
    }
}
