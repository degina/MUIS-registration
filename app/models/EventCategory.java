package models;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

/**
 * Created by Enkh-Amgalan on 4/26/15.
 */
@Entity(name = "tb_event_category")
public class EventCategory extends Model {

    @Required
    public String name;

    @Required
    public String colorClass;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    public List<Event> events;

    public EventCategory(String name, String colorClass) {
        this.name = name;
        this.colorClass = colorClass;
    }
}
