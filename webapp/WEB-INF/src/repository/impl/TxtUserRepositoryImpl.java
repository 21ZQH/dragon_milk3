package repository.impl;

import java.util.List;

import model.Mo;
import model.TA;
import model.User;
import repository.UserRepository;
import store.UserStore;

/**
 * Text-file implementation of {@link UserRepository}.
 *
 * <p>The implementation delegates to {@link UserStore}. UserStore owns CSV
 * parsing and escaping, role-specific reconstruction of TA/MO/Admin objects,
 * and updates for application statuses and unread review notification flags.</p>
 */
public class TxtUserRepositoryImpl implements UserRepository {
    /**
     * Loads TA accounts from the user text store.
     *
     * @return registered TA users
     */
    @Override
    public List<TA> getTAList() {
        return UserStore.getTAList();
    }

    /**
     * Loads MO accounts from the user text store.
     *
     * @return stored MO users
     */
    @Override
    public List<Mo> getMOList() {
        return UserStore.getMOList();
    }

    /**
     * Saves a user through {@link UserStore#saveUser(User)}.
     *
     * @param user user to persist
     */
    @Override
    public void saveUser(User user) {
        UserStore.saveUser(user);
    }

    /**
     * Validates credentials without a role filter.
     *
     * @param password password or access key
     * @param email account email
     * @return authenticated user, or {@code null}
     */
    @Override
    public User validateUser(String password, String email) {
        return UserStore.validateUser(password, email);
    }

    /**
     * Validates credentials with a required role.
     *
     * @param password account password
     * @param role required role
     * @param email account email
     * @return authenticated user, or {@code null}
     */
    @Override
    public User validateUser(String password, String role, String email) {
        return UserStore.validateUser(password, role, email);
    }

    /**
     * Checks email uniqueness through the text store.
     *
     * @param email email to check
     * @return whether the email exists
     */
    @Override
    public boolean isEmailRegistered(String email) {
        return UserStore.isEmailRegistered(email);
    }

    /**
     * Updates TA application metadata, including review unread flags.
     *
     * @param ta TA to update
     */
    @Override
    public void updateAppliedCourseIds(TA ta) {
        UserStore.updateAppliedCourseIds(ta);
    }

    /**
     * Updates TA profile fields.
     *
     * @param ta TA to update
     */
    @Override
    public void updateTaProfile(TA ta) {
        UserStore.updateTaProfile(ta);
    }

    /**
     * Updates MO profile fields.
     *
     * @param mo MO to update
     */
    @Override
    public void updateMoProfile(Mo mo) {
        UserStore.updateMoProfile(mo);
    }

    /**
     * Updates MO owned course ids.
     *
     * @param mo MO to update
     */
    @Override
    public void updateOwnedCourseIds(Mo mo) {
        UserStore.updateOwnedCourseIds(mo);
    }
}
