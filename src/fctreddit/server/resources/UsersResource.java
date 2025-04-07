package fctreddit.server.resources;

import java.util.List;

import jakarta.inject.Singleton;
import fctreddit.api.User;
import fctreddit.api.java.Users;
import fctreddit.api.rest.RestUsers;
import fctreddit.server.java.JavaUsers;

@Singleton
public class UsersResource extends RestResource implements RestUsers {

	final Users impl;

	public UsersResource() {
		this.impl = new JavaUsers();
	}

	@Override
	public String createUser(User user) {
		return super.fromJavaResult(impl.createUser(user));
	}

	@Override
	public User getUser(String userId, String password) {
		return super.fromJavaResult(impl.getUser(userId, password));
	}

	@Override
	public User updateUser(String userId, String password, User user) {
		return super.fromJavaResult(impl.updateUser(userId, password, user));
	}

	@Override
	public User deleteUser(String userId, String password) {
		return super.fromJavaResult(impl.deleteUser(userId, password));
	}

	@Override
	public List<User> searchUsers(String pattern) {
		return super.fromJavaResult(impl.searchUsers(pattern));
	}
}