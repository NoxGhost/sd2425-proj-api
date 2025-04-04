package fctreddit.clients;



import fctreddit.api.java.Result;
import fctreddit.api.User;
import fctreddit.clients.rest.RestUsersClient;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;


public class UpdateUserClient {

	private static Logger Log = Logger.getLogger(UpdateUserClient.class.getName());


	// In UpdateUserClient.java
	public static void main(String[] args) throws IOException {
		if (args.length < 6 || args.length > 7) {
			System.err.println("Use: java " + UpdateUserClient.class.getCanonicalName() +
					" url userId oldpwd fullName email password [optional: avatarFilename]");
			return;
		}

		String serverUrl = args[0];
		String userId = args[1];
		String oldpwd = args[2];
		String fullName = args[3];
		String email = args[4];
		String newpwd = args[5];
		String avatarUrl = null;

		// Check if an avatar filename is provided
		if (args.length == 7) {
			String filename = args[6];
			try {
				Result<String> imageResult = AvatarHelper.associateAvatar(serverUrl, userId, oldpwd, filename);
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

		RestUsersClient client = new RestUsersClient(URI.create(serverUrl));
		Result<User> result = client.updateUser(userId, oldpwd, usr);

		if (result.isOK()) {
			Log.info("Updated user: " + result.value());
		} else {
			Log.info("Update user failed with error: " + result.error());
		}
	}
}
