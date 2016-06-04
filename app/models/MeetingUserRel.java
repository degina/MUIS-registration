package models;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Created by Enkh-Amgalan on 1/27/15.
 */
@Entity(name = "tb_meeting_userrel")
public class MeetingUserRel extends Model {

    @Required
    @ManyToOne
    public User user;

    @ManyToOne
    public Meeting meeting;

    @Required
    public boolean seen = false;

    @ManyToOne
    public MeetingIrts irts;

    public MeetingUserRel(User user,Meeting meeting){
        this.user=user;
        this.meeting=meeting;
    }
}
