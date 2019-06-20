package com.myframe.base_internet_worm.frame_core.model;

import java.util.List;

/**
 * 配置
 */
public class InternetWormConf {
    public GrobalConf grobal;//全局配置
    public List<LinkConf> links;//访问链接配置
    public List<TablesConf> tables;//table配置

    public GrobalConf getGrobal() {
        return grobal;
    }

    public void setGrobal(GrobalConf grobal) {
        this.grobal = grobal;
    }

    public List<LinkConf> getLinks() {
        return links;
    }

    public void setLinks(List<LinkConf> links) {
        this.links = links;
    }

    public List<TablesConf> getTables() {
        return tables;
    }

    public void setTables(List<TablesConf> tables) {
        this.tables = tables;
    }
}
