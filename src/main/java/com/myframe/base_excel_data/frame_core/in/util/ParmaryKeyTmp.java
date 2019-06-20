package com.myframe.base_excel_data.frame_core.in.util;

/**
 * 主键临时实体
 */
public class ParmaryKeyTmp {
    public String parmaryKey;
    public String keySheet;
    public Integer keyFromCols;
    public String defaultVal;//默认值

    public String getParmaryKey() {
        return parmaryKey;
    }

    public void setParmaryKey(String parmaryKey) {
        this.parmaryKey = parmaryKey;
    }

    public String getKeySheet() {
        return keySheet;
    }

    public void setKeySheet(String keySheet) {
        this.keySheet = keySheet;
    }

    public Integer getKeyFromCols() {
        return keyFromCols;
    }

    public void setKeyFromCols(Integer keyFromCols) {
        this.keyFromCols = keyFromCols;
    }

    public String getDefaultVal() {
        return defaultVal;
    }

    public void setDefaultVal(String defaultVal) {
        this.defaultVal = defaultVal;
    }
}
