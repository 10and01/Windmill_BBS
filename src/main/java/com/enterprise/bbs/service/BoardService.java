package com.enterprise.bbs.service;

import com.enterprise.bbs.dao.BoardDAO;
import com.enterprise.bbs.model.Board;
import com.enterprise.bbs.model.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 版块业务逻辑层
 */
public class BoardService {
    private static final Logger logger = LoggerFactory.getLogger(BoardService.class);
    private final BoardDAO boardDAO = new BoardDAO();

    public Result<Board> createBoard(Board board) {
        if (board == null || isEmpty(board.getBoardName())) {
            return Result.error(400, "版块名称不能为空");
        }
        if (board.getBoardName().length() > 50) {
            return Result.error(400, "版块名称最多50个字符");
        }

        int boardId = boardDAO.insert(board);
        if (boardId > 0) {
            board.setBoardId(boardId);
            logger.info("创建版块成功: {}", board.getBoardName());
            return Result.success("创建成功", board);
        }
        return Result.error("创建失败");
    }

    public Result<Boolean> updateBoard(Board board) {
        if (board == null || board.getBoardId() == null || isEmpty(board.getBoardName())) {
            return Result.error(400, "参数错误");
        }

        Board exist = boardDAO.selectById(board.getBoardId());
        if (exist == null) {
            return Result.error(404, "版块不存在");
        }

        int rows = boardDAO.update(board);
        if (rows > 0) {
            return Result.success("修改成功", true);
        }
        return Result.error("修改失败");
    }

    public Result<Boolean> deleteBoard(int boardId) {
        Board board = boardDAO.selectById(boardId);
        if (board == null) {
            return Result.error(404, "版块不存在");
        }

        int rows = boardDAO.deleteById(boardId);
        if (rows > 0) {
            logger.info("删除版块成功: {}", boardId);
            return Result.success("删除成功", true);
        }
        return Result.error("删除失败");
    }

    public Result<List<Board>> getAllBoards() {
        List<Board> list = boardDAO.selectAll();
        return Result.success(list);
    }

    public Result<Board> getBoardById(int boardId) {
        Board board = boardDAO.selectById(boardId);
        if (board == null) {
            return Result.error(404, "版块不存在");
        }
        return Result.success(board);
    }

    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
