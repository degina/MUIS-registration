package models;

import controllers.CRUD;
import play.db.jpa.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.List;


/**
 * Created by Shagai on 2015-07-04.
 */
@Entity(name = "tb_post_choice")
public class PostChoice extends Model {

    public String choice;

    public int procent = 0;

    @ManyToOne
    public Post post;

    @CRUD.Hidden
    @OneToMany(mappedBy = "choice", cascade = CascadeType.ALL)
    public List<PostChoiceVote> votes;

    public List<PostChoiceVote> getVotes(){
        List<PostChoiceVote> votes = PostChoiceVote.find("choice.id=?1", this.id).fetch(1,3);
        return votes;
    }

    public List<PostChoiceVote> getVotesAll(){
        List<PostChoiceVote> votes = PostChoiceVote.find("choice.id=?1", this.id).fetch();
        return votes;
    }

    public int getVotesSize(){
        return (int) PostChoiceVote.count("choice.id=?1", this.id);
    }

    public String userVote(Long userId, Long choiceId){
        return !PostChoiceVote.find("user.id=?1 and choice.id=?2", userId, choiceId).fetch().isEmpty() ? "checked" : "";
    }
    public String userVoteColor(Long userId, Long choiceId){
        return !PostChoiceVote.find("user.id=?1 and choice.id=?2", userId, choiceId).fetch().isEmpty() ? "progress-bar-danger" : "progress-bar-info";
    }
}
