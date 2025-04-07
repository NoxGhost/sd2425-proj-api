package fctreddit.clients.rest;

import fctreddit.api.Post;
import fctreddit.api.java.Content;
import fctreddit.api.java.Result;
import fctreddit.api.rest.RestContent;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

public class RestContentClient extends RestClient implements Content {
    private static final Logger Log = Logger.getLogger(RestContentClient.class.getName());

    final WebTarget target;

    public RestContentClient(URI serverURI) {
        super(serverURI);
        target = client.target(serverURI).path(RestContent.PATH);
    }

    // Private methods that implement the actual API calls

    private Result<String> clt_createPost(Post post, String userPassword) {
        Response r = target.queryParam(RestContent.PASSWORD, userPassword)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.entity(post, MediaType.APPLICATION_JSON));

        return super.toJavaResult(r, String.class);
    }

    private Result<List<String>> clt_getPosts(long timestamp, String sortOrder) {
        WebTarget t = target;

        if (timestamp > 0)
            t = t.queryParam(RestContent.TIMESTAMP, timestamp);

        if (sortOrder != null)
            t = t.queryParam(RestContent.SORTBY, sortOrder);

        Response r = t.request()
                .accept(MediaType.APPLICATION_JSON)
                .get();

        try {
            int status = r.getStatus();
            if (status != Response.Status.OK.getStatusCode())
                return Result.error(getErrorCodeFrom(status));
            else
                return Result.ok(r.readEntity(new GenericType<List<String>>() {}));
        } finally {
            r.close();
        }
    }

    private Result<Post> clt_getPost(String postId) {
        Response r = target.path(postId)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .get();

        return super.toJavaResult(r, Post.class);
    }

    private Result<List<String>> clt_getPostAnswers(String postId) {
        Response r = target.path(postId).path(RestContent.REPLIES)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .get();

        try {
            int status = r.getStatus();
            if (status != Response.Status.OK.getStatusCode())
                return Result.error(getErrorCodeFrom(status));
            else
                return Result.ok(r.readEntity(new GenericType<List<String>>() {}));
        } finally {
            r.close();
        }
    }

    private Result<Post> clt_updatePost(String postId, String userPassword, Post post) {
        Response r = target.path(postId)
                .queryParam(RestContent.PASSWORD, userPassword)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .put(Entity.entity(post, MediaType.APPLICATION_JSON));

        return super.toJavaResult(r, Post.class);
    }

    private Result<Void> clt_deletePost(String postId, String userPassword) {
        Response r = target.path(postId)
                .queryParam(RestContent.PASSWORD, userPassword)
                .request()
                .delete();

        int status = r.getStatus();
        if (status != Response.Status.NO_CONTENT.getStatusCode())
            return Result.error(getErrorCodeFrom(status));
        else
            return Result.ok();
    }

    private Result<Void> clt_upVotePost(String postId, String userId, String userPassword) {
        Response r = target.path(postId)
                .path(RestContent.UPVOTE)
                .path(userId)
                .queryParam(RestContent.PASSWORD, userPassword)
                .request()
                .post(null);

        int status = r.getStatus();
        if (status != Response.Status.NO_CONTENT.getStatusCode())
            return Result.error(getErrorCodeFrom(status));
        else
            return Result.ok();
    }

    private Result<Void> clt_removeUpVotePost(String postId, String userId, String userPassword) {
        Response r = target.path(postId)
                .path(RestContent.UPVOTE)
                .path(userId)
                .queryParam(RestContent.PASSWORD, userPassword)
                .request()
                .delete();

        int status = r.getStatus();
        if (status != Response.Status.NO_CONTENT.getStatusCode())
            return Result.error(getErrorCodeFrom(status));
        else
            return Result.ok();
    }

    private Result<Void> clt_downVotePost(String postId, String userId, String userPassword) {
        Response r = target.path(postId)
                .path(RestContent.DOWNVOTE)
                .path(userId)
                .queryParam(RestContent.PASSWORD, userPassword)
                .request()
                .post(null);

        int status = r.getStatus();
        if (status != Response.Status.NO_CONTENT.getStatusCode())
            return Result.error(getErrorCodeFrom(status));
        else
            return Result.ok();
    }

    private Result<Void> clt_removeDownVotePost(String postId, String userId, String userPassword) {
        Response r = target.path(postId)
                .path(RestContent.DOWNVOTE)
                .path(userId)
                .queryParam(RestContent.PASSWORD, userPassword)
                .request()
                .delete();

        int status = r.getStatus();
        if (status != Response.Status.NO_CONTENT.getStatusCode())
            return Result.error(getErrorCodeFrom(status));
        else
            return Result.ok();
    }

    private Result<Integer> clt_getUpVotes(String postId) {
        Response r = target.path(postId)
                .path(RestContent.UPVOTE)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .get();

        return super.toJavaResult(r, Integer.class);
    }

    private Result<Integer> clt_getDownVotes(String postId) {
        Response r = target.path(postId)
                .path(RestContent.DOWNVOTE)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .get();

        return super.toJavaResult(r, Integer.class);
    }

    // Public API methods that use retry logic

    public Result<String> createPost(Post post, String userPassword) {
        return super.reTry(() -> clt_createPost(post, userPassword));
    }

    public Result<List<String>> getPosts(long timestamp, String sortOrder) {
        return super.reTry(() -> clt_getPosts(timestamp, sortOrder));
    }

    public Result<Post> getPost(String postId) {
        // Original implementation didn't use retry logic for this method
        try {
            return clt_getPost(postId);
        } catch (Exception x) {
            x.printStackTrace();
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public Result<List<String>> getPostAnswers(String postId, long maxTimeout) {
        return null;
    }

    public Result<List<String>> getPostAnswers(String postId) {
        return super.reTry(() -> clt_getPostAnswers(postId));
    }

    public Result<Post> updatePost(String postId, String userPassword, Post post) {
        return super.reTry(() -> clt_updatePost(postId, userPassword, post));
    }

    public Result<Void> deletePost(String postId, String userPassword) {
        return super.reTry(() -> clt_deletePost(postId, userPassword));
    }

    public Result<Void> upVotePost(String postId, String userId, String userPassword) {
        return super.reTry(() -> clt_upVotePost(postId, userId, userPassword));
    }

    public Result<Void> removeUpVotePost(String postId, String userId, String userPassword) {
        return super.reTry(() -> clt_removeUpVotePost(postId, userId, userPassword));
    }

    public Result<Void> downVotePost(String postId, String userId, String userPassword) {
        return super.reTry(() -> clt_downVotePost(postId, userId, userPassword));
    }

    public Result<Void> removeDownVotePost(String postId, String userId, String userPassword) {
        return super.reTry(() -> clt_removeDownVotePost(postId, userId, userPassword));
    }

    @Override
    public Result<Integer> getupVotes(String postId) {
        return null;
    }

    public Result<Integer> getUpVotes(String postId) {
        // Original implementation didn't use retry logic for this method
        try {
            return clt_getUpVotes(postId);
        } catch (Exception x) {
            x.printStackTrace();
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
    }

    public Result<Integer> getDownVotes(String postId) {
        // Original implementation didn't use retry logic for this method
        try {
            return clt_getDownVotes(postId);
        } catch (Exception x) {
            x.printStackTrace();
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
    }
}