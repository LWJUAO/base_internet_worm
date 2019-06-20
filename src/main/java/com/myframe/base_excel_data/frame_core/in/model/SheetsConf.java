package com.myframe.base_excel_data.frame_core.in.model;

import java.util.List;

/**
 * Excel sheets配置
 */
public class SheetsConf {
    public String sheetName;//工作空间名
    public String readOrientation;//读数据方向,水平:horizontal,垂直:vertical
    public Integer startRows;//开始行
    public List<ColumsConf> colums;

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public String getReadOrientation() {
        return readOrientation;
    }

    public void setReadOrientation(String readOrientation) {
        this.readOrientation = readOrientation;
    }

    public Integer getStartRows() {
        return startRows;
    }

    public void setStartRows(Integer startRows) {
        this.startRows = startRows;
    }

    public List<ColumsConf> getColums() {
        return colums;
    }

    public void setColums(List<ColumsConf> colums) {
        this.colums = colums;
    }
}
