package models;

import controllers.CRUD;
import play.data.validation.Required;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.Date;
import java.util.List;

/**
 * Created by Rina on 3/28/2015.
 */
@Entity(name = "tb_drawingPDF")
public class DrawingPDF extends ModelAttach {
    @Required
    public String name;

    @Required
    public String url;

    @ManyToOne
    public User uploader;

    @ManyToOne
    public Project project;

    @Required
    public int pageCount;

    @CRUD.Hidden
    @OneToMany(mappedBy = "pdf", cascade = CascadeType.ALL)
    public List<DrawingRevision> revisions;

    public DrawingPDF() {
    }

    public DrawingPDF(String n, String u, Long d, int p, Long pr) {
        name = n;
        url = u;
        uploader = User.findById(d);
        pageCount = p;
        project = Project.findById(pr);
        createdDate = new Date();
    }

    public int getPageCount() {
        return pageCount;
    }

}
