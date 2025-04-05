package fctreddit.clients.userOperations;

import fctreddit.api.User;
import fctreddit.clients.rest.RestUsersClient;
import fctreddit.clients.rest.RestImageClient;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

public class DeleteUserClient {
	private static Logger Log = Logger.getLogger(DeleteUserClient.class.getName());

	public static void main(String[] args) throws IOException {
		if (args.length != 3) {
			System.err.println("Use: java " + DeleteUserClient.class.getCanonicalName() + " url userId password");
			return;
		}

		String serverUrl = args[0];
		String userId = args[1];
		String password = args[2];

		var userClient = new RestUsersClient(URI.create(serverUrl));
		var imageClient = new RestImageClient(URI.create(serverUrl));

		// Delete the user and get the deleted User object
		User deletedUser = userClient.deleteUser(userId, password).value();
		Log.info("Deleted user: " + deletedUser.getUserId());

		// If the deleted user has an avatar, extract its imageId and delete the image.
		String avatarUrl = deletedUser.getAvatarUrl();
		if (avatarUrl != null) {
			String imageId = extractImageId(avatarUrl);
			if (imageId != null) {
				try {
					imageClient.deleteImage(userId, imageId, password);
					Log.info("Deleted avatar with imageId: " + imageId);
				} catch (Exception e) {
					Log.warning("Failed to delete avatar for imageId " + imageId + ": " + e.getMessage());
				}
			} else {
				Log.warning("User has no avatar: " + avatarUrl);
			}
		}
	}

	/**
	 * Extracts the imageId from the avatar URL.
	 */
	private static String extractImageId(String avatarUrl) {
		final String prefix = "/image/";
		if (avatarUrl.startsWith(prefix)) {
			return avatarUrl.substring(prefix.length());
		}
		return null;
	}
}
