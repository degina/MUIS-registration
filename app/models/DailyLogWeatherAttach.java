package models;

import play.data.validation.Required;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * Created by Enkh-Amgalan on 4/14/15.
 */
@Entity(name = "tb_dailylog_weather_attach")
public class DailyLogWeatherAttach extends ModelAttach {
    @Required
    public String name;

    @Required
    public String dir;

    @Required
    public String extension;

    @ManyToOne
    public DailyLogWeather weather;

    public DailyLogWeatherAttach(String name, String dir, String extension, Float filesize, DailyLogWeather weather) {
        this.name = name;
        this.dir = dir;
        this.extension = extension;
        this.filesize = filesize;
        this.weather = weather;
        this.createdDate = new Date();
    }
}
