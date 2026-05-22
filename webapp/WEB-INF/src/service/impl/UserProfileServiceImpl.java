package service.impl;

import java.util.List;

import model.Mo;
import model.TA;
import repository.UserRepository;
import repository.impl.TxtUserRepositoryImpl;
import service.UserProfileService;

/**
 * Implementation of the {@link UserProfileService} interface.
 * Provides concrete operations for managing TA and MO user profiles,
 * including retrieving TA lists, updating applied course IDs, and
 * persisting profile changes. Delegates all data access to
 * {@link UserRepository}.
 *
 * @author BUPT TA Recruitment Team
 * @version 1.0
 * @since 2024-2025
 */
public class UserProfileServiceImpl implements UserProfileService {
    /** Repository for user persistence operations. */
    private final UserRepository userRepository;

    /**
     * Constructs a new {@code UserProfileServiceImpl} with a default
     * {@link TxtUserRepositoryImpl}.
     */
    public UserProfileServiceImpl() {
        this(new TxtUserRepositoryImpl());
    }

    /**
     * Constructs a new {@code UserProfileServiceImpl} with the given repository.
     *
     * @param userRepository the repository for user data access
     */
    UserProfileServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Retrieves a list of all TA users.
     *
     * @return a list of {@link TA} objects
     */
    @Override
    public List<TA> getTAList() {
        return userRepository.getTAList();
    }

    /**
     * Updates the applied course IDs for the given TA.
     *
     * @param ta the TA whose applied course IDs should be updated
     */
    @Override
    public void updateAppliedCourseIds(TA ta) {
        userRepository.updateAppliedCourseIds(ta);
    }

    /**
     * Updates the profile information for the given TA.
     *
     * @param ta the TA with updated profile data
     */
    @Override
    public void updateTaProfile(TA ta) {
        userRepository.updateTaProfile(ta);
    }

    /**
     * Updates the profile information for the given MO.
     *
     * @param mo the MO with updated profile data
     */
    @Override
    public void updateMoProfile(Mo mo) {
        userRepository.updateMoProfile(mo);
    }

    /**
     * Updates the owned course IDs for the given MO.
     *
     * @param mo the MO whose owned course IDs should be updated
     */
    @Override
    public void updateOwnedCourseIds(Mo mo) {
        userRepository.updateOwnedCourseIds(mo);
    }
}
