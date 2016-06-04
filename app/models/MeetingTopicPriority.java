package models;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

/**
 * Created by Khuubuu on 1/22/2015.
 */
@Entity(name = "tb_meeting_topic_priority")
public class MeetingTopicPriority extends Model {
    @Required
    public String priority;

    @OneToMany(mappedBy = "priority", cascade = CascadeType.ALL)
    public List<MeetingTopic> meetingTopics;
}
