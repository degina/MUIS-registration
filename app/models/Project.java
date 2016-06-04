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
@Entity(name = "tb_project")
public class Project extends Model {

    public String code;

    @Required
    public String name;

    @CRUD.Hidden
    public Date startDate;

    @CRUD.Hidden
    public Long duration;

    @CRUD.Hidden
    public Date finishDate;

    @CRUD.Hidden
    public Date actualFinish;

    public String status;

    public String depends = "";

    public boolean startIsMilestone = false;

    @Max(100)
    @CRUD.Hidden
    public Float completedPercent = 0.0f;

    @CRUD.Hidden
    @Required
    @ManyToOne
    public User owner;

    @CRUD.Hidden
    public String workCount;

    public Float manHours = 0f;

    public String holidays;

    public String weekend;

    @CRUD.Hidden
    @OneToOne(mappedBy = "project", cascade = CascadeType.ALL)
    public ProjectCounter projectCounter;

    @CRUD.Hidden
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    public List<ProjectObject> projectObjects;

    @CRUD.Hidden
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    public List<ProjectAssignRel> projectAssignRels;

    @CRUD.Hidden
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    public List<FolderStructure> folderStructures;

    @CRUD.Hidden
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    public List<RFI> rfis;

    @CRUD.Hidden
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    public List<PunchList> punchLists;

    @CRUD.Hidden
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    public List<Meeting> meetings;

    @CRUD.Hidden
    @OneToOne(mappedBy = "project", cascade = CascadeType.ALL)
    public Portfolio portfolio;

    @CRUD.Hidden
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    public List<PercentHistoryProject> percentHistoryProjects;

    @CRUD.Hidden
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    public List<DrawingDiscipline> drawingDisciplines;

    @CRUD.Hidden
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    public List<DrawingPDF> drawingPDFs;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    public List<OrganizationChart> organizationCharts;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    public List<OrganizationTeam> organizationTeams;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    public List<InventoryLocation> inventoryLocations;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    public List<ManPower> manPowers;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    public List<Equipment> equipments;

    public void setCompletedPercent() {
        float percent = 0;
        for (ProjectObject projectObject : this.projectObjects) {
            percent += (projectObject.completedPercent * projectObject.scopePercent / 100);
        }
        this.completedPercent = percent;
        this._save();
    }

    public List<ProjectObject> getProjectObjects() {
        return ProjectObject.find("project.id=?1 order by orderGantt", this.id).fetch();
    }

    public String continuedDuration(Date date) {
        String value;
        if (this.actualFinish.getTime() <= this.finishDate.getTime()) { // хоцроогүй
            date = new Date();
            if (this.completedPercent >= 100 || date.getTime() <= this.startDate.getTime())
                value = "<span class='c-green'>" + this.duration + "</span>";
            else
                value = "<span class='c-green'>" + this.duration + " - " + Functions.getDifferenceWorkDays(this, this.startDate, date) + "</span>";
        } else
            value = "<span class='c-red'>" + this.duration + " + " + Functions.getDifferenceWorkDays(this, this.finishDate, this.actualFinish) + "</span>";
        return value;
    }
}
