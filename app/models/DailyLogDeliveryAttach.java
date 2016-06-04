package models;

import play.data.validation.Required;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * Created by Enkh-Amgalan on 4/14/15.
 */
@Entity(name = "tb_dailylog_delivery_attach")
public class DailyLogDeliveryAttach extends ModelAttach {
    @Required
    public String name;

    @Required
    public String dir;

    @Required
    public String extension;

    @ManyToOne
    public DailyLogDelivery delivery;

    public DailyLogDeliveryAttach(String name, String dir, String extension,Float filesize, DailyLogDelivery delivery) {
        this.name = name;
        this.dir = dir;
        this.filesize = filesize;
        this.extension = extension;
        this.delivery =delivery;
        this.createdDate = new Date();
    }
}
