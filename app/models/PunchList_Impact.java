package models;

import controllers.CRUD;
import play.db.jpa.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

/**
 * Created by Rina on 1/12/2015.
 */
@Entity(name = "tb_punchlist_impact")
public class PunchList_Impact extends Model{
    public String impact;

    @CRUD.Hidden
    @OneToMany(mappedBy = "scheduleImpact", cascade = CascadeType.ALL)
    public List<PunchList> punchLists;

    @CRUD.Hidden
    @OneToMany(mappedBy = "costImpact", cascade = CascadeType.ALL)
    public List<PunchList> punchLists_;
}
