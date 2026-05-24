package com.enterprise.bbs.service;

import com.enterprise.bbs.dao.LikeDAO;
import com.enterprise.bbs.dao.PostDAO;
import com.enterprise.bbs.dao.ReplyDAO;
import com.enterprise.bbs.model.Like;
import com.enterprise.bbs.model.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 点赞业务逻辑层
 */
public class LikeService {
    private static final Logger logger = LoggerFactory.getLogger(LikeService.class);
    private final LikeDAO likeDAO = new LikeDAO();
    private final PostDAO postDAO = new PostDAO();
    private final ReplyDAO replyDAO = new ReplyDAO();

    public Result<Boolean> toggleLike(int userId, int targetType, int targetId) {
        if (targetType != 1 && targetType != 2) {
            return Result.error(400, "参数错误");
        }
        if (targetId <= 0) {
            return Result.error(400, "参数错误");
        }

        Like existing = likeDAO.selectByUserAndTarget(userId, targetType, targetId);
        if (existing != null) {
            likeDAO.delete(userId, targetType, targetId);
            if (targetType == 1) {
                postDAO.updateLikeCount(targetId, -1);
            } else {
                replyDAO.updateLikeCount(targetId, -1);
            }
            logger.info("取消点赞: user={}, targetType={}, targetId={}", userId, targetType, targetId);
            return Result.success("取消点赞", false);
        } else {
            Like like = new Like();
            like.setUserId(userId);
            like.setTargetType(targetType);
            like.setTargetId(targetId);
            likeDAO.insert(like);
            if (targetType == 1) {
                postDAO.updateLikeCount(targetId, 1);
            } else {
                replyDAO.updateLikeCount(targetId, 1);
            }
            logger.info("点赞成功: user={}, targetType={}, targetId={}", userId, targetType, targetId);
            return Result.success("点赞成功", true);
        }
    }

    public Result<Map<String, Object>> getLikeStatus(int userId, int targetType, int targetId) {
        boolean liked = likeDAO.selectByUserAndTarget(userId, targetType, targetId) != null;
        int count = likeDAO.countByTarget(targetType, targetId);
        Map<String, Object> data = new HashMap<>();
        data.put("liked", liked);
        data.put("count", count);
        return Result.success(data);
    }
}
