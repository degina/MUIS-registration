package models;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity(name = "tb_meeting_topic")
public class MeetingTopic extends Model {

    @Required
    public String title;

    @Lob
    public String description;

    @Lob
    public String minutes;

    @ManyToOne
    public MeetingTopicStatus status;

    @ManyToOne
    public MeetingTopicNewOld newOld;

    @ManyToOne
    public MeetingTopicPriority priority;

    @ManyToOne
    public Task task;

    @Required
    @ManyToOne
    public User owner;

    @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL)
    public List<MeetingTopicAttachment> topicAttachments;

    @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL)
    public List<MeetingTopicUserRel> topicUserRels;

    @ManyToOne
    public Meeting meeting;

    public String getRelUsers(){
        String names="";
        for(MeetingTopicUserRel userRel:topicUserRels){
            names += userRel.user.toString()+" \n";
        }
        return names;
    }
    public String getFristRelUser(){
        String name="";
        if(topicUserRels.size()>0)
            name += topicUserRels.get(0).user.toString();
        if(topicUserRels.size()>2)
            name += " <span class='badge badge-warning m-l-5'>+"+(topicUserRels.size()-1)+"</span>";
        return name;
    }
}
