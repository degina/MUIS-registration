package models;

import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Created by enkhamgalan on 3/21/15.
 */
@Entity(name = "tb_org_permissionrelation")
public class OrgPermissionRelation extends Model {

    @ManyToOne
    public OrganizationChart organizationChart;

    @ManyToOne
    public OrgPermissionType permissionType;

}
