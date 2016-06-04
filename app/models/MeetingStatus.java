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
@Entity(name = "tb_meeting_status")
public class MeetingStatus extends Model {
    @Required
    public String status;

    @OneToMany(mappedBy = "status", cascade = CascadeType.ALL)
    public List<Meeting> meetings;
}
