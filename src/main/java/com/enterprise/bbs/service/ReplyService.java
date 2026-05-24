package com.enterprise.bbs.service;

import com.enterprise.bbs.dao.PostDAO;
import com.enterprise.bbs.dao.ReplyDAO;
import com.enterprise.bbs.model.PageInfo;
import com.enterprise.bbs.model.Reply;
import com.enterprise.bbs.model.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 回复业务逻辑层
 */
public class ReplyService {
    private static final Logger logger = LoggerFactory.getLogger(ReplyService.class);
    private final ReplyDAO replyDAO = new ReplyDAO();
    private final PostDAO postDAO = new PostDAO();

    public Result<Reply> createReply(Reply reply) {
        if (reply == null || isEmpty(reply.getContent())) {
            return Result.error(400, "回复内容不能为空");
        }
        if (reply.getPostId() == null || reply.getPostId() <= 0) {
            return Result.error(400, "参数错误");
        }
        if (reply.getAuthorId() == null || reply.getAuthorId() <= 0) {
            return Result.error(401, "请先登录");
        }

        // 检查帖子是否存在
        if (postDAO.selectById(reply.getPostId()) == null) {
            return Result.error(404, "帖子不存在");
        }

        int maxFloor = replyDAO.selectMaxFloorByPostId(reply.getPostId());
        reply.setFloorNum(maxFloor + 1);
        reply.setStatus(1);

        int replyId = replyDAO.insert(reply);
        if (replyId > 0) {
            reply.setReplyId(replyId);
            postDAO.updateReplyCount(reply.getPostId(), 1);
            logger.info("发表回复成功: {}, 帖子: {}", replyId, reply.getPostId());
            return Result.success("回复成功", reply);
        }
        return Result.error("回复失败");
    }

    public Result<Boolean> deleteReply(int replyId, int operatorId) {
        Reply reply = replyDAO.selectById(replyId);
        if (reply == null) {
            return Result.error(404, "回复不存在");
        }
        if (reply.getAuthorId() != operatorId) {
            return Result.error(403, "无权删除他人回复");
        }

        int rows = replyDAO.deleteById(replyId);
        if (rows > 0) {
            postDAO.updateReplyCount(reply.getPostId(), -1);
            logger.info("删除回复成功: {}, 操作人: {}", replyId, operatorId);
            return Result.success("删除成功", true);
        }
        return Result.error("删除失败");
    }

    public Result<PageInfo<Reply>> getRepliesByPostId(int postId, int page, int pageSize) {
        if (page < 1) page = 1;
        if (pageSize < 1 || pageSize > 100) pageSize = 20;

        int total = replyDAO.countByPostId(postId);
        int offset = (page - 1) * pageSize;
        List<Reply> list = replyDAO.selectByPostId(postId, offset, pageSize);

        PageInfo<Reply> pageInfo = new PageInfo<>(page, pageSize, total, list);
        return Result.success(pageInfo);
    }

    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
