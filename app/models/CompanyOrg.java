package models;

import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Created by enkhamgalan on 6/22/15.
 */
@Entity(name = "tb_company_org")
public class CompanyOrg extends Model {

    @ManyToOne
    public User user;

    public Long parentId;
}
