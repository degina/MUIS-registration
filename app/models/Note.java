package models;

import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * Created by enkhamgalan on 5/3/15.
 */
@Entity(name = "tb_note")
public class Note extends Model {
    @ManyToOne
    public User user;

    public String title;

    @Column(length = 65535)
    public String notes;

    public Date date;
}
