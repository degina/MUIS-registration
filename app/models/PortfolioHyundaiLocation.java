package models;

import controllers.Users;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

/**
 * Created by Enkhbayar on 6/8/2015.
 */
@Entity(name = "tb_portfolio_hyundai_location")
public class PortfolioHyundaiLocation extends Model {

    @Required
    public String name;

    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL)
    public List<PortfolioHyundai> portfolioHyundais;

    public PortfolioHyundaiLocation(String name) {
        this.name = name;
    }

    public List<Portfolio> getPortfolioHyundais() {
        User user = Users.getUser();
        return Portfolio.find("SELECT DISTINCT f FROM tb_portfolio f LEFT JOIN f.project AS p LEFT JOIN p.organizationCharts AS c" +
                " WHERE f.portfolioHyundai.location.id=?1 AND (f.owner.id=?2 OR ( p.id=f.project.id AND p.id = c.project.id AND c.user.id=?2 ))", this.id, user.id).fetch();
    }
}
