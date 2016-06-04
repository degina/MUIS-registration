package models;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Created by Enkh-Amgalan on 1/27/15.
 */
@Entity(name = "tb_meeting_topic_attachment")
public class MeetingTopicAttachment extends ModelAttach{
    @Required
    public String name;

    @Required
    public String dir;

    @Required
    public String extension;

    @Required
    @ManyToOne
    public MeetingTopic topic;

    public MeetingTopicAttachment(String name, String dir, String extension,Float filesize, MeetingTopic topic) {
        this.name = name;
        this.dir = dir;
        this.filesize = filesize;
        this.extension = extension;
        this.topic = topic;
    }
}
