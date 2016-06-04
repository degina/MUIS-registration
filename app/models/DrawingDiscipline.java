package models;

import controllers.CRUD;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Rina on 3/28/2015.
 */
@Entity(name = "tb_drawingDiscipline")
public class DrawingDiscipline extends Model {

    @Required
    @Column(length = 65535)
    public String name;

    @CRUD.Hidden
    @ManyToOne
    public Project project;

    @CRUD.Hidden
    @OneToMany(mappedBy = "discipline", cascade = CascadeType.ALL)
    public List<Drawing> drawings;

    public DrawingDiscipline(){

    }

    public DrawingDiscipline(String name_, Long pid){
        name = name_;
        project = Project.findById(pid);
    }
//
//    public int isDelete() {
//        if (drawings != null) {
//            for (int i = 0; i < drawings.size(); i++) {
//                if (drawings.get(i).pins != null) {
//                    for (int j = 0; j < drawings.get(i).pins.size(); j++) {
//                        if (drawings.get(i).pins.get(j).rfi != null || drawings.get(i).pins.get(j).punchList != null ||
//                                drawings.get(i).pins.get(j).tracking != null || drawings.get(i).pins.get(j).reply != null) {
//                            return 0;
//                        }
//                    }
//                }
//            }
//            return 1;
//        }
//        return 1;
//    }
}
