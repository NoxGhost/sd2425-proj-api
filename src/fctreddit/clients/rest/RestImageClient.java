package fctreddit.clients.rest;

import fctreddit.api.java.Image;
import fctreddit.api.java.Result;
import fctreddit.api.rest.RestImage;
import fctreddit.api.rest.RestUsers;
import fctreddit.api.utils.Discovery;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.logging.Logger;

public class RestImageClient extends RestClient implements Image {
    private static final Logger Log = Logger.getLogger(RestImageClient.class.getName());

    final WebTarget target;

    public RestImageClient() throws Exception {
        this(discoverServiceURI());
    }

    public RestImageClient(URI uri) {
        super(uri);
        if (uri == null) {
            throw new IllegalArgumentException("URI cannot be null");
        }

        Log.info("Using server URI: " + uri);
        target = client.target(serverURI).path(RestImage.PATH);
    }

    private static URI discoverServiceURI() throws Exception {
        // Initialize Discovery
        Discovery discovery = Discovery.getInstance(Discovery.DISCOVERY_ADDR, null, null);
        discovery.start();

        // Find the service
        URI[] serviceURIs = discovery.knownUrisOf(RestImage.SERVICE_NAME, 1);

        // Use the first URI found
        return serviceURIs[0];
    }

    // Private methods that implement the actual API calls

    private Result<String> clt_createImage(String userId, String pwd, byte[] imageContents) {
        Response r = target.path(userId)
                .queryParam(RestUsers.PASSWORD, pwd).request()
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.entity(imageContents, MediaType.APPLICATION_OCTET_STREAM));

        return super.toJavaResult(r, String.class);
    }

    private Result<byte[]> clt_getImage(String userId, String imageId) {
        Response r = target.path(userId).path(imageId).request()
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .get();

        return super.toJavaResult(r, byte[].class);
    }

    private Result<Void> clt_deleteImage(String userId, String imageId, String pwd) {
        Response r = target.path(userId).path(imageId)
                .queryParam(RestUsers.PASSWORD, pwd).request()
                .accept(MediaType.APPLICATION_JSON)
                .delete();

        // Special handling for both OK and NO_CONTENT status
        int status = r.getStatus();
        if (status != Response.Status.NO_CONTENT.getStatusCode() && status != Response.Status.OK.getStatusCode())
            return Result.error(getErrorCodeFrom(status));
        else
            return Result.ok();
    }

    // Public API methods that use retry logic

    public Result<String> createImage(String userId, byte[] imageContents,String pwd) {
        return super.reTry(() -> clt_createImage(userId, pwd, imageContents));
    }


    public Result<byte[]> getImage(String userId, String imageId) {
        return super.reTry(() -> clt_getImage(userId, imageId));
    }

    public Result<Void> deleteImage(String userId, String imageId, String pwd) {
        return super.reTry(() -> clt_deleteImage(userId, imageId, pwd));
    }
}