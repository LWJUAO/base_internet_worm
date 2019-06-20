package com.myframe.base_internet_worm.frame_core.util;

/**
 * 结果数据
 */
public class NetResData {
    public String id;
    public String type;
    public Object data;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
