package models;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * Created by enkhamgalan on 3/12/15.
 */
@Entity(name = "tb_reportsummary")
public class ReportSummary extends Model {

    @Required
    @Column(length = 65535)
    public String executiveSummary;

    public int timeType;

    public Date date;

    @ManyToOne
    public Task task;

    @ManyToOne
    public DailyLogMyPlan myPlan;
}
