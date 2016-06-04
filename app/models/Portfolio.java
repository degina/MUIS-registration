package models;

import controllers.CRUD;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by enkhamgalan on 3/12/15.
 */

@Entity(name = "tb_portfolio")
public class Portfolio extends Model {

    @OneToOne
    public Project project;

    @CRUD.Hidden
    @OneToOne(mappedBy = "portfolio", cascade = CascadeType.ALL)
    public PortfolioHyundai portfolioHyundai;

    public String address;

    @Lob
    public String description;

    public Date testStartDate;

    public Date contractDate;

    public Date serviceStartDate;

    public String mapLon;

    public String mapLat;

    @Required
    @ManyToOne
    public PortfolioCategory category;

    @Required
    @ManyToOne
    public PortfolioStage stage;

    @ManyToOne
    public User owner;

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL)
    public List<PortfolioContact> contacts;

    @Required
    public boolean isActive;

    public String imageUrl;

    public int x;
    public int y;
    public int w;
    public int h;

    @Transient
    public Long allMeetings;
    @Transient
    public Long overdueMeetings;
    @Transient
    public Long nextWeekMeetings;
    @Transient
    public Long nNextWeekMeetings;

    @Transient
    public Long allRFIs;
    @Transient
    public Long overdueRFIs;
    @Transient
    public Long nextWeekRFIs;
    @Transient
    public Long nNextWeekRFIs;

    @Transient
    public Long allPunchLists;
    @Transient
    public Long overduePunchLists;
    @Transient
    public Long nextWeekPunchLists;
    @Transient
    public Long nNextWeekPunchLists;

    @Transient
    public Long allSchedule;
    @Transient
    public Long overdueSchedule;
    @Transient
    public Long nextWeekSchedule;
    @Transient
    public Long nNextWeekSchedule;

    public List<PortfolioContact> getContacts() {
        return PortfolioContact.find("portfolio.id=?1", this.id).fetch();
    }
}
