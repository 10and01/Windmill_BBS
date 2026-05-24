package com.enterprise.bbs.dao;

import com.enterprise.bbs.model.Board;
import com.enterprise.bbs.util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 版块数据访问层
 */
public class BoardDAO {
    private static final Logger logger = LoggerFactory.getLogger(BoardDAO.class);

    public int insert(Board board) {
        String sql = "INSERT INTO t_board (board_name, description, post_count, sort_order, post_permission) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, board.getBoardName());
            ps.setString(2, board.getDescription());
            ps.setInt(3, board.getPostCount() != null ? board.getPostCount() : 0);
            ps.setInt(4, board.getSortOrder() != null ? board.getSortOrder() : 0);
            ps.setInt(5, board.getPostPermission() != null ? board.getPostPermission() : 0);
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("插入版块失败", e);
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return -1;
    }

    public int update(Board board) {
        String sql = "UPDATE t_board SET board_name=?, description=?, sort_order=?, post_permission=? WHERE board_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, board.getBoardName());
            ps.setString(2, board.getDescription());
            ps.setInt(3, board.getSortOrder() != null ? board.getSortOrder() : 0);
            ps.setInt(4, board.getPostPermission() != null ? board.getPostPermission() : 0);
            ps.setInt(5, board.getBoardId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("更新版块失败", e);
        } finally {
            DBUtil.close(conn, ps);
        }
        return 0;
    }

    public int updatePostCount(int boardId, int delta) {
        String sql = "UPDATE t_board SET post_count = post_count + ? WHERE board_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, delta);
            ps.setInt(2, boardId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("更新版块帖子数失败", e);
        } finally {
            DBUtil.close(conn, ps);
        }
        return 0;
    }

    public int deleteById(int boardId) {
        String sql = "DELETE FROM t_board WHERE board_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, boardId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("删除版块失败", e);
        } finally {
            DBUtil.close(conn, ps);
        }
        return 0;
    }

    public Board selectById(int boardId) {
        String sql = "SELECT * FROM t_board WHERE board_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, boardId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapBoard(rs);
            }
        } catch (SQLException e) {
            logger.error("查询版块失败", e);
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return null;
    }

    public List<Board> selectAll() {
        String sql = "SELECT * FROM t_board ORDER BY sort_order ASC, board_id ASC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Board> list = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapBoard(rs));
            }
        } catch (SQLException e) {
            logger.error("查询所有版块失败", e);
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return list;
    }

    private Board mapBoard(ResultSet rs) throws SQLException {
        Board board = new Board();
        board.setBoardId(rs.getInt("board_id"));
        board.setBoardName(rs.getString("board_name"));
        board.setDescription(rs.getString("description"));
        board.setPostCount(rs.getInt("post_count"));
        board.setSortOrder(rs.getInt("sort_order"));
        board.setPostPermission(rs.getInt("post_permission"));
        board.setCreateTime(rs.getTimestamp("create_time"));
        return board;
    }
}
