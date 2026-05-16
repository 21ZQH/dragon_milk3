package service.impl;

import java.util.List;

import model.Mo;
import model.TA;
import service.UserProfileService;
import store.UserStore;

public class UserProfileServiceImpl implements UserProfileService {
    @Override
    public List<TA> getTAList() {
        return UserStore.getTAList();
    }

    @Override
    public void updateAppliedCourseIds(TA ta) {
        UserStore.updateAppliedCourseIds(ta);
    }

    @Override
    public void updateTaProfile(TA ta) {
        UserStore.updateTaProfile(ta);
    }

    @Override
    public void updateMoProfile(Mo mo) {
        UserStore.updateMoProfile(mo);
    }

    @Override
    public void updateOwnedCourseIds(Mo mo) {
        UserStore.updateOwnedCourseIds(mo);
    }
}
