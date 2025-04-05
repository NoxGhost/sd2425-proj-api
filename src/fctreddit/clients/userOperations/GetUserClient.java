package fctreddit.clients.userOperations;

import fctreddit.clients.rest.RestUsersClient;

import java.io.IOException;
import java.util.logging.Logger;

public class GetUserClient {

	private static Logger Log = Logger.getLogger(GetUserClient.class.getName());

	public static void main(String[] args) throws Exception {

		if (args.length != 2) {
			System.err.println("Use: java " + GetUserClient.class.getCanonicalName() + " userId password");
			return;
		}

		String userId = args[0];
		String password = args[1];

		// Use discovery-based client
		var client = new RestUsersClient();

		var result = client.getUser(userId, password);
		if (result.isOK())
			Log.info("Get user: " + result.value());
		else
			Log.info("Get user failed with error: " + result.error());
	}
}
