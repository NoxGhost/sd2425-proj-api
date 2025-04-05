package fctreddit.clients.rest;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import fctreddit.api.java.Result;
import fctreddit.api.java.Result.ErrorCode;
import fctreddit.api.Post;
import fctreddit.api.rest.RestContent;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

public class RestContentClient {
    private static Logger Log = Logger.getLogger(RestContentClient.class.getName());

    protected static final int READ_TIMEOUT = 5000;
    protected static final int CONNECT_TIMEOUT = 5000;

    protected static final int MAX_RETRIES = 10;
    protected static final int RETRY_SLEEP = 5000;

    final URI serverURI;
    final Client client;
    final ClientConfig config;

    final WebTarget target;

    public RestContentClient(URI serverURI) {
        this.serverURI = serverURI;

        this.config = new ClientConfig();

        config.property(ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
        config.property(ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);

        this.client = ClientBuilder.newClient(config);

        target = client.target(serverURI).path(RestContent.PATH);
    }

    public Result<String> createPost(Post post, String userPassword) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                Response r = target.queryParam(RestContent.PASSWORD, userPassword)
                        .request()
                        .accept(MediaType.APPLICATION_JSON)
                        .post(Entity.entity(post, MediaType.APPLICATION_JSON));

                int status = r.getStatus();
                if (status != Status.OK.getStatusCode())
                    return Result.error(getErrorCodeFrom(status));
                else
                    return Result.ok(r.readEntity(String.class));

            } catch (ProcessingException x) {
                Log.info(x.getMessage());

                try {
                    Thread.sleep(RETRY_SLEEP);
                } catch (InterruptedException e) {
                    // Nothing to be done here.
                }
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
        return Result.error(ErrorCode.TIMEOUT);
    }

    public Result<List<String>> getPosts(long timestamp, String sortOrder) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                WebTarget t = target;

                if (timestamp > 0)
                    t = t.queryParam(RestContent.TIMESTAMP, timestamp);

                if (sortOrder != null)
                    t = t.queryParam(RestContent.SORTBY, sortOrder);

                Response r = t.request()
                        .accept(MediaType.APPLICATION_JSON)
                        .get();

                int status = r.getStatus();
                if (status != Status.OK.getStatusCode())
                    return Result.error(getErrorCodeFrom(status));
                else
                    return Result.ok(r.readEntity(new GenericType<List<String>>() {}));

            } catch (ProcessingException x) {
                Log.info(x.getMessage());

                try {
                    Thread.sleep(RETRY_SLEEP);
                } catch (InterruptedException e) {
                    // Nothing to be done here.
                }
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
        return Result.error(ErrorCode.TIMEOUT);
    }

    public Result<Post> getPost(String postId) {
        try {
            Response r = target.path(postId)
                    .request()
                    .accept(MediaType.APPLICATION_JSON)
                    .get();

            int status = r.getStatus();
            if (status != Status.OK.getStatusCode())
                return Result.error(getErrorCodeFrom(status));
            else
                return Result.ok(r.readEntity(Post.class));

        } catch (Exception x) {
            x.printStackTrace();
            return Result.error(ErrorCode.INTERNAL_ERROR);
        }
    }

    public Result<List<String>> getPostAnswers(String postId) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                Response r = target.path(postId).path(RestContent.REPLIES)
                        .request()
                        .accept(MediaType.APPLICATION_JSON)
                        .get();

                int status = r.getStatus();
                if (status != Status.OK.getStatusCode())
                    return Result.error(getErrorCodeFrom(status));
                else
                    return Result.ok(r.readEntity(new GenericType<List<String>>() {}));

            } catch (ProcessingException x) {
                Log.info(x.getMessage());

                try {
                    Thread.sleep(RETRY_SLEEP);
                } catch (InterruptedException e) {
                    // Nothing to be done here.
                }
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
        return Result.error(ErrorCode.TIMEOUT);
    }

    public Result<Post> updatePost(String postId, String userPassword, Post post) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                Response r = target.path(postId)
                        .queryParam(RestContent.PASSWORD, userPassword)
                        .request()
                        .accept(MediaType.APPLICATION_JSON)
                        .put(Entity.entity(post, MediaType.APPLICATION_JSON));

                int status = r.getStatus();
                if (status != Status.OK.getStatusCode())
                    return Result.error(getErrorCodeFrom(status));
                else
                    return Result.ok(r.readEntity(Post.class));

            } catch (ProcessingException x) {
                Log.info(x.getMessage());

                try {
                    Thread.sleep(RETRY_SLEEP);
                } catch (InterruptedException e) {
                    // Nothing to be done here.
                }
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
        return Result.error(ErrorCode.TIMEOUT);
    }

    public Result<Void> deletePost(String postId, String userPassword) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                Response r = target.path(postId)
                        .queryParam(RestContent.PASSWORD, userPassword)
                        .request()
                        .delete();

                int status = r.getStatus();
                if (status != Status.NO_CONTENT.getStatusCode())
                    return Result.error(getErrorCodeFrom(status));
                else
                    return Result.ok();

            } catch (ProcessingException x) {
                Log.info(x.getMessage());

                try {
                    Thread.sleep(RETRY_SLEEP);
                } catch (InterruptedException e) {
                    // Nothing to be done here.
                }
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
        return Result.error(ErrorCode.TIMEOUT);
    }

    public Result<Void> upVotePost(String postId, String userId, String userPassword) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                Response r = target.path(postId)
                        .path(RestContent.UPVOTE)
                        .path(userId)
                        .queryParam(RestContent.PASSWORD, userPassword)
                        .request()
                        .post(null);

                int status = r.getStatus();
                if (status != Status.NO_CONTENT.getStatusCode())
                    return Result.error(getErrorCodeFrom(status));
                else
                    return Result.ok();

            } catch (ProcessingException x) {
                Log.info(x.getMessage());

                try {
                    Thread.sleep(RETRY_SLEEP);
                } catch (InterruptedException e) {
                    // Nothing to be done here.
                }
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
        return Result.error(ErrorCode.TIMEOUT);
    }

    public Result<Void> removeUpVotePost(String postId, String userId, String userPassword) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                Response r = target.path(postId)
                        .path(RestContent.UPVOTE)
                        .path(userId)
                        .queryParam(RestContent.PASSWORD, userPassword)
                        .request()
                        .delete();

                int status = r.getStatus();
                if (status != Status.NO_CONTENT.getStatusCode())
                    return Result.error(getErrorCodeFrom(status));
                else
                    return Result.ok();

            } catch (ProcessingException x) {
                Log.info(x.getMessage());

                try {
                    Thread.sleep(RETRY_SLEEP);
                } catch (InterruptedException e) {
                    // Nothing to be done here.
                }
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
        return Result.error(ErrorCode.TIMEOUT);
    }

    public Result<Void> downVotePost(String postId, String userId, String userPassword) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                Response r = target.path(postId)
                        .path(RestContent.DOWNVOTE)
                        .path(userId)
                        .queryParam(RestContent.PASSWORD, userPassword)
                        .request()
                        .post(null);

                int status = r.getStatus();
                if (status != Status.NO_CONTENT.getStatusCode())
                    return Result.error(getErrorCodeFrom(status));
                else
                    return Result.ok();

            } catch (ProcessingException x) {
                Log.info(x.getMessage());

                try {
                    Thread.sleep(RETRY_SLEEP);
                } catch (InterruptedException e) {
                    // Nothing to be done here.
                }
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
        return Result.error(ErrorCode.TIMEOUT);
    }

    public Result<Void> removeDownVotePost(String postId, String userId, String userPassword) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                Response r = target.path(postId)
                        .path(RestContent.DOWNVOTE)
                        .path(userId)
                        .queryParam(RestContent.PASSWORD, userPassword)
                        .request()
                        .delete();

                int status = r.getStatus();
                if (status != Status.NO_CONTENT.getStatusCode())
                    return Result.error(getErrorCodeFrom(status));
                else
                    return Result.ok();

            } catch (ProcessingException x) {
                Log.info(x.getMessage());

                try {
                    Thread.sleep(RETRY_SLEEP);
                } catch (InterruptedException e) {
                    // Nothing to be done here.
                }
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
        return Result.error(ErrorCode.TIMEOUT);
    }

    public Result<Integer> getUpVotes(String postId) {
        try {
            Response r = target.path(postId)
                    .path(RestContent.UPVOTE)
                    .request()
                    .accept(MediaType.APPLICATION_JSON)
                    .get();

            int status = r.getStatus();
            if (status != Status.OK.getStatusCode())
                return Result.error(getErrorCodeFrom(status));
            else
                return Result.ok(r.readEntity(Integer.class));

        } catch (Exception x) {
            x.printStackTrace();
            return Result.error(ErrorCode.INTERNAL_ERROR);
        }
    }

    public Result<Integer> getDownVotes(String postId) {
        try {
            Response r = target.path(postId)
                    .path(RestContent.DOWNVOTE)
                    .request()
                    .accept(MediaType.APPLICATION_JSON)
                    .get();

            int status = r.getStatus();
            if (status != Status.OK.getStatusCode())
                return Result.error(getErrorCodeFrom(status));
            else
                return Result.ok(r.readEntity(Integer.class));

        } catch (Exception x) {
            x.printStackTrace();
            return Result.error(ErrorCode.INTERNAL_ERROR);
        }
    }

    public static ErrorCode getErrorCodeFrom(int status) {
        return switch (status) {
            case 200, 204, 209 -> ErrorCode.OK;
            case 409 -> ErrorCode.CONFLICT;
            case 403 -> ErrorCode.FORBIDDEN;
            case 404 -> ErrorCode.NOT_FOUND;
            case 400 -> ErrorCode.BAD_REQUEST;
            case 500 -> ErrorCode.INTERNAL_ERROR;
            case 501 -> ErrorCode.NOT_IMPLEMENTED;
            default -> ErrorCode.INTERNAL_ERROR;
        };
    }
}