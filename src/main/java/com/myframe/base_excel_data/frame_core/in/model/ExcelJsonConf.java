package com.myframe.base_excel_data.frame_core.in.model;

import java.util.List;

/**
 * Excel配置
 */
public class ExcelJsonConf {
    public GrobalConf grobal;//全局配置
    public List<SheetsConf> sheets;//sheet配置
    public List<TablesConf> tables;//table配置

    public GrobalConf getGrobal() {
        return grobal;
    }

    public void setGrobal(GrobalConf grobal) {
        this.grobal = grobal;
    }

    public List<SheetsConf> getSheets() {
        return sheets;
    }

    public void setSheets(List<SheetsConf> sheets) {
        this.sheets = sheets;
    }

    public List<TablesConf> getTables() {
        return tables;
    }

    public void setTables(List<TablesConf> tables) {
        this.tables = tables;
    }
}
