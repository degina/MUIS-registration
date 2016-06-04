package models;

import play.data.validation.Max;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by Enkh-Amgalan on 3/1/15.
 */
@Entity(name = "tb_budget")
public class Budget extends Model {

    @Required
    public Date date;

    @Required
    public Long operatingProfitActual=0L;

    @Required
    public Long operatingProfitBudget=0L;

    @Required
    public Long operatingProfitVariance = 0L;

    @Max(100)
    public Float operatingProfitVariancePercent = 0.0f;

    @Column(length = 65535)
    public String operatingProfitDescription;

    @Required
    public Long otherExpensesActual=0L;

    @Required
    public Long otherExpensesBudget=0L;

    @Required
    public Long otherExpensesVariance = 0L;

    @Max(100)
    public Float otherExpensesVariancePercent = 0.0f;

    @Column(length = 65535)
    public String otherExpensesDescription;

    @Required
    public Long otherIncomeActual=0L;

    @Required
    public Long otherIncomeBudget=0L;

    @Required
    public Long otherIncomeVariance = 0L;

    @Max(100)
    public Float otherIncomeVariancePercent = 0.0f;

    @Column(length = 65535)
    public String otherIncomeDescription;

    @Required
    public Long taxBeforeProfitActual=0L;

    @Required
    public Long taxBeforeProfitBudget=0L;

    @Required
    public Long taxBeforeProfitVariance = 0L;

    @Max(100)
    public Float taxBeforeProfitVariancePercent = 0.0f;

    @Column(length = 65535)
    public String taxBeforeProfitDescription;

    @Required
    public Long taxActual=0L;

    @Required
    public Long taxBudget=0L;

    @Required
    public Long taxVariance = 0L;

    @Max(100)
    public Float taxVariancePercent = 0.0f;

    @Column(length = 65535)
    public String taxDescription;

    @Required
    public Long netIncomeActual=0L;

    @Required
    public Long netIncomeBudget=0L;

    @Required
    public Long netIncomeVariance = 0L;

    @Max(100)
    public Float netIncomeVariancePercent = 0.0f;

    @Column(length = 65535)
    public String netIncomeDescription;

    @Required
    public Long ebitdaActual=0L;

    @Required
    public Long ebitdaBudget=0L;

    @Required
    public Long ebitdaVariance = 0L;

    @Max(100)
    public Float ebitdaVariancePercent = 0.0f;

    @Column(length = 65535)
    public String ebitdaDescription;

    @OneToOne(mappedBy = "budget", cascade = CascadeType.ALL)
    public BudgetNetSales netSales;

    @OneToOne(mappedBy = "budget", cascade = CascadeType.ALL)
    public BudgetCogs cogs;

    @OneToOne(mappedBy = "budget", cascade = CascadeType.ALL)
    public BudgetGrossMargin grossMargin;

    @OneToMany(mappedBy = "budget", cascade = CascadeType.ALL)
    public List<BudgetActivity> activities;

    @OneToOne(mappedBy = "budget", cascade = CascadeType.ALL)
    public BudgetOpex opex;

    @OneToOne(mappedBy = "budget", cascade = CascadeType.ALL)
    public BudgetBalance balance;

    @OneToMany(mappedBy = "budget", cascade = CascadeType.ALL)
    public List<BudgetCash> cashs;

}
