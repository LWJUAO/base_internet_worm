package com.myframe.base_excel_data.frame_core.in.model;

import java.io.File;

/**
 * 单个Excel与json配置文件对应关系实体
 */
public class ExcelJsonFileData {
    public String excelFilePath;//Excel的数据文件
    public File jsonFile;//json配置文件

    public String getExcelFilePath() {
        return excelFilePath;
    }

    public void setExcelFilePath(String excelFilePath) {
        this.excelFilePath = excelFilePath;
    }

    public File getJsonFile() {
        return jsonFile;
    }

    public void setJsonFile(File jsonFile) {
        this.jsonFile = jsonFile;
    }
}
