package models;

import javax.persistence.*;
import controllers.CRUD;
import play.data.validation.*;
import play.db.jpa.*;
import java.util.List;

/**
 * Created by Dell on 7/1/2015.
 */
@Entity(name = "tb_aktstyle")
public class AktStyle extends Model {

    @Required
    public String name;

    @Required
    public String logoAgent;

    @Required
    public String logoClient;

    @Required
    public String agentStaffName;

    @Required
    public String agentStaffPos;

    @Required
    public String clientStaffName;

    @Required
    public String clientStaffPos;

    public String agentStaffName1;

    public String agentStaffPos1;

    public String clientStaffName1;

    public String clientStaffPos1;

    @CRUD.Hidden
    @ManyToOne
    public User uploader;


    @Required
    @ManyToOne
    public AktType aktType;

    //@Required
    @ManyToOne
    public Project project;

//    @ManyToOne
//    public FolderStructure folderStructure;

    @CRUD.Hidden
    @OneToMany(mappedBy = "aktStyle", cascade = CascadeType.ALL)
    public List<Akt> akts;

}
