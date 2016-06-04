package models;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;


/**
 * Created by Personal on 2015-01-11.
 */
@Entity(name = "tb_test")
public class Test extends Model {

    @Required
    public String col1;

    @Required
    public Date col2;

    @Required
    public Float col3;

}
