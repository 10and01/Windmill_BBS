package com.enterprise.bbs.dao;

import com.enterprise.bbs.model.Reply;
import com.enterprise.bbs.util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 回复数据访问层
 */
public class ReplyDAO {
    private static final Logger logger = LoggerFactory.getLogger(ReplyDAO.class);

    public int insert(Reply reply) {
        String sql = "INSERT INTO t_reply (post_id, author_id, content, images, reply_time, floor_num, like_count, status) VALUES (?, ?, ?, ?, NOW(), ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, reply.getPostId());
            ps.setInt(2, reply.getAuthorId());
            ps.setString(3, reply.getContent());
            ps.setString(4, reply.getImages());
            ps.setInt(5, reply.getFloorNum());
            ps.setInt(6, reply.getLikeCount() != null ? reply.getLikeCount() : 0);
            ps.setInt(7, reply.getStatus() != null ? reply.getStatus() : 1);
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("插入回复失败", e);
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return -1;
    }

    public int deleteById(int replyId) {
        String sql = "UPDATE t_reply SET status=0 WHERE reply_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, replyId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("删除回复失败", e);
        } finally {
            DBUtil.close(conn, ps);
        }
        return 0;
    }

    public int deleteByPostId(int postId) {
        String sql = "UPDATE t_reply SET status=0 WHERE post_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, postId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("按帖子删除回复失败", e);
        } finally {
            DBUtil.close(conn, ps);
        }
        return 0;
    }

    public Reply selectById(int replyId) {
        String sql = "SELECT r.*, u.nickname as author_name, u.avatar as author_avatar FROM t_reply r " +
                "LEFT JOIN t_user u ON r.author_id = u.user_id " +
                "WHERE r.reply_id=? AND r.status=1";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, replyId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapReply(rs);
            }
        } catch (SQLException e) {
            logger.error("查询回复失败", e);
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return null;
    }

    public List<Reply> selectByPostId(int postId, int offset, int limit) {
        String sql = "SELECT r.*, u.nickname as author_name, u.avatar as author_avatar FROM t_reply r " +
                "LEFT JOIN t_user u ON r.author_id = u.user_id " +
                "WHERE r.post_id=? AND r.status=1 ORDER BY r.floor_num ASC LIMIT ?,?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Reply> list = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, postId);
            ps.setInt(2, offset);
            ps.setInt(3, limit);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapReply(rs));
            }
        } catch (SQLException e) {
            logger.error("查询回复列表失败", e);
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return list;
    }

    public int countByPostId(int postId) {
        String sql = "SELECT COUNT(*) FROM t_reply WHERE post_id=? AND status=1";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, postId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("统计回复数失败", e);
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return 0;
    }

    public int countByAuthorId(int authorId) {
        String sql = "SELECT COUNT(*) FROM t_reply WHERE author_id=? AND status=1";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, authorId);
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

    public int selectMaxFloorByPostId(int postId) {
        String sql = "SELECT MAX(floor_num) FROM t_reply WHERE post_id=? AND status=1";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, postId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("查询最大楼层失败", e);
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return 0;
    }

    public int updateLikeCount(int replyId, int delta) {
        String sql = "UPDATE t_reply SET like_count = like_count + ? WHERE reply_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, delta);
            ps.setInt(2, replyId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("更新回复点赞数失败", e);
        } finally {
            DBUtil.close(conn, ps);
        }
        return 0;
    }

    private Reply mapReply(ResultSet rs) throws SQLException {
        Reply reply = new Reply();
        reply.setReplyId(rs.getInt("reply_id"));
        reply.setPostId(rs.getInt("post_id"));
        reply.setAuthorId(rs.getInt("author_id"));
        reply.setContent(rs.getString("content"));
        reply.setImages(rs.getString("images"));
        reply.setReplyTime(rs.getTimestamp("reply_time"));
        reply.setFloorNum(rs.getInt("floor_num"));
        reply.setLikeCount(rs.getInt("like_count"));
        reply.setStatus(rs.getInt("status"));
        reply.setAuthorName(rs.getString("author_name"));
        reply.setAuthorAvatar(rs.getString("author_avatar"));
        return reply;
    }
}
