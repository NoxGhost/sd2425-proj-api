package fctreddit.clients.userOperations;

import java.io.IOException;
import java.util.logging.Logger;

import fctreddit.api.java.Result;
import fctreddit.api.User;
import fctreddit.clients.imageOperations.CreateImageClient;
import fctreddit.clients.rest.RestUsersClient;

public class CreateUserClient {

	private static Logger Log = Logger.getLogger(CreateUserClient.class.getName());

	public static void main(String[] args) throws Exception {
		if (args.length !=5) {
			System.err.println("Use: java " + CreateUserClient.class.getCanonicalName() +
					" userId fullName email password avatarFilename");
			return;
		}

		String userId = args[0];
		String fullName = args[1];
		String email = args[2];
		String password = args[3];
		String avatarUrl = args[4];

		// Create user with or without avatar URL
		User usr = new User(userId, fullName, email, password, avatarUrl);

		// Use the discovery-based client constructor directly
		RestUsersClient client = new RestUsersClient();
		Result<String> result = client.createUser(usr);

		if (result.isOK()) {
			Log.info("Created user: " + result.value());
		} else {
			Log.info("User creation failed with error: " + result.error());
		}
	}
}