package models;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * Created by Nandia on 8/6/2015.
 */
@Entity(name = "tb_gallery_picture")
public class GalleryPicture extends Model {

    @Required
    public String name;

    @Required
    public Date date;

    @Required
    public String path;

    public String pathTumb;

    public String description;

    public String extension;

    @ManyToOne
    public User uploader;

    @ManyToOne
    public Task task;

    //@Required
    @ManyToOne
    public GalleryAlbum album;

    public String getNameMax() {
        if (this.name != null && this.name.length() > 15)
            return this.name.substring(0, 15)+"...";
        else if(this.name != null && this.name.length() <= 15)
            return this.name;
        return "";
    }

}
