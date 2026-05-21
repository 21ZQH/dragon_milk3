package service;

import java.util.List;

import model.Course;
import model.Mo;
import model.TA;

public interface AdminManagementService {
    List<TA> getTAList();

    List<Mo> getMOList();

    CreateMoResult createMoAccount(String account, String password, String courseNamesText);

    final class CreateMoResult {
        private final boolean success;
        private final String message;
        private final Mo mo;
        private final List<Course> assignedCourses;

        private CreateMoResult(boolean success, String message, Mo mo, List<Course> assignedCourses) {
            this.success = success;
            this.message = message;
            this.mo = mo;
            this.assignedCourses = assignedCourses;
        }

        public static CreateMoResult success(Mo mo, List<Course> assignedCourses) {
            return new CreateMoResult(true, "MO account created successfully.", mo, assignedCourses);
        }

        public static CreateMoResult failure(String message) {
            return new CreateMoResult(false, message, null, List.of());
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public Mo getMo() {
            return mo;
        }

        public List<Course> getAssignedCourses() {
            return assignedCourses;
        }
    }
}
