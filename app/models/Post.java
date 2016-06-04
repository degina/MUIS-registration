package models;

import controllers.CRUD;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by Enkh-Amgalan on 5/3/15.
 */
@Entity(name = "tb_post")
public class Post extends Model {

    @Required
    @Column(length = 65535)
    public String content;

    @Required
    public String type = null;

    public Long typeModelId = null;

    public String typeIconName = null;

    @CRUD.Hidden
    public Date createdDate;

    @CRUD.Hidden
    public Date activeDate;

    @ManyToOne
    public User owner;

    public Long likes=0l;

    public String likeUsers="";

    public Boolean pin = false;

    public boolean seeAll = false;

    @CRUD.Hidden
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    public List<PostComment> comments;

    @CRUD.Hidden
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    public List<PostSeeUser> seeUsers;

    @ManyToOne
    public Event event;

    @CRUD.Hidden
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    public List<PostAttach> attaches;

    @CRUD.Hidden
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    public List<PostChoice> choices;

    public boolean create() {
        this.createdDate = new Date();
        this.activeDate = new Date();
        return super.create();
    }
    public List<PostComment> getLastTwoComment(){
        List<PostComment> commentTwo = PostComment.find("post.id=?1 ORDER BY createdDate desc",this.id).fetch(1,2);
        Collections.reverse(commentTwo);
        return commentTwo;
    }
    public PostAttach getFirstAttach(){
        return PostAttach.find("post.id=?1 ORDER BY createdDate desc",this.id).first();
    }
    public List<PostAttach> getFirstFiveAttach(){
        List<PostAttach> postAttaches = PostAttach.find("post.id=?1 ORDER BY createdDate desc",this.id).fetch(1,5);
        if (postAttaches.size()>0)
            postAttaches.remove(0);
        return postAttaches;
    }
    public String getShortContent(){
        if (content !=null && content.length() > 1000 ){
         return content.substring(0,1000)+"... </br> <span class='post-read-more' onclick=readMorePost("+this.id+") >Цааш нь унших</span>";
        }
        else
            return content;
    }

    public Long attachSize(){
        return PostAttach.count("post.id=?1",this.id);
    }
}
