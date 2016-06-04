package models;

import com.sun.org.apache.xpath.internal.operations.Mod;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Created by Enkh-Amgalan on 1/27/15.
 */
@Entity(name = "tb_meeting_topicuserrel")
public class MeetingTopicUserRel extends Model{

    @Required
    @ManyToOne
    public User user;

    @ManyToOne
    public MeetingTopic topic;

    public MeetingTopicUserRel(User user,MeetingTopic topic){
        this.user=user;
        this.topic=topic;
    }
}
