package models;

import controllers.CRUD;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

/**
 * Created by enkhamgalan on 3/21/15.
 */
@Entity(name = "tb_org_permission")
public class OrgPermission extends Model {

    @Required
    public String name;

    @Required
    public int queue = 0;

    public boolean active;

    @CRUD.Hidden
    @Required
    public String alias;

    public String toString() {
        return this.name;
    }

    @CRUD.Hidden
    @OneToMany(mappedBy = "permission", cascade = CascadeType.ALL)
    public List<OrgPermissionType> permissionTypes;

    public OrgPermissionType getPermissionType(int value) {
        for (OrgPermissionType permissionType : this.permissionTypes) {
            if (permissionType.value == value) return permissionType;
        }
        return null;
    }
}
