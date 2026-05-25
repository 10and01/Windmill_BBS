package com.enterprise.bbs.controller;

import com.enterprise.bbs.model.Result;
import com.enterprise.bbs.model.User;
import com.enterprise.bbs.service.UserService;
import com.enterprise.bbs.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.*;
import java.io.IOException;

/**
 * 用户控制器
 */
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,
    maxFileSize = 1024 * 1024 * 5,
    maxRequestSize = 1024 * 1024 * 10
)
public class UserServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(UserServlet.class);
    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) path = "";

        try {
            switch (path) {
                case "/info":
                    doGetInfo(req, resp);
                    break;
                case "/profile":
                    doGetProfile(req, resp);
                    break;
                default:
                    resp.sendError(404);
            }
        } catch (Exception e) {
            logger.error("UserServlet GET error", e);
            JsonUtil.writeJson(resp, Result.error("服务器内部错误"));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) path = "";

        try {
            switch (path) {
                case "/register":
                    doRegister(req, resp);
                    break;
                case "/login":
                    doLogin(req, resp);
                    break;
                case "/logout":
                    doLogout(req, resp);
                    break;
                case "/update":
                    doUpdate(req, resp);
                    break;
                case "/uploadAvatar":
                    doUploadAvatar(req, resp);
                    break;
                default:
                    resp.sendError(404);
            }
        } catch (Exception e) {
            logger.error("UserServlet POST error", e);
            JsonUtil.writeJson(resp, Result.error("服务器内部错误"));
        }
    }

    private void doRegister(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = new User();
        user.setUsername(req.getParameter("username"));
        user.setPassword(req.getParameter("password"));
        user.setEmail(req.getParameter("email"));
        user.setNickname(req.getParameter("nickname"));

        Result<User> result = userService.register(user);
        if (result.isSuccess()) {
            req.getSession().setAttribute("currentUser", result.getData());
        }
        JsonUtil.writeJson(resp, result);
    }

    private void doLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        Result<User> result = userService.login(username, password);
        if (result.isSuccess()) {
            req.getSession().setAttribute("currentUser", result.getData());
        }
        JsonUtil.writeJson(resp, result);
    }

    private void doLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        JsonUtil.writeJson(resp, Result.success("退出成功", true));
    }

    private void doUpdate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User currentUser = (User) req.getSession().getAttribute("currentUser");
        User user = new User();
        user.setUserId(currentUser.getUserId());
        user.setNickname(req.getParameter("nickname"));
        user.setEmail(req.getParameter("email"));
        String avatar = req.getParameter("avatar");
        if (avatar == null || avatar.isEmpty()) {
            user.setAvatar(currentUser.getAvatar());
        } else {
            user.setAvatar(avatar);
        }

        Result<User> result = userService.updateUser(user);
        if (result.isSuccess()) {
            req.getSession().setAttribute("currentUser", result.getData());
        }
        JsonUtil.writeJson(resp, result);
    }

    private void doGetInfo(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User currentUser = (User) req.getSession().getAttribute("currentUser");
        if (currentUser == null) {
            JsonUtil.writeJson(resp, Result.error(401, "未登录"));
            return;
        }
        Result<User> result = userService.getUserById(currentUser.getUserId());
        if (result.isSuccess()) {
            req.getSession().setAttribute("currentUser", result.getData());
        }
        JsonUtil.writeJson(resp, result);
    }

    private void doGetProfile(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String userIdStr = req.getParameter("userId");
        if (userIdStr == null || userIdStr.isEmpty()) {
            JsonUtil.writeJson(resp, Result.error(400, "参数错误"));
            return;
        }
        int userId = Integer.parseInt(userIdStr);
        JsonUtil.writeJson(resp, userService.getUserProfile(userId));
    }

    private void doUploadAvatar(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        User currentUser = (User) req.getSession().getAttribute("currentUser");
        if (currentUser == null) {
            JsonUtil.writeJson(resp, Result.error(401, "未登录"));
            return;
        }

        javax.servlet.http.Part filePart = req.getPart("file");
        if (filePart == null || filePart.getSize() == 0) {
            JsonUtil.writeJson(resp, Result.error(400, "请选择图片"));
            return;
        }

        String contentType = filePart.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            JsonUtil.writeJson(resp, Result.error(400, "仅支持图片格式"));
            return;
        }

        if (filePart.getSize() > 5 * 1024 * 1024) {
            JsonUtil.writeJson(resp, Result.error(400, "图片大小不能超过5MB"));
            return;
        }

        String fileName = java.nio.file.Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
        String ext = "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            ext = fileName.substring(dotIndex).toLowerCase();
        }
        if (ext.isEmpty()) {
            ext = ".jpg";
        }

        String newFileName = java.util.UUID.randomUUID().toString() + ext;
        String uploadPath = req.getServletContext().getRealPath("/uploads");
        java.io.File uploadDir = new java.io.File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        java.io.File file = new java.io.File(uploadDir, newFileName);
        filePart.write(file.getAbsolutePath());

        String url = req.getContextPath() + "/uploads/" + newFileName;
        Result<User> result = userService.updateAvatar(currentUser.getUserId(), url);
        if (result.isSuccess()) {
            req.getSession().setAttribute("currentUser", result.getData());
        }
        JsonUtil.writeJson(resp, result);
    }
}
