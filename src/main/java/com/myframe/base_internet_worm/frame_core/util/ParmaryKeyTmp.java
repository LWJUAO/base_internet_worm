package com.myframe.base_internet_worm.frame_core.util;

/**
 * 主键临时实体
 */
public class ParmaryKeyTmp {
    public String parmaryKey;
    public String linkId;
    public String fromValKey;
    public String defaultVal;//默认值

    public String getParmaryKey() {
        return parmaryKey;
    }

    public void setParmaryKey(String parmaryKey) {
        this.parmaryKey = parmaryKey;
    }

    public String getLinkId() {
        return linkId;
    }

    public void setLinkId(String linkId) {
        this.linkId = linkId;
    }

    public String getFromValKey() {
        return fromValKey;
    }

    public void setFromValKey(String fromValKey) {
        this.fromValKey = fromValKey;
    }

    public String getDefaultVal() {
        return defaultVal;
    }

    public void setDefaultVal(String defaultVal) {
        this.defaultVal = defaultVal;
    }
}
