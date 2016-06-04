package models;

import controllers.Users;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

/**
 * Created by Enkh-Amgalan on 5/14/15.
 */
@Entity(name = "tb_portfolio_stage")
public class PortfolioStage extends Model {
    @Required
    public String name;

    public String mapIcon;

    public String colorClass;

    @OneToMany(mappedBy = "stage", cascade = CascadeType.ALL)
    public List<Portfolio> portfolios;

    public PortfolioStage(String name,String stageColor) {
        this.name = name;
        this.mapIcon = "/public/images/map/m-"+stageColor+".png";
        this.colorClass = stageColor;
    }

    public List<Portfolio> getPortfolios() {
        User user = Users.getUser();
        return Portfolio.find("SELECT DISTINCT f FROM tb_portfolio f LEFT JOIN f.project AS p LEFT JOIN p.organizationCharts AS c" +
                " WHERE f.stage.id=?1 AND (f.owner.id=?2 OR ( p.id=f.project.id AND p.id = c.project.id AND c.user.id=?2 ))", this.id, user.id).fetch();
    }
}
