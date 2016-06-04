package models;

import controllers.CRUD;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by Enkh-Amgalan on 2/14/15.
 */
@Entity(name = "tb_dailylog_delivery")
public class DailyLogDelivery extends Model {
    @Required
    public Date deliveryHour;

    @Required
    public String deliveryFrom;

    public String trackingNumber;

    public String content;

    @Column(length = 65535)
    public String comments;

    public Long tempId=0l;

    @Required
    public Date date;

    @Required
    @ManyToOne
    public User owner;

    @ManyToOne
    public Task task;

    @ManyToOne
    public DailyLogMyPlan myPlan;

    @CRUD.Hidden
    @OneToMany(mappedBy = "delivery", cascade = CascadeType.ALL)
    public List<DailyLogDeliveryAttach> attaches;

    public DailyLogDelivery(Date deliveryHour, String deliveryFrom, String trackingNumber, String content, String comments, Date date, User owner, Task task) {
        this.deliveryHour = deliveryHour;
        this.deliveryFrom = deliveryFrom;
        this.trackingNumber = trackingNumber;
        this.content = content;
        this.comments = comments;
        this.date = date;
        this.owner = owner;
        this.task = task;
    }

    public DailyLogDelivery(Date deliveryHour, String deliveryFrom, String trackingNumber, String content, String comments, Date date, User owner, DailyLogMyPlan myPlan) {
        this.deliveryHour = deliveryHour;
        this.deliveryFrom = deliveryFrom;
        this.trackingNumber = trackingNumber;
        this.content = content;
        this.comments = comments;
        this.date = date;
        this.owner = owner;
        this.myPlan = myPlan;
    }
}
