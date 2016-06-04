package models;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Created by enkhamgalan on 12/20/14.
 */
@Entity(name = "tb_task_assign_rel")
public class TaskAssignRel extends Model {

    @Required
    @ManyToOne
    public Task task;

    @Required
    @ManyToOne
    public User user;

    public Float hours;

    public int getCount(String uid, String percent) {
        if (percent == null || percent.length() == 0) percent = "100";
        return TaskAssignRel.find("user.id=?1 and task.completedPercent<?2 and" +
                " task.id=?3 order by task.startDate", Long.parseLong(uid), Float.parseFloat(percent), this.id).fetch().size();
    }
}
