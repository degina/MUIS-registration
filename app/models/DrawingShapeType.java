package models;

import controllers.CRUD;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.List;

/**
 * Created by Rina on 3/28/2015.
 */
@Entity(name = "tb_drawingShapeType")
public class DrawingShapeType extends Model {
    public String type;

    @CRUD.Hidden
    @OneToMany(mappedBy = "type", cascade = CascadeType.ALL)
    private List<DrawingShape> shapes;
}
