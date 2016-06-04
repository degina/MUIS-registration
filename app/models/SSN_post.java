package models;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity(name = "tb_ssn_post")
public class SSN_post extends Model {

    @Required
    @ManyToOne
    public SSN_user user;

    @Required
    @ManyToOne
    public SSN_category category;

    @Required
    public String title;

    @Required
    @Column(length=67000)
    public String content;

    @Required
    public Date create_date;

    @Required
    public String update_date;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    public List<SSN_comment> commentList;


}
