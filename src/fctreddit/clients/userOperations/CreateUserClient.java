package fctreddit.clients.userOperations;

import java.io.IOException;
import java.util.logging.Logger;

import fctreddit.api.java.Result;
import fctreddit.api.User;
import fctreddit.api.rest.RestImage;
import fctreddit.clients.rest.RestUsersClient;

public class CreateUserClient {

	private static Logger Log = Logger.getLogger(CreateUserClient.class.getName());

	public static void main(String[] args) throws Exception {
		if (args.length < 4 || args.length > 5) {
			System.err.println("Use: java " + CreateUserClient.class.getCanonicalName() +
					" userId fullName email password [optional: avatarFilename]");
			return;
		}

		String userId = args[0];
		String fullName = args[1];
		String email = args[2];
		String password = args[3];
		String avatarUrl = null;

		// Check if an avatar filename is provided
		if (args.length == 5) {
			String filename = args[4];
			try {
				// Note: ImageHelper would also need to be modified to use Discovery
				Result<String> imageResult = ImageHelper.associateImage(RestImage.SERVICE_NAME, userId, password, filename);
				if (imageResult.isOK()) {
					avatarUrl = imageResult.value();
					Log.info("Avatar uploaded: " + avatarUrl);
				} else {
					Log.warning("Avatar upload failed: " + imageResult.error());
					// Continue with user creation without avatar
				}
			} catch (IOException e) {
				Log.warning("Error processing avatar: " + e.getMessage());
				// Continue with user creation without avatar
			}
		}

		// Create user with or without avatar URL
		User usr = avatarUrl != null ?
				new User(userId, fullName, email, password, avatarUrl) :
				new User(userId, fullName, email, password);

		// Use the discovery-based client constructor directly
		RestUsersClient client = new RestUsersClient();
		Result<String> result = client.createUser(usr);

		if (result.isOK()) {
			Log.info("Created user: " + result.value());
		} else {
			Log.info("Create user failed with error: " + result.error());
		}
	}
}