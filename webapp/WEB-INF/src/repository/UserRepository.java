package repository;

import java.util.List;

import model.Mo;
import model.TA;
import model.User;

/**
 * Repository boundary for all user account data.
 *
 * <p>The system stores Admin, MO, and TA users in the same text-file backed
 * store. Service classes use this interface for authentication, account
 * creation, profile updates, MO course ownership, and TA application status
 * updates.</p>
 */
public interface UserRepository {
    /**
     * Loads all registered TA users.
     *
     * @return list of TA accounts; empty when no TA records exist
     */
    List<TA> getTAList();

    /**
     * Loads all MO users, including built-in and Admin-created accounts.
     *
     * @return list of MO accounts
     */
    List<Mo> getMOList();

    /**
     * Saves a new user record.
     *
     * @param user TA, MO, or Admin user to persist
     */
    void saveUser(User user);

    /**
     * Validates a user without restricting the role.
     *
     * @param password password or TA access key
     * @param email account email
     * @return authenticated user, or {@code null} when credentials fail
     */
    User validateUser(String password, String email);

    /**
     * Validates a user while requiring a specific role.
     *
     * @param password account password
     * @param role expected role name such as {@code Mo} or {@code Admin}
     * @param email account email
     * @return authenticated user, or {@code null} when credentials or role fail
     */
    User validateUser(String password, String role, String email);

    /**
     * Checks whether an email is already used by any stored user.
     *
     * @param email email address to check
     * @return {@code true} when the email is registered
     */
    boolean isEmailRegistered(String email);

    /**
     * Persists TA application-course links, statuses, and unread review flags.
     *
     * @param ta TA whose application metadata should be updated
     */
    void updateAppliedCourseIds(TA ta);

    /**
     * Persists editable TA profile fields such as name, college, skills, and
     * master resume path.
     *
     * @param ta TA profile to update
     */
    void updateTaProfile(TA ta);

    /**
     * Persists editable MO profile fields.
     *
     * @param mo MO profile to update
     */
    void updateMoProfile(Mo mo);

    /**
     * Persists the courses owned by an MO account.
     *
     * @param mo MO whose owned course ids should be stored
     */
    void updateOwnedCourseIds(Mo mo);

    void resetTaApplicationState();
}
