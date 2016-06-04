package models;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

/**
 * Created by Enkh-Amgalan on 1/30/15.
 */
@Entity(name = "tb_meeting_topic_newold")
public class MeetingTopicNewOld extends Model {
    @Required
    public String newOld;

    @OneToMany(mappedBy = "newOld", cascade = CascadeType.ALL)
    public List<MeetingTopic> topics;
}
