package service.ai;

import java.io.IOException;

public interface MOCourseDraftAiClient {
    MOCourseDraft generate(String courseName) throws IOException, InterruptedException;

    final class MOCourseDraft {
        private final String jobTitle;
        private final String jobDescription;
        private final String jobRequirement;

        public MOCourseDraft(String jobTitle, String jobDescription, String jobRequirement) {
            this.jobTitle = jobTitle;
            this.jobDescription = jobDescription;
            this.jobRequirement = jobRequirement;
        }

        public String getJobTitle() {
            return jobTitle;
        }

        public String getJobDescription() {
            return jobDescription;
        }

        public String getJobRequirement() {
            return jobRequirement;
        }
    }
}
