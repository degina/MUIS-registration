package models;

import controllers.CRUD;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by Rina on 1/11/2015.
 */
@Entity(name = "tb_punchlist_reply")
public class PunchList_Reply extends Model {

    @Required
    @CRUD.Hidden
    @ManyToOne
    public PunchList punchList;

    @Column(length = 65535)
    public String response;

    @Required
    @CRUD.Hidden
    @ManyToOne
    public User author;

    @Required
    public Date createDate;

    public Long tempId;

    @OneToMany(mappedBy = "reply", cascade = CascadeType.ALL)
    public List<PunchList_Attach> attaches;

    @CRUD.Hidden
    @OneToMany(mappedBy = "reply", cascade = CascadeType.ALL)
    public List<DrawingLayer> pins;

    @CRUD.Hidden
    @OneToMany(mappedBy = "reply", cascade = CascadeType.ALL)
    public List<NotificationMessage> notificationMessages;

}
