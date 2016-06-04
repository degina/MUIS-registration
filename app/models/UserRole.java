package models;

import controllers.CRUD;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

/**
 * Created by enkhamgalan on 6/7/15.
 */
@Entity(name = "tb_userrole")
public class UserRole extends Model {
    @Required
    public String name;

    @CRUD.Hidden
    @OneToMany(mappedBy = "userRole", cascade = CascadeType.ALL)
    public List<User> users;

    public String toString() {
        return this.name;
    }

}
