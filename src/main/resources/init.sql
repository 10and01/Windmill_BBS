SET NAMES utf8mb4;

DROP DATABASE IF EXISTS enterprise_bbs;
CREATE DATABASE enterprise_bbs CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE enterprise_bbs;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS t_like;
DROP TABLE IF EXISTS t_report;
DROP TABLE IF EXISTS t_reply;
DROP TABLE IF EXISTS t_post;
DROP TABLE IF EXISTS t_board;
DROP TABLE IF EXISTS t_user;

SET FOREIGN_KEY_CHECKS = 1;

-- 用户表
CREATE TABLE t_user (
    user_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(32) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(128) NOT NULL COMMENT '密码(BCrypt加密)',
    email VARCHAR(64) NOT NULL UNIQUE COMMENT '邮箱',
    nickname VARCHAR(32) COMMENT '昵称',
    avatar VARCHAR(255) COMMENT '头像URL',
    role TINYINT NOT NULL DEFAULT 1 COMMENT '角色：1=普通用户，2=管理员',
    register_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0=禁用，1=正常',
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 版块表
CREATE TABLE t_board (
    board_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '版块ID',
    board_name VARCHAR(50) NOT NULL UNIQUE COMMENT '版块名称',
    description VARCHAR(255) COMMENT '版块描述',
    post_count INT NOT NULL DEFAULT 0 COMMENT '帖子数量',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序序号',
    post_permission TINYINT NOT NULL DEFAULT 0 COMMENT '发帖权限：0=所有人，1=仅管理员',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='版块表';

-- 帖子表
CREATE TABLE t_post (
    post_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '帖子ID',
    title VARCHAR(200) NOT NULL COMMENT '帖子标题',
    content TEXT NOT NULL COMMENT '帖子内容',
    images TEXT COMMENT '帖子图片URL列表，JSON数组格式',
    author_id INT NOT NULL COMMENT '作者ID',
    board_id INT NOT NULL COMMENT '版块ID',
    reply_count INT NOT NULL DEFAULT 0 COMMENT '回复数量',
    view_count INT NOT NULL DEFAULT 0 COMMENT '浏览次数',
    like_count INT NOT NULL DEFAULT 0 COMMENT '点赞数',
    post_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发表时间',
    is_top TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶：0=否，1=是',
    is_elite TINYINT NOT NULL DEFAULT 0 COMMENT '是否精华：0=否，1=是',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0=删除，1=正常',
    INDEX idx_board_id (board_id, status, is_top, post_time),
    INDEX idx_author_id (author_id),
    INDEX idx_post_time (post_time),
    CONSTRAINT fk_post_author FOREIGN KEY (author_id) REFERENCES t_user(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_post_board FOREIGN KEY (board_id) REFERENCES t_board(board_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子表';

-- 回复表
CREATE TABLE t_reply (
    reply_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '回复ID',
    post_id INT NOT NULL COMMENT '帖子ID',
    author_id INT NOT NULL COMMENT '作者ID',
    content TEXT NOT NULL COMMENT '回复内容',
    images TEXT COMMENT '回复图片URL列表，JSON数组格式',
    reply_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '回复时间',
    floor_num INT NOT NULL COMMENT '楼层号',
    like_count INT NOT NULL DEFAULT 0 COMMENT '点赞数',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0=删除，1=正常',
    INDEX idx_post_id (post_id, status, reply_time),
    INDEX idx_author_id (author_id),
    CONSTRAINT fk_reply_post FOREIGN KEY (post_id) REFERENCES t_post(post_id) ON DELETE CASCADE,
    CONSTRAINT fk_reply_author FOREIGN KEY (author_id) REFERENCES t_user(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='回复表';

-- 点赞记录表
CREATE TABLE t_like (
    like_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL COMMENT '点赞用户ID',
    target_type TINYINT NOT NULL COMMENT '1=帖子, 2=回复',
    target_id INT NOT NULL COMMENT '帖子ID或回复ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_target (user_id, target_type, target_id),
    INDEX idx_target (target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞记录表';

-- 举报记录表
CREATE TABLE t_report (
    report_id INT PRIMARY KEY AUTO_INCREMENT,
    reporter_id INT NOT NULL COMMENT '举报人ID',
    target_type TINYINT NOT NULL COMMENT '1=帖子, 2=回复',
    target_id INT NOT NULL COMMENT '被举报对象ID',
    reason VARCHAR(255) NOT NULL COMMENT '举报原因',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0=待处理, 1=已处理(删除), 2=已处理(忽略)',
    admin_id INT COMMENT '处理管理员ID',
    handle_time DATETIME COMMENT '处理时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_target (target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='举报记录表';

-- 插入版块
INSERT INTO t_board (board_id, board_name, description, sort_order, post_permission) VALUES
(1, '公告通知', '企业公告、重要通知', 1, 1),
(2, '技术分享', '技术文章、经验分享', 2, 0),
(3, '闲聊杂谈', '轻松话题、生活交流', 3, 0),
(4, '问答求助', '技术问题、工作求助', 4, 0);
