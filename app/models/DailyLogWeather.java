package models;

import controllers.CRUD;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by Enkh-Amgalan on 2/13/15.
 */
@Entity(name = "tb_dailylog_weather")
public class DailyLogWeather extends Model {

    @Required
    public Date startDate;

    @Required
    public Date finishDate;

    @Column(length = 65535)
    public String notes;

    public Long tempId=0l;

    @Required
    public Date date;

    @Required
    @ManyToOne
    public User owner;

    @Required
    @ManyToOne
    public WeatherCondition weatherCondition;

    @ManyToOne
    public Task task;

    @ManyToOne
    public DailyLogMyPlan myPlan;

    @CRUD.Hidden
    @OneToMany(mappedBy = "weather", cascade = CascadeType.ALL)
    public List<DailyLogWeatherAttach> attaches;

    public DailyLogWeather(WeatherCondition weatherCondition,Date date, Date startDate, Date finishDate,String notes,Task task,User owner) {
        this.weatherCondition = weatherCondition;
        this.startDate = startDate;
        this.date = date;
        this.finishDate = finishDate;
        this.notes = notes;
        this.task = task;
        this.owner = owner;
    }
    public DailyLogWeather(WeatherCondition weatherCondition,Date date, Date startDate, Date finishDate,String notes,DailyLogMyPlan myPlan,User owner) {
        this.weatherCondition = weatherCondition;
        this.startDate = startDate;
        this.date = date;
        this.finishDate = finishDate;
        this.notes = notes;
        this.myPlan = myPlan;
        this.owner = owner;
    }

}