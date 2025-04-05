package fctreddit.clients.rest;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import fctreddit.api.java.Result;
import fctreddit.api.java.Result.ErrorCode;
import fctreddit.api.rest.RestImage;
import fctreddit.api.rest.RestUsers;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import java.net.URI;
import java.util.logging.Logger;

public class RestImageClient {
    private static Logger Log = Logger.getLogger(RestImageClient.class.getName());

    protected static final int READ_TIMEOUT = 5000;
    protected static final int CONNECT_TIMEOUT = 5000;

    protected static final int MAX_RETRIES = 10;
    protected static final int RETRY_SLEEP = 5000;

    final URI serverURI;
    final Client client;
    final ClientConfig config;

    final WebTarget target;

    public RestImageClient(URI serverURI) {
        this.serverURI = serverURI;

        this.config = new ClientConfig();

        config.property(ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
        config.property(ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);

        this.client = ClientBuilder.newClient(config);

        target = client.target(serverURI).path(RestImage.PATH);
    }

    public Result<String> createImage(String userId, String pwd, byte[] imageContents) {
        for(int i = 0; i < MAX_RETRIES; i++) {
            try {

                Response r = target.path( userId )
                        .queryParam(RestUsers.PASSWORD, pwd).request()
                        .accept( MediaType.APPLICATION_JSON)
                        .post(Entity.entity(imageContents, MediaType.APPLICATION_OCTET_STREAM));

                int status = r.getStatus();
                if(status != Status.OK.getStatusCode())
                    return Result.error(getErrorCodeFrom(status));
                else
                    return Result.ok(r.readEntity(String.class));

            } catch(ProcessingException x) {
                Log.info(x.getMessage());

                try {
                    Thread.sleep(RETRY_SLEEP);
                } catch (InterruptedException e) {
                    // Nothing to be done here
                }
            }
            catch(Exception x) {
                x.printStackTrace();
            }
        }
        return Result.error(ErrorCode.TIMEOUT);
    }

    public Result<byte[]> getImage(String userId, String imageId) {
        for(int i = 0; i < MAX_RETRIES; i++) {
            try {
                Response r = target.path(userId).path(imageId).request()
                        .accept(MediaType.APPLICATION_OCTET_STREAM)
                        .get();

                int status = r.getStatus();
                if(status != Status.OK.getStatusCode())
                    return Result.error(getErrorCodeFrom(status));
                else
                    return Result.ok(r.readEntity(byte[].class));

            } catch(ProcessingException x) {
                Log.info(x.getMessage());

                try {
                    Thread.sleep(RETRY_SLEEP);
                } catch (InterruptedException e) {
                    // Nothing to be done here
                }
            }
            catch(Exception x) {
                x.printStackTrace();
            }
        }
        return Result.error(ErrorCode.TIMEOUT);
    }

    public Result<Void> deleteImage(String userId, String imageId, String pwd) {
        for(int i = 0; i < MAX_RETRIES; i++) {
            try {
                Response r = target.path(userId).path(imageId)
                        .queryParam(RestUsers.PASSWORD, pwd).request()
                        .accept( MediaType.APPLICATION_JSON)
                        .delete();

                int status = r.getStatus();
                if(status != Status.NO_CONTENT.getStatusCode() && status != Status.OK.getStatusCode())
                    return Result.error(getErrorCodeFrom(status));
                else
                    return Result.ok();

            } catch(ProcessingException x) {
                Log.info(x.getMessage());

                try {
                    Thread.sleep(RETRY_SLEEP);
                } catch (InterruptedException e) {
                    // Nothing to be done here
                }
            }
            catch(Exception x) {
                x.printStackTrace();
            }
        }
        return Result.error(ErrorCode.TIMEOUT);
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