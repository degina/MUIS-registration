package models;

import play.data.validation.Required;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Created by Enkh-Amgalan on 4/26/15.
 */
@Entity(name = "tb_event_attach")
public class EventAttach extends ModelAttach {
    @Required
    public String name;

    @Required
    public String dir;

    @Required
    public String extension;

    @ManyToOne
    public Event event;

    public EventAttach(String name, String dir, String extension,Float filesize, Event event) {
        this.name = name;
        this.dir = dir;
        this.filesize = filesize;
        this.extension = extension;
        this.event = event;
    }
}
