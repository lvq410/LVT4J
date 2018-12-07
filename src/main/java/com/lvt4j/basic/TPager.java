package com.lvt4j.basic;


/**
 * 将前端的翻页数据:页码及每页大小转为
 * 数据库limit实际需要的start和size
 * @author LV
 */
public final class TPager {
    private static final int Default_PageNo = 1;
    private static final int Default_PageSize = 10;
    
    /** 页码 */
    private int pageNo;
    /** 页大小 */
    private int pageSize;
    /** limit,start */
    private int start;
    /** limit,size */
    private int size;
    
    public TPager() {
        this(Default_PageNo, Default_PageSize);
    }
    
    public TPager(int pageNo, int pageSize) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        init();
    }
    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
        init();
    }
    
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
        init();
    }

    public int nextPage() {
        pageNo++;
        init();
        return pageNo;
    }
    
    public int prevPage() {
        pageNo--;
        init();
        return pageNo;
    }
    
    private void init(){
        if (pageNo<1) pageNo = Default_PageNo;
        if (pageSize<1) pageSize = Default_PageSize;
        start = (pageNo-1)*pageSize;
        size = pageSize;
    }
    
    public int getPageSize() {
        return pageSize;
    }
    
    public int getPageNo() {
        return pageNo;
    }
    
    public int getStart() {
        return start;
    }
    
    public int getSize() {
        return size;
    }
    
    /** 即{@link #start}+{@link size} */
    public int getEnd() {
        return start+size;
    }
    
    /** 转为sql用" limit {@link #start}, {@link size}"格式 */
    public String toLimit() {
        return "limit "+start+","+size;
    }
    
}