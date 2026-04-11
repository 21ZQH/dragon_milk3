<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Create New Project</title>
    <style>
        body {
            background: #f7f7f9;
            font-family: 'Segoe UI', Arial, sans-serif;
            margin: 0;
            padding: 40px 0;
            display: flex;
            justify-content: center;
            align-items: flex-start;
            min-height: 100vh;
        }

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

        .top-line {
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 16px;
            flex-wrap: wrap;
            margin-bottom: 10px;
        }

        .page-title {
            font-size: 2.2em;
            font-weight: 700;
            color: #22223b;
            margin: 0;
            border-bottom: 2px solid #22223b;
            padding-bottom: 15px;
            flex: 1 1 auto;
        }

        .back-link {
            display: inline-block;
            text-decoration: none;
            color: #22223b;
            font-weight: 600;
            border: 2px solid #22223b;
            padding: 10px 16px;
            border-radius: 12px;
            background: #fff;
            transition: all 0.2s ease;
        }

        .back-link:hover {
            background: #22223b;
            color: #fff;
            transform: translateY(-1px);
            box-shadow: 0 4px 10px rgba(34,34,59,0.12);
        }

        .form-group {
            margin-bottom: 30px;
        }

        .form-label {
            display: block;
            font-size: 1.5em;
            font-weight: 700;
            color: #22223b;
            margin-bottom: 10px;
        }

        .form-control {
            width: 100%;
            padding: 15px;
            font-size: 1.1em;
            font-family: inherit;
            color: #22223b;
            background: #fdfdfd;
            border: 2px solid #22223b;
            border-radius: 8px;
            box-sizing: border-box;
            transition: border-color 0.2s, box-shadow 0.2s;
        }

        .form-control:focus {
            outline: none;
            border-color: #4a5c9a;
            box-shadow: 0 0 8px rgba(74, 92, 154, 0.2);
        }

        textarea.form-control {
            resize: vertical;
            min-height: 150px;
        }

        .submit-section {
            text-align: center;
            margin-top: 40px;
        }

        .publish-btn {
            background: #22223b;
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
            transform: translateY(-2px);
        }
    </style>
</head>
<body>

    <div class="container">
        <div class="top-line">
            <div class="page-title">Create new project</div>
            <a class="back-link" href="<%= response.encodeURL("MOclasscontroller?action=dashboard") %>">Back</a>
        </div>

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
