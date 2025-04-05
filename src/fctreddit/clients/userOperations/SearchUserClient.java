package fctreddit.clients.userOperations;


import fctreddit.api.User;
import fctreddit.clients.rest.RestUsersClient;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

public class SearchUserClient {

	private static Logger Log = Logger.getLogger(SearchUserClient.class.getName());


	public static void main(String[] args) throws IOException {

		if (args.length != 2) {
			System.err.println("Use: java " + CreateUserClient.class.getCanonicalName() + " url query");
			return;
		}

		String serverUrl = args[0];
		String pattern = args[1];

		var client = new RestUsersClient(URI.create(serverUrl));

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
