package com.myframe.base_internet_worm.frame_core.model;

/**
 * 数据获取配置
 */
public class LinkValMapsConf {
    public String name;//名称
    public String key;//键
    public String selectorOrField;//获取公式或者json对象字段

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSelectorOrField() {
        return selectorOrField;
    }

    public void setSelectorOrField(String selectorOrField) {
        this.selectorOrField = selectorOrField;
    }
}
