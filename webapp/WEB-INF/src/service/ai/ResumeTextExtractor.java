package service.ai;

import java.io.File;
import java.io.IOException;

public interface ResumeTextExtractor {
    String extract(File resumeFile) throws IOException;
}
