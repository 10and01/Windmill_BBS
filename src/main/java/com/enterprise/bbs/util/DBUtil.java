package com.enterprise.bbs.util;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

/**
 * 数据库连接工具类（Druid连接池）
 */
public class DBUtil {
    private static final Logger logger = LoggerFactory.getLogger(DBUtil.class);
    private static DruidDataSource dataSource;

    static {
        try {
            Properties props = new Properties();
            InputStream is = DBUtil.class.getClassLoader().getResourceAsStream("db.properties");
            if (is == null) {
                throw new RuntimeException("db.properties not found");
            }
            props.load(is);
            dataSource = (DruidDataSource) DruidDataSourceFactory.createDataSource(props);
            logger.info("Druid连接池初始化成功");
        } catch (Exception e) {
            logger.error("Druid连接池初始化失败", e);
            throw new RuntimeException("数据库连接池初始化失败", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void close(Connection conn, Statement stmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
        } catch (SQLException e) {
            logger.error("关闭ResultSet失败", e);
        }
        try {
            if (stmt != null) stmt.close();
        } catch (SQLException e) {
            logger.error("关闭Statement失败", e);
        }
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            logger.error("关闭Connection失败", e);
        }
    }

    public static void close(Connection conn, Statement stmt) {
        close(conn, stmt, null);
    }

    public static void close(Connection conn) {
        close(conn, null, null);
    }
}
