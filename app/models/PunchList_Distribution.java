package models;

import controllers.CRUD;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Created by Rina on 1/11/2015.
 */
@Entity(name = "tb_punchlist_distribution")
public class PunchList_Distribution extends Model {
    public String code;

    @ManyToOne
    @CRUD.Hidden
    public User user;

    @ManyToOne
    @CRUD.Hidden
    public UserTeam userTeam;

    @ManyToOne
    @CRUD.Hidden
    public PunchList punchList;

    public PunchList_Distribution(String code_, PunchList punchList_, Long id) {
        code = code_;
        punchList = punchList_;
        if(code.equalsIgnoreCase("u"))
            user = User.findById(id);
        else
            userTeam = UserTeam.findById(id);
    }

    public String getName() {
        if (code.equalsIgnoreCase("u"))
            return user.firstName + "." + user.getLastnameFirstCharacter();
        else
            return userTeam.name;
    }

    public Long getId() {
        if (code.equalsIgnoreCase("u"))
            return user.id;
        else
            return userTeam.id;

    }
    public String getCode() {
        return code;
    }
}
