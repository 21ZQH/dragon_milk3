<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Candidate" %>
<%
    List<Candidate> candidateList = (List<Candidate>) request.getAttribute("candidateList");
%>
<!DOCTYPE html>
<html>
<head>
    <title>Review Candidates</title>
    <style>
        body {
            background: #f7f7f9;
            font-family: 'Segoe UI', Arial, sans-serif;
            margin: 0;
            padding: 30px 20px;
        }

        .container {
            max-width: 980px;
            margin: 0 auto;
            background: #fff;
            border: 2px solid #22223b;
            border-radius: 16px;
            box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
            padding: 30px;
            box-sizing: border-box;
        }

        .title {
            margin: 0;
            font-size: 2em;
            color: #22223b;
        }

        .toolbar {
            margin-top: 20px;
            margin-bottom: 18px;
            display: flex;
            align-items: flex-start;
            gap: 10px;
            flex-wrap: wrap;
        }

        .toolbar label {
            font-weight: 600;
            color: #22223b;
        }

        .toolbar select {
            border: 2px solid #22223b;
            border-radius: 8px;
            padding: 6px 10px;
            font-size: 0.95em;
            background: #fff;
        }

        .back-link {
            text-decoration: none;
            color: #3b5998;
            font-weight: 600;
        }

        .toolbar-right {
            margin-left: auto;
            display: flex;
            flex-direction: column;
            align-items: flex-end;
            gap: 8px;
        }

        .picked-counter {
            font-size: 0.95em;
            font-weight: 700;
            color: #22223b;
            background: #f2f3fb;
            border: 1px solid #d8daf0;
            border-radius: 8px;
            padding: 6px 10px;
        }

        .picked-counter span {
            display: inline-block;
            min-width: 20px;
            text-align: center;
            text-decoration: none;
        }

        .table-wrap {
            max-height: 320px;
            overflow-y: auto;
            border: 1px solid #d9d9e3;
            border-radius: 10px;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            background: #fff;
        }

        th, td {
            border: 1px solid #e5e5ee;
            padding: 12px;
            text-align: left;
            vertical-align: top;
        }

        th {
            background: #f2f3fb;
            color: #1f2140;
            font-size: 0.95em;
        }

        tbody tr:hover {
            background: #fafafe;
        }

        .pick-cell {
            text-align: center;
            width: 70px;
        }

        .empty {
            color: #666;
            text-align: center;
            padding: 16px;
        }

        .actions {
            margin-top: 16px;
            display: flex;
            justify-content: center;
        }

        .save-btn {
            border: 2px solid #22223b;
            background: #22223b;
            color: #fff;
            border-radius: 10px;
            padding: 10px 18px;
            font-size: 0.95em;
            font-weight: 700;
            cursor: pointer;
        }

        .save-btn:hover {
            background: #30325f;
            border-color: #30325f;
        }

        .confirm-mask {
            position: fixed;
            inset: 0;
            background: rgba(0, 0, 0, 0.45);
            display: none;
            align-items: center;
            justify-content: center;
            z-index: 9999;
        }

        .confirm-dialog {
            width: min(92vw, 430px);
            background: #fff;
            border-radius: 12px;
            border: 2px solid #22223b;
            box-shadow: 0 12px 28px rgba(0, 0, 0, 0.22);
            padding: 18px;
            box-sizing: border-box;
        }

        .confirm-title {
            margin: 0 0 8px 0;
            color: #22223b;
            font-size: 1.1em;
            font-weight: 700;
        }

        .confirm-text {
            margin: 0;
            color: #333;
            line-height: 1.55;
        }

        .confirm-actions {
            margin-top: 14px;
            display: flex;
            justify-content: flex-end;
            gap: 10px;
        }

        .dialog-btn {
            border: 2px solid #22223b;
            border-radius: 8px;
            padding: 7px 14px;
            font-weight: 700;
            cursor: pointer;
            background: #fff;
            color: #22223b;
        }

        .dialog-btn.primary {
            background: #22223b;
            color: #fff;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1 class="title">Review</h1>

        <div class="toolbar">
            <label for="filterMode">Show:</label>
            <select id="filterMode">
                <option value="all">All candidates</option>
                <option value="picked">Picked candidates</option>
            </select>
            <div class="toolbar-right">
                <a id="backLink" class="back-link" href="<%= response.encodeURL("MOclasscontroller?action=personal_center") %>">Back to Personal Centre</a>
                <div class="picked-counter">Picked: <span id="pickedCount">0</span></div>
            </div>
        </div>

        <form id="reviewForm" method="post" action="<%= response.encodeURL("MOclasscontroller") %>">
            <input type="hidden" name="action" value="save_review_picks" />
            <input type="hidden" id="returnToField" name="returnTo" value="review" />

            <div class="table-wrap">
                <table id="candidateTable">
                    <thead>
                        <tr>
                            <th class="pick-cell">Pick</th>
                            <th>Name</th>
                            <th>Education</th>
                            <th>Other Information</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (candidateList == null || candidateList.isEmpty()) { %>
                        <tr>
                            <td colspan="4" class="empty">No candidate data found.</td>
                        </tr>
                        <% } else { %>
                            <% for (int i = 0; i < candidateList.size(); i++) { %>
                            <% Candidate candidate = candidateList.get(i); %>
                            <tr data-picked="<%= candidate.isPicked() %>">
                                <td class="pick-cell">
                                    <input
                                        type="checkbox"
                                        class="pick-checkbox"
                                        name="pickedIndex"
                                        value="<%= i %>"
                                        <%= candidate.isPicked() ? "checked" : "" %>
                                    />
                                </td>
                                <td><%= candidate.getName() %></td>
                                <td><%= candidate.getEducation() %></td>
                                <td><%= candidate.getDetails() %></td>
                            </tr>
                            <% } %>
                        <% } %>
                    </tbody>
                </table>
            </div>

            <div class="actions">
                <button id="saveButton" type="submit" class="save-btn">Save Changes</button>
            </div>
        </form>
    </div>

    <div id="confirmMask" class="confirm-mask" aria-hidden="true">
        <div class="confirm-dialog" role="dialog" aria-modal="true" aria-labelledby="confirmTitle">
            <h2 id="confirmTitle" class="confirm-title">Confirm</h2>
            <p class="confirm-text">Do you want to save your pick changes?</p>
            <div class="confirm-actions">
                <button id="confirmYes" type="button" class="dialog-btn primary">Yes</button>
                <button id="confirmNo" type="button" class="dialog-btn">No</button>
            </div>
        </div>
    </div>

    <script>
        (function () {
            var reviewForm = document.getElementById('reviewForm');
            var filterMode = document.getElementById('filterMode');
            var backLink = document.getElementById('backLink');
            var returnToField = document.getElementById('returnToField');
            var confirmMask = document.getElementById('confirmMask');
            var confirmYes = document.getElementById('confirmYes');
            var confirmNo = document.getElementById('confirmNo');
            var rows = document.querySelectorAll('#candidateTable tbody tr');
            var checkboxes = document.querySelectorAll('.pick-checkbox');
            var initialState = Array.prototype.map.call(checkboxes, function (checkbox) {
                return checkbox.checked;
            });

            function hasUnsavedChanges() {
                for (var i = 0; i < checkboxes.length; i++) {
                    if (checkboxes[i].checked !== initialState[i]) {
                        return true;
                    }
                }
                return false;
            }

            function applyFilter() {
                var mode = filterMode.value;
                rows.forEach(function (row) {
                    if (!row.querySelector('.pick-checkbox')) {
                        row.style.display = '';
                        return;
                    }
                    var picked = row.getAttribute('data-picked') === 'true';
                    row.style.display = (mode === 'picked' && !picked) ? 'none' : '';
                });
            }

            function updatePickedCounter() {
                var pickedCount = 0;
                checkboxes.forEach(function (checkbox) {
                    if (checkbox.checked) {
                        pickedCount++;
                    }
                });
                document.getElementById('pickedCount').textContent = pickedCount;
            }

            checkboxes.forEach(function (checkbox) {
                checkbox.addEventListener('change', function () {
                    var row = checkbox.closest('tr');
                    row.setAttribute('data-picked', checkbox.checked ? 'true' : 'false');
                    updatePickedCounter();
                    applyFilter();
                });
            });

            reviewForm.addEventListener('submit', function () {
                returnToField.value = 'review';
            });

            function openConfirm() {
                confirmMask.style.display = 'flex';
                confirmMask.setAttribute('aria-hidden', 'false');
            }

            function closeConfirm() {
                confirmMask.style.display = 'none';
                confirmMask.setAttribute('aria-hidden', 'true');
            }

            backLink.addEventListener('click', function (event) {
                if (!hasUnsavedChanges()) {
                    return;
                }

                event.preventDefault();
                openConfirm();
            });

            confirmYes.addEventListener('click', function () {
                returnToField.value = 'personal_center';
                closeConfirm();
                reviewForm.submit();
            });

            confirmNo.addEventListener('click', function () {
                closeConfirm();
                window.location.href = backLink.getAttribute('href');
            });

            confirmMask.addEventListener('click', function (event) {
                if (event.target === confirmMask) {
                    closeConfirm();
                }
            });

            filterMode.addEventListener('change', applyFilter);
            updatePickedCounter();
            applyFilter();
        })();
    </script>
</body>
</html>
