package com.nurulquran.audio.object;

/**
 * Created by pruthvi.chauhan on 31-07-2016.
 */
public class OfflineData {
    private String level;

    private String pageno;

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getOfflinedata() {
        return offlinedata;
    }

    public void setOfflinedata(String offlinedata) {
        this.offlinedata = offlinedata;
    }

    private String parentId;
    private String offlinedata=null;

    public String getPageno() {
        return pageno;
    }

    public void setPageno(String pageno) {
        this.pageno = pageno;
    }
}
