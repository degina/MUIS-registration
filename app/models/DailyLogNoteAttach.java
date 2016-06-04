package models;

import play.data.validation.Required;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * Created by Enkh-Amgalan on 4/14/15.
 */
@Entity(name = "tb_dailylog_note_attach")
public class DailyLogNoteAttach extends ModelAttach{

    @Required
    public String name;

    @Required
    public String dir;

    @Required
    public String extension;

    @ManyToOne
    public DailyLogNote note;

    public DailyLogNoteAttach(String name, String dir, String extension,Float filesize, DailyLogNote note) {
        this.name = name;
        this.dir = dir;
        this.filesize = filesize;
        this.extension = extension;
        this.note = note;
        this.createdDate = new Date();
    }
}
