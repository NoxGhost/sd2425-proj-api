package fctreddit.clients.imageOperations;

import fctreddit.api.java.Result;
import fctreddit.clients.rest.RestImageClient;

import java.util.logging.Logger;

public class CreateImageClient {
    private static final Logger Log = Logger.getLogger(CreateImageClient.class.getName());

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.err.println("Use: java " + CreateImageClient.class.getCanonicalName() + " userId password");
            return;
        }

        String userId = args[0];
        String password = args[1];
        byte[] avatarData = args[2].getBytes();


        RestImageClient imageClient = new RestImageClient();
        Result<String> imageResult = imageClient.createImage(userId, avatarData,password);

        if (imageResult.isOK()) {
            Log.info("Created image: " + imageResult.value());
        } else {
            Log.info("Image creation failed with error: " + imageResult.error());
        }


    }
}
