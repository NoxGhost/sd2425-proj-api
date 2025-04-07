package fctreddit.clients.userOperations;

import fctreddit.api.java.Result;
import fctreddit.api.User;
import fctreddit.clients.imageOperations.CreateImageClient;
import fctreddit.clients.rest.RestUsersClient;

import java.io.IOException;
import java.util.logging.Logger;

public class UpdateUserClient {

	private static Logger Log = Logger.getLogger(UpdateUserClient.class.getName());

	public static void main(String[] args) throws Exception {
		if (args.length != 6) {
			System.err.println("Use: java " + UpdateUserClient.class.getCanonicalName() +
					" userId oldpwd fullName email password avatarFilename");
			return;
		}

		String userId = args[0];
		String oldpwd = args[1];
		String fullName = args[2];
		String email = args[3];
		String newpwd = args[4];
		String avatarUrl = args[5];;

		// Create user object to update, with or without new avatar URL
		User usr = new User(userId, fullName, email, newpwd, avatarUrl);

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
