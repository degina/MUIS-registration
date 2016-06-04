package models;

import controllers.CRUD;
import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.List;

@Entity(name = "tb_userteam")
public class UserTeam extends Model {

    @Required
    public String name;

    @Required
    public Long queue = 1L;

    public boolean contractor = false;

    @CRUD.Hidden
    @OneToMany(mappedBy = "userTeam")
    public List<User> users;

    @CRUD.Hidden
    @OneToMany(mappedBy = "userTeam", cascade = CascadeType.ALL)
    public List<RFI_Distribution> rfi_distributions;

    @CRUD.Hidden
    @OneToMany(mappedBy = "userTeam", cascade = CascadeType.ALL)
    public List<PunchList_Distribution> punchList_distributions;

    public String toString() {
        return this.name;
    }

    public List<User> getUsers() {
        return User.find("userTeam.id=? and active=true ORDER BY userPosition.rate, firstName", this.id).fetch();
    }

    @Transient
    public List<User> userLocals;
}
