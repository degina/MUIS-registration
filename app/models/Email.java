package models;

import play.db.jpa.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.util.List;

/**
 * Created by enkhamgalan on 8/1/15.
 */
@Entity(name = "tb_email")
public class Email extends Model {

    @OneToOne
    public User user;

    public String username;

    public String fullname;

    public String password;

}
