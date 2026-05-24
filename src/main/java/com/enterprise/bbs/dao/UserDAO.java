package com.enterprise.bbs.dao;

import com.enterprise.bbs.model.User;
import com.enterprise.bbs.util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * 用户数据访问层
 */
public class UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);

    public int insert(User user) {
        String sql = "INSERT INTO t_user (username, password, email, nickname, avatar, role, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getNickname());
            ps.setString(5, user.getAvatar());
            ps.setInt(6, user.getRole() != null ? user.getRole() : 1);
            ps.setInt(7, user.getStatus() != null ? user.getStatus() : 1);
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("插入用户失败", e);
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return -1;
    }

    public int update(User user) {
        String sql = "UPDATE t_user SET nickname=?, email=?, avatar=? WHERE user_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, user.getNickname());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getAvatar());
            ps.setInt(4, user.getUserId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("更新用户失败", e);
        } finally {
            DBUtil.close(conn, ps);
        }
        return 0;
    }

    public int updateAvatar(int userId, String avatar) {
        String sql = "UPDATE t_user SET avatar=? WHERE user_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, avatar);
            ps.setInt(2, userId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("更新用户头像失败", e);
        } finally {
            DBUtil.close(conn, ps);
        }
        return 0;
    }

    public int updateStatus(int userId, int status) {
        String sql = "UPDATE t_user SET status=? WHERE user_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, status);
            ps.setInt(2, userId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("更新用户状态失败", e);
        } finally {
            DBUtil.close(conn, ps);
        }
        return 0;
    }

    public User selectById(int userId) {
        String sql = "SELECT * FROM t_user WHERE user_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapUser(rs);
            }
        } catch (SQLException e) {
            logger.error("查询用户失败", e);
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return null;
    }

    public User selectByUsername(String username) {
        String sql = "SELECT * FROM t_user WHERE username=?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapUser(rs);
            }
        } catch (SQLException e) {
            logger.error("查询用户失败", e);
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return null;
    }

    public int countByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM t_user WHERE username=?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("统计用户名失败", e);
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return 0;
    }

    public int countPostsByUserId(int userId) {
        String sql = "SELECT COUNT(*) FROM t_post WHERE author_id=? AND status=1";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("统计用户发帖数失败", e);
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return 0;
    }

    public int countRepliesByUserId(int userId) {
        String sql = "SELECT COUNT(*) FROM t_reply WHERE author_id=? AND status=1";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("统计用户回复数失败", e);
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return 0;
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM t_user";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("统计用户总数失败", e);
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return 0;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setEmail(rs.getString("email"));
        user.setNickname(rs.getString("nickname"));
        user.setAvatar(rs.getString("avatar"));
        user.setRole(rs.getInt("role"));
        user.setRegisterTime(rs.getTimestamp("register_time"));
        user.setStatus(rs.getInt("status"));
        return user;
    }
}
