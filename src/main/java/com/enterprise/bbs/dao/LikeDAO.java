package com.enterprise.bbs.dao;

import com.enterprise.bbs.model.Like;
import com.enterprise.bbs.util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * 点赞数据访问层
 */
public class LikeDAO {
    private static final Logger logger = LoggerFactory.getLogger(LikeDAO.class);

    public int insert(Like like) {
        String sql = "INSERT INTO t_like (user_id, target_type, target_id, create_time) VALUES (?, ?, ?, NOW())";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, like.getUserId());
            ps.setInt(2, like.getTargetType());
            ps.setInt(3, like.getTargetId());
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("插入点赞记录失败", e);
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return -1;
    }

    public int delete(int userId, int targetType, int targetId) {
        String sql = "DELETE FROM t_like WHERE user_id=? AND target_type=? AND target_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, targetType);
            ps.setInt(3, targetId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("删除点赞记录失败", e);
        } finally {
            DBUtil.close(conn, ps);
        }
        return 0;
    }

    public Like selectByUserAndTarget(int userId, int targetType, int targetId) {
        String sql = "SELECT * FROM t_like WHERE user_id=? AND target_type=? AND target_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, targetType);
            ps.setInt(3, targetId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapLike(rs);
            }
        } catch (SQLException e) {
            logger.error("查询点赞记录失败", e);
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return null;
    }

    public int countByTarget(int targetType, int targetId) {
        String sql = "SELECT COUNT(*) FROM t_like WHERE target_type=? AND target_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, targetType);
            ps.setInt(2, targetId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("统计点赞数失败", e);
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return 0;
    }

    private Like mapLike(ResultSet rs) throws SQLException {
        Like like = new Like();
        like.setLikeId(rs.getInt("like_id"));
        like.setUserId(rs.getInt("user_id"));
        like.setTargetType(rs.getInt("target_type"));
        like.setTargetId(rs.getInt("target_id"));
        like.setCreateTime(rs.getTimestamp("create_time"));
        return like;
    }
}
