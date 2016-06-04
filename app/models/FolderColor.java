package models;

import controllers.CRUD;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

/**
 * Created by enkhamgalan on 2/25/15.
 */
@Entity(name = "tb_foldercolor")
public class FolderColor extends Model {

    @Required
    public String name;

    @Required
    public String color;

    @CRUD.Hidden
    @OneToMany(mappedBy = "folderColor", cascade = CascadeType.ALL)
    public List<FolderBasic> folderBasics;

    @CRUD.Hidden
    @OneToMany(mappedBy = "folderColor", cascade = CascadeType.ALL)
    public List<FolderStructure> folderStructures;

    public String toString() {
        return this.name;
    }

}
