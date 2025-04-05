package fctreddit.clients.userOperations;

import fctreddit.api.java.Result;
import fctreddit.api.User;
import fctreddit.api.rest.RestImage;
import fctreddit.clients.rest.RestUsersClient;

import java.io.IOException;
import java.util.logging.Logger;

public class UpdateUserClient {

	private static Logger Log = Logger.getLogger(UpdateUserClient.class.getName());

	public static void main(String[] args) throws Exception {
		if (args.length < 5 || args.length > 6) {
			System.err.println("Use: java " + UpdateUserClient.class.getCanonicalName() +
					" userId oldpwd fullName email password [optional: avatarFilename]");
			return;
		}

		String userId = args[0];
		String oldpwd = args[1];
		String fullName = args[2];
		String email = args[3];
		String newpwd = args[4];
		String avatarUrl = null;

		// Check if an avatar filename is provided
		if (args.length == 6) {
			String filename = args[5];
			try {
				Result<String> imageResult = ImageHelper.associateImage(RestImage.SERVICE_NAME, userId, oldpwd, filename);
				if (imageResult.isOK()) {
					avatarUrl = imageResult.value();
					Log.info("Avatar uploaded: " + avatarUrl);
				} else {
					Log.warning("Avatar upload failed: " + imageResult.error());
					// Continue with user update without changing avatar
				}
			} catch (IOException e) {
				Log.warning("Error processing avatar: " + e.getMessage());
				// Continue with user update without changing avatar
			}
		}

		// Create user object to update, with or without new avatar URL
		User usr = new User(userId, fullName, email, newpwd);
		if (avatarUrl != null) {
			usr.setAvatarUrl(avatarUrl);
		}

		// Use the discovery-based client
		RestUsersClient client = new RestUsersClient();
		Result<User> result = client.updateUser(userId, oldpwd, usr);

		if (result.isOK()) {
			Log.info("Updated user: " + result.value());
		} else {
			Log.info("Update user failed with error: " + result.error());
		}
	}
}
