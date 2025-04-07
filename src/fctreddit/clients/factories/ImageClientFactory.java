package fctreddit.clients.factories;



import fctreddit.api.java.Image;
import fctreddit.clients.rest.RestImageClient;
import java.net.URI;

public class ImageClientFactory {

    private static final String REST = "/rest";
    private static final String GRPC = "/grpc";

    public static Image get(URI serverURI) {
        var uriString = serverURI.toString();

        if (uriString.endsWith(REST))
            return new RestImageClient(serverURI);
        else if (uriString.endsWith(GRPC))
            return  null;
            // return GrpcImageClient(serverURI)
        else
            throw new RuntimeException("Unknown service type..." + uriString);
    }
}
