package models;

import play.data.validation.*;
import play.db.jpa.*;
import javax.persistence.*;

/**
 * Created by Dell on 7/1/2015.
 */
@Entity(name = "tb_aktimage")
public class AktImage extends Model {

    @Required
    public String image;

    @Required
    public String description;

    @Required
    public String date;
}
