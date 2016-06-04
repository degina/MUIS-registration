package models;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

/**
 * Created by Enkh-Amgalan on 2/14/15.
 */
@Entity(name = "tb_weather_condition")
public class WeatherCondition extends Model {

    @Required
    public String conditionWeather;

    @OneToMany(mappedBy = "weatherCondition", cascade = CascadeType.ALL)
    public List<DailyLogWeather> dailyLogWeathers;
}
