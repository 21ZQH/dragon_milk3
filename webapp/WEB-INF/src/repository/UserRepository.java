package repository;

import java.util.List;

import model.Mo;
import model.TA;
import model.User;

public interface UserRepository {
    List<TA> getTAList();

    List<Mo> getMOList();

    void saveUser(User user);

    User validateUser(String password, String email);

    User validateUser(String password, String role, String email);

    boolean isEmailRegistered(String email);

    void updateAppliedCourseIds(TA ta);

    void updateTaProfile(TA ta);

    void updateMoProfile(Mo mo);

    void updateOwnedCourseIds(Mo mo);
}
