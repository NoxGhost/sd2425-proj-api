package fctreddit.server.resources;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import fctreddit.api.User;
import fctreddit.api.rest.RestUsers;
import fctreddit.server.persistence.Hibernate;

import java.util.List;
import java.util.logging.Logger;

public class UsersResource implements RestUsers {

	private static final Logger Log = Logger.getLogger(UsersResource.class.getName());

	private final Hibernate hibernate;
	
	public UsersResource() {
		hibernate = Hibernate.getInstance();
	}

	@Override
	public String createUser(User user) {
		Log.info("createUser : " + user);

		// Check if user data is valid
		if (user == null || user.getUserId() == null || user.getPassword() == null ||
				user.getFullName() == null || user.getEmail() == null) {
			Log.info("User object invalid.");
			throw new WebApplicationException(Status.BAD_REQUEST);
		}

		try {
			hibernate.persist(user);
		} catch (Exception e) {
			if (e.getMessage() != null && e.getMessage().toLowerCase().contains("duplicate")) {
				Log.info("User already exists: " + e.getMessage());
				throw new WebApplicationException(Status.CONFLICT);
			} else {
				Log.severe("Unable to store user: " + e.getMessage());
				throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
			}
		}
		
		return user.getUserId();
	}

	@Override
	public User getUser(String userId, String password) {
		Log.info("getUser : user = " + userId + "; pwd = " + password);

		//Not mentioned in the interface
		if (userId == null || password == null) {
			Log.info("UserId or password null.");
			throw new WebApplicationException(Status.BAD_REQUEST);
		}

		User user;
		try {
			user = hibernate.get(User.class, userId);
		} catch (Exception e) {
			Log.severe("Database error retrieving user: " + e.getMessage());
			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
		}

		// Check if user exists
		if (user == null) {
			Log.info("User does not exist: " + userId);
			throw new WebApplicationException(Status.NOT_FOUND);
		}

		// Check if the password is correct
		if (!user.getPassword().equals(password)) {
			Log.info("Password is incorrect for user: " + userId);
			throw new WebApplicationException(Status.FORBIDDEN);
		}

		return user;
	}

	@Override
	public User updateUser(String userId, String password, User user) {
		Log.info("updateUser: userId = " + userId);

		//Not mentioned in the interface
		if (userId == null || password == null || user == null) {
			Log.info("UserId, password or user null.");
			throw new WebApplicationException(Status.BAD_REQUEST);
		}

		User userToUpdate = null;
		try {
			userToUpdate = hibernate.get(User.class, userId);
		} catch (Exception e) {
			Log.severe("Database error retrieving user for update: " + e.getMessage());
			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
		}

		if (userToUpdate == null) {
			Log.info("User does not exist: " + userId);
			throw new WebApplicationException(Status.NOT_FOUND);
		}

		if (!userToUpdate.getPassword().equals(password)) {
			Log.info("Password is incorrect for user: " + userId);
			throw new WebApplicationException(Status.FORBIDDEN);
		}

		// Update user fields if they are not null
		if (user.getFullName() != null) {
			userToUpdate.setFullName(user.getFullName());
		}
		if (user.getEmail() != null) {
			userToUpdate.setEmail(user.getEmail());
		}
		if (user.getPassword() != null) {
			userToUpdate.setPassword(user.getPassword());
		}
		if (user.getAvatarUrl() != null) {
			userToUpdate.setAvatarUrl(user.getAvatarUrl());
		}

		try {
			hibernate.update(userToUpdate);
		} catch (Exception e) {
			Log.severe("Database error updating user: " + e.getMessage());
			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
		}

		return userToUpdate;
	}

	@Override
	public User deleteUser(String userId, String password) {
		Log.info("deleteUser: userId = " + userId);

		//Not mentioned in the interface
		if (userId == null || password == null) {
			Log.info("UserId or password null.");
			throw new WebApplicationException(Status.BAD_REQUEST);
		}

		User user;
		try {
			user = hibernate.get(User.class, userId);
		} catch (Exception e) {
			Log.severe("Database error retrieving user for deletion: " + e.getMessage());
			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
		}

		if (user == null) {
			Log.info("User does not exist: " + userId);
			throw new WebApplicationException(Status.NOT_FOUND);
		}

		if (!user.getPassword().equals(password)) {
			Log.info("Password is incorrect for user: " + userId);
			throw new WebApplicationException(Status.FORBIDDEN);
		}

		try {
			hibernate.delete(user);
		} catch (Exception e) {
			Log.severe("Database error deleting user: " + e.getMessage());
			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
		}

		return user;
	}

	@Override
	public List<User> searchUsers(String pattern) {
		Log.info("searchUsers : pattern = " + pattern);

		try {
			List<User> users;

			// Empty pattern should return all users
			if (pattern.trim().isEmpty()) {
				users = hibernate.jpql("SELECT u FROM User u", User.class);
			} else {
				users = hibernate.jpql(
						"SELECT u FROM User u WHERE LOWER(u.userId) LIKE LOWER('%" + pattern + "%')",
						User.class
				);
			}

			// Clear passwords as required by the interface
			for (User user : users) {
				user.setPassword("");
			}

			return users;
		} catch (Exception e) {
			Log.severe("Database error searching users: " + e.getMessage());
			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
		}
	}

}
