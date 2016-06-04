package models;

import play.data.validation.Required;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * Created by Enkh-Amgalan on 4/14/15.
 */
@Entity(name = "tb_dailylog_dumpster_attach")
public class DailyLogDumpsterAttach extends ModelAttach{
    @Required
    public String name;

    @Required
    public String dir;

    @Required
    public String extension;

    @ManyToOne
    public DailyLogDumpster dumpster;

    public DailyLogDumpsterAttach(String name, String dir, String extension,Float filesize, DailyLogDumpster dumpster) {
        this.name = name;
        this.dir = dir;
        this.filesize = filesize;
        this.extension = extension;
        this.dumpster = dumpster;
        this.createdDate = new Date();
    }
}
