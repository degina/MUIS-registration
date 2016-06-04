package models;

import play.data.validation.Required;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * Created by Enkh-Amgalan on 4/14/15.
 */
@Entity(name = "tb_dailylog_material_attach")
public class DailyLogMaterialAttach extends ModelAttach {
    @Required
    public String name;

    @Required
    public String dir;

    @Required
    public String extension;

    @ManyToOne
    public DailyLogMaterial material;

    public DailyLogMaterialAttach(String name, String dir, String extension,Float filesize, DailyLogMaterial material) {
        this.name = name;
        this.dir = dir;
        this.extension = extension;
        this.filesize = filesize;
        this.material = material;
        this.createdDate = new Date();
    }
}
