package models;

import controllers.CRUD;
import play.data.validation.*;
import play.db.jpa.*;
import javax.persistence.*;
import java.util.*;

/**
 * Created by Dell on 7/1/2015.
 */
@Entity(name = "tb_akt")
public class Akt extends Model{

    @Required
    public String queue;

    @Required
    @ManyToOne
    public AktStyle aktStyle;

    @Required
    @CRUD.Hidden
    @ManyToOne
    public User uploader;

    public String dir;

    @OneToOne(mappedBy = "akt", cascade = CascadeType.ALL)
    public FileShare fileShare;

    @OneToOne
    public AktImage aktImage1;

    @OneToOne
    public AktImage aktImage2;

    @OneToOne
    public AktImage aktImage3;

    @OneToOne
    public AktImage aktImage4;

    @ManyToOne
    public FolderStructure folderStructure;
}
