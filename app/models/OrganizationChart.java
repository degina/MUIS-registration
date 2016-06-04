package models;

import controllers.CRUD;
import play.db.jpa.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.List;

/**
 * Created by enkhamgalan on 6/7/15.
 */
@Entity(name = "tb_org_chart")
public class OrganizationChart extends Model {

    @ManyToOne
    public Project project;

    @ManyToOne
    public User user;

    @ManyToOne
    public UserPosition userPosition;

    public Long parentId;

    @ManyToOne
    public OrganizationTeam team;

    @CRUD.Hidden
    @OneToMany(mappedBy = "organizationChart", cascade = CascadeType.ALL)
    public List<OrgPermissionRelation> permissionRelations;

    public int getPermission(Long permissionId) {
        for (OrgPermissionRelation rel : this.permissionRelations) {
            if (rel.permissionType.permission.id.compareTo(permissionId) == 0) {
                return rel.permissionType.value;
            }
        }
        return 0;
    }

    public OrgPermissionRelation getPermissionRelation(Long permissionId) {
        for (OrgPermissionRelation rel : this.permissionRelations) {
            if (rel.permissionType.permission.id.compareTo(permissionId) == 0) {
                return rel;
            }
        }
        return null;
    }
}
