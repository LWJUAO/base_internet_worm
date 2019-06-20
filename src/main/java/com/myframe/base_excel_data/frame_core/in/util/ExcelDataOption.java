package com.myframe.base_excel_data.frame_core.in.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.myframe.base_excel_data.frame_core.in.model.*;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Excel工具类
 */
public class ExcelDataOption {
    private Integer type;//实例化类型
    private String excelFilePath;//Excel的数据文件
    private String json;//配置json
    private File jsonFile;//json配置文件
    private ExcelJsonData[] excelJsonDatas;//Excel与配置json对应关系集
    private ExcelJsonFileData[] excelJsonFileDatas;//Excel与json配置文件对应关系集
    //数据连接集合
    private Map<String,Connection> conns = new HashMap<String,Connection>();
    //数据表信息集合
    private Map<String,List<String>> tableInfos = new HashMap<String,List<String>>();

    //数据
    private Map<String,Object> datas = new HashMap<String,Object>();

    public Map<String, Object> getDatas() {
        return datas;
    }

    public void setDatas(Map<String, Object> datas) {
        this.datas = datas;
    }

    /**
     * 构造函数
     * @param excelFilePath Excel的数据文件
     * @param json 配置json
     */
    public ExcelDataOption(String excelFilePath,String json){
        //先判断必要数据不为空
        Assert.notNull(excelFilePath,"excelFilePath must is not null");
        Assert.notNull(json,"json must is not null");
        this.excelFilePath = excelFilePath;
        this.json = json;
        this.type = 0;
    }

    /**
     * 构造函数
     * @param excelFilePath Excel的数据文件
     * @param jsonFile json配置文件
     */
    public ExcelDataOption(String excelFilePath,File jsonFile){
        //先判断必要数据不为空
        Assert.notNull(excelFilePath,"excelFilePath must is not null");
        Assert.notNull(jsonFile,"jsonFile must is not null");
        this.excelFilePath = excelFilePath;
        this.jsonFile = jsonFile;
        this.type = 1;
    }

    /**
     * 构造函数
     * @param excelJsonDatas Excel与配置json对应关系集
     */
    public ExcelDataOption(ExcelJsonData[] excelJsonDatas){
        //先遍历判断必要数据不为空
        Assert.notNull(excelJsonDatas,"excelJsonDatas must is not null");
        if(excelJsonDatas != null && excelJsonDatas.length > 0){
            for(int i = 0 ; i < excelJsonDatas.length ; i ++){
                Assert.notNull(excelJsonDatas[i].getExcelFilePath(),"excelFilePath must is not null");
                Assert.notNull(excelJsonDatas[i].getJson(),"json must is not null");
            }
        }
        this.excelJsonDatas = excelJsonDatas;
        this.type = 2;
    }

    /**
     * 构造函数
     * @param excelJsonFileDatas Excel与json配置文件对应关系集
     */
    public ExcelDataOption(ExcelJsonFileData[] excelJsonFileDatas){
        //先遍历判断必要数据不为空
        Assert.notNull(excelJsonFileDatas,"excelJsonDatas must is not null");
        if(excelJsonFileDatas != null && excelJsonFileDatas.length > 0){
            for(int i = 0 ; i < excelJsonFileDatas.length ; i ++){
                Assert.notNull(excelJsonFileDatas[i].getExcelFilePath(),"excelFilePath must is not null");
                Assert.notNull(excelJsonFileDatas[i].getJsonFile(),"jsonFile must is not null");
            }
        }
        this.excelJsonFileDatas = excelJsonFileDatas;
        this.type = 3;
    }

    /**
     * 读取配置文件中的配置信息
     * @param jsonFile
     * @return
     */
    public String getJsonFromFile(File jsonFile){
        String res = "";
        //判断文件存在再执行
        if(jsonFile != null && jsonFile.exists()){
            StringBuffer sbBuffer = new StringBuffer();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(jsonFile)))) {
                for (String line = br.readLine(); line != null; line = br.readLine()) {
                    sbBuffer.append(line);
                }
                res = sbBuffer.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return res;
    }

    /**
     * 将json配置字符串转配置对象
     * @param json
     * @return
     */
    public ExcelJsonConf jsonToConfObj(String json){
        ExcelJsonConf jc = JSON.parseObject(json, new TypeReference<ExcelJsonConf>() {});
        return jc;
    }

    /**
     * 配置默认值初始化
     * @param ejc
     */
    private void initDefaultValue(ExcelJsonConf ejc){
        if(ejc == null){return;}
        if(ejc.getGrobal() == null){
            GrobalConf grobal = new GrobalConf();
            grobal.setAutoCreateTable(false);
            ejc.setGrobal(grobal);
        }
        //判断不为空执行
        if(ejc.getSheets() != null && ejc.getSheets().size() > 0){
            //遍历
            for(int i = 0 ; i < ejc.getSheets().size() ; i ++){
                //判断为空执行
                if(StringUtils.isEmpty(ejc.getSheets().get(i).getSheetName())){
                    ejc.getSheets().get(i).setSheetName("sheet"+i);
                }
                //判断为空执行
                if(StringUtils.isEmpty(ejc.getSheets().get(i).getReadOrientation())){
                    //默认数据读取方向
                    ejc.getSheets().get(i).setReadOrientation("horizontal");
                }
            }
        }
    }

    /**
     * 运行读取验证数据但不操作数据直接回调方法
     * @return
     */
    public void startButNoDB(ExcelDataCallBackIF callBackIF){
        try {
            switch (type) {
                case 0:
                    //获取配置对象
                    ExcelJsonConf ejc0 = jsonToConfObj(json);
                    //配置默认值初始化
                    initDefaultValue(ejc0);
                    //读取数据
                    Map<String,Object> data0 = ExcelDataUtil.readExcel(excelFilePath, ejc0);
                    //执行回调
                    runOnceCallBack(callBackIF,data0,ejc0);
                    break;
                case 1:
                    //获取配置字符串
                    String jsonStr1 = getJsonFromFile(jsonFile);
                    //判断不为空执行
                    if(!StringUtils.isEmpty(jsonStr1)){
                        //获取配置对象
                        ExcelJsonConf ejc1 = jsonToConfObj(jsonStr1);
                        //配置默认值初始化
                        initDefaultValue(ejc1);
                        //读取数据
                        Map<String,Object> data1 = ExcelDataUtil.readExcel(excelFilePath, ejc1);
                        //执行回调
                        runOnceCallBack(callBackIF,data1,ejc1);
                    }
                    break;
                case 2:
                    //判断不为空执行
                    if(excelJsonDatas != null && excelJsonDatas.length > 0){
                        //遍历
                        for(int i = 0 ; i < excelJsonDatas.length ; i ++){
                            //获取配置对象
                            ExcelJsonConf ejc2 = jsonToConfObj(excelJsonDatas[i].getJson());
                            //配置默认值初始化
                            initDefaultValue(ejc2);
                            //读取数据
                            Map<String,Object> data2 = ExcelDataUtil.readExcel(excelJsonDatas[i].getExcelFilePath(), ejc2);
                            //执行回调
                            runOnceCallBack(callBackIF,data2,ejc2);
                        }
                    }
                    break;
                case 3:
                    //判断不为空执行
                    if(excelJsonFileDatas != null && excelJsonFileDatas.length > 0){
                        //遍历
                        for(int i = 0 ; i < excelJsonFileDatas.length ; i ++){
                            //获取配置字符串
                            String jsonStr3 = getJsonFromFile(excelJsonFileDatas[i].getJsonFile());
                            //判断不为空执行
                            if(!StringUtils.isEmpty(jsonStr3)){
                                //获取配置对象
                                ExcelJsonConf ejc3 = jsonToConfObj(jsonStr3);
                                //配置默认值初始化
                                initDefaultValue(ejc3);
                                //读取数据
                                Map<String,Object> data3 = ExcelDataUtil.readExcel(excelJsonFileDatas[i].getExcelFilePath(), ejc3);
                                //执行回调
                                runOnceCallBack(callBackIF,data3,ejc3);
                            }
                        }
                    }
                    break;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 执行回调
     * @param callBackIF
     * @param data
     * @param ejc
     * @throws Exception
     */
    private void runOnceCallBack(ExcelDataCallBackIF callBackIF,Map<String,Object> data,ExcelJsonConf ejc) throws Exception {
        //获取返回结果，true表示继续执行导入数据库，false表示结束
        boolean callBacked = false;
        //判断为空执行
        if(data == null){
            //回调方法
            callBacked = callBackIF.callBack(-1,data);
        }else{
            //回调方法
            callBacked = callBackIF.callBack(type,data);
        }
        //判断是否继续执行
        if(callBacked && data != null){
            //数据库处理前操作
            StartAutoDBResult bdb = beforeUpdateDB(ejc);
            //判断数据库处理前操作是否成功
            if(bdb.isSuccessed()){
                //数据验证通过执行
                if(data.containsKey("verify") && (boolean)data.get("verify")){
                    //数据库处理
                    StartAutoDBResult upDbRes0 = updateDB(ejc,data);
                }
            }
        }
    }

    /**
     * 运行读取验证数据并更新数据库方法
     * @return
     */
    public StartAutoDBResult startAndAutoDB(){
        //成功运行标识,默认是运行成功的
        boolean successed = true;
        //描述
        StringBuffer msg = new StringBuffer();
        try {
            switch (type) {
                case 0:
                    //获取配置对象
                    ExcelJsonConf ejc0 = jsonToConfObj(json);
                    //配置默认值初始化
                    initDefaultValue(ejc0);
                    //数据库处理操作
                    StartAutoDBResult upDbRes0 = putData(excelFilePath,ejc0);
                    //判断成功执行
                    if(!upDbRes0.isSuccessed()){
                        successed = false;
                        msg.append(upDbRes0.getMsg());
                    }
                    break;
                case 1:
                    //获取配置字符串
                    String jsonStr1 = getJsonFromFile(jsonFile);
                    //判断不为空执行
                    if(!StringUtils.isEmpty(jsonStr1)){
                        //获取配置对象
                        ExcelJsonConf ejc1 = jsonToConfObj(jsonStr1);
                        //配置默认值初始化
                        initDefaultValue(ejc1);
                        //数据库处理操作
                        StartAutoDBResult upDbRes1 = putData(excelFilePath,ejc1);
                        //判断成功执行
                        if(!upDbRes1.isSuccessed()){
                            successed = false;
                            msg.append(upDbRes1.getMsg());
                        }
                    }else{
                        successed = false;
                        msg.append("Excel json配置字符串不能为空");
                    }
                    break;
                case 2:
                    //判断不为空执行
                    if(excelJsonDatas != null && excelJsonDatas.length > 0){
                        //遍历
                        for(int i = 0 ; i < excelJsonDatas.length ; i ++){
                            //获取配置对象
                            ExcelJsonConf ejc2 = jsonToConfObj(excelJsonDatas[i].getJson());
                            //配置默认值初始化
                            initDefaultValue(ejc2);
                            //数据库处理操作
                            StartAutoDBResult upDbRes2 = putData(excelJsonDatas[i].getExcelFilePath(),ejc2);
                            //判断成功执行
                            if(!upDbRes2.isSuccessed()){
                                successed = false;
                                msg.append(upDbRes2.getMsg());
                            }
                        }
                    }
                    break;
                case 3:
                    //判断不为空执行
                    if(excelJsonFileDatas != null && excelJsonFileDatas.length > 0){
                        //遍历
                        for(int i = 0 ; i < excelJsonFileDatas.length ; i ++){
                            //获取配置字符串
                            String jsonStr3 = getJsonFromFile(excelJsonFileDatas[i].getJsonFile());
                            //判断不为空执行
                            if(!StringUtils.isEmpty(jsonStr3)){
                                //获取配置对象
                                ExcelJsonConf ejc3 = jsonToConfObj(jsonStr3);
                                //配置默认值初始化
                                initDefaultValue(ejc3);
                                //数据库处理操作
                                StartAutoDBResult upDbRes3 = putData(excelJsonFileDatas[i].getExcelFilePath(),ejc3);
                                //判断成功执行
                                if(!upDbRes3.isSuccessed()){
                                    successed = false;
                                    msg.append(upDbRes3.getMsg());
                                }
                            }else{
                                successed = false;
                                msg.append("Excel json配置字符串不能为空");
                            }
                        }
                    }
                    break;
            }
            //判断不为空执行
            if(conns != null && !conns.isEmpty()){
                //遍历
                for (String key : conns.keySet()) {
                    //关闭连接
                    if(conns.get(key) != null){
                        conns.get(key).close();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        //结果对象
        StartAutoDBResult res = new StartAutoDBResult();
        res.setSuccessed(successed);
        res.setMsg(msg.toString());
        return res;
    }

    /**
     * 获取数据库连接
     * @param dbUrl
     * @param dbDriver
     * @param dbUser
     * @param dbPassword
     * @return
     */
    private Connection getConn(String dbUrl, String dbDriver, String dbUser, String dbPassword) {
        Connection conn = null;
        try {
            //判断不为空执行
            if(!StringUtils.isEmpty(dbUrl) && !StringUtils.isEmpty(dbDriver) &&
                    !StringUtils.isEmpty(dbUser) && !StringUtils.isEmpty(dbPassword)){
                Class.forName(dbDriver); //classLoader,加载对应驱动
                conn = (Connection) DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    /**
     * 初始化数据库连接
     * @param tables
     * @return
     */
    private StartAutoDBResult initConns(List<TablesConf> tables){
        //成功运行标识
        boolean successed = true;
        //描述
        StringBuffer msg = new StringBuffer();
        //判断不为空执行
        if(tables != null && tables.size() > 0){
            //遍历
            for(int i = 0 ; i < tables.size() ; i ++){
                //判断不为空执行
                if(!StringUtils.isEmpty(tables.get(i).getDbUrl()) && !StringUtils.isEmpty(tables.get(i).getDbDriver())
                        && !StringUtils.isEmpty(tables.get(i).getDbUser()) && !StringUtils.isEmpty(tables.get(i).getTableName())){
                    //获取数据库路径
                    String dbUrl = tables.get(i).getDbUrl();
                    if(!conns.containsKey(dbUrl)){
                        //创建数据库连接
                        Connection conn = getConn(tables.get(i).getDbUrl(),tables.get(i).getDbDriver(),tables.get(i).getDbUser(),tables.get(i).getDbPassword());
                        if(conn != null){
                            conns.put(dbUrl,conn);
                        }else{
                            successed = false;
                            msg.append("数据库:"+dbUrl+" 连接不上");
                        }
                    }
                }else{
                    successed = false;
                    msg.append("数据库必要配置信息不能为空");
                }
            }
        }else{
            successed = false;
            msg.append("table对应关系配置为空，找不到数据库配置信息");
        }
        //结果对象
        StartAutoDBResult res = new StartAutoDBResult();
        res.setSuccessed(successed);
        res.setMsg(msg.toString());
        return res;
    }

    /**
     * 数据库处理前操作
     * @param ejc
     * @return
     */
    private StartAutoDBResult beforeUpdateDB(ExcelJsonConf ejc) throws Exception {
        //成功运行标识
        boolean successed = false;
        //描述
        StringBuffer msg = new StringBuffer();
        if(ejc != null){
            //初始化数据库连接
            StartAutoDBResult intiRes0 = initConns(ejc.getTables());
            //判断成功执行
            if(intiRes0.isSuccessed()){
                successed = true;

                //判断全局配置是否设置了自动创建表格
                if(ejc.getGrobal() != null && ejc.getGrobal().getAutoCreateTable()){
                    //数据表配置信息
                    List<TablesConf> tbs = ejc.getTables();
                    //判断不为空执行
                    if(tbs != null && tbs.size() > 0){
                        //遍历
                        for(int i = 0 ; i < tbs.size() ; i ++){
                            TablesConf tb = tbs.get(i);
                            //判断存在则执行
                            if(conns.containsKey(tb.getDbUrl())){
                                //获取所有表
                                List<String> tableNames = null;
                                if(tableInfos.containsKey(tb.getDbUrl())){
                                    tableNames = tableInfos.get(tb.getDbUrl());
                                }else{
                                    tableNames = listAllTables(conns.get(tb.getDbUrl()));
                                    tableInfos.put(tb.getDbUrl(),tableNames);
                                }
                                //判断不存在创建
                                if(!(tableNames != null && listContains(tableNames,tb.getTableName()))){
                                    //获取映射关系
                                    List<TablesMap> maps = tb.getMaps();
                                    //判断不为空执行
                                    if(maps != null && maps.size() > 0){
                                        //主键名称组
                                        List<String> keyCols = new ArrayList<String>();
                                        //主键数
                                        Integer keyColNum = 0;
                                        //遍历
                                        for(int x = 0 ; x < maps.size() ; x ++){
                                            //判断是否是主键
                                            if(maps.get(x).isParmaryKey()){
                                                keyColNum ++;
                                                keyCols.add(maps.get(x).getToCols());
                                            }
                                        }
                                        //字段sql语句
                                        StringBuffer params = new StringBuffer();
                                        //遍历
                                        for(int x = 0 ; x < maps.size() ; x ++){
                                            if(params.length() > 0){
                                                params.append(",");
                                            }
                                            params.append(maps.get(x).getToCols());
                                            //判断是否是主键
                                            if(maps.get(x).isParmaryKey() && keyColNum == 1){
                                                params.append(" varchar(200)");
                                                params.append(" primary key");
                                            }else{
                                                params.append(" varchar(200)");
                                            }
                                        }
                                        //SQL语句
                                        StringBuffer sql = new StringBuffer("create table " + tb.getTableName() + "(" + params.toString() + ")");
                                        Statement newSmt = conns.get(tb.getDbUrl()).createStatement();
                                        newSmt.executeUpdate(sql.toString());//DDL语句返回值为0;

                                        //判断是多主键执行
                                        if(keyCols != null && keyCols.size() > 1){
                                            StringBuffer ks = new StringBuffer();
                                            //遍历
                                            for(int x = 0 ; x < keyCols.size() ; x ++){
                                                if(ks.length() > 0){
                                                    ks.append(",");
                                                }
                                                ks.append(keyCols.get(x));
                                            }
                                            Statement mkSmt = conns.get(tb.getDbUrl()).createStatement();
                                            StringBuffer mksql = new StringBuffer("ALTER TABLE "+tb.getTableName()+" ADD CONSTRAINT pk_"+tb.getTableName()+" PRIMARY KEY("+ks.toString()+")");
                                            mkSmt.executeUpdate(mksql.toString());//DDL语句返回值为0;
                                        }

                                        //判断不包含并添加到表数组中
                                        if(!tableNames.contains(tb.getTableName())){
                                            tableNames.add(tb.getTableName());
                                        }
                                    }
                                }else{
                                    //获取映射关系
                                    List<TablesMap> maps = tb.getMaps();
                                    //获取所有字段
                                    List<String> fields = listAllFields(conns.get(tb.getDbUrl()),tb.getTableName());
                                    //判断不为空执行
                                    if(fields != null && fields.size() > 0 && maps != null && maps.size() > 0){
                                        //遍历
                                        for(int x = 0 ; x < maps.size() ; x ++){
                                            //判断字段是否存在
                                            if(!listContains(fields,maps.get(x).getToCols())){
                                                //SQL语句
                                                StringBuffer sql = new StringBuffer("ALTER TABLE "+tb.getTableName()+" ADD "+maps.get(x).getToCols()+" varchar(512)");
                                                Statement newSmt = conns.get(tb.getDbUrl()).createStatement();
                                                newSmt.executeUpdate(sql.toString());//DDL语句返回值为0;

                                                //添加到集合中
                                                fields.add(maps.get(x).getToCols());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }else{
                successed = false;
                msg.append(intiRes0.getMsg());
            }
        }else{
            successed = false;
            msg.append("Excel配置不能为空");
        }
        //结果对象
        StartAutoDBResult res = new StartAutoDBResult();
        res.setSuccessed(successed);
        res.setMsg(msg.toString());
        return res;
    }
    /**
     * 数据库处理前操作
     * @param excelFilePath
     * @param ejc
     * @return
     */
    private StartAutoDBResult putData(String excelFilePath,ExcelJsonConf ejc) throws Exception {
        //成功运行标识
        boolean successed = false;
        //描述
        StringBuffer msg = new StringBuffer();
        //数据库处理前操作
        StartAutoDBResult bdb = beforeUpdateDB(ejc);
        //判断数据库处理前操作是否成功
        if(bdb.isSuccessed()){
            //读取数据
            Map<String,Object> data0 = ExcelDataUtil.readExcel(excelFilePath, ejc);
            //判断数据不为空执行
            if(data0 != null){
                //数据验证通过执行
                if(data0.containsKey("verify") && (boolean)data0.get("verify")){
                    //数据库处理
                    StartAutoDBResult upDbRes0 = updateDB(ejc,data0);
                    //判断成功执行
                    if(!upDbRes0.isSuccessed()){
                        successed = false;
                        msg.append(upDbRes0.getMsg());
                    }
                }else{
                    successed = false;
                    msg.append("Excel数据格式验证不通过");
                }
            }else{
                successed = false;
                msg.append("Excel数据为空");
            }
        }else{
            successed = bdb.isSuccessed();
            msg.append(bdb.getMsg());
        }
        //结果对象
        StartAutoDBResult res = new StartAutoDBResult();
        res.setSuccessed(successed);
        res.setMsg(msg.toString());
        return res;
    }
    /**
     * 数据库处理
     * @param ejc
     * @param data
     * @return
     */
    private StartAutoDBResult updateDB(ExcelJsonConf ejc,Map<String,Object> data){
        //成功运行标识
        boolean successed = false;
        //描述
        StringBuffer msg = new StringBuffer();
        //判断不为空执行
        if(ejc != null && data != null){
            //数据集
            Map<String,Object> datas = (Map<String, Object>) data.get("data");
            //判断不为空执行
            if(datas != null && datas.size() > 0){
                //获取数据映射配置
                List<TablesConf> tables = ejc.getTables();
                //判断不为空执行
                if(tables != null && tables.size() > 0){
                    //遍历处理
                    A:for(int i = 0 ; i < tables.size() ; i ++){
                        TablesConf tc = tables.get(i);
                        //判断不为空执行
                        if(tc != null){
                            //判断不为空执行
                            if(tc.getMaps() != null && tc.getMaps().size() > 0) {
                                //主键
                                List<ParmaryKeyTmp> kts = new ArrayList<ParmaryKeyTmp>();
                                //遍历maps
                                B:for (int x = 0 ; x < tc.getMaps().size() ; x ++) {
                                    //判断不为空执行
                                    if(!StringUtils.isEmpty(tc.getMaps().get(x).getToCols()) && !StringUtils.isEmpty(tc.getMaps().get(x).getSheet())){
                                        if(tc.getMaps().get(x).isParmaryKey()){
                                            ParmaryKeyTmp pkt = new ParmaryKeyTmp();
                                            pkt.setParmaryKey(tc.getMaps().get(x).getToCols());
                                            pkt.setKeySheet(tc.getMaps().get(x).getSheet());
                                            pkt.setKeyFromCols(tc.getMaps().get(x).getFromCols());
                                            pkt.setDefaultVal(preDefaultVal(tc.getMaps().get(x).getDefaultVal()));
                                            kts.add(pkt);
                                            //break B;
                                        }
                                    }else{
                                        tc.getMaps().remove(x);
                                    }
                                }
                                //数据总条数
                                Integer totalSize = 0;
                                //判断不为空执行
                                if(kts != null && kts.size() > 0){
                                    //遍历
                                    for(ParmaryKeyTmp kt : kts){
                                        //获取数据数量
                                        Integer size = getTotalSize(datas,kt.getKeySheet(),ejc);
                                        //判断是否大于totalSize
                                        if(size > totalSize){
                                            totalSize = size;
                                        }
                                    }
                                }else{
                                    //遍历
                                    for(TablesMap tm : tc.getMaps()){
                                        //获取数据数量
                                        Integer size = getTotalSize(datas,tm.getSheet(),ejc);
                                        //判断是否大于totalSize
                                        if(size > totalSize){
                                            totalSize = size;
                                        }
                                    }
                                }
                                //遍历处理
                                for(int p = 0 ; p < totalSize ; p ++){
                                    //存在条数
                                    int hasCount = 0;
                                    //判断不为空执行
                                    if(kts != null && kts.size() > 0){
                                        try {
                                            //创建查询语句
                                            StringBuffer sql = new StringBuffer("select count(0) from "+tc.getTableName()+" where 1=1");
                                            //参数
                                            List params = new ArrayList();
                                            //遍历
                                            for(int pi = 0 ; pi < kts.size() ; pi ++){
                                                sql.append(" and "+kts.get(pi).getParmaryKey()+"=?");
                                                //获取参数值
                                                Object obj = getDataObj(ejc,datas,kts.get(pi).getKeySheet(),p,kts.get(pi).getKeyFromCols());
                                                //判断为空执行
                                                if(StringUtils.isEmpty(obj)){
                                                    //判断是否设置默认值
                                                    if(!StringUtils.isEmpty(kts.get(pi).getDefaultVal())){
                                                        //默认值
                                                        obj = preDefaultVal(kts.get(pi).getDefaultVal());
                                                    }
                                                }
                                                params.add(obj);
                                            }
                                            //判断不为空执行
                                            if(params != null && params.size() > 0){
                                                PreparedStatement ps = (conns.get(tc.getDbUrl())).prepareStatement(sql.toString());
                                                //遍历
                                                for(int x = 0 ; x < params.size() ; x ++){
                                                    //设置参数
                                                    ps.setObject((x+1), params.get(x));
                                                }
                                                ResultSet rs = ps.executeQuery();
                                                if (rs.next()) {
                                                    hasCount = rs.getInt(1);
                                                }
                                                ps.close();
                                            }
                                        }catch (Exception e){}
                                    }
                                    try {
                                        //获取映射关系
                                        List<TablesMap> maps = tc.getMaps();
                                        //创建更新语句
                                        StringBuffer sql = new StringBuffer();
                                        //参数集合
                                        List<Object> params = new ArrayList<Object>();
                                        //判断存在则执行
                                        if(hasCount > 0){
                                            //创建更新语句
                                            sql.append("update "+tc.getTableName()+" set ");
                                            //遍历
                                            for(int x = 0 ; x < maps.size() ; x ++){
                                                //判断不存在执行
                                                if(!(sql.indexOf(maps.get(x).getToCols()) > -1)){
                                                    if(x >= 1){
                                                        sql.append(",");
                                                    }
                                                    sql.append(maps.get(x).getToCols()+"=?");
                                                    //获取参数值
                                                    Object obj = getDataObj(ejc,datas,maps.get(x).getSheet(),p,maps.get(x).getFromCols());
                                                    //判断为空并autoValueModel不为空处理
                                                    if(StringUtils.isEmpty(obj)){
                                                        //判断是否设置默认值
                                                        if(!StringUtils.isEmpty(maps.get(x).getDefaultVal())){
                                                            //默认值
                                                            obj = preDefaultVal(maps.get(x).getDefaultVal());
                                                        }else if(!StringUtils.isEmpty(maps.get(x).getAutoValueModel())){}{
                                                            //生成随机值
                                                            obj = getAutoValue(maps.get(x).getAutoValueModel());
                                                        }
                                                    }
                                                    params.add(obj);
                                                }
                                            }
                                            sql.append(" where ");
                                            //遍历
                                            for(int pi = 0 ; pi < kts.size() ; pi ++){
                                                if(pi >= 1){
                                                    sql.append(" and ");
                                                }
                                                sql.append(kts.get(pi).getParmaryKey()+"=?");
                                                //获取参数值
                                                Object obj = getDataObj(ejc,datas,kts.get(pi).getKeySheet(),p,kts.get(pi).getKeyFromCols());
                                                //判断为空执行
                                                if(StringUtils.isEmpty(obj)){
                                                    //判断是否设置默认值
                                                    if(!StringUtils.isEmpty(kts.get(pi).getDefaultVal())){
                                                        //默认值
                                                        obj = preDefaultVal(kts.get(pi).getDefaultVal());
                                                    }
                                                }
                                                params.add(obj);
                                            }
                                        }else{
                                            //名称
                                            StringBuffer name = new StringBuffer();
                                            //值
                                            StringBuffer value = new StringBuffer();
                                            //遍历
                                            for(int x = 0 ; x < maps.size() ; x ++){
                                                //判断不存在执行
                                                if(!(sql.indexOf(maps.get(x).getToCols()) > -1)){
                                                    if(name.length() > 0){
                                                        name.append(",");
                                                    }
                                                    if(value.length() > 0){
                                                        value.append(",");
                                                    }
                                                    name.append(maps.get(x).getToCols());
                                                    value.append("?");
                                                    //参数值
                                                    Object obj = getDataObj(ejc,datas,maps.get(x).getSheet(),p,maps.get(x).getFromCols());
                                                    //判断为空并autoValueModel不为空处理
                                                    if(StringUtils.isEmpty(obj)){
                                                        //判断是否设置默认值
                                                        if(!StringUtils.isEmpty(maps.get(x).getDefaultVal())){
                                                            //默认值
                                                            obj = preDefaultVal(maps.get(x).getDefaultVal());
                                                        }else if(!StringUtils.isEmpty(maps.get(x).getAutoValueModel())){
                                                            //生成随机值
                                                            obj = getAutoValue(maps.get(x).getAutoValueModel());
                                                        }
                                                    }
                                                    params.add(obj);
                                                }
                                            }
                                            //创建更新语句
                                            sql.append("insert into "+tc.getTableName()+"("+name.toString()+") values("+value.toString()+")");
                                        }
                                        //参数全部是空表示
                                        boolean isAllNulled = true;
                                        //遍历
                                        for(int x = 0 ; x < params.size() ; x ++){
                                            if(params.get(x) != null){
                                                isAllNulled = false;
                                            }
                                        }
                                        //判断不是全部为空执行
                                        if(!isAllNulled) {
                                            PreparedStatement ps = conns.get(tc.getDbUrl()).prepareStatement(sql.toString());
                                            //遍历
                                            for (int x = 0; x < params.size(); x++) {
                                                //设置参数
                                                ps.setObject((x + 1), params.get(x));
                                            }
                                            //运行语句
                                            ps.executeUpdate();
                                            ps.close();
                                        }
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                    successed = true;
                }else{
                    successed = false;
                    msg.append("table对应关系配置不能为空");
                }
            }
        }else{
            successed = false;
            msg.append("Excel配置或数据不能为空");
        }
        //结果对象
        StartAutoDBResult res = new StartAutoDBResult();
        res.setSuccessed(successed);
        res.setMsg(msg.toString());
        return res;
    }

    /**
     * 生成随机值
     * @param autoValueModel
     * @return
     */
    private Object getAutoValue(String autoValueModel){
        Object obj = null;
        //判断匹配执行
        if("UUID".equals(autoValueModel.toUpperCase())){
            obj = UUID.randomUUID().toString();
        }else if("AUTO".equals(autoValueModel.toUpperCase())){
            SnowflakesTools st = SnowflakesTools.newInstance();
            long id = st.nextId();
            obj = String.valueOf(id);
        }
        return obj;
    }

    /**
     * 获取数据总数
     * @param datas
     * @param keySheet
     * @return
     */
    private Integer getTotalSize(Map<String,Object> datas,String keySheet,ExcelJsonConf ejc){
        //数据总条数
        Integer totalSize = 0;
        if(!StringUtils.isEmpty(keySheet)){
            if(datas != null && datas.containsKey(keySheet)){
                //文件数据条数
                Integer dataSize = ((List<Object>)datas.get(keySheet)).size();
                if(ejc != null){
                    //获取sheet配置信息
                    SheetsConf sheetsConf = ExcelDataUtil.getSheetsConf(ejc,keySheet);
                    //判断不为空执行
                    if(sheetsConf != null){
                        dataSize = dataSize - ((sheetsConf.getStartRows() != null)?sheetsConf.getStartRows():0);
                    }
                }
                totalSize = dataSize;
            }
        }
        if(totalSize < 1 && datas != null && !datas.isEmpty()){
            for (String key : datas.keySet()) {
                //文件数据条数
                Integer dataSize = ((List<Object>)datas.get(key)).size();
                if(ejc != null){
                    //获取sheet配置信息
                    SheetsConf sheetsConf = ExcelDataUtil.getSheetsConf(ejc,key);
                    //判断不为空执行
                    if(sheetsConf != null){
                        dataSize = dataSize - ((sheetsConf.getStartRows() != null)?(sheetsConf.getStartRows()+1):0);
                    }
                }
                if(dataSize > totalSize){
                    totalSize = dataSize;
                }
            }
        }
        return totalSize;
    }

    /**
     * 获取某行某列数据
     * @param ejc
     * @param data
     * @param sheetName
     * @param rowIndex
     * @return
     */
    private Object getDataObj(ExcelJsonConf ejc,Map<String,Object> data,String sheetName,Integer rowIndex,Integer colIndex){
        Object obj = null;
        if(ejc != null){
            //开始行数
            Integer startrowIndex = 0;
            //获取sheet配置信息
            SheetsConf sheetsConf = ExcelDataUtil.getSheetsConf(ejc,sheetName);
            //判断不为空执行
            if(sheetsConf != null){
                startrowIndex = ((sheetsConf.getStartRows() != null)?sheetsConf.getStartRows():0);
            }
            rowIndex += startrowIndex;
        }
        //判断不为空执行
        if(data != null && !data.isEmpty() && data.containsKey(sheetName)){
            //数据行集
            List<Object> rows = (List<Object>) data.get(sheetName);
            if(rows != null && rows.size() > rowIndex){
                //数据列集
                List<Object> cols = (List<Object>) rows.get(rowIndex);
                if(cols != null && cols.size() > colIndex){
                    obj = cols.get(colIndex);
                }
            }
        }
        return obj;
    }

    /**
     * 获取数据中的所有表
     * @param conn
     * @return
     */
    private List<String> listAllTables(Connection conn) {
        if(conn == null){
            return null;
        }

        List<String> result = new ArrayList<>();
        ResultSet rs = null;
        try{
            //参数1 int resultSetType
            //ResultSet.TYPE_FORWORD_ONLY 结果集的游标只能向下滚动。
            //ResultSet.TYPE_SCROLL_INSENSITIVE 结果集的游标可以上下移动，当数据库变化时，当前结果集不变。
            //ResultSet.TYPE_SCROLL_SENSITIVE 返回可滚动的结果集，当数据库变化时，当前结果集同步改变。
            //参数2 int resultSetConcurrency
            //ResultSet.CONCUR_READ_ONLY 不能用结果集更新数据库中的表。
            //ResultSet.CONCUR_UPDATETABLE 能用结果集更新数据库中的表
            conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            DatabaseMetaData meta = conn.getMetaData();
            //目录名称; 数据库名; 表名称; 表类型;
            rs = meta.getTables(conn.getCatalog(), "%", "%", new String[]{"TABLE", "VIEW"});
            while(rs.next()){
                result.add(rs.getString("TABLE_NAME"));
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 获取中的所有字段
     * @param conn
     * @param tableName
     * @return
     */
    private List<String> listAllFields(Connection conn,String tableName) {
        if(conn == null){
            return null;
        }
        List<String> result = new ArrayList<>();
        ResultSet rs = null;
        try{
            DatabaseMetaData meta = conn.getMetaData();
            rs = meta.getColumns(conn.getCatalog(), "%", tableName.trim(), "%");
            while(rs.next()){
                result.add(rs.getString("COLUMN_NAME"));
            }
        }catch(Exception e){

        }
        return result;
    }

    /**
     * 判断包含，字符串不区分大小写
     * @param list
     * @param ele
     * @return
     */
    private static boolean listContains(List list, Object ele){
        //包含标识
        boolean hased = false;
        //判断类型
        if(ele instanceof String){
            if(list.contains(ele)){
                hased = true;
            }else{
                //遍历
                for(Object e : list){
                    if(((String)e).equalsIgnoreCase((String)ele)){
                        hased = true;
                    }
                }
            }
        }else{
            if(list.contains(ele)){
                hased = true;
            }
        }
        return hased;
    }

    /**
     * 默认值预处理
     * @param defaultVal
     * @return
     */
    private static String preDefaultVal(String defaultVal){
        String res = "";
        //判断不为空执行
        if(!StringUtils.isEmpty(defaultVal)){
            //判断并处理特殊函数
            if(defaultVal.toLowerCase().indexOf("now()") > -1){
                //当前时间戳
                String timestamp = String.valueOf((new Date()).getTime());
                res = defaultVal.replaceAll("now\\(\\)",timestamp).replaceAll("NOW\\(\\)",timestamp);
            }
            //判断是否是格式时间
            if(defaultVal.indexOf("DateFormat") > -1){
                //获取表达式
                String format = "";
                List<String> formats = getValues(defaultVal,"DateFormat\\(","\\)");
                //判断不为空执行并遍历
                if(formats != null && formats.size() > 0){
                    for(String fm : formats){
                        format = fm;
                    }
                }
                //判断不为空执行
                if(!StringUtils.isEmpty(format)){
                    //当前时间戳
                    SimpleDateFormat formatter = new SimpleDateFormat(format);
                    res = formatter.format(new Date());
                }
            }
        }
        return res;
    }

    /**
     * 获取割取字符串数据
     * @param val
     * @param start
     * @param end
     * @return
     */
    public static List<String> getValues(String val,String start,String end){
        List<String> vals = new ArrayList<String>();
        String regex = (start+"(.*?)"+end);
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(val);
        while(m.find()){
            vals.add(m.group(1));
        }
        return vals;
    }

    public static void main(String[] args){
        try{
            //String excelFilePath = "C:\\Users\\Administrator\\Desktop\\data.xls";
            String excelFilePath = "C:\\Users\\Administrator\\Desktop\\dataModel.xls";
            String jsonFile = "F:\\javacode\\base_excel_data\\src\\main\\java\\com\\myframe\\base_excel_data\\frame_core\\in\\util\\CONF.json";
            File file = new File(jsonFile);
            ExcelDataOption option = new ExcelDataOption(excelFilePath,file);
            /*option.startButNoDB(new ExcelDataCallBackIF() {
                @Override
                public boolean callBack(Integer type, Object data) {
                    System.out.println(data);
                    return false;
                }
            });*/
            option.startAndAutoDB();
            //System.out.println(ExcelDataUtil.readExcel(excelFilePath));
            /*ExcelJsonConf conf = option.jsonToConfObj(option.getJsonFromFile(file));
            boolean res = ExcelDataUtil.writeModelExcel(excelFilePath,conf);
            System.out.println(res);*/
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
