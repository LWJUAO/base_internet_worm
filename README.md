# base_internet_worm

## 作用说明：
  根据配置的JSON文件从网络上获取数据，其中包括JSON数据，网页数据，分页JSON数据和文件；
  可设置获取完成回调处理
  最后根据配置导入数据库

## 使用步骤：
  配置JSON文件；
  使用InternetWormOption类解析JSON配置
  执行startAndAutoDB()方法或者startButNoDB(new IWDataCallBackIF(){})方法
  (startAndAutoDB方法是获取数据后直接导入数据库，startButNoDB方法是获取数据后回调执行，当回调方法返回true时导入数据库)

## 配置说明：
```
{
  "grobal" : {  //全局配置
    "autoCreateTable" : true,           //是否创建表
    "repetitionType" : "timer",         //重复类型，disposable一次性，loop顺序循环，timer定时循环，interval时间间隔循环
    "repetitions" : 0,                  //重复次数
    "timeInterval" : 0,                 //时间间隔（毫秒）
    "timerFormula" : "*/5 * * * * ?"    //定时器表达式
  },
  "links" : [   //访问链接集合
    {
      "id" : "link1",           //访问链接标识
      "name" : "测试",            //访问链接名称
      "type" : "pageJson",          //链接类型，file:文件，html:网页，json:普通对象JSON，pageJson:分页对象集合
      "url" : "http://192.168.0.113:16082/esb/EsbServiceInfo/page",     //链接路径
      "fileSavePath" : "D://IWTest",        //文件存储路径
      "dataField" : "rows",                 //数据字段
      "pageNumberField" : "currentPage",    //当前页码字段
      "headers" : [     //请求头集合
        {
           "name" : "Cookies",    //请求头键
           "value" : "************"     //请求头值
        }
      ],
      "params" : [      //请求参数集合
        {
          "name" : "token",     //参数名
          "value" : "5a41***********"       //参数值
        }
      ],
      "valMaps" : [     //请求结果数据映射，没有配置则表示是直接使用返回数据
        {
          "name" : "标题",    //数据说明
          "key" : "serviceName",    //数据键
          "selectorOrField" : "serviceName"     //数据获取字段或公式
        }
      ]
    }
  ],
  "tables" : [      //数据库表集合
    {
      "tableName" : "excelTest2",       //表名
      "dbUrl" : "jdbc:mysql://localhost:3306/test?useSSL=false&serverTimezone=UTC",     //数据库连接
      "dbDriver" : "com.mysql.cj.jdbc.Driver",      //驱动
      "dbUser" : "root",        //数据库用户
      "dbPassword" : "qida1403",    //数据库密码
      "maps" : [    //表属性字段映射集合
        {
          "linkId" : "model",   //来源的访问链接标识
          "fromValKey" : "",    //来源的数据字段
          "toCols" : "mt_name", //在数据库表中的字段名
          "isParmaryKey" : false, //是否是主键
          "autoValueModel" : "AUTO" //自动创建值模式，有UUID、AUTO
        }
      ]
    }
  ]
}
```
## 案例：
```String jsonFile = "F:/test/CONF.json";
File file = new File(jsonFile);
InternetWormOption option = new InternetWormOption(file);
option.startButNoDB(new IWDataCallBackIF() {
    @Override
    public boolean callBack(Object data) {
        System.out.println(JsonXmlUtil.toJson(data));
        return true;
    }
});```