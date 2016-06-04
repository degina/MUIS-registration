package models;

import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Created by Shagai on 2015-07-06.
 */
@Entity(name = "tb_post_choice_vote")
public class PostChoiceVote extends Model {

    @ManyToOne
    public PostChoice choice;

    @ManyToOne
    public User user;
}
