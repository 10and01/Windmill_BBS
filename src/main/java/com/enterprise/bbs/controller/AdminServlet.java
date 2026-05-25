package com.enterprise.bbs.controller;

import com.enterprise.bbs.model.Result;
import com.enterprise.bbs.model.User;
import com.enterprise.bbs.service.BoardService;
import com.enterprise.bbs.service.PostService;
import com.enterprise.bbs.service.ReportService;
import com.enterprise.bbs.service.UserService;
import com.enterprise.bbs.util.JsonUtil;
import com.enterprise.bbs.dao.BoardDAO;
import com.enterprise.bbs.dao.PostDAO;
import com.enterprise.bbs.dao.UserDAO;
import com.enterprise.bbs.model.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;

/**
 * 管理员控制器
 */
public class AdminServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(AdminServlet.class);
    private final UserService userService = new UserService();
    private final ReportService reportService = new ReportService();
    private final PostDAO postDAO = new PostDAO();
    private final UserDAO userDAO = new UserDAO();
    private final BoardDAO boardDAO = new BoardDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) path = "";

        try {
            switch (path) {
                case "/post/list":
                    doPostList(req, resp);
                    break;
                case "/user/list":
                    doUserList(req, resp);
                    break;
                case "/report/list":
                    doReportList(req, resp);
                    break;
                default:
                    resp.sendError(404);
            }
        } catch (Exception e) {
            logger.error("AdminServlet GET error", e);
            JsonUtil.writeJson(resp, Result.error("服务器内部错误"));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) path = "";

        try {
            switch (path) {
                case "/login":
                    doLogin(req, resp);
                    break;
                case "/post/delete":
                    doDeletePost(req, resp);
                    break;
                case "/post/top":
                    doToggleTop(req, resp);
                    break;
                case "/post/elite":
                    doToggleElite(req, resp);
                    break;
                case "/user/ban":
                    doBanUser(req, resp);
                    break;
                case "/report/handle":
                    doHandleReport(req, resp);
                    break;
                default:
                    resp.sendError(404);
            }
        } catch (Exception e) {
            logger.error("AdminServlet POST error", e);
            JsonUtil.writeJson(resp, Result.error("服务器内部错误"));
        }
    }

    private void doLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        Result<User> result = userService.login(username, password);
        if (result.isSuccess()) {
            if (!result.getData().isAdmin()) {
                JsonUtil.writeJson(resp, Result.error(403, "非管理员账户"));
                return;
            }
            req.getSession().setAttribute("currentUser", result.getData());
        }
        JsonUtil.writeJson(resp, result);
    }

    private void doPostList(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pageStr = req.getParameter("page");
        String pageSizeStr = req.getParameter("pageSize");

        int page = pageStr != null ? Integer.parseInt(pageStr) : 1;
        int pageSize = pageSizeStr != null ? Integer.parseInt(pageSizeStr) : 20;
        int offset = (page - 1) * pageSize;

        com.enterprise.bbs.model.PageInfo<com.enterprise.bbs.model.Post> pageInfo =
                new com.enterprise.bbs.model.PageInfo<>(page, pageSize, postDAO.countAll(), postDAO.selectAll(offset, pageSize));
        JsonUtil.writeJson(resp, Result.success(pageInfo));
    }

    private void doDeletePost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String postIdStr = req.getParameter("postId");
        if (postIdStr == null || postIdStr.isEmpty()) {
            JsonUtil.writeJson(resp, Result.error(400, "参数错误"));
            return;
        }
        int postId = Integer.parseInt(postIdStr);
        Post post = postDAO.selectById(postId);
        if (post == null) {
            JsonUtil.writeJson(resp, Result.error(404, "帖子不存在"));
            return;
        }
        postDAO.updateStatus(postId, 0);
        boardDAO.updatePostCount(post.getBoardId(), -1);
        JsonUtil.writeJson(resp, Result.success("删除成功", true));
    }

    private void doToggleTop(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String postIdStr = req.getParameter("postId");
        String isTopStr = req.getParameter("isTop");
        if (postIdStr == null || isTopStr == null) {
            JsonUtil.writeJson(resp, Result.error(400, "参数错误"));
            return;
        }
        int postId = Integer.parseInt(postIdStr);
        boolean isTop = Boolean.parseBoolean(isTopStr);
        postDAO.updateIsTop(postId, isTop);
        JsonUtil.writeJson(resp, Result.success("操作成功", true));
    }

    private void doToggleElite(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String postIdStr = req.getParameter("postId");
        String isEliteStr = req.getParameter("isElite");
        if (postIdStr == null || isEliteStr == null) {
            JsonUtil.writeJson(resp, Result.error(400, "参数错误"));
            return;
        }
        int postId = Integer.parseInt(postIdStr);
        boolean isElite = Boolean.parseBoolean(isEliteStr);
        postDAO.updateIsElite(postId, isElite);
        JsonUtil.writeJson(resp, Result.success("操作成功", true));
    }

    private void doUserList(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pageStr = req.getParameter("page");
        String pageSizeStr = req.getParameter("pageSize");

        int page = pageStr != null ? Integer.parseInt(pageStr) : 1;
        int pageSize = pageSizeStr != null ? Integer.parseInt(pageSizeStr) : 20;

        JsonUtil.writeJson(resp, Result.success(userDAO.countAll()));
    }

    private void doBanUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String userIdStr = req.getParameter("userId");
        String statusStr = req.getParameter("status");
        if (userIdStr == null || statusStr == null) {
            JsonUtil.writeJson(resp, Result.error(400, "参数错误"));
            return;
        }
        int userId = Integer.parseInt(userIdStr);
        int status = Integer.parseInt(statusStr);
        userDAO.updateStatus(userId, status);
        JsonUtil.writeJson(resp, Result.success("操作成功", true));
    }

    private void doReportList(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pageStr = req.getParameter("page");
        String pageSizeStr = req.getParameter("pageSize");

        int page = pageStr != null ? Integer.parseInt(pageStr) : 1;
        int pageSize = pageSizeStr != null ? Integer.parseInt(pageSizeStr) : 20;

        JsonUtil.writeJson(resp, reportService.getReportList(page, pageSize));
    }

    private void doHandleReport(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User currentUser = (User) req.getSession().getAttribute("currentUser");
        String reportIdStr = req.getParameter("reportId");
        String actionStr = req.getParameter("action");
        if (reportIdStr == null || actionStr == null) {
            JsonUtil.writeJson(resp, Result.error(400, "参数错误"));
            return;
        }
        int reportId = Integer.parseInt(reportIdStr);
        int action = Integer.parseInt(actionStr);
        JsonUtil.writeJson(resp, reportService.handleReport(reportId, action, currentUser.getUserId()));
    }
}
