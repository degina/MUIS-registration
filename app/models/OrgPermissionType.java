package models;

import controllers.CRUD;
import play.db.jpa.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.List;

/**
 * Created by enkhamgalan on 5/1/15.
 */
@Entity(name = "tb_org_permission_type")
public class OrgPermissionType extends Model {

    public int value;

    @ManyToOne
    public OrgPermission permission;

    public String description;

    @CRUD.Hidden
    @OneToMany(mappedBy = "permissionType", cascade = CascadeType.ALL)
    public List<OrgPermissionRelation> relations;

}
