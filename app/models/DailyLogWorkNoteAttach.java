package models;

import play.data.validation.Required;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * Created by Enkh-Amgalan on 4/14/15.
 */
@Entity(name = "tb_dailyLog_workNote_attach")
public class DailyLogWorkNoteAttach extends ModelAttach {
    
    @Required
    public String name;

    @Required
    public String dir;

    @Required
    public String extension;

    @ManyToOne
    public DailyLogWorkNote workNote;

    public DailyLogWorkNoteAttach(String name, String dir, String extension,Float filesize, DailyLogWorkNote workNote) {
        this.name = name;
        this.dir = dir;
        this.extension = extension;
        this.workNote = workNote;
        this.filesize = filesize;
        this.createdDate = new Date();
    }
}
