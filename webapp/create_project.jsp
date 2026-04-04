<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Create New Project</title>
    <style>
        /* 页面基础背景和居中排版 */
        body {
            background: #f7f7f9;
            font-family: 'Segoe UI', Arial, sans-serif;
            margin: 0;
            padding: 40px 0; /* 上下留一点空隙，防止内容太长贴边 */
            display: flex;
            justify-content: center;
            align-items: flex-start;
            min-height: 100vh;
        }

        /* 主容器白板 */
        .container {
            background: #fff;
            width: 100%;
            max-width: 800px;
            border-radius: 20px;
            box-shadow: 0 4px 16px rgba(0,0,0,0.12);
            border: 2px solid #22223b;
            padding: 40px 50px;
            box-sizing: border-box;
        }

        /* 顶部大标题样式 */
        .page-title {
            font-size: 2.2em;
            font-weight: 700;
            color: #22223b;
            margin-bottom: 5px;
            border-bottom: 2px solid #22223b; /* 对应草图里标题下方的那条横线 */
            padding-bottom: 15px;
        }

        /* 草图里的 "// MO can add details..." 蓝色注释文字 */
        .page-subtitle {
            color: #3b5998; /* 使用蓝色来模拟代码注释的感觉 */
            font-size: 1.1em;
            font-style: italic;
            margin-bottom: 30px;
            margin-top: 10px;
        }

        /* 每一个输入区块的包裹层 */
        .form-group {
            margin-bottom: 30px;
        }

        /* 草图里的 "Course Name", "Job Description", "Job Requirement" 标题 */
        .form-label {
            display: block;
            font-size: 1.5em;
            font-weight: 700;
            color: #22223b;
            margin-bottom: 10px;
        }

        /* 单行输入框 (Course Name) 和 多行文本框 (Description/Requirement) 的通用样式 */
        .form-control {
            width: 100%;
            padding: 15px;
            font-size: 1.1em;
            font-family: inherit;
            color: #22223b;
            background: #fdfdfd;
            border: 2px solid #22223b;
            border-radius: 8px; /* 稍微一点圆角，比草图的直角更现代 */
            box-sizing: border-box;
            transition: border-color 0.2s, box-shadow 0.2s;
        }

        /* 鼠标点击输入框时的聚焦特效 */
        .form-control:focus {
            outline: none;
            border-color: #4a5c9a;
            box-shadow: 0 0 8px rgba(74, 92, 154, 0.2);
        }

        /* 针对多行文本框的特殊设置，允许用户垂直拉长 */
        textarea.form-control {
            resize: vertical;
            min-height: 150px; /* 对应草图里比较高的大框 */
        }

        /* 底部发布按钮的居中容器 */
        .submit-section {
            text-align: center;
            margin-top: 40px;
        }

        /* "publish" 按钮样式 */
        .publish-btn {
            background: #22223b; /* 实心深色背景，突出主要操作 */
            color: #fff;
            font-size: 1.5em;
            font-weight: 700;
            border: none;
            border-radius: 12px;
            padding: 15px 50px;
            cursor: pointer;
            transition: background 0.2s, transform 0.1s;
            box-shadow: 0 4px 12px rgba(34, 34, 59, 0.2);
        }
        .publish-btn:hover {
            background: #3b3e5b;
            transform: translateY(-2px); /* 悬浮时轻微上浮效果 */
        }
    </style>
</head>
<body>

    <div class="container">
        
        <div class="page-title">Create new project</div>
        
    

        <form action="<%= response.encodeURL("MOclasscontroller") %>" method="post">
            
            <input type="hidden" name="action" value="publish_course">

            <div class="form-group">
                <label class="form-label" for="courseName">Course Name</label>
                <input type="text" id="courseName" name="courseName" class="form-control" placeholder="Enter course name here..." required>
            </div>

            <div class="form-group">
                <label class="form-label" for="jobTitle">Job Title</label>
                <input type="text" id="jobTitle" name="jobTitle" class="form-control" placeholder="e.g. Teaching Assistant" required>
            </div>

            <div class="form-group">
                <label class="form-label" for="workingHours">Working Hours</label>
                <input type="text" id="workingHours" name="workingHours" class="form-control" placeholder="e.g. 10 hours/week" required>
            </div>

            <div class="form-group">
                <label class="form-label" for="jobDescription">Job Description</label>
                <textarea id="jobDescription" name="jobDescription" class="form-control" placeholder="Details..."></textarea>
            </div>

            <div class="form-group">
                <label class="form-label" for="jobRequirement">Job Requirement</label>
                <textarea id="jobRequirement" name="jobRequirement" class="form-control" placeholder="Details..."></textarea>
            </div>

            <div class="submit-section">
                
                <button type="submit" class="publish-btn">Publish</button>
            </div>

        </form>
    </div>

</body>
</html>