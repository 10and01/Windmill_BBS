package com.enterprise.bbs.dao;

import com.enterprise.bbs.model.Post;
import com.enterprise.bbs.util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 帖子数据访问层
 */
public class PostDAO {
    private static final Logger logger = LoggerFactory.getLogger(PostDAO.class);

    public int insert(Post post) {
        String sql = "INSERT INTO t_post (title, content, images, author_id, board_id, reply_count, view_count, like_count, post_time, is_top, is_elite, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, post.getTitle());
            ps.setString(2, post.getContent());
            ps.setString(3, post.getImages());
            ps.setInt(4, post.getAuthorId());
            ps.setInt(5, post.getBoardId());
            ps.setInt(6, post.getReplyCount() != null ? post.getReplyCount() : 0);
            ps.setInt(7, post.getViewCount() != null ? post.getViewCount() : 0);
            ps.setInt(8, post.getLikeCount() != null ? post.getLikeCount() : 0);
            ps.setBoolean(9, post.getIsTop() != null ? post.getIsTop() : false);
            ps.setBoolean(10, post.getIsElite() != null ? post.getIsElite() : false);
            ps.setInt(11, post.getStatus() != null ? post.getStatus() : 1);
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("插入帖子失败", e);
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return -1;
    }

    public int update(Post post) {
        String sql = "UPDATE t_post SET title=?, content=?, board_id=?, images=? WHERE post_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, post.getTitle());
            ps.setString(2, post.getContent());
            ps.setInt(3, post.getBoardId());
            ps.setString(4, post.getImages());
            ps.setInt(5, post.getPostId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("更新帖子失败", e);
        } finally {
            DBUtil.close(conn, ps);
        }
        return 0;
    }

    public int updateStatus(int postId, int status) {
        String sql = "UPDATE t_post SET status=? WHERE post_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, status);
            ps.setInt(2, postId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("更新帖子状态失败", e);
        } finally {
            DBUtil.close(conn, ps);
        }
        return 0;
    }

    public int updateIsTop(int postId, boolean isTop) {
        String sql = "UPDATE t_post SET is_top=? WHERE post_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setBoolean(1, isTop);
            ps.setInt(2, postId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("更新帖子置顶状态失败", e);
        } finally {
            DBUtil.close(conn, ps);
        }
        return 0;
    }

    public int updateIsElite(int postId, boolean isElite) {
        String sql = "UPDATE t_post SET is_elite=? WHERE post_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setBoolean(1, isElite);
            ps.setInt(2, postId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("更新帖子精华状态失败", e);
        } finally {
            DBUtil.close(conn, ps);
        }
        return 0;
    }

    public int updateViewCount(int postId) {
        String sql = "UPDATE t_post SET view_count = view_count + 1 WHERE post_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, postId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("更新浏览次数失败", e);
        } finally {
            DBUtil.close(conn, ps);
        }
        return 0;
    }

    public int updateReplyCount(int postId, int delta) {
        String sql = "UPDATE t_post SET reply_count = reply_count + ? WHERE post_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, delta);
            ps.setInt(2, postId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("更新回复数失败", e);
        } finally {
            DBUtil.close(conn, ps);
        }
        return 0;
    }

    public int updateLikeCount(int postId, int delta) {
        String sql = "UPDATE t_post SET like_count = like_count + ? WHERE post_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, delta);
            ps.setInt(2, postId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("更新帖子点赞数失败", e);
        } finally {
            DBUtil.close(conn, ps);
        }
        return 0;
    }

    public int deleteById(int postId) {
        return updateStatus(postId, 0);
    }

    public Post selectById(int postId) {
        String sql = "SELECT p.*, u.nickname as author_name, b.board_name, u.avatar as author_avatar FROM t_post p " +
                "LEFT JOIN t_user u ON p.author_id = u.user_id " +
                "LEFT JOIN t_board b ON p.board_id = b.board_id " +
                "WHERE p.post_id=? AND p.status=1";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, postId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapPost(rs);
            }
        } catch (SQLException e) {
            logger.error("查询帖子失败", e);
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return null;
    }

    public List<Post> selectByBoardId(int boardId, int offset, int limit) {
        String sql;
        if (boardId > 0) {
            sql = "SELECT p.*, u.nickname as author_name, b.board_name, u.avatar as author_avatar FROM t_post p " +
                    "LEFT JOIN t_user u ON p.author_id = u.user_id " +
                    "LEFT JOIN t_board b ON p.board_id = b.board_id " +
                    "WHERE p.board_id=? AND p.status=1 ORDER BY p.is_top DESC, p.post_time DESC LIMIT ?,?";
        } else {
            sql = "SELECT p.*, u.nickname as author_name, b.board_name, u.avatar as author_avatar FROM t_post p " +
                    "LEFT JOIN t_user u ON p.author_id = u.user_id " +
                    "LEFT JOIN t_board b ON p.board_id = b.board_id " +
                    "WHERE p.status=1 ORDER BY p.is_top DESC, p.post_time DESC LIMIT ?,?";
        }
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Post> list = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            if (boardId > 0) {
                ps.setInt(1, boardId);
                ps.setInt(2, offset);
                ps.setInt(3, limit);
            } else {
                ps.setInt(1, offset);
                ps.setInt(2, limit);
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapPost(rs));
            }
        } catch (SQLException e) {
            logger.error("查询帖子列表失败", e);
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return list;
    }

    public List<Post> selectByAuthorId(int authorId, int offset, int limit) {
        String sql = "SELECT p.*, u.nickname as author_name, b.board_name, u.avatar as author_avatar FROM t_post p " +
                "LEFT JOIN t_user u ON p.author_id = u.user_id " +
                "LEFT JOIN t_board b ON p.board_id = b.board_id " +
                "WHERE p.author_id=? AND p.status=1 ORDER BY p.post_time DESC LIMIT ?,?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Post> list = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, authorId);
            ps.setInt(2, offset);
            ps.setInt(3, limit);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapPost(rs));
            }
        } catch (SQLException e) {
            logger.error("查询用户帖子列表失败", e);
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return list;
    }

    public int countByBoardId(int boardId) {
        String sql;
        if (boardId > 0) {
            sql = "SELECT COUNT(*) FROM t_post WHERE board_id=? AND status=1";
        } else {
            sql = "SELECT COUNT(*) FROM t_post WHERE status=1";
        }
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            if (boardId > 0) {
                ps.setInt(1, boardId);
            }
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("统计帖子数失败", e);
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return 0;
    }

    public int countByAuthorId(int authorId) {
        String sql = "SELECT COUNT(*) FROM t_post WHERE author_id=? AND status=1";
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
            logger.error("统计用户帖子数失败", e);
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return 0;
    }

    public List<Post> searchByKeyword(String keyword, int offset, int limit) {
        String sql = "SELECT p.*, u.nickname as author_name, b.board_name, u.avatar as author_avatar FROM t_post p " +
                "LEFT JOIN t_user u ON p.author_id = u.user_id " +
                "LEFT JOIN t_board b ON p.board_id = b.board_id " +
                "WHERE p.status=1 AND (p.title LIKE ? OR p.content LIKE ?) " +
                "ORDER BY p.post_time DESC LIMIT ?,?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Post> list = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setInt(3, offset);
            ps.setInt(4, limit);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapPost(rs));
            }
        } catch (SQLException e) {
            logger.error("搜索帖子失败", e);
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return list;
    }

    public List<Post> searchByKeywordOrderByLike(String keyword, int offset, int limit) {
        String sql = "SELECT p.*, u.nickname as author_name, b.board_name, u.avatar as author_avatar FROM t_post p " +
                "LEFT JOIN t_user u ON p.author_id = u.user_id " +
                "LEFT JOIN t_board b ON p.board_id = b.board_id " +
                "WHERE p.status=1 AND (p.title LIKE ? OR p.content LIKE ?) " +
                "ORDER BY p.like_count DESC, p.post_time DESC LIMIT ?,?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Post> list = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setInt(3, offset);
            ps.setInt(4, limit);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapPost(rs));
            }
        } catch (SQLException e) {
            logger.error("搜索帖子失败", e);
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return list;
    }

    public int countByKeyword(String keyword) {
        String sql = "SELECT COUNT(*) FROM t_post WHERE status=1 AND (title LIKE ? OR content LIKE ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("统计搜索帖子数失败", e);
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return 0;
    }

    public List<Post> selectAll(int offset, int limit) {
        String sql = "SELECT p.*, u.nickname as author_name, b.board_name, u.avatar as author_avatar FROM t_post p " +
                "LEFT JOIN t_user u ON p.author_id = u.user_id " +
                "LEFT JOIN t_board b ON p.board_id = b.board_id " +
                "ORDER BY p.post_id DESC LIMIT ?,?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Post> list = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, offset);
            ps.setInt(2, limit);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapPost(rs));
            }
        } catch (SQLException e) {
            logger.error("查询所有帖子失败", e);
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return list;
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM t_post";
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
            logger.error("统计帖子总数失败", e);
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return 0;
    }

    private Post mapPost(ResultSet rs) throws SQLException {
        Post post = new Post();
        post.setPostId(rs.getInt("post_id"));
        post.setTitle(rs.getString("title"));
        post.setContent(rs.getString("content"));
        post.setImages(rs.getString("images"));
        post.setAuthorId(rs.getInt("author_id"));
        post.setBoardId(rs.getInt("board_id"));
        post.setReplyCount(rs.getInt("reply_count"));
        post.setViewCount(rs.getInt("view_count"));
        post.setLikeCount(rs.getInt("like_count"));
        post.setPostTime(rs.getTimestamp("post_time"));
        post.setIsTop(rs.getBoolean("is_top"));
        post.setIsElite(rs.getBoolean("is_elite"));
        post.setStatus(rs.getInt("status"));
        post.setAuthorName(rs.getString("author_name"));
        post.setBoardName(rs.getString("board_name"));
        post.setAuthorAvatar(rs.getString("author_avatar"));
        return post;
    }
}
