package models;

import play.data.validation.Required;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * Created by Enkh-Amgalan on 4/14/15.
 */
@Entity(name = "tb_dailylog_equipment_attach")
public class DailyLogEquipmentAttach extends ModelAttach{
    @Required
    public String name;

    @Required
    public String dir;

    @Required
    public String extension;

    @ManyToOne
    public DailyLogEquipment equipment;

    public DailyLogEquipmentAttach(String name, String dir, String extension,Float filesize, DailyLogEquipment equipment) {
        this.name = name;
        this.dir = dir;
        this.filesize = filesize;
        this.extension = extension;
        this.equipment = equipment;
        this.createdDate = new Date();
    }
}
