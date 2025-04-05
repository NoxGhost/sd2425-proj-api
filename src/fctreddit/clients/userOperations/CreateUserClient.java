package fctreddit.clients.userOperations;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

import fctreddit.api.java.Result;
import fctreddit.api.User;
import fctreddit.clients.rest.RestUsersClient;

public class CreateUserClient {

	private static Logger Log = Logger.getLogger(CreateUserClient.class.getName());

	public static void main(String[] args) throws IOException {
		if (args.length < 5 || args.length > 6) {
			System.err.println("Use: java " + CreateUserClient.class.getCanonicalName() +
					" url userId fullName email password [optional: avatarFilename]");
			return;
		}

		String serverUrl = args[0];
		String userId = args[1];
		String fullName = args[2];
		String email = args[3];
		String password = args[4];
		String avatarUrl = null;

		// Check if an avatar filename is provided
		if (args.length == 6) {
			String filename = args[5];
			try {
				Result<String> imageResult = ImageHelper.associateImage(serverUrl, userId, password, filename);
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

		RestUsersClient client = new RestUsersClient(URI.create(serverUrl));
		Result<String> result = client.createUser(usr);

		if (result.isOK()) {
			Log.info("Created user: " + result.value());
		} else {
			Log.info("Create user failed with error: " + result.error());
		}
	}
}
