package models;

import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Created by Enkh-Amgalan on 5/3/15.
 */
@Entity(name = "tb_post_see_user")
public class PostSeeUser extends Model {

    @ManyToOne
    public User user;

    @ManyToOne
    public Post post;

    public boolean likeThisPost = false;

    public PostSeeUser(User user, Post post) {
        this.user = user;
        this.post = post;
    }
}
