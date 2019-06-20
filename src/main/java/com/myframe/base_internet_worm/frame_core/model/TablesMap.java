package com.myframe.base_internet_worm.frame_core.model;

/**
 * Excel table对应关系Map
 */
public class TablesMap {
    public String linkId;//link id工作空间
    public String fromValKey;//对应的键
    public String toCols;//对应的表字段
    public boolean isParmaryKey;//是否为主键，true表示是主键，false表示不是主键
    public String autoValueModel;//自动创建值模式，有UUID、AUTO
    public String defaultVal;//默认值

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

    public String getToCols() {
        return toCols;
    }

    public void setToCols(String toCols) {
        this.toCols = toCols;
    }

    public boolean isParmaryKey() {
        return isParmaryKey;
    }

    public void setParmaryKey(boolean parmaryKey) {
        isParmaryKey = parmaryKey;
    }

    public String getAutoValueModel() {
        return autoValueModel;
    }

    public void setAutoValueModel(String autoValueModel) {
        this.autoValueModel = autoValueModel;
    }

    public String getDefaultVal() {
        return defaultVal;
    }

    public void setDefaultVal(String defaultVal) {
        this.defaultVal = defaultVal;
    }
}
