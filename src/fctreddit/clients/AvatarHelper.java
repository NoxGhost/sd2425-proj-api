package fctreddit.clients;

import fctreddit.api.java.Result;
import fctreddit.clients.rest.RestImageClient;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class AvatarHelper {
    private static final Logger Log = Logger.getLogger(AvatarHelper.class.getName());

    public static Result<String> associateAvatar(String serverUrl, String userId, String password, String filename) throws IOException {
        Path avatarFilePath = Paths.get(filename);

        if (!Files.exists(avatarFilePath)) {
            throw new IOException("File " + filename + " does not exist.");
        }

        byte[] avatarData = Files.readAllBytes(avatarFilePath);

        if (avatarData.length == 0) {
            throw new IOException("File " + filename + " is empty.");
        }

        RestImageClient imageClient = new RestImageClient(URI.create(serverUrl));
        Result<String> imageResult = imageClient.createImage(userId, password, avatarData);

        if (imageResult.isOK()) {
            Log.info("Avatar associated with user: " + imageResult.value());
        } else {
            Log.info("Avatar upload failed with error: " + imageResult.error());
        }

        return imageResult;

    }
}
