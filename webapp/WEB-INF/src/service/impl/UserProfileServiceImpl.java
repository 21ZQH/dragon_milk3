package service.impl;

import java.util.List;

import model.Mo;
import model.TA;
import repository.UserRepository;
import repository.impl.TxtUserRepositoryImpl;
import service.UserProfileService;

public class UserProfileServiceImpl implements UserProfileService {
    private final UserRepository userRepository;

    public UserProfileServiceImpl() {
        this(new TxtUserRepositoryImpl());
    }

    UserProfileServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<TA> getTAList() {
        return userRepository.getTAList();
    }

    @Override
    public void updateAppliedCourseIds(TA ta) {
        userRepository.updateAppliedCourseIds(ta);
    }

    @Override
    public void updateTaProfile(TA ta) {
        userRepository.updateTaProfile(ta);
    }

    @Override
    public void updateMoProfile(Mo mo) {
        userRepository.updateMoProfile(mo);
    }

    @Override
    public void updateOwnedCourseIds(Mo mo) {
        userRepository.updateOwnedCourseIds(mo);
    }
}
