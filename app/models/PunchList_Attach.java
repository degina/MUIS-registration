package models;

import controllers.CRUD;
import play.data.validation.Required;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.List;

/**
 * Created by Rina on 1/11/2015.
 */
@Entity(name = "tb_punchlist_attach")
public class PunchList_Attach extends ModelAttach {
    @Required
    public String name;
    @Required
    public String dir;

    @Required
    public String extension;

    @ManyToOne
    @CRUD.Hidden
    public PunchList punchList;

    @ManyToOne
    @CRUD.Hidden
    public PunchList_Reply reply;

    @CRUD.Hidden
    @OneToMany(mappedBy = "punchListAttach", cascade = CascadeType.ALL)
    public List<DrawingLayer> pins;
}
