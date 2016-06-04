package models;

import controllers.CRUD;
import controllers.Users;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by Khuubuu on 1/19/2015.
 */
@Entity(name = "tb_meeting")
public class Meeting extends Model {

    @Required
    public String title;

    @Required
    public Date meetingDate;

    @Required
    public Date finishDate;

    @Required
    public String location;

    @Required
    public boolean viewMeeting;

    @Required
    public boolean privateMeeting;

    @Lob
    public String overview;

    public Date minuteDate;

    @Required
    @ManyToOne
    public MeetingStatus status;

    @Required
    @ManyToOne
    public Meeting urgeljlel;

    @Required
    @ManyToOne
    public User owner;

    @ManyToOne
    public User closedUser;

    @Required
    @ManyToOne
    public Project project;

    @Required
    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL)
    public List<ReminderModel> reminderModels;

    @CRUD.Hidden
    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL)
    public List<MeetingAttachment> meetingAttachments;

    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL)
    public List<MeetingTopic> meetingTopics;

    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL)
    public List<MeetingUserRel> meetingUserRels;

    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL)
    public List<Notification> notifications;

    public boolean checkNew(){
        MeetingUserRel rel;
        Long id= Users.getUser().id;
        rel = MeetingUserRel.find("meeting.id=?1 AND seen=false AND user.id =?2",this.id,id).first();
        if (rel != null) return true;
        return false;
    }
}
