package models;

import controllers.Users;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

/**
 * Created by Enkh-Amgalan on 3/21/15.
 */
@Entity(name = "tb_portfolio_category")
public class PortfolioCategory extends Model {

    @Required
    public String name;

    public String mapIcon;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    public List<Portfolio> portfolios;

    public PortfolioCategory(String name) {
        this.name = name;
    }
    public List<Portfolio> getPortfolios() {
        User user = Users.getUser();
        return Portfolio.find("SELECT DISTINCT f FROM tb_portfolio f LEFT JOIN f.project AS p LEFT JOIN p.organizationCharts AS c" +
                " WHERE f.category.id=?1 AND ( f.owner.id=?2 OR ( p.id=f.project.id AND p.id=c.project.id AND c.user.id=?2 ))", this.id,user.id).fetch();
    }
}
