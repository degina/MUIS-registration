package models;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * Created by Enkh-Amgalan on 4/14/15.
 */
@Entity(name = "tb_dailylog_inspection_attach")
public class DailyLogInspectionAttach extends ModelAttach {
    @Required
    public String name;

    @Required
    public String dir;

    @Required
    public String extension;

    @ManyToOne
    public DailyLogInspection inspection;

    public DailyLogInspectionAttach(String name, String dir, String extension,Float filesize, DailyLogInspection inspection) {
        this.name = name;
        this.dir = dir;
        this.extension = extension;
        this.filesize = filesize;
        this.inspection = inspection;
        this.createdDate = new Date();
    }
}
