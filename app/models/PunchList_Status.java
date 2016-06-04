package models;

import controllers.CRUD;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import java.util.List;

/**
 * Created by Rina on 1/11/2015.
 */
@Entity(name = "tb_punchlist_status")
public class PunchList_Status extends Model {
    @Required
    public String status;

    @CRUD.Hidden
    @OneToMany(mappedBy = "status", cascade = CascadeType.ALL)
    public List<PunchList> punchLists;

    @Transient
    public float count;

}
