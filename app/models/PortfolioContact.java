package models;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Created by Enkh-Amgalan on 5/14/15.
 */
@Entity(name = "tb_portfolio_contact")
public class PortfolioContact extends Model {

    public String name;

    public String position;

    public String phoneNumber;

    @Required
    @ManyToOne
    public Portfolio portfolio;

    public PortfolioContact(String name, String position, String phoneNumber,Portfolio portfolio) {
        this.name = name;
        this.portfolio= portfolio;
        this.position = position;
        this.phoneNumber = phoneNumber;
    }
}
