package models;

import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Created by enkhamgalan on 8/20/15.
 */
@Entity(name = "tb_email_address")
public class EmailAddress extends Model {

    @ManyToOne
    public User user;

    public String address;
}
