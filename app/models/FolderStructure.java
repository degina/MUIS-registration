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
@Entity(name = "tb_folderstructure")
public class FolderStructure extends Model {

    @Required
    public Long queue;

    @Required
    public String name;

    @ManyToOne
    public FolderType folderType;

    @ManyToOne
    public FolderColor folderColor;

    @ManyToOne
    public FolderStructure folderStructure;

    public boolean systemFolder = false;

    public String permission;

    public int extension=0;

    @CRUD.Hidden
    @OneToMany(mappedBy = "folderStructure", cascade = CascadeType.ALL)
    public List<FolderStructure> folderStructures;

    @ManyToOne
    public Project project;

    @CRUD.Hidden
    @OneToMany(mappedBy = "folderStructure", cascade = CascadeType.ALL)
    public List<FileShare> fileShares;

    @CRUD.Hidden
    @OneToMany(mappedBy = "folderStructure", cascade = CascadeType.ALL)
    public List<Akt> akts;

    public List<FolderStructure> getFolderStructures() {
        return FolderStructure.find("folderStructure.id=?1 order by queue, name", this.id).fetch();
    }

    public String toString() {
        return this.name;
    }
}
