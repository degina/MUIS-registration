package models;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * Created by enkhamgalan on 3/8/15.
 */
@Entity(name = "tb_notification")
public class Notification extends Model {

    @Required
    public Date date;

    public String message;

    @Required
    @ManyToOne
    public User acceptor;

    @Required
    @ManyToOne
    public User sender;

    public boolean seen = false;

    @ManyToOne
    public Meeting meeting;

    @ManyToOne
    public Event event;

    public String lateDate() {
        Date now = new Date();
        long diff = now.getTime() - date.getTime();
        long d = diff / (24 * 60 * 60 * 1000);
        if (d > 0) return (d + 1) + " өдрийн өмнө";
        else {
            d = diff / (60 * 60 * 1000) % 24;
            if (d > 0) return d + " цагийн өмнө";
            else {
                d = diff / (60 * 1000) % 60;
                if (d > 0) return d + " минутын өмнө";
                else {
                    d = diff / 1000 % 60;
                    if (d > 0) return d + " секундын өмнө";
                }
            }
        }
        return "Яг одоо";
    }
}
