package com.enterprise.bbs.controller;

import com.enterprise.bbs.model.Post;
import com.enterprise.bbs.model.Result;
import com.enterprise.bbs.model.User;
import com.enterprise.bbs.service.PostService;
import com.enterprise.bbs.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;

/**
 * 帖子控制器
 */
public class PostServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(PostServlet.class);
    private final PostService postService = new PostService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) path = "";

        try {
            switch (path) {
                case "/list":
                    doList(req, resp);
                    break;
                case "/detail":
                    doDetail(req, resp);
                    break;
                case "/search":
                    doSearch(req, resp);
                    break;
                default:
                    resp.sendError(404);
            }
        } catch (Exception e) {
            logger.error("PostServlet GET error", e);
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
                case "/update":
                    doUpdate(req, resp);
                    break;
                case "/delete":
                    doDelete(req, resp);
                    break;
                default:
                    resp.sendError(404);
            }
        } catch (Exception e) {
            logger.error("PostServlet POST error", e);
            JsonUtil.writeJson(resp, Result.error("服务器内部错误"));
        }
    }

    private void doList(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String boardIdStr = req.getParameter("boardId");
        String pageStr = req.getParameter("page");
        String pageSizeStr = req.getParameter("pageSize");

        Integer boardId = null;
        if (boardIdStr != null && !boardIdStr.isEmpty()) {
            boardId = Integer.parseInt(boardIdStr);
        }
        int page = pageStr != null ? Integer.parseInt(pageStr) : 1;
        int pageSize = pageSizeStr != null ? Integer.parseInt(pageSizeStr) : 20;

        JsonUtil.writeJson(resp, postService.getPostList(boardId, page, pageSize));
    }

    private void doDetail(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idStr = req.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            JsonUtil.writeJson(resp, Result.error(400, "参数错误"));
            return;
        }
        int postId = Integer.parseInt(idStr);
        JsonUtil.writeJson(resp, postService.getPostById(postId));
    }

    private void doSearch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String keyword = req.getParameter("keyword");
        String pageStr = req.getParameter("page");
        String pageSizeStr = req.getParameter("pageSize");

        int page = pageStr != null ? Integer.parseInt(pageStr) : 1;
        int pageSize = pageSizeStr != null ? Integer.parseInt(pageSizeStr) : 20;

        JsonUtil.writeJson(resp, postService.searchPosts(keyword, page, pageSize));
    }

    private void doPublish(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User currentUser = (User) req.getSession().getAttribute("currentUser");
        Post post = new Post();
        post.setTitle(req.getParameter("title"));
        post.setContent(req.getParameter("content"));
        post.setImages(req.getParameter("images"));
        post.setBoardId(Integer.parseInt(req.getParameter("boardId")));
        post.setAuthorId(currentUser.getUserId());

        JsonUtil.writeJson(resp, postService.createPost(post));
    }

    private void doUpdate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User currentUser = (User) req.getSession().getAttribute("currentUser");
        Post post = new Post();
        post.setPostId(Integer.parseInt(req.getParameter("postId")));
        post.setTitle(req.getParameter("title"));
        post.setContent(req.getParameter("content"));
        post.setImages(req.getParameter("images"));
        post.setBoardId(Integer.parseInt(req.getParameter("boardId")));

        JsonUtil.writeJson(resp, postService.updatePost(post, currentUser.getUserId()));
    }

    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User currentUser = (User) req.getSession().getAttribute("currentUser");
        String postIdStr = req.getParameter("postId");
        if (postIdStr == null || postIdStr.isEmpty()) {
            JsonUtil.writeJson(resp, Result.error(400, "参数错误"));
            return;
        }
        int postId = Integer.parseInt(postIdStr);
        JsonUtil.writeJson(resp, postService.deletePost(postId, currentUser.getUserId()));
    }
}
