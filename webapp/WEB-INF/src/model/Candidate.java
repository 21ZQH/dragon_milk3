package model;

public class Candidate {
    private String name;
    private String education;
    private String details;
    private boolean picked;

    public Candidate(String name, String education, String details) {
        this(name, education, details, false);
    }

    public Candidate(String name, String education, String details, boolean picked) {
        this.name = name;
        this.education = education;
        this.details = details;
        this.picked = picked;
    }

    public String getName() {
        return name;
    }

    public String getEducation() {
        return education;
    }

    public String getDetails() {
        return details;
    }

    public boolean isPicked() {
        return picked;
    }

    public void setPicked(boolean picked) {
        this.picked = picked;
    }
}
