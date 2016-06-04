package models;

import play.db.jpa.Model;

import javax.persistence.*;
import java.util.List;

/**
 * Created by enkhamgalan on 8/1/15.
 */
@Entity(name = "tb_email_folder")
public class EmailFolder extends Model {

    public String name;

    public String nameMng;

}
