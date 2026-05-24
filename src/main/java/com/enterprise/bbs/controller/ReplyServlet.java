package com.enterprise.bbs.controller;

import com.enterprise.bbs.model.Reply;
import com.enterprise.bbs.model.Result;
import com.enterprise.bbs.model.User;
import com.enterprise.bbs.service.ReplyService;
import com.enterprise.bbs.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;

/**
 * 回复控制器
 */
public class ReplyServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(ReplyServlet.class);
    private final ReplyService replyService = new ReplyService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) path = "";

        try {
            if ("/list".equals(path)) {
                doList(req, resp);
            } else {
                resp.sendError(404);
            }
        } catch (Exception e) {
            logger.error("ReplyServlet GET error", e);
            JsonUtil.writeJson(resp, Result.error("服务器内部错误"));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) path = "";

        try {
            switch (path) {
                case "/publish":
                    doPublish(req, resp);
                    break;
                case "/delete":
                    doDelete(req, resp);
                    break;
                default:
                    resp.sendError(404);
            }
        } catch (Exception e) {
            logger.error("ReplyServlet POST error", e);
            JsonUtil.writeJson(resp, Result.error("服务器内部错误"));
        }
    }

    private void doList(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String postIdStr = req.getParameter("postId");
        String pageStr = req.getParameter("page");
        String pageSizeStr = req.getParameter("pageSize");

        if (postIdStr == null || postIdStr.isEmpty()) {
            JsonUtil.writeJson(resp, Result.error(400, "参数错误"));
            return;
        }

        int postId = Integer.parseInt(postIdStr);
        int page = pageStr != null ? Integer.parseInt(pageStr) : 1;
        int pageSize = pageSizeStr != null ? Integer.parseInt(pageSizeStr) : 20;

        JsonUtil.writeJson(resp, replyService.getRepliesByPostId(postId, page, pageSize));
    }

    private void doPublish(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User currentUser = (User) req.getSession().getAttribute("currentUser");
        Reply reply = new Reply();
        reply.setPostId(Integer.parseInt(req.getParameter("postId")));
        reply.setContent(req.getParameter("content"));
        reply.setAuthorId(currentUser.getUserId());
        reply.setImages(req.getParameter("images"));

        JsonUtil.writeJson(resp, replyService.createReply(reply));
    }

    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User currentUser = (User) req.getSession().getAttribute("currentUser");
        String replyIdStr = req.getParameter("replyId");
        if (replyIdStr == null || replyIdStr.isEmpty()) {
            JsonUtil.writeJson(resp, Result.error(400, "参数错误"));
            return;
        }
        int replyId = Integer.parseInt(replyIdStr);
        JsonUtil.writeJson(resp, replyService.deleteReply(replyId, currentUser.getUserId()));
    }
}
