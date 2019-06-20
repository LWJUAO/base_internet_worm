package com.myframe.base_internet_worm.frame_core.model;

/**
 * 请求参数或请求头配置
 */
public class LinkParamOrHeaderConf {
    public String name;//参数名
    public String value;//参数值

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
