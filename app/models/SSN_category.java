package models;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Entity;

@Entity(name = "tb_ssn_category")
public class SSN_category extends Model {
    
        @Required
        public String name;
}
