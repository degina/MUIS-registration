package controllers.App;

import com.google.gson.*;
import controllers.Consts;
import models.*;

import java.util.List;

/**
 * Created by Enkhbayar on 7/2/2015.
 */
public class DelegatePost extends Delegate {

    public static void postList(String body) {
        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            Long userId = user.id;
            int postIndex = json.get("index").getAsInt();
            List<Post> posts = Post.find("SELECT DISTINCT p FROM tb_post p LEFT JOIN p.seeUsers AS u WHERE p.seeAll=true OR (p.owner.id=?1 OR (u.post.id=p.id AND u.user.id=?2))  ORDER BY p.activeDate desc", userId, userId).fetch(postIndex, 10);
            JsonArray responseArray = new JsonArray();

            for (Post post : posts) {
                JsonObject object = new JsonObject();
                object.addProperty("id", post.id);
                object.addProperty("createDate", Consts.myDateFormat.format(post.createdDate));
                object.addProperty("ownerId", post.owner.id + "");
                object.addProperty("ownerName", post.owner.toString());
                object.addProperty("postType", (post.type == null) ? "" : post.type);
                object.addProperty("content", post.content);
                object.addProperty("activeDate", Consts.myDateFormat.format(post.activeDate));
                object.addProperty("likes", post.likes + "");
                object.addProperty("likeUsers", post.likeUsers);
                object.addProperty("comments", post.comments.size());

                JsonArray files = new JsonArray();
                for (PostAttach attach : post.attaches) {
                    files.add(new JsonPrimitive(attach.dir.replace("/", "-") + "." + attach.extension));
                }
                object.add("filenames", files);
//                JsonArray comments = new JsonArray();
//                for (PostComment comment : post.comments) {
//                    JsonObject comObject = new JsonObject();
//                    comObject.addProperty("id", comment.id);
//                    comObject.addProperty("ownerId", comment.owner.id);
//                    comObject.addProperty("ownerName", comment.owner.toString());
//                    comObject.addProperty("createDate", Consts.myDateFormat.format(comment.createdDate));
//                    comObject.addProperty("content", comment.comment);
//                    comments.add(comObject);
//                }
                responseArray.add(object);
            }
            renderJSON(responseArray);
        }
    }

    public static void commentList(String body) {
        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            Long postId = json.get("postId").getAsLong();
            JsonArray responseObj = new JsonArray();
            Post post = Post.findById(postId);
            if (post.comments != null)
                for (PostComment comment : post.comments) {
                    JsonObject comObject = new JsonObject();
                    comObject.addProperty("id", comment.id);
                    comObject.addProperty("ownerId", comment.owner.id);
                    comObject.addProperty("ownerName", comment.owner.toString());
                    comObject.addProperty("createDate", Consts.myDateFormat.format(comment.createdDate));
                    comObject.addProperty("content", comment.comment);
                    responseObj.add(comObject);
                }
            renderJSON(responseObj);
        }
    }

    public static void commentNew(String body) {
        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            JsonObject commentObj = json.get("PostComment").getAsJsonObject();
            Long postId = commentObj.get("postId").getAsLong();
            JsonObject responseObj = new JsonObject();
            Post post = Post.findById(postId);
            PostComment comment = new PostComment(commentObj.get("content").getAsString(), post, user);
            comment.create();
            post.activeDate = comment.createdDate;
            post._save();
            responseObj.addProperty("newId", comment.id);
            responseObj.addProperty("createdDate", Consts.myDateFormat.format(comment.createdDate));
            renderJSON(responseObj);
        }
    }

    public static void likePost(String body) {
        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            JsonObject commentObj = json.get("PostLike").getAsJsonObject();
            Long postId = commentObj.get("postId").getAsLong();
            JsonObject responseObj = new JsonObject();
            Post post = Post.findById(postId);
            String userName = user.toString();
            if (post.seeAll) {
                if (!post.likeUsers.contains(userName)) {
                    post.likeUsers += (userName + "\n");
                    post.likes += 1;
                    post._save();
                }
            } else {
                PostSeeUser seeUser = PostSeeUser.find("post.id=?1 AND user.id=?2", postId, user.id).first();
                if (seeUser != null) {
                    if (seeUser.likeThisPost == false) {
                        seeUser.likeThisPost = true;
                        seeUser._save();
                        post.likeUsers += (userName + "\n");
                        post.likes += 1;
                        post._save();
                    }
                } else if (post.owner.id == user.id) {
                    if (!post.likeUsers.contains(userName)) {
                        post.likeUsers += (userName + "\n");
                        post.likes += 1;
                        post._save();
                    }
                }
            }
            responseObj.addProperty("likes", post.likes);
            responseObj.addProperty("likeUsers", post.likeUsers);
            renderJSON(responseObj);
        }
    }

    public static void postNew(String body) {
        JsonObject json = (JsonObject) new JsonParser().parse(body);
        User user = getUser(json.get("session").getAsString());
        if (user != null) {
            JsonObject postObj = json.get("Post").getAsJsonObject();
            Long postId = postObj.get("postId").getAsLong();
            JsonObject responseObj = new JsonObject();
            Post post;
            if (postId == 0) {
                post = new Post();
                post.owner = user;
                post.content = postObj.get("content").getAsString();
                boolean seeAll = postObj.get("seeAll").getAsBoolean();
                post.seeAll = seeAll;
                post.create();
                if (!seeAll) {
                    JsonArray userArray = postObj.get("seeUserIds").getAsJsonArray();
                    for (JsonElement jsonElement : userArray) {
                        String seeUserId = jsonElement.getAsString();
                        User userO = User.findById(Long.parseLong(seeUserId));
                        PostSeeUser seeUser = new PostSeeUser(userO, post);
                        seeUser.create();
                    }
                }
                JsonArray filenames = postObj.get("filenames").getAsJsonArray();
                if (filenames.size() > 0) {
                    for (int i = 0; i < filenames.size(); i++) {
                        JsonPrimitive ids = filenames.get(i).getAsJsonPrimitive();
                        String[] file = ids.getAsString().split("\\.");
                        String[] fileName = file[0].split("/");
                        PostAttach attach = new PostAttach(fileName[fileName.length - 1], file[0], file[1],null, post);
                        attach.create();
                    }
                }
            } else {
                post = Post.findById(postId);
                post.content = postObj.get("content").getAsString();
                post.save();
            }

            responseObj.addProperty("newId", post.id);
            responseObj.addProperty("createDate", Consts.myDateFormat.format(post.createdDate));
            renderJSON(responseObj);
        }
    }
}
