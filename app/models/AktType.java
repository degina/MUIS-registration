package models;

import javax.persistence.*;
import controllers.CRUD;
import play.data.validation.*;
import play.db.jpa.*;
import java.util.*;

/**
 * Created by Dell on 7/1/2015.
 */
@Entity(name = "tb_akttype")
public class AktType extends Model {

    @Required
    public String name;

    @CRUD.Hidden
    @OneToMany(mappedBy = "aktType", cascade = CascadeType.ALL)
    public List<AktStyle> aktStyles;
}
