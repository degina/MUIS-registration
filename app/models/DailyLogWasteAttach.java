package models;

import play.data.validation.Required;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * Created by Enkh-Amgalan on 4/14/15.
 */
@Entity(name = "tb_dailylog_waste_attach")
public class DailyLogWasteAttach extends ModelAttach{
    @Required
    public String name;

    @Required
    public String dir;

    @Required
    public String extension;

    @ManyToOne
    public DailyLogWaste waste;

    public DailyLogWasteAttach(String name, String dir, String extension,Float filesize, DailyLogWaste waste) {
        this.name = name;
        this.dir = dir;
        this.extension = extension;
        this.filesize = filesize;
        this.waste = waste;
    }
}
