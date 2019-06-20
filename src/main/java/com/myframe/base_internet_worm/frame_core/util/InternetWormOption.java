package com.myframe.base_internet_worm.frame_core.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.myframe.base_internet_worm.frame_core.model.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.cglib.core.CollectionUtils;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * InternetWorm工具类
 */
public class InternetWormOption{
    //数据连接集合
    private Map<String,Connection> conns = new HashMap<String,Connection>();
    //数据表信息集合
    private Map<String,List<String>> tableInfos = new HashMap<String,List<String>>();
    //数据
    public static Map<String, Object> datas = new ConcurrentHashMap<String, Object>();

    public InternetWormConf conf;//配置

    public InternetWormConf getConf() {
        return conf;
    }

    public void setConf(InternetWormConf conf) {
        this.conf = conf;
    }

    /**
     * 构造函数
     * @param json
     */
    public InternetWormOption(String json){
        //先判断必要数据不为空
        Assert.notNull(json,"json must is not null");
        this.conf = jsonToConfObj(json);
        //配置默认值初始化
        initDefaultValue(conf);
    }

    /**
     * 构造函数
     * @param jsonFile
     */
    public InternetWormOption(File jsonFile){
        //先判断必要数据不为空
        Assert.notNull(jsonFile,"json must is not null");
        String json = getJsonFromFile(jsonFile);
        Assert.notNull(json,"jsonFile content must is not null");
        this.conf = jsonToConfObj(json);
        //配置默认值初始化
        initDefaultValue(conf);
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
    public InternetWormConf jsonToConfObj(String json){
        InternetWormConf jc = JSON.parseObject(json, new TypeReference<InternetWormConf>() {});
        return jc;
    }

    /**
     * 配置默认值初始化
     * @param iwc
     */
    private void initDefaultValue(InternetWormConf iwc){
        if(iwc == null){return;}
        if(iwc.getGrobal() == null){
            GrobalConf grobal = new GrobalConf();
            grobal.setAutoCreateTable(false);
            grobal.setRepetitionType("disposable");
            iwc.setGrobal(grobal);
        }
    }

    /**
     * 运行读取验证数据但不操作数据直接回调方法
     * @return
     */
    public void startButNoDB(final IWDataCallBackIF callBackIF) throws Exception {
        //先判断必要数据不为空
        Assert.notNull(conf,"json must is not null");
        //重复类型
        String repetitionType;
        //判断不为空执行
        if(conf.getGrobal() != null && !StringUtils.isEmpty(conf.getGrobal().getRepetitionType())){
            repetitionType = conf.getGrobal().getRepetitionType();
        }else{
            //默认disposable一次性
            repetitionType = "disposable";
        }
        //开关判断
        switch (repetitionType){
            //一次性
            case "disposable" : {
                    runOnce(callBackIF);
                }
                break;
            //顺序循环
            case "loop" : {
                    //重复次数
                    Integer repetitions = ((conf.getGrobal() != null && conf.getGrobal().getRepetitions() != null)?conf.getGrobal().getRepetitions():1);
                    //循环
                    for(int i = 0 ; i < repetitions ; i ++){
                        runOnce(callBackIF);
                    }
                }
                break;
            //定时循环
            case "timer" : {
                    //定时器表达式
                    String timerFormula = ((conf.getGrobal() != null && conf.getGrobal().getTimerFormula() != null)?conf.getGrobal().getTimerFormula():"");
                    Assert.hasLength(timerFormula,"请设置定时器表达式:timerFormula");
                    //判断不为空执行
                    if(!StringUtils.isEmpty(timerFormula)){
                        // 任务名称
                        String jobNameOrGroup = "IWOption";
                        // 构建JobDetail
                        JobDetail jobDetail = JobBuilder.newJob(TimerJobCallBack.class)
                                .withIdentity(jobNameOrGroup,jobNameOrGroup)
                                .build();
                        //传参数
                        jobDetail.getJobDataMap().put("IWDataCallBackIF",callBackIF);
                        jobDetail.getJobDataMap().put("InternetWormOption",this);
                        // 触发名称
                        String triName = "IWOption";
                        CronExpression express = new CronExpression(timerFormula);
                        // 构建触发器
                        Trigger trigger= TriggerBuilder.newTrigger()
                                .withIdentity(triName,jobNameOrGroup)
                                .startNow()
                                .withSchedule(CronScheduleBuilder.cronSchedule(express))
                                .build();
                        // 创建调度器（Scheduler）
                        SchedulerFactory sf = new StdSchedulerFactory();
                        Scheduler sched = sf.getScheduler();
                        // 注册调度器（Scheduler）
                        sched.scheduleJob(jobDetail,trigger);
                        // 启动调度器（Scheduler）
                        sched.start();
                    }
                }
                break;
            //时间间隔循环
            case "interval" : {
                    //时间间隔（毫秒）
                    final Integer timeInterval = ((conf.getGrobal() != null && conf.getGrobal().getTimeInterval() != null)?conf.getGrobal().getTimeInterval():1);
                    //判断timeInterval大于0
                    if(timeInterval > 0){
                        //启动线程
                        new Thread(new Runnable() {
                            public void run() {
                                try {
                                    runOnce(callBackIF);
                                    Thread.sleep(timeInterval);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                }
                break;
        }
    }

    /**
     * 运行读取验证数据并更新数据库方法
     * @return
     */
    public void startAndAutoDB() throws Exception {
        //先判断必要数据不为空
        Assert.notNull(conf,"json must is not null");
        //重复类型
        String repetitionType;
        //判断不为空执行
        if(conf.getGrobal() != null && !StringUtils.isEmpty(conf.getGrobal().getRepetitionType())){
            repetitionType = conf.getGrobal().getRepetitionType();
        }else{
            //默认disposable一次性
            repetitionType = "disposable";
        }
        //开关判断
        switch (repetitionType){
            //一次性
            case "disposable" : {
                runOnce();
            }
            break;
            //顺序循环
            case "loop" : {
                //重复次数
                Integer repetitions = ((conf.getGrobal() != null && conf.getGrobal().getRepetitions() != null)?conf.getGrobal().getRepetitions():1);
                //循环
                for(int i = 0 ; i < repetitions ; i ++){
                    runOnce();
                }
            }
            break;
            //定时循环
            case "timer" : {
                //定时器表达式
                String timerFormula = ((conf.getGrobal() != null && conf.getGrobal().getTimerFormula() != null)?conf.getGrobal().getTimerFormula():"");
                Assert.hasLength(timerFormula,"请设置定时器表达式:timerFormula");
                //判断不为空执行
                if(!StringUtils.isEmpty(timerFormula)){
                    // 任务名称
                    String jobNameOrGroup = "IWOption";
                    // 构建JobDetail
                    JobDetail jobDetail = JobBuilder.newJob(TimerJob.class)
                            .withIdentity(jobNameOrGroup,jobNameOrGroup)
                            .build();
                    //传参数
                    jobDetail.getJobDataMap().put("InternetWormOption",this);
                    // 触发名称
                    String triName = "IWOption";
                    CronExpression express = new CronExpression(timerFormula);
                    // 构建触发器
                    Trigger trigger= TriggerBuilder.newTrigger()
                            .withIdentity(triName,jobNameOrGroup)
                            .startNow()
                            .withSchedule(CronScheduleBuilder.cronSchedule(express))
                            .build();
                    // 创建调度器（Scheduler）
                    SchedulerFactory sf = new StdSchedulerFactory();
                    Scheduler sched = sf.getScheduler();
                    // 注册调度器（Scheduler）
                    sched.scheduleJob(jobDetail,trigger);
                    // 启动调度器（Scheduler）
                    sched.start();
                }
            }
            break;
            //时间间隔循环
            case "interval" : {
                //时间间隔（毫秒）
                final Integer timeInterval = ((conf.getGrobal() != null && conf.getGrobal().getTimeInterval() != null)?conf.getGrobal().getTimeInterval():1);
                //判断timeInterval大于0
                if(timeInterval > 0){
                    //启动线程
                    new Thread(new Runnable() {
                        public void run() {
                            try {
                                runOnce();
                                Thread.sleep(timeInterval);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            }
            break;
        }
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
     * 数据库处理操作
     * @param iwc
     * @return
     */
    private StartAutoDBResult initDB(InternetWormConf iwc) throws Exception {
        //成功运行标识
        boolean successed;
        //描述
        StringBuffer msg = new StringBuffer();
        if(iwc != null){
            //初始化数据库连接
            StartAutoDBResult intiRes0 = initConns(iwc.getTables());
            //判断成功执行
            if(intiRes0.isSuccessed()){
                successed = true;
                //判断全局配置是否设置了自动创建表格
                if(iwc.getGrobal() != null && iwc.getGrobal().getAutoCreateTable()){
                    //数据表配置信息
                    List<TablesConf> tbs = iwc.getTables();
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
            msg.append("配置内容不能为空");
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
     * 运行一遍
     */
    public void runOnce(IWDataCallBackIF callBackIF) throws Exception {
        //判断不为空执行
        if(conf != null && conf.getLinks() != null && conf.getLinks().size() > 0){
            //清空数据
            datas.clear();
            //倒数器
            CountDownLatch countDownLatch = new CountDownLatch(conf.getLinks().size());
            //遍历link
            for(LinkConf link : conf.getLinks()){
                //请求线程对象
                Task task = new Task(countDownLatch,link,conf.getGrobal());
                //线程
                Thread th = new Thread(task);
                th.start();
            }
            //等待检查，即上述线程执行完毕之后，执行await后边的代码
            countDownLatch.await();
        }
        //获取返回结果，true表示继续执行导入数据库，false表示结束
        boolean callBacked = callBackIF.callBack(datas);
        //判断是否继续执行
        if(callBacked && datas != null){
            //数据库处理前操作
            StartAutoDBResult bdb = initDB(conf);
            //判断数据库处理前操作是否成功
            if(bdb.isSuccessed()){
                //数据库处理
                updateDB(conf,datas);
            }
        }
        //清空数据
        if(datas != null){
            datas.clear();
        }
    }

    /**
     * 运行一遍
     */
    public void runOnce() throws Exception {
        //判断不为空执行
        if(conf != null && conf.getLinks() != null && conf.getLinks().size() > 0){
            //清空数据
            datas.clear();
            //倒数器
            CountDownLatch countDownLatch = new CountDownLatch(conf.getLinks().size());
            //遍历link
            for(LinkConf link : conf.getLinks()){
                //请求线程对象
                Task task = new Task(countDownLatch,link,conf.getGrobal());
                //线程
                Thread th = new Thread(task);
                th.start();
            }
            //等待检查，即上述线程执行完毕之后，执行await后边的代码
            countDownLatch.await();
        }
        //判断是否继续执行
        if(datas != null){
            //数据库处理前操作
            StartAutoDBResult bdb = initDB(conf);
            //判断数据库处理前操作是否成功
            if(bdb.isSuccessed()){
                //数据库处理
                updateDB(conf,datas);
            }
        }
        //清空数据
        if(datas != null){
            datas.clear();
        }
    }

    /**
     * 数据库处理
     * @param iwc
     * @param data
     * @return
     */
    private StartAutoDBResult updateDB(InternetWormConf iwc,Map<String,Object> data){
        //成功运行标识
        boolean successed = false;
        //描述
        StringBuffer msg = new StringBuffer();
        //判断不为空执行
        if(iwc != null && data != null && data.size() > 0){
            //获取数据映射配置
            List<TablesConf> tables = iwc.getTables();
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
                                if(!StringUtils.isEmpty(tc.getMaps().get(x).getToCols()) && !StringUtils.isEmpty(tc.getMaps().get(x).getLinkId())){
                                    if(tc.getMaps().get(x).isParmaryKey()){
                                        ParmaryKeyTmp pkt = new ParmaryKeyTmp();
                                        pkt.setParmaryKey(tc.getMaps().get(x).getToCols());
                                        pkt.setLinkId(tc.getMaps().get(x).getLinkId());
                                        pkt.setFromValKey(tc.getMaps().get(x).getFromValKey());
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
                                    Integer size = getTotalSize(datas,kt.getLinkId(),kt.getFromValKey());
                                    //判断是否大于totalSize
                                    if(size > totalSize){
                                        totalSize = size;
                                    }
                                }
                            }else{
                                //遍历
                                for(TablesMap tm : tc.getMaps()){
                                    //获取数据数量
                                    Integer size = getTotalSize(datas,tm.getLinkId(),tm.getFromValKey());
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
                                            Object obj = getDataObj(datas,kts.get(pi).getLinkId(),kts.get(pi).getFromValKey(),p);
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
                                                Object obj = getDataObj(datas,maps.get(x).getLinkId(),maps.get(x).getFromValKey(),p);
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
                                        sql.append(" where ");
                                        //遍历
                                        for(int pi = 0 ; pi < kts.size() ; pi ++){
                                            if(pi >= 1){
                                                sql.append(" and ");
                                            }
                                            sql.append(kts.get(pi).getParmaryKey()+"=?");
                                            //获取参数值
                                            Object obj = getDataObj(datas,kts.get(pi).getLinkId(),kts.get(pi).getFromValKey(),p);
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
                                                Object obj = getDataObj(datas,maps.get(x).getLinkId(),maps.get(x).getFromValKey(),p);
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
                                    if(!isAllNulled){
                                        PreparedStatement ps = conns.get(tc.getDbUrl()).prepareStatement(sql.toString());
                                        //遍历
                                        for(int x = 0 ; x < params.size() ; x ++){
                                            //设置参数
                                            ps.setObject((x+1), params.get(x));
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
        //结果对象
        StartAutoDBResult res = new StartAutoDBResult();
        res.setSuccessed(successed);
        res.setMsg(msg.toString());
        return res;
    }

    /**
     * 获取数据总数
     * @param datas
     * @param linkId
     * @return
     */
    private Integer getTotalSize(Map<String,Object> datas,String linkId,String fromValKey){
        //数据总条数
        Integer totalSize = 0;
        if(!StringUtils.isEmpty(linkId)){
            if(datas != null && datas.containsKey(linkId)){
                //获取数据载体
                NetResData nd = (NetResData) datas.get(linkId);
                //判断不为空执行
                if(nd != null && nd.getData() != null){
                    Object dataObj = ObjectParameterUtil.getValOnObj(nd.getData(),fromValKey);
                    if(dataObj instanceof List){
                        //数据条数
                        totalSize = ((List)dataObj).size();
                    }else{
                        //数据条数
                        totalSize = 1;
                    }
                }
            }
        }
        return totalSize;
    }

    /**
     * 获取数据
     * @param data
     * @param linkId
     * @param fromValKey
     * @param index
     * @return
     */
    private Object getDataObj(Map<String,Object> data,String linkId,String fromValKey,Integer index){
        Object obj = null;
        //判断不为空执行
        if(data != null && !data.isEmpty() && data.containsKey(linkId)){
            //获取数据载体
            NetResData nd = (NetResData) datas.get(linkId);
            //判断不为空执行
            if(nd != null && nd.getData() != null){
                //通过fromValKey获取数据
                Object dataObj = ObjectParameterUtil.getValOnObj(nd.getData(),fromValKey);
                //判断数据类型
                if(dataObj instanceof List){
                    //获取数据
                    List list = (List) dataObj;
                    //判断是否存在
                    if(list != null && list.size() > index){
                        obj = list.get(index);
                    }
                }else{
                    obj = dataObj;
                }
            }
        }
        return obj;
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
                List<String> formats = ObjectParameterUtil.getValues(defaultVal,"DateFormat\\(","\\)");
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
     * 请求线程类
     */
    class Task implements Runnable{

        //倒数器
        private CountDownLatch countDownLatch;
        //访问链接配置
        private LinkConf link;
        //全局配置
        private GrobalConf grobal;

        public Task(CountDownLatch countDownLatch,LinkConf link,GrobalConf grobal){
            this.countDownLatch = countDownLatch;
            this.link = link;
            this.grobal = grobal;
        }

        /**
         * 请求参数或请求头配置转Map
         * @param params
         * @return
         */
        public Map<String,String> toMap(List<LinkParamOrHeaderConf> params){
            Map<String,String> maps = new HashMap<String,String>();
            //判断不为空执行
            if(params != null && params.size() > 0){
                //遍历
                for(LinkParamOrHeaderConf lp : params){
                    //判断不为空执行
                    if(lp != null && !StringUtils.isEmpty(lp.getName())){
                        //获取值
                        String value = lp.getValue();
                        //默认
                        if(StringUtils.isEmpty(value)){
                            value = "";
                        }
                        maps.put(lp.getName(),value);
                    }
                }
            }
            return maps;
        }

        /**
         * 将请求结果字符串转Map或Map集合
         * @param resObject
         * @return
         */
        public Object resObjectToObj(String resObject){
            //数据
            Object dataObj = null;
            //判断是json执行
            if(JsonXmlUtil.isJson(resObject)){
                //判断开始符号
                if(resObject.startsWith("[")){
                    dataObj = JsonXmlUtil.json2List(resObject,Map.class);
                }else if(resObject.startsWith("{")){
                    dataObj = JsonXmlUtil.json2Ojbect(resObject,Map.class);
                }
            }else if(JsonXmlUtil.isXML(resObject)){//判断是xml执行
                dataObj = JsonXmlUtil.xmltoMap(resObject);
            }
            return dataObj;
        }

        /**
         * 请求
         * @param url 连接
         * @param headersMap 请求头
         * @param paramsMap 请求参数
         * @return
         */
        public String getResByUrl(String url,Map<String,String> headersMap,Map<String,String> paramsMap){
            //结果数据
            String resObject = "";
            try {
                //判断是否是https请求
                if (url.toLowerCase().startsWith("https")) {
                    //判断请求参数不为空执行
                    if (paramsMap != null && !paramsMap.isEmpty()) {
                        resObject = HttpNoSSL.postSSL(url, headersMap, paramsMap);
                    } else {
                        resObject = HttpNoSSL.getSSL(url, headersMap);
                    }
                } else {
                    //判断请求参数不为空执行
                    if (paramsMap != null && !paramsMap.isEmpty()) {
                        resObject = HttpNoSSL.post(url, headersMap, paramsMap);
                    } else {
                        resObject = HttpNoSSL.getSSL(url, headersMap);
                    }
                }
                //去空
                resObject = resObject.trim().replaceAll("\t","").replaceAll("\n","");
            }catch (Exception e){
                e.printStackTrace();
            }
            return resObject;
        }

        @Override
        public void run() {
            //判断不为空执行
            if(link != null && !StringUtils.isEmpty(link.getId()) &&
                    !StringUtils.isEmpty(link.getUrl()) && !StringUtils.isEmpty(link.getType())){
                //数据
                NetResData data = new NetResData();
                data.setId(link.getId());
                data.setType(link.getType());

                //请求头
                Map<String,String> headersMap = new HashMap<String,String>();
                //请求参数
                Map<String,String> paramsMap = new HashMap<String,String>();
                //判断不为空执行
                if(link.getHeaders() != null && link.getHeaders().size() > 0){
                    headersMap = toMap(link.getHeaders());
                }
                //判断请求参数不为空执行
                if(link.getParams() != null && link.getParams().size() > 0){
                    paramsMap = toMap(link.getParams());
                }

                //判断是否还有动态参数
                if(link.getUrl().indexOf("${") > -1){
                    //获取路径
                    String url = link.getUrl();
                    //遍历请求头
                    for (Map.Entry<String, String> entry : headersMap.entrySet()) {
                        //替换动态参数
                        url = url.replaceAll("\\$\\{"+entry.getKey()+"}",entry.getValue());
                    }
                    //遍历请求参数
                    for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
                        //替换动态参数
                        url = url.replaceAll("\\$\\{"+entry.getKey()+"}",entry.getValue());
                    }
                    link.setUrl(url);
                }

                //判断
                if("file".equals(link.getType())){//文件
                    //文件路径
                    String filePath = "";
                    //文件存储路径
                    String fileSavePath = link.getFileSavePath();
                    //默认
                    if(StringUtils.isEmpty(fileSavePath)){
                        //获取项目路径
                        File directory = new File("");// 参数为空
                        fileSavePath = directory.getAbsolutePath();
                    }
                    try {
                        //判断是否是https请求
                        if (link.getUrl().toLowerCase().startsWith("https")) {
                            //判断请求参数不为空执行
                            if (paramsMap != null && !paramsMap.isEmpty()) {
                                filePath = HttpNoSSL.downloadFileSSL(link.getUrl(), fileSavePath, headersMap, paramsMap);
                            } else {
                                filePath = HttpNoSSL.downloadFileSSL(link.getUrl(), fileSavePath, headersMap);
                            }
                        }else{
                            //判断请求参数不为空执行
                            if (paramsMap != null && !paramsMap.isEmpty()) {
                                filePath = HttpNoSSL.downloadFile(link.getUrl(), fileSavePath, headersMap, paramsMap);
                            } else {
                                filePath = HttpNoSSL.downloadFile(link.getUrl(), fileSavePath, headersMap);
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    //设置数据
                    data.setData(filePath);
                }else{
                    if("html".equals(link.getType())) {//网页
                        //值
                        Map map = new HashMap();
                        //发起请求
                        String res = getResByUrl(link.getUrl(),headersMap,paramsMap);
                        //判断不为空执行
                        if(!StringUtils.isEmpty(res)){
                            //解析HTML
                            Document doc = Jsoup.parse(res);
                            //判断不为空执行
                            if(doc != null && link.getValMaps() != null && link.getValMaps().size() > 0){
                                //遍历
                                for(LinkValMapsConf valMap : link.getValMaps()){
                                    //键
                                    String key = valMap.getKey();
                                    //获取公式或者json对象字段
                                    String selectorOrField = valMap.getSelectorOrField();
                                    //判断不为空执行
                                    if(!StringUtils.isEmpty(key) && !StringUtils.isEmpty(selectorOrField)){
                                        //选择器
                                        String selector = "";
                                        //属性名
                                        String attr = "";
                                        //判断是否包含>>字符
                                        if(selectorOrField.indexOf(">>") > -1){
                                            //分割
                                            String[] sa = selectorOrField.split(">>");
                                            //判断不为空执行
                                            if(sa != null && sa.length > 0){
                                                //选择器
                                                selector = sa[0];
                                                //属性名
                                                attr = ((!StringUtils.isEmpty(sa[1]))?sa[1]:"text");
                                            }
                                        }else{
                                            selector = selectorOrField;
                                            attr = "text";
                                        }
                                        //获取节点
                                        Elements links = doc.select(selector);
                                        //判断不为空
                                        if(links != null && links.size() > 0){
                                            if(links.size() == 1){
                                                Object val = null;
                                                //取文本
                                                if(attr.trim().equals("text")){
                                                    val = links.first().text();
                                                }else{
                                                    val = links.first().attr(attr);
                                                }
                                                //设置数据
                                                map.put(key,val);
                                            }else{
                                                //值集合
                                                List vals = new ArrayList();
                                                //遍历
                                                for(Element link : links){
                                                    Object val = null;
                                                    //取文本
                                                    if(attr.trim().equals("text")){
                                                        val = link.text();
                                                    }else{
                                                        val = link.attr(attr);
                                                    }
                                                    vals.add(val);
                                                }
                                                //设置数据
                                                map.put(key,vals);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        //设置数据
                        data.setData(map);
                    }else if("json".equals(link.getType())){//普通对象JSON
                        //发起请求
                        String res = getResByUrl(link.getUrl(),headersMap,paramsMap);
                        //值
                        Map map = new HashMap();
                        //数据
                        Object dataObj = resObjectToObj(res);
                        //数据获取配置集合
                        if(dataObj != null && link.getValMaps() != null && link.getValMaps().size() > 0){
                            //遍历
                            for(LinkValMapsConf valMap : link.getValMaps()){
                                //键
                                String key = valMap.getKey();
                                //获取公式或者json对象字段
                                String selectorOrField = valMap.getSelectorOrField();
                                //判断不为空执行
                                if(!StringUtils.isEmpty(key) && !StringUtils.isEmpty(selectorOrField)){
                                    Object obj = ObjectParameterUtil.getValOnObj(dataObj , selectorOrField);
                                    map.put(key,obj);
                                }
                            }
                        }
                        //判断不为空执行
                        if(map != null && !map.isEmpty()){
                            //设置数据
                            data.setData(map);
                        }else{
                            //设置数据
                            data.setData(dataObj);
                        }
                    }else if("pageJson".equals(link.getType())){//分页对象集合
                        //数据
                        Object dataObj = null;
                        String dataField = link.getDataField();//数据字段
                        String pageNumberField = link.getPageNumberField();//当前页码字段
                        //判断不为空执行
                        if(!StringUtils.isEmpty(dataField) && !StringUtils.isEmpty(pageNumberField)){
                            //总结果数据
                            List rs = new ArrayList();
                            //当前页
                            Integer currentPageNumber = 1;
                            //添加参数
                            paramsMap.put(pageNumberField,String.valueOf(currentPageNumber));
                            //发起请求
                            String res = getResByUrl(link.getUrl(),headersMap,paramsMap);
                            //继续下一页标识
                            boolean nexted = true;
                            //判断不为空执行
                            while (nexted){
                                //数据
                                Object nd = resObjectToObj(res);
                                //获取数据
                                List nds = (List) ObjectParameterUtil.getValOnObj(nd , dataField);
                                //判断不为空执行
                                if(nds != null && nds.size() > 0){
                                    rs.addAll(nds);

                                    //下一页
                                    currentPageNumber ++;
                                    //添加参数
                                    paramsMap.put(pageNumberField,String.valueOf(currentPageNumber));
                                    //发起请求
                                    res = getResByUrl(link.getUrl(),headersMap,paramsMap);
                                }else{
                                    //继续下一页标识
                                    nexted = false;
                                }
                            }
                            //数据获取配置集合
                            if(link.getValMaps() != null && link.getValMaps().size() > 0){
                                //值
                                Map map = new HashMap();
                                //遍历
                                for(LinkValMapsConf valMap : link.getValMaps()){
                                    //键
                                    String key = valMap.getKey();
                                    //获取公式或者json对象字段
                                    String selectorOrField = valMap.getSelectorOrField();
                                    //判断不为空执行
                                    if(!StringUtils.isEmpty(key) && !StringUtils.isEmpty(selectorOrField)){
                                        Object obj = ObjectParameterUtil.getValOnObj(rs , selectorOrField);
                                        map.put(key,obj);
                                    }
                                }
                                //数据
                                dataObj = map;
                            }else{
                                //数据
                                dataObj = rs;
                            }
                        }else{//判断如果不设置相关信息则只执行一次
                            //发起请求
                            String res = getResByUrl(link.getUrl(),headersMap,paramsMap);
                            //数据
                            dataObj = resObjectToObj(res);
                        }
                        //设置数据
                        data.setData(dataObj);
                    }
                }
                //添加
                datas.put(link.getId(),data);
            }
            //计算
            countDownLatch.countDown();
        }
    }

    public static void main(String[] args){
        try{
            /*String jsonFile = "F:\\javacode\\base_internet_worm\\src\\main\\java\\com\\myframe\\base_internet_worm\\frame_core\\util\\CONF.json";
            File file = new File(jsonFile);
            InternetWormOption option = new InternetWormOption(file);
            option.startButNoDB(new IWDataCallBackIF() {
                @Override
                public boolean callBack(Object data) {
                    System.out.println(JsonXmlUtil.toJson(data));
                    return true;
                }
            });*/
            String str = "http://www.test.com?name=${name}";
            //判断是否还有动态参数
            if(str.indexOf("${") > -1){
                System.out.println(true);
                str = str.replaceAll("\\$\\{name}","123");
                System.out.println(str);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
