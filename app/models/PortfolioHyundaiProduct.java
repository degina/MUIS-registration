package models;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

/**
 * Created by Enkhbayar on 6/8/2015.
 */
@Entity(name = "tb_portfolio_hyundai_product")
public class PortfolioHyundaiProduct extends Model {

    @Required
    public String name;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    public List<PortfolioHyundaiProductList> productList;

    public PortfolioHyundaiProduct(String name) {
        this.name = name;
    }
}