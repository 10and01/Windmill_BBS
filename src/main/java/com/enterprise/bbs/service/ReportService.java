package com.enterprise.bbs.service;

import com.enterprise.bbs.dao.PostDAO;
import com.enterprise.bbs.dao.ReplyDAO;
import com.enterprise.bbs.dao.ReportDAO;
import com.enterprise.bbs.model.PageInfo;
import com.enterprise.bbs.model.Report;
import com.enterprise.bbs.model.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 举报业务逻辑层
 */
public class ReportService {
    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);
    private final ReportDAO reportDAO = new ReportDAO();
    private final PostDAO postDAO = new PostDAO();
    private final ReplyDAO replyDAO = new ReplyDAO();

    public Result<Report> submitReport(Report report) {
        if (report == null || report.getReporterId() == null || report.getTargetId() == null) {
            return Result.error(400, "参数错误");
        }
        if (report.getTargetType() == null || (report.getTargetType() != 1 && report.getTargetType() != 2)) {
            return Result.error(400, "参数错误");
        }
        if (report.getReason() == null || report.getReason().trim().isEmpty()) {
            return Result.error(400, "请填写举报原因");
        }
        if (report.getReason().length() > 255) {
            return Result.error(400, "举报原因不能超过255个字符");
        }

        int reportId = reportDAO.insert(report);
        if (reportId > 0) {
            report.setReportId(reportId);
            logger.info("提交举报成功: reporter={}, targetType={}, targetId={}", report.getReporterId(), report.getTargetType(), report.getTargetId());
            return Result.success("举报成功", report);
        }
        return Result.error("举报失败");
    }

    public Result<PageInfo<Report>> getReportList(int page, int pageSize) {
        if (page < 1) page = 1;
        if (pageSize < 1 || pageSize > 100) pageSize = 20;

        int total = reportDAO.countAll();
        int offset = (page - 1) * pageSize;
        List<Report> list = reportDAO.selectList(offset, pageSize);

        PageInfo<Report> pageInfo = new PageInfo<>(page, pageSize, total, list);
        return Result.success(pageInfo);
    }

    public Result<Boolean> handleReport(int reportId, int action, int adminId) {
        Report report = reportDAO.selectById(reportId);
        if (report == null) {
            return Result.error(404, "举报记录不存在");
        }
        if (report.getStatus() != 0) {
            return Result.error(400, "该举报已处理");
        }

        int status;
        if (action == 1) {
            status = 1;
            if (report.getTargetType() == 1) {
                postDAO.updateStatus(report.getTargetId(), 0);
            } else {
                replyDAO.deleteById(report.getTargetId());
            }
        } else if (action == 2) {
            status = 2;
        } else {
            return Result.error(400, "参数错误");
        }

        int rows = reportDAO.updateStatus(reportId, status, adminId);
        if (rows > 0) {
            logger.info("处理举报成功: reportId={}, action={}, adminId={}", reportId, action, adminId);
            return Result.success("处理成功", true);
        }
        return Result.error("处理失败");
    }
}
