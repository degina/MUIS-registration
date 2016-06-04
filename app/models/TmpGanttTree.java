package models;

import play.data.validation.Max;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * Created by enkhamgalan on 5/18/15.
 */
@Entity(name = "tb_tmp_gantt")
public class TmpGanttTree extends Model {

    public String name;

    public Date startDate;

    public int duration;

    public int level;

    @Max(100)
    public Float scopePercent;

    public String depends;

    public String status;
    public String workCount;
    public boolean startIsMilestone;

    @ManyToOne
    public TmpGantt tmpGantt;
}
