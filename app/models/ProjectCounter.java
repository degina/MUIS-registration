package models;

import controllers.CRUD;
import controllers.Functions;
import play.data.validation.Max;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by enkhamgalan on 12/20/14.
 */
@Entity(name = "tb_project_counter")
public class ProjectCounter extends Model {

    public Long lastRFINo = 0l;

    public Long lastPunchListNo = 0l;

    @OneToOne
    public Project project;

}
