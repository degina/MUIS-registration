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
@Entity(name = "tb_foldertype")
public class FolderType extends Model {

    @Required
    public String name;

    @Required
    public String val;

    @CRUD.Hidden
    @OneToMany(mappedBy = "folderType", cascade = CascadeType.ALL)
    public List<FolderBasic> folderBasics;

    @CRUD.Hidden
    @OneToMany(mappedBy = "folderType", cascade = CascadeType.ALL)
    public List<FolderStructure> folderStructures;

    public String toString() {
        return this.name;
    }

}
