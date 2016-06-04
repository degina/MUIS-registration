package models;

import play.data.validation.Required;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * Created by Enkh-Amgalan on 4/14/15.
 */
@Entity(name = "tb_dailylog_manpower_attach")
public class DailyLogManpowerAttach extends ModelAttach{

    @Required
    public String name;

    @Required
    public String dir;

    @Required
    public String extension;

    @ManyToOne
    public DailyLogManpower manpower;

    public DailyLogManpowerAttach(String name, String dir, String extension,Float filesize, DailyLogManpower manpower) {
        this.name = name;
        this.dir = dir;
        this.extension = extension;
        this.filesize = filesize;
        this.manpower = manpower;
        this.createdDate = new Date();
    }
}
