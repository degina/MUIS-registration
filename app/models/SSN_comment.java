package models;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;

@Entity(name = "tb_ssn_comment")
public class SSN_comment extends Model {

    @Required
    @ManyToOne
    public SSN_post post;

    @Required
    @ManyToOne
    public SSN_user user;

    @Required
    public String content;

    @Required
    public Date create_date;
    

}
