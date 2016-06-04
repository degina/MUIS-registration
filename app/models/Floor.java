package models;

import controllers.CRUD;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.List;

/**
 * Created by enkhamgalan on 12/20/14.
 */
@Entity(name = "tb_floor")
public class Floor extends Model {

    @Required
    public String name;

    @Required
    @ManyToOne
    @CRUD.Hidden
    public ProjectObject projectObject;

    @CRUD.Hidden
    @OneToMany(mappedBy = "floor", cascade = CascadeType.ALL)
    public List<Task> tasks;
}
