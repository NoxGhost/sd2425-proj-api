package fctreddit.server.resources;

import fctreddit.api.User;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import fctreddit.api.rest.RestImage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.logging.Logger;
import java.util.zip.CRC32;

public class ImageResource implements RestImage {
    private static final Logger Log = Logger.getLogger(ImageResource.class.getName());

    private static final String IMAGE_FOLDER = "/app/images";

    private final UsersResource usersResource;

    public ImageResource() {
        this.usersResource = new UsersResource();
        File storageDir = new File(IMAGE_FOLDER);
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
    }

    @Override
    public String createImage(String userId, byte[] imageContents, String password) {
        Log.info("createImage: user = " + userId);

        // Validate parameters
        if (password == null || imageContents.length == 0) {
            Log.warning("Null password or empty image data");
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        // Get user and validate
        User user = usersResource.getUser(userId, password);

        // Handle user not found
        if (user == null) {
            Log.info("User does not exist: " + userId);
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        // Password is already checked in getUser method, but keep this for clarity
        if (!user.getPassword().equals(password)) {
            Log.info("Password is incorrect for user: " + userId);
            throw new WebApplicationException(Response.Status.FORBIDDEN);
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
                return imageId;
            }

            // Save the image
            Files.write(imagePath, imageContents, StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            Log.severe("Failed to save image: " + e.getMessage());
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }

        return "/image/" + imageId;
    }

    @Override
    public byte[] getImage(String userId, String imageId) {
        Log.info("getImage: user = " + userId + ", image = " + imageId);

        Path imagePath = Path.of(IMAGE_FOLDER, userId, imageId + ".png");

        if (!Files.exists(imagePath)) {
            Log.warning("Image not found: " + imageId + " for user: " + userId);
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        try {
            return Files.readAllBytes(imagePath);
        } catch (IOException e) {
            Log.severe("Failed to read image: " + e.getMessage());
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void deleteImage(String userId, String imageId, String password) {
        Log.info("deleteImage: user = " + userId + ", image = " + imageId);

        // Validate parameters
        if (password == null) {
            Log.warning("Password is null");
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        // Get user and validate
        User user = usersResource.getUser(userId, password);

        // Handle user not found
        if (user == null) {
            Log.info("User does not exist: " + userId);
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        if (!user.getPassword().equals(password)) {
            Log.info("Password is incorrect for user: " + userId);
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }

        String avatarUrl = user.getAvatarUrl();
        String avatarImageId = avatarUrl.substring(avatarUrl.lastIndexOf("/") + 1);

        if (!avatarImageId.equals(imageId)) {
            Log.info("User is not the owner of the image " + userId);
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }

        Path imagePath = Path.of(IMAGE_FOLDER, userId, imageId + ".png");

        if (!Files.exists(imagePath)) {
            Log.warning("Image not found: " + imageId + " for user: " + userId);
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        try {
            Files.delete(imagePath);
        } catch (IOException e) {
            Log.severe("Failed to delete image: " + e.getMessage());
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private String generateId(byte[] imageContents) {
        CRC32 crc = new CRC32();
        crc.update(imageContents);
        long hash = crc.getValue();
        return Long.toHexString(hash);
    }
}