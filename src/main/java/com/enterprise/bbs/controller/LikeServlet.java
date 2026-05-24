package com.enterprise.bbs.controller;

import com.enterprise.bbs.model.Result;
import com.enterprise.bbs.model.User;
import com.enterprise.bbs.service.LikeService;
import com.enterprise.bbs.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;

/**
 * 点赞控制器
 */
public class LikeServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(LikeServlet.class);
    private final LikeService likeService = new LikeService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) path = "";

        try {
            switch (path) {
                case "/status":
                    doGetStatus(req, resp);
                    break;
                default:
                    resp.sendError(404);
            }
        } catch (Exception e) {
            logger.error("LikeServlet GET error", e);
            JsonUtil.writeJson(resp, Result.error("服务器内部错误"));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) path = "";

        try {
            switch (path) {
                case "/toggle":
                    doToggle(req, resp);
                    break;
                default:
                    resp.sendError(404);
            }
        } catch (Exception e) {
            logger.error("LikeServlet POST error", e);
            JsonUtil.writeJson(resp, Result.error("服务器内部错误"));
        }
    }

    private void doGetStatus(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User currentUser = (User) req.getSession().getAttribute("currentUser");
        String targetTypeStr = req.getParameter("targetType");
        String targetIdStr = req.getParameter("targetId");

        if (targetTypeStr == null || targetIdStr == null) {
            JsonUtil.writeJson(resp, Result.error(400, "参数错误"));
            return;
        }

        int targetType = Integer.parseInt(targetTypeStr);
        int targetId = Integer.parseInt(targetIdStr);
        JsonUtil.writeJson(resp, likeService.getLikeStatus(currentUser.getUserId(), targetType, targetId));
    }

    private void doToggle(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User currentUser = (User) req.getSession().getAttribute("currentUser");
        String targetTypeStr = req.getParameter("targetType");
        String targetIdStr = req.getParameter("targetId");

        if (targetTypeStr == null || targetIdStr == null) {
            JsonUtil.writeJson(resp, Result.error(400, "参数错误"));
            return;
        }

        int targetType = Integer.parseInt(targetTypeStr);
        int targetId = Integer.parseInt(targetIdStr);
        JsonUtil.writeJson(resp, likeService.toggleLike(currentUser.getUserId(), targetType, targetId));
    }
}
