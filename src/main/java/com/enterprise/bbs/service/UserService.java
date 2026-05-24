package com.enterprise.bbs.service;

import com.enterprise.bbs.dao.PostDAO;
import com.enterprise.bbs.dao.ReplyDAO;
import com.enterprise.bbs.dao.UserDAO;
import com.enterprise.bbs.model.Result;
import com.enterprise.bbs.model.User;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户业务逻辑层
 */
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserDAO userDAO = new UserDAO();
    private final PostDAO postDAO = new PostDAO();
    private final ReplyDAO replyDAO = new ReplyDAO();

    public Result<User> register(User user) {
        if (user == null || isEmpty(user.getUsername()) || isEmpty(user.getPassword()) || isEmpty(user.getEmail())) {
            return Result.error(400, "用户名、密码和邮箱不能为空");
        }
        if (user.getUsername().length() < 3 || user.getUsername().length() > 32) {
            return Result.error(400, "用户名长度为3-32位");
        }
        if (user.getPassword().length() < 6 || user.getPassword().length() > 64) {
            return Result.error(400, "密码长度为6-64位");
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!user.getEmail().matches(emailRegex)) {
            return Result.error(400, "邮箱格式不正确");
        }

        if (userDAO.countByUsername(user.getUsername()) > 0) {
            return Result.error(400, "用户名已存在");
        }

        user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(12)));
        if (isEmpty(user.getNickname())) {
            user.setNickname(user.getUsername());
        }
        user.setRole(1);
        user.setStatus(1);

        int userId = userDAO.insert(user);
        if (userId > 0) {
            user.setUserId(userId);
            user.setPassword(null);
            logger.info("用户注册成功: {}", user.getUsername());
            return Result.success("注册成功", user);
        }
        return Result.error("注册失败，请稍后重试");
    }

    public Result<User> login(String username, String password) {
        if (isEmpty(username) || isEmpty(password)) {
            return Result.error(400, "用户名和密码不能为空");
        }

        User user = userDAO.selectByUsername(username);
        if (user == null) {
            return Result.error(401, "用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            return Result.error(403, "账户已被禁用");
        }
        if (!BCrypt.checkpw(password, user.getPassword())) {
            return Result.error(401, "用户名或密码错误");
        }

        user.setPassword(null);
        logger.info("用户登录成功: {}", username);
        return Result.success("登录成功", user);
    }

    public Result<User> updateUser(User user) {
        if (user == null || user.getUserId() == null) {
            return Result.error(400, "参数错误");
        }
        if (isEmpty(user.getNickname())) {
            return Result.error(400, "昵称不能为空");
        }

        int rows = userDAO.update(user);
        if (rows > 0) {
            User updated = userDAO.selectById(user.getUserId());
            if (updated != null) {
                updated.setPassword(null);
                return Result.success("修改成功", updated);
            }
        }
        return Result.error("修改失败");
    }

    public Result<User> getUserById(int userId) {
        User user = userDAO.selectById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        user.setPassword(null);
        return Result.success(user);
    }

    public Result<Map<String, Object>> getUserProfile(int userId) {
        User user = userDAO.selectById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        int postCount = postDAO.countByAuthorId(userId);
        int replyCount = replyDAO.countByAuthorId(userId);

        Map<String, Object> profile = new HashMap<>();
        profile.put("userId", user.getUserId());
        profile.put("username", user.getUsername());
        profile.put("nickname", user.getNickname());
        profile.put("avatar", user.getAvatar());
        profile.put("role", user.getRole());
        profile.put("registerTime", user.getRegisterTime());
        profile.put("postCount", postCount);
        profile.put("replyCount", replyCount);

        return Result.success(profile);
    }

    public Result<User> updateAvatar(int userId, String avatarUrl) {
        if (isEmpty(avatarUrl)) {
            return Result.error(400, "头像地址不能为空");
        }
        int rows = userDAO.updateAvatar(userId, avatarUrl);
        if (rows > 0) {
            User updated = userDAO.selectById(userId);
            if (updated != null) {
                updated.setPassword(null);
                return Result.success("头像更新成功", updated);
            }
        }
        return Result.error("头像更新失败");
    }

    public boolean checkUsernameExists(String username) {
        return userDAO.countByUsername(username) > 0;
    }

    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
