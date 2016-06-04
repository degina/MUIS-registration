package models;

import controllers.CRUD;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Created by Khuubuu on 1/21/2015.
 */
@Entity(name = "tb_meeting_attachment")
public class MeetingAttachment extends ModelAttach {

    @Required
    public String name;

    @Required
    public String dir;

    @Required
    public String extension;

    @Required
    @ManyToOne
    public Meeting meeting;

    public MeetingAttachment(String name, String dir, String extension,Float filesize, Meeting meeting) {
        this.name = name;
        this.dir = dir;
        this.filesize = filesize;
        this.extension = extension;
        this.meeting = meeting;
    }
}

