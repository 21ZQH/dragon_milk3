package service;

import java.util.List;

import model.Mo;
import model.TA;

public interface UserProfileService {
    List<TA> getTAList();

    void updateAppliedCourseIds(TA ta);

    void updateTaProfile(TA ta);

    void updateMoProfile(Mo mo);

    void updateOwnedCourseIds(Mo mo);
}
