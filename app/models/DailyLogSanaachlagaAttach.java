package models;

import play.data.validation.Required;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * Created by Enkh-Amgalan on 4/14/15.
 */
@Entity(name = "tb_dailylog_sanaachlaga_attach")
public class DailyLogSanaachlagaAttach extends ModelAttach {
    @Required
    public String name;

    @Required
    public String dir;

    @Required
    public String extension;

    @ManyToOne
    public DailyLogSanaachlaga sanaachlaga;

    public DailyLogSanaachlagaAttach(String name, String dir, String extension,Float filesize, DailyLogSanaachlaga sanaachlaga) {
        this.name = name;
        this.dir = dir;
        this.filesize = filesize;
        this.extension = extension;
        this.sanaachlaga = sanaachlaga;
        this.createdDate = new Date();
    }
}
