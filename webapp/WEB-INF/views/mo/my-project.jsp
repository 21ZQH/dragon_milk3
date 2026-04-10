<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Course" %>
<%
    List<Course> courseList = (List<Course>) request.getAttribute("courseList");
%>
<!DOCTYPE html>
<html>
<head>
    <title>My Project</title>
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
            max-width: 900px;
            border-radius: 20px;
            box-shadow: 0 4px 16px rgba(0,0,0,0.12);
            border: 2px solid #22223b;
            padding: 36px 40px 40px 40px;
            box-sizing: border-box;
        }

        .page-title {
            font-size: 2.2em;
            font-weight: 700;
            color: #22223b;
            margin-bottom: 5px;
            border-bottom: 2px solid #22223b;
            padding-bottom: 15px;
        }

        .page-subtitle {
            color: #3b5998;
            font-size: 1.05em;
            font-style: italic;
            margin-top: 10px;
            margin-bottom: 24px;
        }

        .top-bar {
            display: flex;
            gap: 12px;
            align-items: center;
            margin-bottom: 28px;
            flex-wrap: wrap;
        }

        .search-box {
            flex: 1;
            min-width: 260px;
            padding: 14px 16px;
            font-size: 1em;
            border: 2px solid #22223b;
            border-radius: 12px;
            outline: none;
            box-sizing: border-box;
        }

        .search-box:focus {
            border-color: #4a5c9a;
            box-shadow: 0 0 8px rgba(74, 92, 154, 0.2);
        }

        .search-btn {
            display: inline-block;
            border: 2px solid #22223b;
            background: #fff;
            color: #22223b;
            font-weight: 600;
            padding: 12px 18px;
            border-radius: 12px;
            cursor: pointer;
            transition: all 0.2s ease;
            font-size: 1em;
        }

        .search-btn:hover {
            background: #22223b;
            color: #fff;
            transform: translateY(-1px);
            box-shadow: 0 4px 10px rgba(34,34,59,0.12);
        }

        .back-link {
            display: inline-block;
            text-decoration: none;
            color: #22223b;
            font-weight: 600;
            border: 2px solid #22223b;
            padding: 12px 18px;
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

        .course-list {
            display: flex;
            flex-direction: column;
            gap: 22px;
        }

        .course-card-link {
            text-decoration: none;
            color: inherit;
        }

        .course-card {
            border: 2px solid #22223b;
            border-radius: 16px;
            padding: 24px 26px;
            background: #fcfcfe;
            transition: all 0.2s ease;
        }

        .course-card:hover {
            transform: translateY(-3px);
            box-shadow: 0 8px 18px rgba(34,34,59,0.12);
            border-color: #4a5c9a;
        }

        .course-name {
            font-size: 1.7em;
            font-weight: 700;
            color: #22223b;
            margin-bottom: 14px;
            word-break: break-word;
        }

        .course-meta {
            font-size: 1.1em;
            color: #444;
            line-height: 1.8;
        }

        .empty-box {
            border: 2px dashed #bbb;
            border-radius: 14px;
            padding: 36px 24px;
            text-align: center;
            color: #666;
            background: #fafafa;
            font-size: 1.1em;
        }

        .hidden {
            display: none !important;
        }

        .result-tip {
            margin-top: 18px;
            color: #666;
            font-size: 0.98em;
        }
    </style>
</head>
<body>

<div class="container">
    <div class="page-title">My project</div>
    <div class="page-subtitle">// check courses below</div>

    <div class="top-bar">
        <input type="text" id="searchInput" class="search-box" placeholder="Search by course name or job title...">
        <button type="button" id="searchBtn" class="search-btn">Search</button>
        <a class="back-link" href="<%= response.encodeURL("MOclasscontroller?action=personal_center") %>">Back</a>
    </div>

    <div class="course-list" id="courseList">
        <%
            if (courseList != null && !courseList.isEmpty()) {
                for (int i = 0; i < courseList.size(); i++) {
                    Course c = courseList.get(i);
        %>
        <a class="course-card-link course-item"
           href="<%= response.encodeURL("MOclasscontroller?action=project_detail&courseIndex=" + i) %>"
           data-name="<%= c.getCourseName() == null ? "" : c.getCourseName().toLowerCase() %>"
           data-job="<%= c.getJobTitle() == null ? "" : c.getJobTitle().toLowerCase() %>">
            <div class="course-card">
                <div class="course-name">
                    <%= c.getCourseName() == null || c.getCourseName().trim().isEmpty() ? "Untitled Course" : c.getCourseName() %>
                </div>
                <div class="course-meta">
                    <strong>Job Title:</strong>
                    <%= c.getJobTitle() == null || c.getJobTitle().trim().isEmpty() ? "Not set" : c.getJobTitle() %>
                    <br>
                    <strong>Working Hours:</strong>
                    <%= c.getWorkingHours() == null || c.getWorkingHours().trim().isEmpty() ? "Not set" : c.getWorkingHours() %>
                </div>
            </div>
        </a>
        <%
                }
            } else {
        %>
        <div class="empty-box">No course project has been published yet.</div>
        <%
            }
        %>
    </div>

    <div class="result-tip" id="resultTip"></div>
</div>

<script>
    const searchInput = document.getElementById("searchInput");
    const searchBtn = document.getElementById("searchBtn");
    const items = document.querySelectorAll(".course-item");
    const resultTip = document.getElementById("resultTip");

    function filterCourses() {
        const keyword = searchInput.value.trim().toLowerCase();
        let visibleCount = 0;

        items.forEach(item => {
            const name = item.getAttribute("data-name") || "";
            const job = item.getAttribute("data-job") || "";
            const matched = keyword === "" || name.includes(keyword) || job.includes(keyword);

            if (matched) {
                item.classList.remove("hidden");
                visibleCount++;
            } else {
                item.classList.add("hidden");
            }
        });

        if (items.length > 0) {
            resultTip.textContent = "Showing " + visibleCount + " result(s).";
        }
    }

    if (searchBtn) {
        searchBtn.addEventListener("click", filterCourses);
    }

    if (searchInput) {
        searchInput.addEventListener("keydown", function(event) {
            if (event.key === "Enter") {
                event.preventDefault();
                filterCourses();
            }
        });
    }

    if (resultTip && items.length > 0) {
        resultTip.textContent = "Showing " + items.length + " result(s).";
    }
</script>

</body>
</html>
