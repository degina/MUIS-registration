package models;

import play.data.validation.Required;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * Created by Enkh-Amgalan on 4/14/15.
 */
@Entity(name = "tb_dailylog_safety_attach")
public class DailyLogSafetyAttach extends ModelAttach{
    @Required
    public String name;

    @Required
    public String dir;

    @Required
    public String extension;

    @ManyToOne
    public DailyLogSafety safety;

    public DailyLogSafetyAttach(String name, String dir, String extension,Float filesize, DailyLogSafety safety) {
        this.name = name;
        this.dir = dir;
        this.extension = extension;
        this.filesize = filesize;
        this.safety = safety;
        this.createdDate = new Date();
    }
}
