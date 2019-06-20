package com.myframe.base_excel_data.frame_core.in.model;

/**
 * Excel table对应关系Map
 */
public class TablesMap {
    public String sheet;//excel工作空间
    public Integer fromCols;//对应的Excel列序号
    public String toCols;//对应的表字段
    public boolean isParmaryKey;//是否为主键，true表示是主键，false表示不是主键
    public String autoValueModel;//自动创建值模式，有UUID、AUTO
    public String defaultVal;//默认值

    public String getSheet() {
        return sheet;
    }

    public void setSheet(String sheet) {
        this.sheet = sheet;
    }

    public Integer getFromCols() {
        return fromCols;
    }

    public void setFromCols(Integer fromCols) {
        this.fromCols = fromCols;
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
