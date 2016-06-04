package models;

import play.data.validation.Required;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Created by Enkh-Amgalan on 5/3/15.
 */
@Entity(name = "tb_post_attach")
public class PostAttach extends ModelAttach {
    @Required
    public String name;

    @Required
    public String dir;

    @Required
    public String extension;

    @Required
    @ManyToOne
    public Post post;

    public PostAttach(String name, String dir, String extension,Float filesize, Post post) {
        this.name = name;
        this.dir = dir;
        this.filesize = filesize;
        this.extension = extension;
        this.post = post;
    }
}
