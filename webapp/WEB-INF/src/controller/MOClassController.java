package controller;

import model.*;
import store.CandidateStore;
import store.CourseStore;
import java.util.*;
import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
 

public class MOClassController extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        
        // Handle action from request.
        if ("create_class".equals(action)) {
            create_class(request, response);
        }
        else if("personal_center".equals(action)){
         show_personal_center(request, response);
        }
        else if ("review_candidates".equals(action)) {
            show_review_candidates(request, response);
        }
        // TODO: Add more action branches here when needed.
    }

    // Handle form submissions.
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        
        // This action is posted from create-project.jsp.
        if ("publish_course".equals(action)) {
            // Read form fields.
            String courseName = request.getParameter("courseName");
            String jobTitle = request.getParameter("jobTitle");
            String workingHours = request.getParameter("workingHours");
            String jobDescription = request.getParameter("jobDescription");
            String jobRequirement = request.getParameter("jobRequirement");
            
            // Default salary placeholder.
            String salary = "TBD"; // To Be Determined
            
            // Build a course entity.
            Course newCourse = new Course(courseName, jobTitle, workingHours, salary, jobDescription, jobRequirement);
            
            // Persist to storage.
            CourseStore.saveCourse(newCourse);
            
            // Forward back to MO dashboard.
            request.getRequestDispatcher("/WEB-INF/views/mo/dashboard.jsp").forward(request, response);
        } else if ("save_review_picks".equals(action)) {
            save_review_picks(request, response);
        }
    }


    private void show_personal_center(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Forward to personal-center page.
        request.getRequestDispatcher("/WEB-INF/views/mo/personal-center.jsp").forward(request, response);
    }

    // Forward to create-project page.
   private void create_class(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // No additional data required for this page.
        
        request.getRequestDispatcher("/WEB-INF/views/mo/create-project.jsp").forward(request, response);
       }


   private void mark_suitable_applicant(){
   }

   private void show_review_candidates(HttpServletRequest request, HttpServletResponse response)
           throws ServletException, IOException {
       List<Candidate> candidateList = CandidateStore.getCandidateList();
       request.setAttribute("candidateList", candidateList);
       request.getRequestDispatcher("/WEB-INF/views/mo/review.jsp").forward(request, response);
   }

   private void save_review_picks(HttpServletRequest request, HttpServletResponse response)
           throws IOException {
       String[] pickedIndexes = request.getParameterValues("pickedIndex");
       CandidateStore.savePickedIndexes(pickedIndexes);

       String returnTo = request.getParameter("returnTo");
       if ("personal_center".equals(returnTo)) {
           response.sendRedirect(request.getContextPath() + "/MOclasscontroller?action=personal_center");
       } else {
           response.sendRedirect(request.getContextPath() + "/MOclasscontroller?action=review_candidates");
       }
   }
}
