package models;

import play.db.jpa.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

/**
 * Created by enkhamgalan on 5/18/15.
 */
@Entity(name = "tb_tmp_gantttree")
public class TmpGantt extends Model {

    public String name;

    public boolean project;

    @OneToMany(mappedBy = "tmpGantt", cascade = CascadeType.ALL)
    public List<TmpGanttTree> tmpGanttTrees;
}
