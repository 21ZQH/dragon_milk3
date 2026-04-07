package controller;

import model.*;
import store.CourseStore;
import java.util.*;
import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class MOClassController extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        
        // 捕获前端传来的 action 动作
        if ("create_class".equals(action)) {
            create_class(request, response);
        }
        else if("personal_center".equals(action)){
         show_personal_center(request, response);
        }
        // TODO: 后续可以继续在这里添加 if ("mark_suitable_applicant".equals(action)) 等分支
    }

    // doPost 方法来处理表单的提交
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        
        // 对应 create_project.jsp 表单里的隐藏域 <input type="hidden" name="action" value="publish_course">
        if ("publish_course".equals(action)) {
            // 获取前端表单用户填写的各项数据
            String courseName = request.getParameter("courseName");
            String jobTitle = request.getParameter("jobTitle");
            String workingHours = request.getParameter("workingHours");
            String jobDescription = request.getParameter("jobDescription");
            String jobRequirement = request.getParameter("jobRequirement");
            
            // 给暂时没有输入框的 salary 赋一个初始值
            String salary = "TBD"; // TBD 表示 To Be Determined (待定)
            
            // 将收集到的数据打包成一个 Course 对象
            Course newCourse = new Course(courseName, jobTitle, workingHours, salary, jobDescription, jobRequirement);
            
            // 调用工具类，把对象写入 txt 文件
            CourseStore.saveCourse(newCourse);
            
            //提交成功后，让页面跳转回 MO 的主面板 (Dashboard)
            request.getRequestDispatcher("/WEB-INF/views/mo/dashboard.jsp").forward(request, response);
        }
    }


    private void show_personal_center(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 将请求转发到对应的 JSP 视图
        request.getRequestDispatcher("/WEB-INF/views/mo/personal-center.jsp").forward(request, response);
    }

    // 处理跳转到“创建课程”页面的逻辑
   private void create_class(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 直接将请求转发到我们即将编写的 create_project.jsp 页面
        
        request.getRequestDispatcher("/WEB-INF/views/mo/create-project.jsp").forward(request, response);
       }


   private void mark_suitable_applicant(){
   }

   private void show_applicant_information(){
   }
}
