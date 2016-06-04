package models;

import controllers.CRUD;
import play.data.validation.Required;

import javax.persistence.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Rina on 3/28/2015.
 */
@Entity(name = "tb_drawingRevision")
public class DrawingRevision extends ModelAttach {
    @Required
    public int number = 0;

    @Required
    public String dir;

    @Required
    public String extension;

    public Date drawingDate;

    @CRUD.Hidden
    @ManyToOne
    public Drawing drawing;

    @CRUD.Hidden
    @ManyToOne
    public DrawingPDF pdf;

    public int pageNumber = 0;

    @CRUD.Hidden
    @OneToMany(mappedBy = "revision", cascade = CascadeType.ALL)
    public List<DrawingLayer> layers;

    public DrawingRevision() {
    }

    public DrawingRevision(String dir_, String ext_, Long drawing_, Long pdf_, int page, String date_) {
        dir = dir_;
        extension = ext_;
        pageNumber = page;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            drawingDate = dateFormat.parse(date_);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        createdDate = new Date();
        drawing = Drawing.findById(drawing_);
        number = drawing.newRevisionNumber();
        pdf = DrawingPDF.findById(pdf_);
    }

    public int isDraw() {
        if (layers != null) {
            int cat = 0;
            boolean rfi = false, punch = false;
            for (int i = 0; i < layers.size(); i++) {
                if ((layers.get(i).rfi != null || layers.get(i).tracking != null) && !rfi) {
                    cat += 1;
                    rfi = true;
                }
                if ((layers.get(i).punchList != null || layers.get(i).reply != null) && !punch) {
                    cat += 2;
                    punch = true;
                }
                if (punch && rfi) break;
            }
            return cat;
        } else return 0;
    }

    public DrawingLayer getUserPersonalLayer(Long userId) {
        return DrawingLayer.find("revision.id = " + this.id + " and user.id  = " + userId).first();
    }

    public boolean isDele() {
        return false;
    }
}
