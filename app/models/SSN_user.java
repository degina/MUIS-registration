package models;

import controllers.CRUD;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity(name = "tb_ssn_user")
public class SSN_user extends Model {
    @Required
    public String email;

    @Required
    public String name;

    @Required
    public String password;

    public Date birthday;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    public List<SSN_post> postList;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    public List<SSN_comment> commentList;


}
