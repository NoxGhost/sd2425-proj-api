package fctreddit.server.resources;

import fctreddit.api.java.Image;
import fctreddit.api.java.Users;
import fctreddit.api.rest.RestImage;
import fctreddit.server.java.JavaImages;
import fctreddit.server.java.JavaUsers;

import jakarta.inject.Singleton;

@Singleton
public class ImageResource extends RestResource implements RestImage {

    private final Image impl;

    public ImageResource() {
        Users usersImpl = new JavaUsers();
        this.impl = new JavaImages(usersImpl);
    }

    @Override
    public String createImage(String userId, byte[] imageContents, String password) {
        return super.fromJavaResult(impl.createImage(userId, imageContents, password));
    }

    @Override
    public byte[] getImage(String userId, String imageId) {
        return super.fromJavaResult(impl.getImage(userId, imageId));
    }

    @Override
    public void deleteImage(String userId, String imageId, String password) {
        super.fromJavaResult(impl.deleteImage(userId, imageId, password));
    }
}