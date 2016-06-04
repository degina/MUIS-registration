package models;

import controllers.CRUD;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.List;

/**
 * Created by enkhamgalan on 2/21/15.
 */
@Entity(name = "tb_folderbasic")
public class FolderBasic extends Model {

    @Required
    public Long queue;

    @Required
    public String name;

    @ManyToOne
    public FolderType folderType;

    @ManyToOne
    public FolderColor folderColor;

    @Required
    public boolean parentFolder;

    @ManyToOne
    public FolderBasic folderBasic;

    @CRUD.Hidden
    @OneToMany(mappedBy = "folderBasic", cascade = CascadeType.ALL)
    public List<FolderBasic> folderBasics;

    public String toString() {
        return this.name;
    }

}
