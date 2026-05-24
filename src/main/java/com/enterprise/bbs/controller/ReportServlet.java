package com.enterprise.bbs.controller;

import com.enterprise.bbs.model.Report;
import com.enterprise.bbs.model.Result;
import com.enterprise.bbs.model.User;
import com.enterprise.bbs.service.ReportService;
import com.enterprise.bbs.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;

/**
 * 举报控制器（用户端）
 */
public class ReportServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(ReportServlet.class);
    private final ReportService reportService = new ReportService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) path = "";

        try {
            switch (path) {
                case "/submit":
                    doSubmit(req, resp);
                    break;
                default:
                    resp.sendError(404);
            }
        } catch (Exception e) {
            logger.error("ReportServlet POST error", e);
            JsonUtil.writeJson(resp, Result.error("服务器内部错误"));
        }
    }

    private void doSubmit(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User currentUser = (User) req.getSession().getAttribute("currentUser");
        Report report = new Report();
        report.setReporterId(currentUser.getUserId());
        report.setTargetType(Integer.parseInt(req.getParameter("targetType")));
        report.setTargetId(Integer.parseInt(req.getParameter("targetId")));
        report.setReason(req.getParameter("reason"));

        JsonUtil.writeJson(resp, reportService.submitReport(report));
    }
}
