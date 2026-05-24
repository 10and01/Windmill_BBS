package com.enterprise.bbs.model;

import java.util.Date;

/**
 * 版块实体类
 */
public class Board {
    private Integer boardId;
    private String boardName;
    private String description;
    private Integer postCount;
    private Integer sortOrder;
    private Integer postPermission;
    private Date createTime;

    public Board() {}

    public Integer getBoardId() {
        return boardId;
    }

    public void setBoardId(Integer boardId) {
        this.boardId = boardId;
    }

    public String getBoardName() {
        return boardName;
    }

    public void setBoardName(String boardName) {
        this.boardName = boardName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPostCount() {
        return postCount;
    }

    public void setPostCount(Integer postCount) {
        this.postCount = postCount;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getPostPermission() {
        return postPermission;
    }

    public void setPostPermission(Integer postPermission) {
        this.postPermission = postPermission;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
