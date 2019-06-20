package com.myframe.base_excel_data.frame_core.in.model;

import java.util.List;

/**
 * Excel table对应关系配置
 */
public class TablesConf {
    public String tableName;//表名
    public String dbUrl;//数据库路径
    public String dbDriver;//数据库驱动
    public String dbUser;//数据库用户
    public String dbPassword;//数据库密码
    public List<TablesMap> maps;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public String getDbDriver() {
        return dbDriver;
    }

    public void setDbDriver(String dbDriver) {
        this.dbDriver = dbDriver;
    }

    public String getDbUser() {
        return dbUser;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public List<TablesMap> getMaps() {
        return maps;
    }

    public void setMaps(List<TablesMap> maps) {
        this.maps = maps;
    }
}
