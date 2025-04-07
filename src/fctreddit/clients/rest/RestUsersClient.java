package fctreddit.clients.rest;

import fctreddit.api.User;
import fctreddit.api.java.Result;
import fctreddit.api.java.Users;
import fctreddit.api.rest.RestUsers;
import fctreddit.api.utils.Discovery;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

public class RestUsersClient extends RestClient implements Users {
	private static final Logger Log = Logger.getLogger(RestUsersClient.class.getName());

	final WebTarget target;

	public RestUsersClient() throws Exception {
		this(discoverServiceURI());
	}

	public RestUsersClient(URI uri) {
		super(uri);
		Log.info("Using server URI: " + uri);
		target = client.target(serverURI).path(RestUsers.PATH);
	}

	private static URI discoverServiceURI() throws Exception {
		// Initialize Discovery
		Discovery discovery = Discovery.getInstance(Discovery.DISCOVERY_ADDR, null, null);
		discovery.start();

		// Find the service
		URI[] serviceURIs = discovery.knownUrisOf(RestUsers.SERVICE_NAME, 1);

		// Use the first URI found
		return serviceURIs[0];
	}

	// Private methods that implement the actual API calls

	private Result<String> clt_createUser(User user) {
		Response r = target.request()
				.accept(MediaType.APPLICATION_JSON)
				.post(Entity.entity(user, MediaType.APPLICATION_JSON));

		return super.toJavaResult(r, String.class);
	}

	private Result<User> clt_getUser(String userId, String pwd) {
		Response r = target.path(userId)
				.queryParam(RestUsers.PASSWORD, pwd).request()
				.accept(MediaType.APPLICATION_JSON)
				.get();

		return super.toJavaResult(r, User.class);
	}

	private Result<User> clt_updateUser(String userId, String pwd, User user) {
		Response r = target.path(userId)
				.queryParam(RestUsers.PASSWORD, pwd).request()
				.accept(MediaType.APPLICATION_JSON)
				.put(Entity.entity(user, MediaType.APPLICATION_JSON));

		return super.toJavaResult(r, User.class);
	}

	private Result<User> clt_deleteUser(String userId, String pwd) {
		Response r = target.path(userId)
				.queryParam(RestUsers.PASSWORD, pwd).request()
				.accept(MediaType.APPLICATION_JSON)
				.delete();

		return super.toJavaResult(r, User.class);
	}

	private Result<List<User>> clt_searchUsers(String pattern) {
		Response r = target.queryParam(RestUsers.QUERY, pattern).request()
				.accept(MediaType.APPLICATION_JSON)
				.get();

		try {
			int status = r.getStatus();
			if (status == Response.Status.OK.getStatusCode() && r.hasEntity()) {
				return Result.ok(r.readEntity(new GenericType<List<User>>() {}));
			} else {
				return Result.error(getErrorCodeFrom(status));
			}
		} finally {
			r.close();
		}
	}

	// Public API methods that use retry logic

	public Result<String> createUser(User user) {
		return super.reTry(() -> clt_createUser(user));
	}

	public Result<User> getUser(String userId, String pwd) {
		return super.reTry(() -> clt_getUser(userId, pwd));
	}

	public Result<User> updateUser(String userId, String pwd, User user) {
		return super.reTry(() -> clt_updateUser(userId, pwd, user));
	}

	public Result<User> deleteUser(String userId, String pwd) {
		return super.reTry(() -> clt_deleteUser(userId, pwd));
	}

	public Result<List<User>> searchUsers(String pattern) {
		return super.reTry(() -> clt_searchUsers(pattern));
	}
}