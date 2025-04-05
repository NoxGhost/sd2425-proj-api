package fctreddit.clients.userOperations;

import fctreddit.api.User;
import fctreddit.clients.rest.RestUsersClient;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class SearchUserClient {

	private static Logger Log = Logger.getLogger(SearchUserClient.class.getName());

	public static void main(String[] args) throws Exception {

		if (args.length != 1) {
			System.err.println("Use: java " + SearchUserClient.class.getCanonicalName() + " query");
			return;
		}

		String pattern = args[0];

		// Use discovery-based client constructor
		var client = new RestUsersClient();

		var result = client.searchUsers(pattern);
		if (result.isOK()) {
			List<User> users = result.value();
			Log.info("Search User results:");
			for (User user : users) {
				Log.info(user.toString());
			}
		} else {
			Log.info("Search User failed with error: " + result.error());
		}
	}
}
