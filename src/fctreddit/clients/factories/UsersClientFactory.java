package fctreddit.clients.factories;



import fctreddit.api.java.Users;
import fctreddit.clients.grpc.GrpcUsersClient;
import fctreddit.clients.rest.RestUsersClient;

import java.net.URI;

public class UsersClientFactory {

	private static final String REST = "/rest";
	private static final String GRPC = "/grpc";

	public static Users get(URI serverURI) {
		var uriString = serverURI.toString();

		if (uriString.endsWith(REST))
			return new RestUsersClient(serverURI);
		else if (uriString.endsWith(GRPC))
			return  null;
		// return GrpcUsersClient(serverURI)
		else
			throw new RuntimeException("Unknown service type..." + uriString);
	}
}
