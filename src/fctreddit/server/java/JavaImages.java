package fctreddit.server.java;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.logging.Logger;
import java.util.zip.CRC32;

import fctreddit.api.rest.RestUsers;
import fctreddit.api.utils.Discovery;

import fctreddit.api.User;
import fctreddit.api.java.Image;
import fctreddit.api.java.Result;
import fctreddit.api.java.Result.ErrorCode;
import fctreddit.api.java.Users;
import fctreddit.clients.factories.UsersClientFactory;

public class JavaImages implements Image {
    private static final Logger Log = Logger.getLogger(JavaImages.class.getName());
    private static final String IMAGE_FOLDER = "/app/images";

    Discovery discovery = Discovery.getInstance();


    public JavaImages(Users usersImpl) {
        File storageDir = new File(IMAGE_FOLDER);
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
    }

    @Override
    public Result<String> createImage(String userId, byte[] imageContents, String password) {
        Log.info("createImage: user = " + userId);

        // Validate parameters
        if (password == null || imageContents == null || imageContents.length == 0) {
            Log.warning("Null password or empty image data");
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        URI[] uris = discovery.knownUrisOf(RestUsers.SERVICE_NAME, 1);
        Result<User> userResult = UsersClientFactory.get(uris[uris.length-1]).getUser(userId, password);

        if (!userResult.isOK()) {
            return Result.error(userResult.error());
        }

        // Generate unique ID for image
        String imageId = generateId(imageContents);
        Path imagePath = Path.of(IMAGE_FOLDER, userId, imageId + ".png");

        // Create user directory if it doesn't exist
        File userDir = new File(IMAGE_FOLDER, userId);
        if (!userDir.exists()) {
            userDir.mkdirs();
        }

        try {
            // Check if image already exists
            if (Files.exists(imagePath)) {
                return Result.ok(imageId);
            }

            // Save the image
            Files.write(imagePath, imageContents, StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            Log.severe("Failed to save image: " + e.getMessage());
            return Result.error(ErrorCode.INTERNAL_ERROR);
        }

        return Result.ok("/image/" + imageId);
    }

    @Override
    public Result<byte[]> getImage(String userId, String imageId) {
        Log.info("getImage: user = " + userId + ", image = " + imageId);

        Path imagePath = Path.of(IMAGE_FOLDER, userId, imageId + ".png");

        if (!Files.exists(imagePath)) {
            Log.warning("Image not found: " + imageId + " for user: " + userId);
            return Result.error(ErrorCode.NOT_FOUND);
        }

        try {
            byte[] imageData = Files.readAllBytes(imagePath);
            return Result.ok(imageData);
        } catch (IOException e) {
            Log.severe("Failed to read image: " + e.getMessage());
            return Result.error(ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public Result<Void> deleteImage(String userId, String imageId, String password) {
        Log.info("deleteImage: user = " + userId + ", image = " + imageId);

        // Validate parameters
        if (password == null) {
            Log.warning("Password is null");
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        URI[] uris = discovery.knownUrisOf(RestUsers.SERVICE_NAME, 1);
        Result<User> userResult = UsersClientFactory.get(uris[uris.length-1]).getUser(userId, password);

        if (!userResult.isOK()) {
            return Result.error(userResult.error());
        }

        User user = userResult.value();
        String avatarUrl = user.getAvatarUrl();

        // Check if this is actually the user's avatar image
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            Log.info("User does not have an avatar image");
            return Result.error(ErrorCode.NOT_FOUND);
        }

        String avatarImageId = avatarUrl.substring(avatarUrl.lastIndexOf("/") + 1);

        if (!avatarImageId.equals(imageId)) {
            Log.info("User is not the owner of the image " + userId);
            return Result.error(ErrorCode.FORBIDDEN);
        }

        Path imagePath = Path.of(IMAGE_FOLDER, userId, imageId + ".png");

        if (!Files.exists(imagePath)) {
            Log.warning("Image not found: " + imageId + " for user: " + userId);
            return Result.error(ErrorCode.NOT_FOUND);
        }

        try {
            Files.delete(imagePath);
            return Result.ok();
        } catch (IOException e) {
            Log.severe("Failed to delete image: " + e.getMessage());
            return Result.error(ErrorCode.INTERNAL_ERROR);
        }
    }

    private String generateId(byte[] imageContents) {
        CRC32 crc = new CRC32();
        crc.update(imageContents);
        long hash = crc.getValue();
        return Long.toHexString(hash);
    }
}