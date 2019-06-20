package com.myframe.base_internet_worm.frame_core.model;

import java.util.List;

/**
 * 访问链接配置
 */
public class LinkConf {
    public String id;//访问链接标识
    public String name;//访问链接名称
    public String type;//链接类型，file:文件，html:网页，json:普通对象JSON，pageJson:分页对象集合
    public String url;//链接路径
    public String fileSavePath;//文件存储路径
    public String dataField;//数据字段
    public String pageNumberField;//当前页码字段
    public List<LinkParamOrHeaderConf> headers;//请求头配置集合
    public List<LinkParamOrHeaderConf> params;//请求参数配置集合
    public List<LinkValMapsConf> valMaps;//数据获取配置集合

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFileSavePath() {
        return fileSavePath;
    }

    public void setFileSavePath(String fileSavePath) {
        this.fileSavePath = fileSavePath;
    }

    public String getDataField() {
        return dataField;
    }

    public void setDataField(String dataField) {
        this.dataField = dataField;
    }

    public String getPageNumberField() {
        return pageNumberField;
    }

    public void setPageNumberField(String pageNumberField) {
        this.pageNumberField = pageNumberField;
    }

    public List<LinkParamOrHeaderConf> getHeaders() {
        return headers;
    }

    public void setHeaders(List<LinkParamOrHeaderConf> headers) {
        this.headers = headers;
    }

    public List<LinkParamOrHeaderConf> getParams() {
        return params;
    }

    public void setParams(List<LinkParamOrHeaderConf> params) {
        this.params = params;
    }

    public List<LinkValMapsConf> getValMaps() {
        return valMaps;
    }

    public void setValMaps(List<LinkValMapsConf> valMaps) {
        this.valMaps = valMaps;
    }
}
