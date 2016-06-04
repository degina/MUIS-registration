package models;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Created by Enkhbayar on 6/8/2015.
 */
@Entity(name = "tb_portfolio_hyundai_service_user")
public class PortfolioHyundaiServiceUser extends Model {

    public String name;

    public String albanTushaal;

    public String utas;

    @Required
    @ManyToOne
    public PortfolioHyundai portfolioHyundai;

    public PortfolioHyundaiServiceUser(String name, String albanTushaal, String utas, PortfolioHyundai portfolio) {
        this.name = name;
        this.albanTushaal = albanTushaal;
        this.utas = utas;
        this.portfolioHyundai = portfolio;
    }
}
