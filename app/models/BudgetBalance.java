package models;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

/**
 * Created by Enkh-Amgalan on 3/6/15.
 */
@Entity(name = "tb_budget_balance")
public class BudgetBalance extends Model {

    @Required
    public Long cashEquivalents=0L;

    @Required
    public Long currentLiabilities=0L;

    @Required
    public Long accountsReceivables=0L;

    @Required
    public Long taxesPayAbles=0L;

    @Required
    public Long taxRecoverAbles=0L;

    @Required
    public Long unearnedSales=0L;

    @Required
    public Long inventory=0L;

    @Required
    public Long loans=0L;

    @Required
    public Long supplyMaterial=0L;

    @Required
    public Long nonCurrentPayAbles=0L;

    @Required
    public Long prepayments=0L;

    @Required
    public Long tangibleAssets=0L;

    @Required
    public Long ordinaryShares=0L;

    @Required
    public Long intangibleAssets=0L;

    @Required
    public Long incomeReporting=0L;

    @Required
    public Long investments=0L;

    @Required
    public Long retainedEarnings=0L;

    @Required
    public Long otherNonCurrentAssets=0L;

    @Required
    public Long totalAssets=0L;

    @Required
    public Long totalLiabilitiesAndEquity=0L;

    @Required
    public Long totalEquity=0L;

    @Required
    public Long nonCurrentAssets=0L;

    @Required
    public Long totalLiabilities=0L;

    @Required
    public Long currentAssets=0L;

    @Column(length = 65535)
    public String description;

    @Required
    @OneToOne
    public Budget budget;

    public BudgetBalance(Budget budget) {
        this.budget = budget;
    }

    public void saveBudgetBalance(Long cashEquivalents, Long currentLiabilities, Long accountsReceivables, Long taxesPayAbles,Long taxRecoverAbles, Long inventory,
                         Long loans, Long unearnedSales, Long supplyMaterial, Long nonCurrentPayAbles, Long prepayments, Long tangibleAssets,
                         Long ordinaryShares, Long intangibleAssets, Long incomeReporting, Long investments, Long retainedEarnings,
                         Long otherNonCurrentAssets, String description, Budget budget) {
        this.cashEquivalents = cashEquivalents;
        this.currentLiabilities = currentLiabilities;
        this.accountsReceivables = accountsReceivables;
        this.taxesPayAbles = taxesPayAbles;
        this.taxRecoverAbles=taxRecoverAbles;
        this.inventory = inventory;
        this.loans = loans;
        this.unearnedSales = unearnedSales;
        this.supplyMaterial = supplyMaterial;
        this.nonCurrentPayAbles = nonCurrentPayAbles;
        this.prepayments = prepayments;
        this.tangibleAssets = tangibleAssets;
        this.ordinaryShares = ordinaryShares;
        this.intangibleAssets = intangibleAssets;
        this.incomeReporting = incomeReporting;
        this.investments = investments;
        this.retainedEarnings = retainedEarnings;
        this.otherNonCurrentAssets = otherNonCurrentAssets;
        this.description = description;
        this.budget = budget;
    }
}
