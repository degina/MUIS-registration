package models;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity(name = "tb_event_People")
public class EventPeople extends Model {

    @Required
    @ManyToOne
    public Event event;

    @Required
    @ManyToOne
    public User user;

    @Required
    public int status = 0;

    public EventPeople(Event event, User user,int status){
        this.event = event;
        this.user = user;
        this.status = status;
    }
    public  EventPeople(){}


}