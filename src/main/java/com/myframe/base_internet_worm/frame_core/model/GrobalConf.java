package com.myframe.base_internet_worm.frame_core.model;

/**
 * 全局配置
 */
public class GrobalConf {
    public boolean autoCreateTable;//是否自动创建表格
    public String repetitionType;//重复类型，disposable一次性，loop顺序循环，timer定时循环，interval时间间隔循环
    public Integer repetitions;//重复次数
    public Integer timeInterval;//时间间隔（毫秒）
    public String timerFormula;//定时器表达式

    public boolean getAutoCreateTable() {
        return autoCreateTable;
    }

    public void setAutoCreateTable(boolean autoCreateTable) {
        this.autoCreateTable = autoCreateTable;
    }

    public String getRepetitionType() {
        return repetitionType;
    }

    public void setRepetitionType(String repetitionType) {
        this.repetitionType = repetitionType;
    }

    public Integer getRepetitions() {
        return repetitions;
    }

    public void setRepetitions(Integer repetitions) {
        this.repetitions = repetitions;
    }

    public Integer getTimeInterval() {
        return timeInterval;
    }

    public void setTimeInterval(Integer timeInterval) {
        this.timeInterval = timeInterval;
    }

    public String getTimerFormula() {
        return timerFormula;
    }

    public void setTimerFormula(String timerFormula) {
        this.timerFormula = timerFormula;
    }
}
