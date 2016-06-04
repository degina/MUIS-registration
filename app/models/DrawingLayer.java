package models;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import controllers.CRUD;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by Rina on 3/28/2015.
 */
@Entity(name = "tb_drawingLayer")
public class DrawingLayer extends Model {
    @Required
    public Date updatedDate;

    @Column(length = 262140)
    public String path;

    @CRUD.Hidden
    @ManyToOne
    public DrawingRevision revision;

    @CRUD.Hidden
    @ManyToOne
    public RFI_Attach rfiAttach;

    @CRUD.Hidden
    @ManyToOne
    public PunchList_Attach punchListAttach;

    @CRUD.Hidden
    @ManyToOne
    public RFI rfi;

    @CRUD.Hidden
    @ManyToOne
    public PunchList punchList;

    @CRUD.Hidden
    @ManyToOne
    public RFI_Tracking tracking;

    @CRUD.Hidden
    @ManyToOne
    public PunchList_Reply reply;

    @CRUD.Hidden
    @ManyToOne
    public User user;

    public DrawingLayer() { }
    public DrawingLayer(PunchList punchList_, String path_, Long rev, String type) {
        punchList = punchList_;
        path = path_;
        if (type.equalsIgnoreCase("drawing"))
            revision = DrawingRevision.findById(rev);
        else
            punchListAttach = PunchList_Attach.findById(rev);
        updatedDate = new Date();
    }

    public DrawingLayer(RFI rfi_, String path_, Long rev, String type) {
        rfi = rfi_;
        path = path_;
        if (type.equalsIgnoreCase("drawing"))
            revision = DrawingRevision.findById(rev);
        else
            rfiAttach = RFI_Attach.findById(rev);
        updatedDate = new Date();
    }

    public DrawingLayer(Long usId, String path_, Long rev) {
        user = User.findById(usId);
        path = path_;
        revision = DrawingRevision.findById(rev);
        updatedDate = new Date();
    }
}
