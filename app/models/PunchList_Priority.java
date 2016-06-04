package models;

import controllers.CRUD;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

/**
 * Created by Rina on 1/11/2015.
 */
@Entity(name = "tb_punchlist_priority")
public class PunchList_Priority extends Model {
    @Required
    public String priority;

    @Required
    public int rank;

    @CRUD.Hidden
    @OneToMany(mappedBy = "priority", cascade = CascadeType.ALL)
    public List<PunchList> punchLists;
}
