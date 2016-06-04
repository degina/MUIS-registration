package models;

import play.db.jpa.Model;

import javax.persistence.Entity;

/**
 * Created by Enkh-Amgalan on 4/27/15.
 */
@Entity(name = "tb_dashboard_management")
public class DashboardManagement extends Model {
    public String imageDir;
}