package service.ai;

import java.io.IOException;

import model.ApplicationForm;
import model.Course;
import model.TA;

public interface ApplicationFormAiClient {
    ApplicationForm generate(TA ta, Course course, String resumeText) throws IOException, InterruptedException;
}
