package com.enterprise.bbs.service;

import com.enterprise.bbs.dao.BoardDAO;
import com.enterprise.bbs.dao.PostDAO;
import com.enterprise.bbs.model.PageInfo;
import com.enterprise.bbs.model.Post;
import com.enterprise.bbs.model.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 帖子业务逻辑层
 */
public class PostService {
    private static final Logger logger = LoggerFactory.getLogger(PostService.class);
    private final PostDAO postDAO = new PostDAO();
    private final BoardDAO boardDAO = new BoardDAO();

    public Result<Post> createPost(Post post) {
        if (post == null || isEmpty(post.getTitle()) || isEmpty(post.getContent())) {
            return Result.error(400, "标题和内容不能为空");
        }
        if (post.getTitle().length() > 200) {
            return Result.error(400, "标题最多200个字符");
        }
        if (post.getBoardId() == null || post.getBoardId() <= 0) {
            return Result.error(400, "请选择版块");
        }
        if (post.getAuthorId() == null || post.getAuthorId() <= 0) {
            return Result.error(401, "请先登录");
        }

        int postId = postDAO.insert(post);
        if (postId > 0) {
            post.setPostId(postId);
            boardDAO.updatePostCount(post.getBoardId(), 1);
            logger.info("发表帖子成功: {}, 作者: {}", postId, post.getAuthorId());
            return Result.success("发表成功", post);
        }
        return Result.error("发表失败");
    }

    public Result<Post> updatePost(Post post, int operatorId) {
        if (post == null || post.getPostId() == null) {
            return Result.error(400, "参数错误");
        }
        if (isEmpty(post.getTitle()) || isEmpty(post.getContent())) {
            return Result.error(400, "标题和内容不能为空");
        }
        if (post.getTitle().length() > 200) {
            return Result.error(400, "标题最多200个字符");
        }
        if (post.getBoardId() == null || post.getBoardId() <= 0) {
            return Result.error(400, "请选择版块");
        }

        Post existing = postDAO.selectById(post.getPostId());
        if (existing == null) {
            return Result.error(404, "帖子不存在");
        }
        if (existing.getAuthorId() != operatorId) {
            return Result.error(403, "无权编辑他人帖子");
        }

        int oldBoardId = existing.getBoardId();
        int rows = postDAO.update(post);
        if (rows > 0) {
            if (oldBoardId != post.getBoardId()) {
                boardDAO.updatePostCount(oldBoardId, -1);
                boardDAO.updatePostCount(post.getBoardId(), 1);
            }
            logger.info("更新帖子成功: {}, 操作人: {}", post.getPostId(), operatorId);
            return Result.success("更新成功", postDAO.selectById(post.getPostId()));
        }
        return Result.error("更新失败");
    }

    public Result<Boolean> deletePost(int postId, int operatorId) {
        Post post = postDAO.selectById(postId);
        if (post == null) {
            return Result.error(404, "帖子不存在");
        }
        if (post.getAuthorId() != operatorId) {
            return Result.error(403, "无权删除他人帖子");
        }

        int rows = postDAO.deleteById(postId);
        if (rows > 0) {
            boardDAO.updatePostCount(post.getBoardId(), -1);
            logger.info("删除帖子成功: {}, 操作人: {}", postId, operatorId);
            return Result.success("删除成功", true);
        }
        return Result.error("删除失败");
    }

    public Result<Post> getPostById(int postId) {
        Post post = postDAO.selectById(postId);
        if (post == null) {
            return Result.error(404, "帖子不存在");
        }
        postDAO.updateViewCount(postId);
        post.setViewCount(post.getViewCount() + 1);
        return Result.success(post);
    }

    public Result<PageInfo<Post>> getPostList(Integer boardId, int page, int pageSize) {
        if (page < 1) page = 1;
        if (pageSize < 1 || pageSize > 100) pageSize = 20;

        int bid = boardId != null ? boardId : 0;
        int total = postDAO.countByBoardId(bid);
        int offset = (page - 1) * pageSize;
        List<Post> list = postDAO.selectByBoardId(bid, offset, pageSize);

        PageInfo<Post> pageInfo = new PageInfo<>(page, pageSize, total, list);
        return Result.success(pageInfo);
    }

    public Result<PageInfo<Post>> searchPosts(String keyword, int page, int pageSize) {
        if (isEmpty(keyword)) {
            return Result.error(400, "搜索关键词不能为空");
        }
        if (page < 1) page = 1;
        if (pageSize < 1 || pageSize > 100) pageSize = 20;

        int total = postDAO.countByKeyword(keyword);
        int offset = (page - 1) * pageSize;
        List<Post> list = postDAO.searchByKeyword(keyword, offset, pageSize);

        PageInfo<Post> pageInfo = new PageInfo<>(page, pageSize, total, list);
        return Result.success(pageInfo);
    }

    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
