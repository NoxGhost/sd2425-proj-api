package fctreddit.clients.userOperations;

import fctreddit.api.User;
import fctreddit.api.rest.RestImage;
import fctreddit.clients.rest.RestUsersClient;
import fctreddit.clients.rest.RestImageClient;

import java.io.IOException;
import java.util.logging.Logger;

public class DeleteUserClient {
	private static Logger Log = Logger.getLogger(DeleteUserClient.class.getName());

	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.err.println("Use: java " + DeleteUserClient.class.getCanonicalName() + " userId password");
			return;
		}

		String userId = args[0];
		String password = args[1];

		// Use discovery-based constructors
		var userClient = new RestUsersClient();
		var imageClient = new RestImageClient();

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
