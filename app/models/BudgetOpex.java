package models;

import play.data.validation.Max;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.List;

/**
 * Created by Enkh-Amgalan on 3/1/15.
 */
@Entity(name = "tb_budget_opex")
public class BudgetOpex extends Model {

    @Required
    public Long employeeActual=0L;

    @Required
    public Long employeeBudget=0L;

    @Required
    public Long totalActual=0L;

    @Required
    public Long totalBudget=0L;

    @Required
    public Long variance = 0L;

    @Max(100)
    public Float variancePercent = 0.0f;

    @Column(length = 65535)
    public String description;

    @OneToOne
    public Budget budget;

    @OneToMany(mappedBy = "opex", cascade = CascadeType.ALL)
    public List<BudgetOpexDetail> opexDetails;

    public BudgetOpex(Budget budget){
        this.budget = budget;
    }
}
