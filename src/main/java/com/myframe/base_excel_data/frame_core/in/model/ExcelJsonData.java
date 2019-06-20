package com.myframe.base_excel_data.frame_core.in.model;

/**
 * 单个Excel与配置json对应关系实体
 */
public class ExcelJsonData {
    public String excelFilePath;//Excel的数据文件
    public String json;//配置json

    public String getExcelFilePath() {
        return excelFilePath;
    }

    public void setExcelFilePath(String excelFilePath) {
        this.excelFilePath = excelFilePath;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }
}
