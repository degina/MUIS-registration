package models;

import controllers.CRUD;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by Nandia on 8/6/2015.
 */
@Entity(name = "tb_gallery_album")
public class GalleryAlbum extends Model {

    @Required
    public String name;

    public Date createdDate;

    @ManyToOne
    public User uploader;

    @ManyToOne
    public Project project;

    //if type == 1 dailyLog
    //if type == 2 general_albums
    @Required
    public int type;

    @CRUD.Hidden
    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL)
    public List<GalleryPicture> galleryPictures;

    public String getNameMax() {
        if (this.name != null && this.name.length() > 20)
            return this.name.substring(0, 20)+"...";
        else if(this.name != null && this.name.length() <= 20)
            return this.name;
        return "";
    }
}
