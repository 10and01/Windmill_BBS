package com.enterprise.bbs.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 自动备份任务
 * 每天凌晨2点自动备份数据库
 * 部署后无需配置操作系统定时任务
 */
@WebListener
public class BackupTask implements ServletContextListener {
    private static final Logger logger = LoggerFactory.getLogger(BackupTask.class);
    private ScheduledExecutorService scheduler;

    // 备份配置（可改成从配置文件读取）
    private static final String DB_NAME = "enterprise_bbs";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "1001";  // 改成你的MySQL密码
    private static final String BACKUP_DIR = "D:/bbs-backup";  // Windows路径
    // private static final String BACKUP_DIR = "/opt/bbs-backup";  // Linux路径
    private static final int KEEP_DAYS = 7;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("启动自动备份任务，每天凌晨2:00执行");
        scheduler = Executors.newSingleThreadScheduledExecutor();

        // 计算距离下次凌晨2点的时间
        long initialDelay = calculateInitialDelay();

        // 每天执行一次
        scheduler.scheduleAtFixedRate(this::doBackup, initialDelay, 24 * 60 * 60, TimeUnit.SECONDS);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("关闭自动备份任务");
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }

    /**
     * 计算距离下次凌晨2点的秒数
     */
    private long calculateInitialDelay() {
        java.util.Calendar now = java.util.Calendar.getInstance();
        java.util.Calendar nextRun = java.util.Calendar.getInstance();
        nextRun.set(java.util.Calendar.HOUR_OF_DAY, 2);
        nextRun.set(java.util.Calendar.MINUTE, 0);
        nextRun.set(java.util.Calendar.SECOND, 0);
        nextRun.set(java.util.Calendar.MILLISECOND, 0);

        if (nextRun.before(now)) {
            nextRun.add(java.util.Calendar.DAY_OF_MONTH, 1);
        }

        return (nextRun.getTimeInMillis() - now.getTimeInMillis()) / 1000;
    }

    /**
     * 执行备份
     */
    private void doBackup() {
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File backupDir = new File(BACKUP_DIR);
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }

            String fileName = DB_NAME + "_" + timestamp + ".sql";
            String filePath = BACKUP_DIR + File.separator + fileName;

            logger.info("开始自动备份数据库: {} -> {}", DB_NAME, filePath);

            // 构建 mysqldump 命令
            ProcessBuilder pb = new ProcessBuilder(
                    "mysqldump",
                    "-u" + DB_USER,
                    "-p" + DB_PASS,
                    "--default-character-set=utf8mb4",
                    DB_NAME
            );
            pb.redirectOutput(new File(filePath));
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);

            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                logger.info("数据库备份成功: {}", fileName);
                // 压缩备份文件
                compressBackup(filePath);
                // 清理旧备份
                cleanOldBackups();
            } else {
                logger.error("数据库备份失败，退出码: {}", exitCode);
            }
        } catch (Exception e) {
            logger.error("自动备份执行异常", e);
        }
    }

    /**
     * 压缩备份文件
     */
    private void compressBackup(String filePath) {
        try {
            ProcessBuilder pb = new ProcessBuilder("gzip", "-f", filePath);
            Process process = pb.start();
            process.waitFor();
            logger.info("备份文件压缩完成: {}.gz", filePath);
        } catch (Exception e) {
            logger.warn("备份文件压缩失败", e);
        }
    }

    /**
     * 清理旧备份
     */
    private void cleanOldBackups() {
        File dir = new File(BACKUP_DIR);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".sql") || name.endsWith(".sql.gz"));
        if (files == null) return;

        long now = System.currentTimeMillis();
        long keepMillis = KEEP_DAYS * 24 * 60 * 60 * 1000L;

        for (File file : files) {
            if (now - file.lastModified() > keepMillis) {
                if (file.delete()) {
                    logger.info("删除旧备份: {}", file.getName());
                }
            }
        }
    }
}
