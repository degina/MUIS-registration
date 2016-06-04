package models;

import controllers.CRUD;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by Enkhbayar on 6/8/2015.
 */
@Entity(name = "tb_portfolio_hyundai")
public class PortfolioHyundai extends Model {

    @OneToOne
    public Portfolio portfolio;

    public String zoruilalt;

    public String zahialagch;

    public String zahialagchToloologch;

    public String zahialagchTushaal;

    public String zahialagchUtas;

    public String hariutsagch;

    public String hariutsagchTushaal;

    public String hariutsagchUtas;

    public String borluulaltName;

    public String uildveriinDugaar;

    public Date gereeHiisen;

    public Date zuragBatalsan;

    public Date uildverlelEhelsen;

    public Date uildverlelDuussan;

    public Date teeverleltHiisen;

    public Date gaaliDeerIrsen;

    public Date talbaiDeerIrsen;

    public Date ugsrahStartDate;

    public Date ugsrahEndDate;

    public String ashiglagch;

    public String ashiglaltBag;

    public String ashiglaltAhlagch;

    public Date ashiglaltGereeHiisen;

    public Date ashiglaltEhelsen;

    public Date ashiglaltDuusah;

    public Date ashiglaltMagdlal;

    @Lob
    public String tusgaiTemdeglel;


    @Required
    @ManyToOne
    public PortfolioHyundaiLocation location;

    @Required
    @ManyToOne
    public PortfolioHyundaiFactory factory;

    @Required
    @ManyToOne
    public PortfolioHyundaiStage stage;


    @CRUD.Hidden
    @OneToMany(mappedBy = "portfolioHyundai", cascade = CascadeType.ALL)
    public List<PortfolioHyundaiProductList> portfolioProductLists;

    @CRUD.Hidden
    @OneToMany(mappedBy = "portfolioHyundai", cascade = CascadeType.ALL)
    public List<PortfolioHyundaiInstallTeam> installTeams;

    @CRUD.Hidden
    @OneToMany(mappedBy = "portfolioHyundai", cascade = CascadeType.ALL)
    public List<PortfolioHyundaiServiceUser> portfolioServiceUsers;


}
