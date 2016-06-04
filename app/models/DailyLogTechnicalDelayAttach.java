package models;

import play.data.validation.Required;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * Created by Enkh-Amgalan on 4/14/15.
 */
@Entity(name = "tb_dailylog_technicaldelay_attach")
public class DailyLogTechnicalDelayAttach extends ModelAttach {

    @Required
    public String name;

    @Required
    public String dir;

    @Required
    public String extension;

    @ManyToOne
    public DailyLogTechnicalDelay technicalDelay;

    public DailyLogTechnicalDelayAttach(String name, String dir, String extension,Float filesize, DailyLogTechnicalDelay technicalDelay) {
        this.name = name;
        this.dir = dir;
        this.extension = extension;
        this.filesize = filesize;
        this.technicalDelay = technicalDelay;
        this.createdDate = new Date();
    }
}
