package models;

import controllers.CRUD;
import controllers.Users;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity(name = "tb_event")
public class Event extends Model {
    @Required
    public Date startDate;

    @Required
    public Date endDate;

    @Required
    public String title;

    @Required
    public String imageDir;

    @Required
    public String location;

    @Column(length = 65535)
    @Required
    public String description;

    @Required
    @ManyToOne
    public EventCategory category;

    @Required
    @ManyToOne
    public User owner;

    @CRUD.Hidden
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    public List<EventAttach> attaches;

    @CRUD.Hidden
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    public List<EventPeople> peoples;

    @CRUD.Hidden
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    public List<Post> posts;

    public EventAttach getFirstAttach() {
        return EventAttach.find("event.id=?1", this.id).first();
    }

    public int getUserEventRel() {
        Long userId = Users.getUser().id;
        EventPeople uer = EventPeople.find("user.id=?1 AND event.id=?2", userId, this.id).first();
        return uer == null ? -1 : uer.status;
    }
    public Long get2StatusSize(){
        return EventPeople.count("status=2 and event.id=?1", this.id);
    }

    public Long get1StatusSize(){
        return EventPeople.count("status=1 and event.id=?1", this.id);
    }

    public Long get0StatusSize(){
        return EventPeople.count("status=0 and event.id=?1", this.id);
    }

//    public Long getEventCounter() {
//        for (EventPeople people : this.peoples) {
//            if(people.status==1)eventCounter.maybe++;
//            else if(people.status==2)eventCounter.come++;
//            else if(people.status == 0)eventCounter.invited++;
//            else eventCounter.notCome++;
//        }
//        return eventCounter;
//    }

}