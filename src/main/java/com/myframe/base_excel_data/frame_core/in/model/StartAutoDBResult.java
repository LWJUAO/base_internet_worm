package com.myframe.base_excel_data.frame_core.in.model;

/**
 * 运行读取验证数据并更新数据库方法结果对象
 */
public class StartAutoDBResult {
    private boolean successed;//成功标识
    private String msg;//描述

    public boolean isSuccessed() {
        return successed;
    }

    public void setSuccessed(boolean successed) {
        this.successed = successed;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
