package models;

import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * Created by enkhamgalan on 3/29/15.
 */
@Entity(name = "tb_message")
public class MessageRel extends Model {

    @ManyToOne
    public User msgSender;

    @ManyToOne
    public User msgReceiver;

    public Date date;

    @Column(length = 65535)
    public String content;
}
