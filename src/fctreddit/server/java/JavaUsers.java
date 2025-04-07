package fctreddit.server.java;

import java.util.List;
import java.util.logging.Logger;

import fctreddit.api.User;
import fctreddit.api.java.Result;
import fctreddit.api.java.Result.ErrorCode;
import fctreddit.api.java.Users;
import fctreddit.server.persistence.Hibernate;
import org.hibernate.Transaction;

public class JavaUsers implements Users {

    private static final Logger Log = Logger.getLogger(JavaUsers.class.getName());
    private final Hibernate hibernate;

    public JavaUsers() {
        this.hibernate = Hibernate.getInstance();
    }
    private boolean isNullOrBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    @Override
    public Result<String> createUser(User user) {
        Log.info("createUser : " + user);

        if (user == null ||
                isNullOrBlank(user.getUserId()) ||
                isNullOrBlank(user.getPassword()) ||
                isNullOrBlank(user.getEmail()) ||
                isNullOrBlank(user.getFullName())) {

            return Result.error(ErrorCode.BAD_REQUEST);
        }

        Transaction tx = null;
        try (var session = hibernate.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            User existing = session.get(User.class, user.getUserId());
            if (existing != null) {
                Log.info("User already exists: " + user.getUserId());
                tx.rollback();
                return Result.error(ErrorCode.CONFLICT);
            }

            session.persist(user);
            tx.commit();

            return Result.ok(user.getUserId());
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            Log.severe("Unable to store user: " + e.getMessage());
            return Result.error(ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public Result<User> getUser(String userId, String password) {
        Log.info("getUser : user = " + userId + "; pwd = " + password);

        if (userId == null) {
            Log.info("UserId or password null.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        try {
            User user = hibernate.get(User.class, userId);

            // Check if user exists
            if (user == null) {
                Log.info("User does not exist: " + userId);
                return Result.error(ErrorCode.NOT_FOUND);
            }

            // Check if the password is correct
            if (!user.getPassword().equals(password)) {
                Log.info("Password is incorrect for user: " + userId);
                return Result.error(ErrorCode.FORBIDDEN);
            }

            return Result.ok(user);
        } catch (Exception e) {
            Log.severe("Database error retrieving user: " + e.getMessage());
            return Result.error(ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public Result<User> updateUser(String userId, String password, User user) {
        Log.info("updateUser: userId = " + userId);

        if (userId == null || password == null || user == null) {
            Log.info("UserId, password or user null.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        Transaction tx = null;
        try (var session = hibernate.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            User userToUpdate = session.get(User.class, userId);

            if (userToUpdate == null) {
                Log.info("User does not exist: " + userId);
                tx.rollback();
                return Result.error(ErrorCode.NOT_FOUND);
            }

            if (!userToUpdate.getPassword().equals(password)) {
                Log.info("Password is incorrect for user: " + userId);
                tx.rollback();
                return Result.error(ErrorCode.FORBIDDEN);
            }

            // Update fields if provided
            if (user.getFullName() != null)
                userToUpdate.setFullName(user.getFullName());
            if (user.getEmail() != null)
                userToUpdate.setEmail(user.getEmail());
            if (user.getPassword() != null)
                userToUpdate.setPassword(user.getPassword());
            if (user.getAvatarUrl() != null)
                userToUpdate.setAvatarUrl(user.getAvatarUrl());

            session.merge(userToUpdate);
            tx.commit();

            return Result.ok(userToUpdate);
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            Log.severe("Database error updating user: " + e.getMessage());
            return Result.error(ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public Result<User> deleteUser(String userId, String password) {
        Log.info("deleteUser: userId = " + userId);

        if (userId == null || password == null) {
            Log.info("UserId or password null.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        Transaction tx = null;
        try (var session = hibernate.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            User user = session.get(User.class, userId);

            if (user == null) {
                Log.info("User does not exist: " + userId);
                tx.rollback();
                return Result.error(ErrorCode.NOT_FOUND);
            }

            if (!user.getPassword().equals(password)) {
                Log.info("Password is incorrect for user: " + userId);
                tx.rollback();
                return Result.error(ErrorCode.FORBIDDEN);
            }

            session.remove(user);
            tx.commit();

            return Result.ok(user);
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            Log.severe("Database error deleting user: " + e.getMessage());
            return Result.error(ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public Result<List<User>> searchUsers(String pattern) {
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

            return Result.ok(users);
        } catch (Exception e) {
            Log.severe("Database error searching users: " + e.getMessage());
            return Result.error(ErrorCode.INTERNAL_ERROR);
        }
    }

}