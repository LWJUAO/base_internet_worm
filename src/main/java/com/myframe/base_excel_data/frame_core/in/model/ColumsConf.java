package com.myframe.base_excel_data.frame_core.in.model;

import java.util.List;

/**
 * Excel列配置
 */
public class ColumsConf {
    public Integer colIndex;//列序号
    public String colName;//列名
    public String dataType;//数据类型，string,int,double,date,email
    public String dataFormat;//数据格式，double,date类型有效
    public boolean acceptNulled = true;//是否允许为空，true表示允许，false表示不允许，默认为true
    public List<String> valsScope;//列值范围，string类型有效
    public String regex;//匹配正则表达式，string类型有效
    public List<String> valsDislodge;//列排斥值范围，string类型有效

    public Integer getColIndex() {
        return colIndex;
    }

    public void setColIndex(Integer colIndex) {
        this.colIndex = colIndex;
    }

    public String getColName() {
        return colName;
    }

    public void setColName(String colName) {
        this.colName = colName;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getDataFormat() {
        return dataFormat;
    }

    public void setDataFormat(String dataFormat) {
        this.dataFormat = dataFormat;
    }

    public boolean isAcceptNulled() {
        return acceptNulled;
    }

    public void setAcceptNulled(boolean acceptNulled) {
        this.acceptNulled = acceptNulled;
    }

    public List<String> getValsScope() {
        return valsScope;
    }

    public void setValsScope(List<String> valsScope) {
        this.valsScope = valsScope;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public List<String> getValsDislodge() {
        return valsDislodge;
    }

    public void setValsDislodge(List<String> valsDislodge) {
        this.valsDislodge = valsDislodge;
    }
}
