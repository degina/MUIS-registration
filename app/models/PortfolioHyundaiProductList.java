package models;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Created by Enkhbayar on 6/8/2015.
 */
@Entity(name = "tb_portfolio_hyundai_product_list")
public class PortfolioHyundaiProductList extends Model {

    @Required
    @ManyToOne
    public PortfolioHyundaiProduct product;

    public String rate;

    public String amount;

    @Required
    @ManyToOne
    public PortfolioHyundai portfolioHyundai;

    public PortfolioHyundaiProductList(PortfolioHyundaiProduct product, String rate, String amount, PortfolioHyundai portfolio) {
        this.product = product;
        this.rate = rate;
        this.amount = amount;
        this.portfolioHyundai = portfolio;
    }
}