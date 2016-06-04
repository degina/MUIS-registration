package models;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Created by Enkhbayar on 6/8/2015.
 */
@Entity(name = "tb_portfolio_hyundai_install_team")
public class PortfolioHyundaiInstallTeam extends Model {

    public String teamName;

    public String leaderName;

    @Required
    @ManyToOne
    public PortfolioHyundai portfolioHyundai;

    public PortfolioHyundaiInstallTeam(String teamName, String leaderName, PortfolioHyundai portfolio) {
        this.teamName = teamName;
        this.leaderName = leaderName;
        this.portfolioHyundai = portfolio;
    }
}
