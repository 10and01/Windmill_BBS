package com.enterprise.bbs.controller;

import com.enterprise.bbs.model.Result;
import com.enterprise.bbs.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 图片上传控制器
 */
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,
    maxFileSize = 1024 * 1024 * 5,
    maxRequestSize = 1024 * 1024 * 20
)
public class UploadServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(UploadServlet.class);

    private static final String[] ALLOWED_TYPES = {"image/jpeg", "image/jpg", "image/png", "image/gif"};

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) path = "";

        try {
            if ("/image".equals(path)) {
                doUploadImage(req, resp);
            } else {
                resp.sendError(404);
            }
        } catch (Exception e) {
            logger.error("UploadServlet POST error", e);
            JsonUtil.writeJson(resp, Result.error("上传失败"));
        }
    }

    private void doUploadImage(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        Part filePart = req.getPart("file");
        if (filePart == null || filePart.getSize() == 0) {
            JsonUtil.writeJson(resp, Result.error(400, "请选择图片"));
            return;
        }

        String contentType = filePart.getContentType();
        if (contentType == null || !isAllowedType(contentType)) {
            JsonUtil.writeJson(resp, Result.error(400, "仅支持 jpg/png/gif 格式"));
            return;
        }

        if (filePart.getSize() > 5 * 1024 * 1024) {
            JsonUtil.writeJson(resp, Result.error(400, "图片大小不能超过5MB"));
            return;
        }

        String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
        String ext = "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            ext = fileName.substring(dotIndex).toLowerCase();
        }
        if (ext.isEmpty()) {
            if ("image/jpeg".equals(contentType) || "image/jpg".equals(contentType)) {
                ext = ".jpg";
            } else if ("image/png".equals(contentType)) {
                ext = ".png";
            } else if ("image/gif".equals(contentType)) {
                ext = ".gif";
            }
        }

        String newFileName = UUID.randomUUID().toString() + ext;
        String uploadPath = req.getServletContext().getRealPath("/uploads");
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        File file = new File(uploadDir, newFileName);
        filePart.write(file.getAbsolutePath());

        String url = req.getContextPath() + "/uploads/" + newFileName;
        logger.info("图片上传成功: {}", url);
        JsonUtil.writeJson(resp, Result.success("上传成功", url));
    }

    private boolean isAllowedType(String contentType) {
        for (String type : ALLOWED_TYPES) {
            if (type.equalsIgnoreCase(contentType)) {
                return true;
            }
        }
        return false;
    }
}
