package models;

import controllers.CRUD;
import controllers.CompanyConf;
import controllers.Users;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity(name = "tb_user")
public class User extends Model {

    @Required
    public String firstName;

    @Required
    public String lastName;

    public int lastNameLength;

    @Required
    public String surName;

    @Required
    public String username;

    @Required
    public String password;

    @Required
    public Boolean isMen = true;

    @Required
    public String email;

    public String registerNumber;

    public Date birthday;

    public String birthPlace;

    public Date employmentDate;

    public String education;

    public String specialty; //Эзэмшсэн мэргэжил

    public String grade; //мэргэжилийн зэрэг

    public String gcmRegistrationId = null; //GCM ийн id нь

    public String device = null; //GCM ийн id нь

    @Lob
    public String training; //сургалт

    public String organizationTraining; //байгуулагын сургалт

    @Required
    public String phone1;

    public String phone2;

    public String address1;
    public String address2;
    public String address3;
    public String address4;

    public int x;
    public int y;
    public int w;
    public int h;
    public boolean active;

    @ManyToOne
    public UserTeam userTeam;

    @ManyToOne
    public UserRole userRole;

    @Required
    @ManyToOne
    public UserPosition userPosition;

    @CRUD.Hidden
    @Required
    public String profilePicture;

    @CRUD.Hidden
    public Date createdDate;

    @CRUD.Hidden
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    public AccountSetting accountSetting;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    public Email emailAccount;

    @CRUD.Hidden
    @OneToMany(mappedBy = "uploader", cascade = CascadeType.ALL)
    public List<FileShare> fileShares;

    @CRUD.Hidden
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    public List<FileShareReceivedUser> receivedUsers;

    @CRUD.Hidden
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    public List<TaskAssignRel> taskAssignRels;

    @CRUD.Hidden
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    public List<ProjectAssignRel> projectAssignRels;
    @CRUD.Hidden
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    public List<ProjectObAssignRel> projectObAssignRels;

    @CRUD.Hidden
    @OneToMany(mappedBy = "uploader", cascade = CascadeType.ALL)
    public List<DrawingPDF> drawingPDFs;

    @OneToMany(mappedBy = "assignee", cascade = CascadeType.ALL)
    public List<RFI> rfis;

    @OneToMany(mappedBy = "questionReceivedFrom", cascade = CascadeType.ALL)
    public List<RFI> rfiList;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
    public List<RFI_Tracking> trackings;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    public List<RFI_Distribution> rfi_distributions;

    @OneToMany(mappedBy = "assignee", cascade = CascadeType.ALL)
    public List<PunchList> punchLists;

    @OneToMany(mappedBy = "questionReceivedFrom", cascade = CascadeType.ALL)
    public List<PunchList> punchListList;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
    public List<PunchList_Reply> punchList_replies;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    public List<PunchList_Distribution> punchList_distributions;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    public List<MeetingUserRel> meetingUserRels;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    public List<DrawingLayer> drawingPersonalLayer;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    public List<MeetingTopicUserRel> meetingTopicUserRels;

    @CRUD.Hidden
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    public List<Meeting> meetings;

    @CRUD.Hidden
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    public List<MeetingTopic> meetingTopics;

    @CRUD.Hidden
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    public List<DailyLogWorkNote> dailyLogWorkNotes;

    @CRUD.Hidden
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    public List<DailyLogMyPlan> dailyLogUnTasks;

    @CRUD.Hidden
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL)
    public List<Notification> notifications1;

    @CRUD.Hidden
    @OneToMany(mappedBy = "acceptor", cascade = CascadeType.ALL)
    public List<Notification> notifications2;

    @CRUD.Hidden
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL)
    public List<NotificationMessage> notificationMessages1;

    @CRUD.Hidden
    @OneToMany(mappedBy = "acceptor", cascade = CascadeType.ALL)
    public List<NotificationMessage> notificationMessages2;

    @OneToMany(mappedBy = "msgSender", cascade = CascadeType.ALL)
    public List<MessageRel> messages1;

    @OneToMany(mappedBy = "msgReceiver", cascade = CascadeType.ALL)
    public List<MessageRel> messages2;

    @CRUD.Hidden
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    public List<DailyLogWeather> weathers;

    @CRUD.Hidden
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    public List<DailyLogTechnicalDelay> technicalDelays;

    @CRUD.Hidden
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    public List<DailyLogDelivery> deliveries;

    @CRUD.Hidden
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    public List<DailyLogInspection> inspections;

    @CRUD.Hidden
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    public List<DailyLogNote> notesLog;

    @CRUD.Hidden
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    public List<DailyLogSanaachlaga> sanaachlagas;

    @CRUD.Hidden
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    public List<DailyLogVisitor> visitors;

    @CRUD.Hidden
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    public List<Event> events;

    @CRUD.Hidden
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    public List<Note> note;

    @CRUD.Hidden
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    public List<ToDoList> toDoLists;

    @CRUD.Hidden
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    public List<PostComment> postComments;

    @CRUD.Hidden
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    public List<Post> posts;

    @CRUD.Hidden
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    public List<PostSeeUser> postSeeUsers;

    @CRUD.Hidden
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    public List<Portfolio> portfolios;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    public List<OrganizationChart> organizationCharts;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    public List<UserPermissionRelation> permissionRelations;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    public List<CompanyOrg> companyOrgs;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    public List<ReminderUser> reminderUsers;

    @CRUD.Hidden
    @OneToMany(mappedBy = "uploader", cascade = CascadeType.ALL)
    public List<GalleryPicture> galleryPictures;

    @CRUD.Hidden
    @OneToMany(mappedBy = "uploader", cascade = CascadeType.ALL)
    public List<GalleryAlbum> galleryAlbums;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    public List<EmailAddress> emailAddresses;

    @CRUD.Hidden
    @OneToMany(mappedBy = "worker", cascade = CascadeType.ALL)
    public List<InventoryRelationWorker> inventoryRelationWorkerList;

    public String toString() {
        return this.firstName + "." + this.getLastnameFirstCharacter();
    }

    public boolean isAdmin() {
        if (this.userRole == null) return true;
        return this.userRole.id.compareTo(2L) == 0;
    }

    public boolean isProjectOwner() {
        Long pid = Users.pid();
        if (pid.intValue() == 0) return false;
        for (Portfolio portfolio : this.portfolios) {
            if (portfolio.project.id.compareTo(pid) == 0) return true;
        }
        return false;
    }

    public OrganizationChart getOrganizationChart() {
        Long pid = Users.pid();
        for (OrganizationChart chart : this.organizationCharts) {
            if (chart.project.id.compareTo(pid) == 0) return chart;
        }
        return null;
    }

    public String getLastnameFirstCharacter() {
        if (this.lastName != null && this.lastName.length() > 0) return this.lastName.substring(0, this.lastNameLength);
        return "";
    }

    public int getUserPermissionType(String alias) { // Үндсэн хандах эрхүүд
        UserPermissionRelation relation = UserPermissionRelation.find("user.id=?1 AND permissionType.permission.alias=?2", this.id, alias).first();
        if (relation != null) return relation.permissionType.value;
        return 0;
    }

    public int getPermission(Long permissionId) {
        for (UserPermissionRelation rel : this.permissionRelations) {
            if (rel.permissionType.permission.id.compareTo(permissionId) == 0) {
                return rel.permissionType.value;
            }
        }
        return 0;
    }

    public int getPermissionType(String alias) { // Зөвхөн төслийн хувь дахь хандах эрхүүд
        Long selectedProject = Users.pid();
        if (alias.equals("permissionFileShare") && selectedProject.intValue() == 0)
            return this.getUserPermissionType(alias);
        if (selectedProject.intValue() == 0) return 0;
        OrgPermissionRelation relation = OrgPermissionRelation.find("organizationChart.project.id=?1 AND organizationChart.user.id=?2 AND permissionType.permission.alias=?3", selectedProject, this.id, alias).first();
        if (relation != null) {
            return relation.permissionType.value;
        }
        return 0;
    }
}
