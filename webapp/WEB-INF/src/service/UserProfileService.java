package service;

import java.util.List;

import model.Mo;
import model.TA;

/**
 * Service interface for managing user profiles, including Teaching Assistants (TAs)
 * and Module Officers (MOs). Provides methods for retrieving TA lists and updating
 * profile details such as applied courses, owned courses, and personal information.
 *
 * @version 1.0
 * @since 2025
 */
public interface UserProfileService {
    /**
     * Retrieves a list of all Teaching Assistants in the system.
     *
     * @return a list of {@link TA} objects representing all TAs
     */
    List<TA> getTAList();

    /**
     * Updates the list of course IDs that the given TA has applied to.
     *
     * @param ta the TA whose applied course IDs should be updated
     */
    void updateAppliedCourseIds(TA ta);

    /**
     * Updates the profile information of the specified Teaching Assistant.
     *
     * @param ta the TA object containing the updated profile data
     */
    void updateTaProfile(TA ta);

    /**
     * Updates the profile information of the specified Module Officer.
     *
     * @param mo the MO object containing the updated profile data
     */
    void updateMoProfile(Mo mo);

    /**
     * Updates the list of course IDs owned by the specified Module Officer.
     *
     * @param mo the MO whose owned course IDs should be updated
     */
    void updateOwnedCourseIds(Mo mo);
}
