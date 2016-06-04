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
@Entity(name = "tb_punchlist")
public class PunchList extends Model {
    public Long No;

    @Required
    @Column(length = 65535)
    public String name;

    @Required
    @ManyToOne
    public User assignee;

    @Required
    public Date createDate;

    public Date dueDate;

    @Required
    public Date closedDate;

    @Column(length = 65535)
    public String description;
    @CRUD.Hidden
    @ManyToOne
    public Project project;


    @CRUD.Hidden
    @ManyToOne
    public Task task;

    @Column(length = 65535)
    public String location;

    @Column(length = 65535)
    public String reference;

    @ManyToOne
    public User questionReceivedFrom;

    @CRUD.Hidden
    @ManyToOne
    public PunchList_Status status;

    @CRUD.Hidden
    @ManyToOne
    public PunchList_Impact scheduleImpact;

    @CRUD.Hidden
    @ManyToOne
    public PunchList_Impact costImpact;

    @CRUD.Hidden
    @ManyToOne
    public PunchList_Priority priority;

    public boolean private_;
    public Long tempId;
    @CRUD.Hidden
    @OneToMany(mappedBy = "punchList", cascade = CascadeType.ALL)
    public List<PunchList_Attach> attaches;


    @OneToMany(mappedBy = "punchList", cascade = CascadeType.ALL)
    public List<PunchList_Reply> replies;

    @CRUD.Hidden
    @OneToMany(mappedBy = "punchList", cascade = CascadeType.ALL)
    public List<DrawingLayer> pins;

    @CRUD.Hidden
    @OneToMany(mappedBy = "punchList", cascade = CascadeType.ALL)
    public List<PunchList_Distribution> distributions;

    @CRUD.Hidden
    @OneToMany(mappedBy = "punchList", cascade = CascadeType.ALL)
    public List<NotificationMessage> notificationMessages;

    public boolean isRelated(Long id) {
        if (assignee.id.equals(id)) return true;
        if (questionReceivedFrom.id.equals(id)) return true;
        if (distributions != null) {
            User user = User.findById(id);
            if(distributions.indexOf(user) != -1)
                return true;
        }
        if (!private_) return true;
        return false;
    }
    public boolean isAllowReply(Long id){

        if(assignee.id.equals(id)) return true;
        if(questionReceivedFrom.id.equals(id)) return true;
        if(distributions != null){
            for (PunchList_Distribution dist: distributions){
                if(dist.code.equalsIgnoreCase("u")){
                    if(dist.user.id == id)
                        return true;
                } else {
                    User user = User.findById(id);
                    if(dist.userTeam.users.indexOf(user) != -1)
                        return true;
                }
            }
        }
        return false;
    }
}