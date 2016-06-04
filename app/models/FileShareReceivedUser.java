package models;

import controllers.CRUD;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * Created by enkhamgalan on 3/1/15.
 */
@Entity(name = "tb_fileshare_received_user")
public class FileShareReceivedUser extends Model {

    @CRUD.Hidden
    @Required
    @ManyToOne
    public User user;

    @Required
    public Date date;

    @ManyToOne
    public FileShare fileShare;
}
