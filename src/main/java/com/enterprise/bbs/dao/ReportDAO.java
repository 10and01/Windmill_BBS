package com.enterprise.bbs.dao;

import com.enterprise.bbs.model.Report;
import com.enterprise.bbs.util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 举报数据访问层
 */
public class ReportDAO {
    private static final Logger logger = LoggerFactory.getLogger(ReportDAO.class);

    public int insert(Report report) {
        String sql = "INSERT INTO t_report (reporter_id, target_type, target_id, reason, status, create_time) VALUES (?, ?, ?, ?, 0, NOW())";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, report.getReporterId());
            ps.setInt(2, report.getTargetType());
            ps.setInt(3, report.getTargetId());
            ps.setString(4, report.getReason());
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("插入举报记录失败", e);
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return -1;
    }

    public Report selectById(int reportId) {
        String sql = "SELECT r.*, u.nickname as reporter_name FROM t_report r LEFT JOIN t_user u ON r.reporter_id = u.user_id WHERE r.report_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, reportId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapReport(rs);
            }
        } catch (SQLException e) {
            logger.error("查询举报记录失败", e);
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return null;
    }

    public List<Report> selectList(int offset, int limit) {
        String sql = "SELECT r.*, u.nickname as reporter_name FROM t_report r LEFT JOIN t_user u ON r.reporter_id = u.user_id ORDER BY r.create_time DESC LIMIT ?,?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Report> list = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, offset);
            ps.setInt(2, limit);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapReport(rs));
            }
        } catch (SQLException e) {
            logger.error("查询举报列表失败", e);
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return list;
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM t_report";
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
            logger.error("统计举报总数失败", e);
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return 0;
    }

    public int countByStatus(int status) {
        String sql = "SELECT COUNT(*) FROM t_report WHERE status=?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, status);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("统计举报数失败", e);
        } finally {
            DBUtil.close(conn, ps, rs);
        }
        return 0;
    }

    public int updateStatus(int reportId, int status, int adminId) {
        String sql = "UPDATE t_report SET status=?, admin_id=?, handle_time=NOW() WHERE report_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, status);
            ps.setInt(2, adminId);
            ps.setInt(3, reportId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("更新举报状态失败", e);
        } finally {
            DBUtil.close(conn, ps);
        }
        return 0;
    }

    private Report mapReport(ResultSet rs) throws SQLException {
        Report report = new Report();
        report.setReportId(rs.getInt("report_id"));
        report.setReporterId(rs.getInt("reporter_id"));
        report.setTargetType(rs.getInt("target_type"));
        report.setTargetId(rs.getInt("target_id"));
        report.setReason(rs.getString("reason"));
        report.setStatus(rs.getInt("status"));
        report.setAdminId(rs.getInt("admin_id"));
        report.setHandleTime(rs.getTimestamp("handle_time"));
        report.setCreateTime(rs.getTimestamp("create_time"));
        report.setReporterName(rs.getString("reporter_name"));
        return report;
    }
}
