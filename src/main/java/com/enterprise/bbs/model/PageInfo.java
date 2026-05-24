package com.enterprise.bbs.model;

import java.util.List;

/**
 * 分页信息包装类
 * @param <T> 数据类型
 */
public class PageInfo<T> {
    private int page;
    private int pageSize;
    private long total;
    private int pages;
    private List<T> list;

    public PageInfo() {}

    public PageInfo(int page, int pageSize, long total, List<T> list) {
        this.page = page;
        this.pageSize = pageSize;
        this.total = total;
        this.list = list;
        calcPages();
    }

    public void calcPages() {
        this.pages = (int) Math.ceil((double) total / pageSize);
        if (this.pages < 1) this.pages = 1;
    }

    public int getOffset() {
        return (page - 1) * pageSize;
    }

    public boolean hasNext() {
        return page < pages;
    }

    public boolean hasPrevious() {
        return page > 1;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}
