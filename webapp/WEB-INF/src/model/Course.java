package model;

public class Course {
    private String courseName;
    private String jobTitle;
    private String workingHours;
    private String salary;

    // 构造方法
    public Course(String courseName, String jobTitle, String workingHours, String salary) {
        this.courseName = courseName;
        this.jobTitle = jobTitle;
        this.workingHours = workingHours;
        this.salary = salary;
    }

    // Getter和Setter
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getWorkingHours() { return workingHours; }
    public void setWorkingHours(String workingHours) { this.workingHours = workingHours; }

    public String getSalary() { return salary; }
    public void setSalary(String salary) { this.salary = salary; }
}