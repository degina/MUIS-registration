package models;

import controllers.CRUD;
import play.data.validation.Required;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by Rina on 3/28/2015.
 */
@Entity(name = "tb_drawing")
public class Drawing extends ModelAttach {
    public String Number;

    @Required
    public String title;

    @Required
    public String cipher;

    @CRUD.Hidden
    @ManyToOne
    public DrawingDiscipline discipline;

    @CRUD.Hidden
    @OneToMany(mappedBy = "drawing", cascade = CascadeType.ALL)
    public List<DrawingRevision> revisions;

    public Drawing() {
    }

    public Drawing(String t, Long d, String c, String no) {
        title = t;
        discipline = DrawingDiscipline.findById(d);
        cipher = c;
        createdDate = new Date();
        Number = no;
    }

    public int newRevisionNumber() {
        int number = 0;
        if (revisions != null && revisions.size() > 0) {
            for (DrawingRevision rev : revisions) {
                if (rev.number > number)
                    number = rev.number;
            }
            number++;
        }
        return number;
    }

    public DrawingRevision lastRevision() {
        for (DrawingRevision rev : revisions)
            if (rev.number == revisions.size() - 1)
                return rev;
        return null;
    }

    public DrawingRevision getRevisionIndexOf(int num) {
        for (DrawingRevision rev : revisions)
            if (rev.number == num)
                return rev;
        return null;
    }

    public int[] getRevisionNumbers() {
        int[] list = {};
        int i = 0;
        for (DrawingRevision rev : revisions)
            list[0] = rev.number;
        return list;
    }

    public boolean isDele() {
        return false;
    }
}
