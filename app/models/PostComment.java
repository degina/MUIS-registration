package models;

import controllers.CRUD;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * Created by Enkh-Amgalan on 5/3/15.
 */
@Entity(name = "tb_post_comment")
public class PostComment extends Model {

    @Required
    @Column(length = 65535)
    public String comment;

    @CRUD.Hidden
    public Date createdDate;

    @ManyToOne
    public User owner;

    @ManyToOne
    public Post post;

    public boolean create() {
        this.createdDate = new Date();
        this.post.activeDate = createdDate;
        return super.create();
    }

    public PostComment(String comment, Post post, User owner) {
        this.comment = comment;
        this.post = post;
        this.post.activeDate = new Date();
        this.owner = owner;
    }
}
