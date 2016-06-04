package models;

import controllers.CRUD;
import play.db.jpa.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.List;

/**
 * Created by enkhamgalan on 6/9/15.
 */
@Entity(name = "tb_org_team")
public class OrganizationTeam extends Model {

    @ManyToOne
    public Project project;

    public String name;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL)
    public List<OrganizationChart> organizationCharts;

    @CRUD.Hidden
    @OneToMany(mappedBy = "orgTeam", cascade = CascadeType.ALL)
    public List<DailyLogManpower> dailyLogManpowers;
}
