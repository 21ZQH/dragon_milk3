package model;

import java.util.ArrayList;
import java.util.List;

public class Mo extends User {
    private String role="Mo";
    private List<Course> ownedCourses = new ArrayList<>();

    public Mo(String password,String email) {
        super(password, email);
    }

    @Override
    public String getRole() { 
        return role; 


        
    }

    public List<Course> getOwnedCourses() {
        return ownedCourses;
    }

    public void setOwnedCourses(List<Course> ownedCourses) {
        this.ownedCourses = new ArrayList<>();
        if (ownedCourses != null) {
            for (Course course : ownedCourses) {
                addOwnedCourse(course);
            }
        }
    }

    public void addOwnedCourse(Course course) {
        if (course != null && !ownedCourses.contains(course)) {
            ownedCourses.add(course);
        }
    }

    public void replaceOwnedCourse(Course course) {
        if (course == null) {
            return;
        }

        for (int i = 0; i < ownedCourses.size(); i++) {
            if (course.equals(ownedCourses.get(i))) {
                ownedCourses.set(i, course);
                return;
            }
        }

        addOwnedCourse(course);
    }
}
