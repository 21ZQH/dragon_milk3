package repository.impl;

import java.util.List;

import model.Mo;
import model.TA;
import model.User;
import repository.UserRepository;
import store.UserStore;

public class TxtUserRepositoryImpl implements UserRepository {
    @Override
    public List<TA> getTAList() {
        return UserStore.getTAList();
    }

    @Override
    public List<Mo> getMOList() {
        return UserStore.getMOList();
    }

    @Override
    public void saveUser(User user) {
        UserStore.saveUser(user);
    }

    @Override
    public User validateUser(String password, String email) {
        return UserStore.validateUser(password, email);
    }

    @Override
    public User validateUser(String password, String role, String email) {
        return UserStore.validateUser(password, role, email);
    }

    @Override
    public boolean isEmailRegistered(String email) {
        return UserStore.isEmailRegistered(email);
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
