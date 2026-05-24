package com.enterprise.bbs.controller;

import com.enterprise.bbs.model.Board;
import com.enterprise.bbs.model.Result;
import com.enterprise.bbs.service.BoardService;
import com.enterprise.bbs.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;

/**
 * 版块控制器
 */
public class BoardServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(BoardServlet.class);
    private final BoardService boardService = new BoardService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) path = "";

        try {
            switch (path) {
                case "/list":
                    JsonUtil.writeJson(resp, boardService.getAllBoards());
                    break;
                default:
                    resp.sendError(404);
            }
        } catch (Exception e) {
            logger.error("BoardServlet GET error", e);
            JsonUtil.writeJson(resp, Result.error("服务器内部错误"));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) path = "";

        try {
            switch (path) {
                case "/add":
                    doAdd(req, resp);
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
            logger.error("BoardServlet POST error", e);
            JsonUtil.writeJson(resp, Result.error("服务器内部错误"));
        }
    }

    private void doAdd(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Board board = new Board();
        board.setBoardName(req.getParameter("boardName"));
        board.setDescription(req.getParameter("description"));
        String sortStr = req.getParameter("sortOrder");
        if (sortStr != null && !sortStr.isEmpty()) {
            board.setSortOrder(Integer.parseInt(sortStr));
        }
        String permStr = req.getParameter("postPermission");
        if (permStr != null && !permStr.isEmpty()) {
            board.setPostPermission(Integer.parseInt(permStr));
        }
        JsonUtil.writeJson(resp, boardService.createBoard(board));
    }

    private void doUpdate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Board board = new Board();
        board.setBoardId(Integer.parseInt(req.getParameter("boardId")));
        board.setBoardName(req.getParameter("boardName"));
        board.setDescription(req.getParameter("description"));
        String sortStr = req.getParameter("sortOrder");
        if (sortStr != null && !sortStr.isEmpty()) {
            board.setSortOrder(Integer.parseInt(sortStr));
        }
        String permStr = req.getParameter("postPermission");
        if (permStr != null && !permStr.isEmpty()) {
            board.setPostPermission(Integer.parseInt(permStr));
        }
        JsonUtil.writeJson(resp, boardService.updateBoard(board));
    }

    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String boardIdStr = req.getParameter("boardId");
        if (boardIdStr == null || boardIdStr.isEmpty()) {
            JsonUtil.writeJson(resp, Result.error(400, "参数错误"));
            return;
        }
        int boardId = Integer.parseInt(boardIdStr);
        JsonUtil.writeJson(resp, boardService.deleteBoard(boardId));
    }
}
