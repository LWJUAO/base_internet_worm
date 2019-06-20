package com.myframe.base_excel_data.frame_core.in.util;

import com.myframe.base_excel_data.frame_core.in.model.ColumsConf;
import com.myframe.base_excel_data.frame_core.in.model.ExcelJsonConf;
import com.myframe.base_excel_data.frame_core.in.model.SheetsConf;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Excel文件工具类
 */
public class ExcelDataUtil {

    /**
     * 获取文件后缀名
     * @param filePath
     * @return
     */
    public static String getFileType(String filePath){
        //文件类型
        String fileType = "";
        if(filePath.indexOf(".") > -1){
            //获取文件类型
            fileType = filePath.substring(filePath.lastIndexOf(".")+1).toLowerCase();
        }
        return fileType;
    }

    /**
     * 获取sheet配置
     * @param ejc
     * @return
     */
    public static SheetsConf getSheetsConf(ExcelJsonConf ejc,String sheetName){
        if(ejc != null && ejc.getSheets() != null && ejc.getSheets().size() > 0){
            if(!StringUtils.isEmpty(sheetName)){
                for(int i = 0 ; i < ejc.getSheets().size() ; i++){
                    if(sheetName.equals(ejc.getSheets().get(i).getSheetName())){
                        return ejc.getSheets().get(i);
                    }
                }
            }
        }
        return null;
    }
    /**
     * 获取Colums配置
     * @param colums
     * @return
     */
    public static ColumsConf getColumsConf(List<ColumsConf> colums, Integer colIndex){
        if(colums != null && colums.size() > 0){
            if(colIndex != null){
                for(int i = 0 ; i < colums.size() ; i++){
                    if(colIndex.equals(colums.get(i).getColIndex()) || colIndex == (colums.get(i).getColIndex())){
                        return colums.get(i);
                    }
                }
            }
        }
        return null;
    }

    /**
     * 读Excel文件数据
     * @param excelPath-Excel文件路径
     * @param ejc-配置信息
     * @return
     */
    public static Map<String,Object> readExcel(String excelPath,ExcelJsonConf ejc) throws Exception{
        //检查文件路径不为空
        Assert.notNull(excelPath,"没有指定的Excel");
        if(excelPath != null){
            String fileType = getFileType(excelPath);
            //判断是Excel文件
            if(!"xls".equals(fileType) && !"xlsx".equals(fileType)){
                throw new Exception("当前文件不是Excel文件");
            }
        }
        //建立输入流
        FileInputStream in = new FileInputStream(excelPath);
        return readExcel(in,ejc);
    }

    /**
     * 读Excel文件数据
     * @param in-Excel文件流
     * @param ejc-配置信息
     * @return
     */
    public static Map<String,Object> readExcel(FileInputStream in,ExcelJsonConf ejc) throws Exception{
        //检查文件流不为空
        Assert.notNull(in,"Excel文件流不能为空");
        //验证通过标识，默认true表示验证通过
        boolean verify = true;
        //结果
        Map<String,Object> res = new HashMap<String,Object>();
        //数据集
        Map<String,Object> datas = new HashMap<String,Object>();
        //sheet名称集合
        List<String> sheetNames = new ArrayList<String>();
        //获取Excel文件Workbook对象
        Workbook wb = WorkbookFactory.create(in);
        //获取工作空间名称集合
        Iterator<Sheet> sheets = wb.sheetIterator();
        //遍历sheet
        while(sheets.hasNext()){
            String sheetName = sheets.next().getSheetName();
            sheetNames.add(sheetName);
        }
        //判断不为空执行
        if(sheetNames != null && sheetNames.size() > 0){
            for(int i = 0 ; i < sheetNames.size() ; i ++){
                Sheet sheet = wb.getSheet(sheetNames.get(i));
                //返回合并区域的列表
                List<CellRangeAddress> rangs = sheet.getMergedRegions();
                //判断不为空执行
                if(rangs != null && rangs.size() > 0){
                    //遍历处理合并单元格
                    for(CellRangeAddress ca : rangs) {
                        //获得合并单元格的起始行, 结束行, 起始列, 结束列
                        int firstRow = ca.getFirstRow(),lastRow = ca.getLastRow(),firstCol = ca.getFirstColumn(),lastCol = ca.getLastColumn();
                        //获取合并中第一个单元格
                        Cell fCell = sheet.getRow(firstRow).getCell(firstCol);
                        Object fCellVal = getCellValue(fCell);
                        for(int r = firstRow ; r <= lastRow ; r ++){
                            for(int c = firstCol ; c <= lastCol ; c ++){
                                //判断不是第一个单元格
                                if(!(r == firstRow && c == firstCol)){
                                    Cell cell = sheet.getRow(r).getCell(c);
                                    if(cell == null){
                                        cell = sheet.getRow(r).createCell(c);
                                    }
                                    //修改值
                                    setCellValue(cell,fCell.getCellTypeEnum(),fCellVal);
                                }
                            }
                        }
                    }
                }
                //获取配置信息
                SheetsConf sc = getSheetsConf(ejc,sheetNames.get(i));
                //判断数据并初始化
                if(sc != null && sc.getStartRows() == null){
                    sc.setStartRows(0);
                }
                //行迭代器
                Iterator<Row> rows = sheet.rowIterator();
                //sheet数据集合
                List<Object> sheetDatas = new ArrayList<Object>();
                //行标识
                Integer rowIndex = 0;
                //遍历行
                while(rows.hasNext()){
                    //错误原因描述
                    StringBuffer errorDesc = new StringBuffer();
                    //获取当前行
                    Row row = rows.next();
                    //获取列总单元格数量
                    int cellNum = row.getLastCellNum();
                    //行数据集合
                    List<Object> rowDatas = new ArrayList<Object>();
                    //列标识
                    Integer colIndex = 0;
                    //遍历列
                    for(int cI = 0; cI < cellNum ; cI++ ){
                        Cell cell = row.getCell(cI);
                        Object val = getCellValue(cell);
                        //在设定范围内标识
                        boolean compared = false;
                        //获取列配置
                        ColumsConf columsConf = null;
                        //判断不为空执行
                        if(sc != null){
                            //判断数据读取方向
                            if("horizontal".equals(sc.getReadOrientation())){
                                //获取列配置
                                columsConf = getColumsConf(sc.getColums(),colIndex);
                                //在设定范围内标识
                                compared = (rowIndex >= sc.getStartRows());
                            }else{
                                //获取列配置
                                columsConf = getColumsConf(sc.getColums(),rowIndex);
                                //在设定范围内标识
                                compared = (colIndex >= sc.getStartRows());
                            }
                        }
                        //验证数据
                        if(sc != null && compared){
                            //判断不为空执行
                            if(columsConf != null){
                                //数据类型
                                if(StringUtils.isEmpty(columsConf.getDataType())){columsConf.setDataType("string");}
                                //列名
                                if(StringUtils.isEmpty(columsConf.getColName())){columsConf.setColName("第"+colIndex+"列");}
                                //验证不为空
                                if(!columsConf.isAcceptNulled() && (val == null || StringUtils.isEmpty(val))){
                                    //验证不通过
                                    verify = false;
                                    //错误原因描述
                                    errorDesc.append(columsConf.getColName()+"不能为空");
                                }
                                //判断数据类型
                                if("int".equals(columsConf.getDataType().toLowerCase()) || "integer".equals(columsConf.getDataType().toLowerCase())){
                                    if(!isInteger(val)){
                                        //验证不通过
                                        verify = false;
                                        //错误原因描述
                                        errorDesc.append(columsConf.getColName()+"不是数值int类型");
                                    }
                                }else if("double".equals(columsConf.getDataType().toLowerCase())){
                                    if(!isDouble(val)){
                                        //验证不通过
                                        verify = false;
                                        //错误原因描述
                                        errorDesc.append(columsConf.getColName()+"不是小数double类型");
                                    }else if(!StringUtils.isEmpty(columsConf.getDataFormat().toLowerCase())){////判断格式配置不为空执行
                                        BigDecimal bd = new BigDecimal(String.valueOf(val));
                                        if(columsConf.getDataFormat().indexOf(".") > -1){
                                            //小数位数
                                            Integer pix = columsConf.getDataFormat()
                                                    .substring(columsConf.getDataFormat().indexOf(".")+1).length();
                                            val = bd.setScale(pix, BigDecimal.ROUND_HALF_UP).doubleValue();
                                        }else{
                                            val = bd.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
                                        }
                                    }
                                }else if("date".equals(columsConf.getDataType().toLowerCase())){
                                    if(!isValidDate(val,columsConf.getDataFormat())){
                                        //验证不通过
                                        verify = false;
                                        //错误原因描述
                                        errorDesc.append(columsConf.getColName()+"不是合法的时间类型");
                                    }else if(!StringUtils.isEmpty(columsConf.getDataFormat())){
                                        String str = String.valueOf(val);
                                        SimpleDateFormat format = new SimpleDateFormat(columsConf.getDataFormat());
                                        try {
                                            // 设置lenient为false. 否则SimpleDateFormat会比较宽松地验证日期，比如2007/02/29会被接受，并转换成2007/03/01
                                            format.setLenient(false);
                                            Date date = format.parse(str);
                                            val = format.format(date);
                                        } catch (ParseException e) {
                                        }
                                    }
                                }else if("email".equals(columsConf.getDataType().toLowerCase())){
                                    if(!isEmailValid(val)){
                                        //验证不通过
                                        verify = false;
                                        //错误原因描述
                                        errorDesc.append(columsConf.getColName()+"不是合法的邮箱");
                                    }
                                }else{
                                    //列值范围验证
                                    if(columsConf.getValsScope() != null && (columsConf.getValsScope().size() > 0)){
                                        //存在标识
                                        boolean hased = false;
                                        for(int x = 0 ; x < columsConf.getValsScope().size() ; x ++){
                                            //判断相同
                                            if(columsConf.getValsScope().get(x).equals(val)){
                                                //存在标识
                                                hased = true;
                                                break;
                                            }
                                        }
                                        if(!hased){
                                            //验证不通过
                                            verify = false;
                                            //错误原因描述
                                            errorDesc.append(columsConf.getColName()+"不是合法的值");
                                        }
                                    }
                                    //列排斥值范围验证
                                    if(columsConf.getValsDislodge() != null && (columsConf.getValsDislodge().size() > 0)){
                                        //存在标识
                                        boolean hased = false;
                                        for(int x = 0 ; x < columsConf.getValsDislodge().size() ; x ++){
                                            //判断相同
                                            if(columsConf.getValsDislodge().get(x).equals(val)){
                                                //存在标识
                                                hased = true;
                                                break;
                                            }
                                        }
                                        if(hased){
                                            //验证不通过
                                            verify = false;
                                            //错误原因描述
                                            errorDesc.append(columsConf.getColName()+"不是合法的值");
                                        }
                                    }

                                    //匹配正则表达式验证
                                    if(!StringUtils.isEmpty(columsConf.getRegex())){
                                        if(StringUtils.isEmpty(val)){
                                            //验证不通过
                                            verify = false;
                                            //错误原因描述
                                            errorDesc.append(columsConf.getColName()+"不是合法的值");
                                        }else{
                                            //验证
                                            Pattern pattern = Pattern.compile(columsConf.getRegex());
                                            boolean rs = pattern.matcher(String.valueOf(val)).matches();
                                            if(!rs){
                                                //验证不通过
                                                verify = false;
                                                //错误原因描述
                                                errorDesc.append(columsConf.getColName()+"不是合法的值");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        //添加值到数据集中
                        rowDatas.add(val);
                        //列标识
                        colIndex ++;
                    }
                    //错误原因描述
                    String desc = errorDesc.toString();
                    //判断不为空执行
                    if(!StringUtils.isEmpty(desc)){
                        rowDatas.add(desc);
                    }
                    sheetDatas.add(rowDatas);
                    //行标识
                    rowIndex ++;
                }
                //判断数据读取方向
                if(sc != null && !"horizontal".equals(sc.getReadOrientation())){
                    //数据翻转
                    List<Object> ds = reverse(sheetDatas);
                    datas.put(sheetNames.get(i),ds);
                }else{
                    datas.put(sheetNames.get(i),sheetDatas);
                }
            }
        }
        //关闭流
        if(in != null){
            in.close();
        }
        res.put("verify",verify);
        res.put("data",datas);
        return res;
    }

    /**
     * 读Excel文件数据
     * @param excelPath-Excel文件路径
     * @return
     */
    public static Map<String,Object> readExcel(String excelPath) throws Exception{
        //检查文件路径不为空
        Assert.notNull(excelPath,"没有指定的Excel");
        if(excelPath != null){
            String fileType = getFileType(excelPath);
            //判断是Excel文件
            if(!"xls".equals(fileType) && !"xlsx".equals(fileType)){
                throw new Exception("当前文件不是Excel文件");
            }
        }
        //建立输入流
        FileInputStream in = new FileInputStream(excelPath);
        return readExcel(in);
    }

    /**
     * 读Excel文件数据
     * @param in-Excel文件流
     * @return
     */
    public static Map<String,Object> readExcel(FileInputStream in) throws Exception{
        //检查文件流不为空
        Assert.notNull(in,"Excel文件流不能为空");
        //结果集
        Map<String,Object> datas = new HashMap<String,Object>();
        //sheet名称集合
        List<String> sheetNames = new ArrayList<String>();
        //获取Excel文件Workbook对象
        Workbook wb = WorkbookFactory.create(in);
        //获取工作空间名称集合
        Iterator<Sheet> sheets = wb.sheetIterator();
        //遍历sheet
        while(sheets.hasNext()){
            String sheetName = sheets.next().getSheetName();
            sheetNames.add(sheetName);
        }
        //判断不为空执行
        if(sheetNames != null && sheetNames.size() > 0){
            for(int i = 0 ; i < sheetNames.size() ; i ++){
                Sheet sheet = wb.getSheet(sheetNames.get(i));
                //返回合并区域的列表
                List<CellRangeAddress> rangs = sheet.getMergedRegions();
                //判断不为空执行
                if(rangs != null && rangs.size() > 0){
                    //遍历处理合并单元格
                    for(CellRangeAddress ca : rangs) {
                        //获得合并单元格的起始行, 结束行, 起始列, 结束列
                        int firstRow = ca.getFirstRow(),lastRow = ca.getLastRow(),firstCol = ca.getFirstColumn(),lastCol = ca.getLastColumn();
                        //获取合并中第一个单元格
                        Cell fCell = sheet.getRow(firstRow).getCell(firstCol);
                        Object fCellVal = getCellValue(fCell);
                        for(int r = firstRow ; r <= lastRow ; r ++){
                            for(int c = firstCol ; c <= lastCol ; c ++){
                                //判断不是第一个单元格
                                if(!(r == firstRow && c == firstCol)){
                                    Cell cell = sheet.getRow(r).getCell(c);
                                    if(cell == null){
                                        cell = sheet.getRow(r).createCell(c);
                                    }
                                    //修改值
                                    setCellValue(cell,fCell.getCellTypeEnum(),fCellVal);
                                }
                            }
                        }
                    }
                }
                //行迭代器
                Iterator<Row> rows = sheet.rowIterator();
                //sheet数据集合
                List<Object> sheetDatas = new ArrayList<Object>();
                //遍历行
                while(rows.hasNext()){
                    Row row = rows.next();
                    //获取列总单元格数量
                    int cellNum = row.getLastCellNum();
                    //行数据集合
                    List<Object> rowDatas = new ArrayList<Object>();
                    //遍历列
                    for(int cI = 0; cI < cellNum ; cI++ ){
                        Cell cell = row.getCell(cI);
                        Object val = getCellValue(cell);
                        rowDatas.add(val);
                    }
                    sheetDatas.add(rowDatas);
                }
                datas.put(sheetNames.get(i),sheetDatas);
            }
        }
        //关闭流
        if(in != null){
            in.close();
        }
        return datas;
    }

    /**
     * 读Excel文件数据
     * @param excelPath-Excel文件路径
     * @param sheetName-sheet工作空间
     * @param startIndex-开始行
     * @return
     */
    public static List<Object> readExcel(String excelPath, String sheetName,Integer startIndex) throws Exception{
        //检查文件路径不为空
        Assert.notNull(excelPath,"没有指定的Excel");
        Assert.notNull(sheetName,"没有指定的sheetName");
        if(excelPath != null){
            String fileType = getFileType(excelPath);
            //判断是Excel文件
            if(!"xls".equals(fileType) && !"xlsx".equals(fileType)){
                throw new Exception("当前文件不是Excel文件");
            }
        }
        //建立输入流
        FileInputStream in = new FileInputStream(excelPath);
        return readExcel(in, sheetName,startIndex);
    }

    /**
     * 读Excel文件数据
     * @param in-Excel文件流
     * @param sheetName-sheet工作空间
     * @param startIndex-开始行
     * @return
     */
    public static List<Object> readExcel(FileInputStream in, String sheetName,Integer startIndex) throws Exception{
        //检查文件流不为空
        Assert.notNull(in,"Excel文件流不能为空");
        //默认值
        if(startIndex == null){
            startIndex = 0;
        }
        //结果链表
        List<Object> res = new ArrayList<Object>();
        //获取Excel文件Workbook对象
        Workbook wb = WorkbookFactory.create(in);
        Sheet sheet = wb.getSheet(sheetName);//建立sheet1
        if(sheet != null){
            //返回合并区域的列表
            List<CellRangeAddress> rangs = sheet.getMergedRegions();
            //判断不为空执行
            if(rangs != null && rangs.size() > 0){
                //遍历处理合并单元格
                for(CellRangeAddress ca : rangs) {
                    //获得合并单元格的起始行, 结束行, 起始列, 结束列
                    int firstRow = ca.getFirstRow(),lastRow = ca.getLastRow(),firstCol = ca.getFirstColumn(),lastCol = ca.getLastColumn();
                    //获取合并中第一个单元格
                    Cell fCell = sheet.getRow(firstRow).getCell(firstCol);
                    Object fCellVal = getCellValue(fCell);
                    for(int r = firstRow ; r <= lastRow ; r ++){
                        for(int c = firstCol ; c <= lastCol ; c ++){
                            //判断不是第一个单元格
                            if(!(r == firstRow && c == firstCol)){
                                Cell cell = sheet.getRow(r).getCell(c);
                                if(cell == null){
                                    cell = sheet.getRow(r).createCell(c);
                                }
                                //修改值
                                setCellValue(cell,fCell.getCellTypeEnum(),fCellVal);
                            }
                        }
                    }
                }
            }
            //行迭代器
            Iterator<Row> rows = sheet.rowIterator();
            //sheet数据集合
            List<Object> sheetDatas = new ArrayList<Object>();
            //行标识
            Integer rowIndex = 0;
            //遍历行
            while(rows.hasNext()){
                //获取一行数据
                Row row = rows.next();
                //判断是否再设定范围内
                if(rowIndex >= startIndex){
                    //获取列总单元格数量
                    int cellNum = row.getLastCellNum();
                    //行数据集合
                    List<Object> rowDatas = new ArrayList<Object>();
                    //遍历列
                    for(int cI = 0; cI < cellNum ; cI++ ){
                        Cell cell = row.getCell(cI);
                        Object val = getCellValue(cell);
                        rowDatas.add(val);
                    }
                    sheetDatas.add(rowDatas);
                }
                //行标识
                rowIndex ++;
            }
            res.add(sheetDatas);
        }
        //关闭流
        if(in != null){
            in.close();
        }
        return res;
    }
    /**
     * 写上传Excel文件数据model
     * @param filePath-Excel文件流
     * @param ejc-配置信息
     * @return
     */
    public static boolean writeModelExcel(String filePath,ExcelJsonConf ejc) throws Exception{
        boolean res = false;
        //检查文件路径不为空
        Assert.notNull(filePath,"Excel路径不能为空");
        Assert.notNull(ejc,"配置信息不能为空");
        try {
            //文件类型
            String fileType = getFileType(filePath);
            //判断是Excel文件
            if("xls".equals(fileType) || "xlsx".equals(fileType)){
                //获取sheet配置集合
                List<SheetsConf> sheetsConfs = ejc.getSheets();
                //判断不为空执行
                if (sheetsConfs != null && sheetsConfs.size() > 0) {
                    //创建文件
                    File file = new File(filePath);
                    if(!file.exists()){
                        if(!file.getParentFile().exists()){
                            file.getParentFile().mkdirs();
                        }
                        file.createNewFile();
                    }
                    //获取Excel文件Workbook对象
                    Workbook wb = newWorkbook(fileType);
                    //遍历
                    for (int i = 0; i < sheetsConfs.size(); i++) {
                        SheetsConf sh = sheetsConfs.get(i);
                        //判断为空执行
                        if(StringUtils.isEmpty(sh.getSheetName())){
                            sh.setSheetName("sheet"+i);
                        }
                        //判断为空执行
                        if(StringUtils.isEmpty(sh.getReadOrientation())){
                            //默认数据读取方向
                            sh.setReadOrientation("horizontal");
                        }
                        //数据载体
                        List<Object> datas = new ArrayList<Object>();
                        //创建工作空间
                        Sheet sheet = wb.createSheet(sh.getSheetName());
                        //获取行列配置信息
                        List<ColumsConf> colums = sh.getColums();
                        //去重载体
                        Set<ColumsConf> ts = new HashSet<ColumsConf>();
                        //判断不为空执行
                        if(colums != null && colums.size() > 0){
                            //去重
                            ts.addAll(colums);
                            //获取行列配置信息
                            List<ColumsConf> sts = new ArrayList<ColumsConf>();
                            sts.addAll(ts);
                            //升序排序
                            Collections.sort(sts, new Comparator<ColumsConf>() {
                                public int compare(ColumsConf s1, ColumsConf s2) {
                                    /**
                                     * 升序排的话就是第一个参数.compareTo(第二个参数);
                                     * 降序排的话就是第二个参数.compareTo(第一个参数);
                                     */
                                    return s1.getColIndex().compareTo(s2.getColIndex());
                                }
                            });
                            //获取最大colIndex
                            Integer size = 0;
                            //遍历
                            for (ColumsConf conf: sts) {
                                if(conf.getColIndex() > size){
                                    size = conf.getColIndex();
                                }
                            }
                            //读数据方向,水平:horizontal,垂直:vertical
                            if("horizontal".equals(sh.getReadOrientation())){
                                //数据载体
                                List<String> data = new ArrayList<String>();
                                //遍历
                                A:for(int p = 0 ; p <= size ; p ++){
                                    //内容
                                    String colName = "";
                                    //遍历
                                    B:for (ColumsConf conf: sts) {
                                        if(p == conf.getColIndex()){
                                            //默认空字符串
                                            if(StringUtils.isEmpty(conf.getColName())){
                                                conf.setColName("");
                                            }
                                            colName = conf.getColName();
                                            break B;
                                        }
                                    }
                                    data.add(colName);
                                }
                                datas.add(data);
                            }else{
                                //遍历
                                for(int p = 0 ; p <= size ; p ++){
                                    //数据载体
                                    List<String> data = new ArrayList<String>();
                                    //内容
                                    String colName = "";
                                    //遍历
                                    B:for (ColumsConf conf: sts) {
                                        if(p == conf.getColIndex()){
                                            //默认空字符串
                                            if(StringUtils.isEmpty(conf.getColName())){
                                                conf.setColName("");
                                            }
                                            colName = conf.getColName();
                                            break B;
                                        }
                                    }
                                    data.add(colName);
                                    datas.add(data);
                                }
                            }
                        }
                        //判断不为执行
                        if(datas != null && datas.size() > 0){
                            for(int x = 0 ; x < datas.size() ; x ++){
                                //获取行
                                Row row = sheet.getRow(x);
                                if(row == null){
                                    row = sheet.createRow(x);
                                }
                                //获取数据
                                List<String> cellVals = (List<String>) datas.get(x);
                                //判断数据不为空执行
                                if(cellVals != null && cellVals.size() > 0){
                                    //遍历单元格
                                    for(int y = 0 ; y < cellVals.size() ; y ++){
                                        //获取单元格
                                        Cell cell = row.getCell((short)(y));
                                        if(cell == null){
                                            cell = row.createCell((short)(y));
                                        }
                                        //获取单元格内容
                                        Object val = cellVals.get(y);
                                        //判断不为空执行
                                        if(val != null){
                                            //设置单元格数据类型
                                            if(val instanceof Short || val instanceof Integer
                                                    || val instanceof Float || val instanceof Double){
                                                cell.setCellType(CellType.NUMERIC);
                                            }else if(val instanceof Boolean){
                                                cell.setCellType(CellType.BOOLEAN);
                                            }else{
                                                cell.setCellType(CellType.STRING);
                                            }
                                            //设置单元格值
                                            setCellValue(cell,cell.getCellTypeEnum(),val);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    //文件输出流
                    FileOutputStream out = new FileOutputStream(file);
                    if(wb != null){
                        wb.write(out);
                        res = true;
                    }
                    //关闭流
                    if(out != null){
                        out.close();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return res;
    }

    /**
     * 写入Excel
     * @param sheetName-工作空间名称
     * @param descPath-生成的文件路径
     * @param vals-写入值
     */
    public static void writeExcel(String sheetName,String descPath,List<List<Object>> vals) throws Exception{
        //根据模板写入Excel
        writeExcel(sheetName,descPath,null,null,vals);
    }

    /**
     * 根据模板写入Excel
     * @param modelFilePath-模板路径
     * @param sheetName-工作空间名称
     * @param descPath-生成的文件路径
     * @param vals-写入值
     */
    public static void writeExcel(String modelFilePath,String sheetName,String descPath,List<List<Object>> vals) throws Exception{
        //根据模板写入Excel
        writeExcel(modelFilePath,sheetName,descPath,null,null,vals);
    }

    /**
     * 根据模板写入Excel
     * @param modelFile-模板文件
     * @param sheetName-工作空间名称
     * @param descFile-生成的文件
     * @param vals-写入值
     */
    public static void writeExcel(File modelFile,String sheetName,File descFile,List<List<Object>> vals) throws Exception{
        //根据模板写入Excel
        writeExcel(modelFile,sheetName,descFile,null,null,vals);
    }

    /**
     * 根据模板写入Excel
     * @param modelFilePath-模板路径
     * @param sheetName-工作空间名称
     * @param descPath-生成的文件路径
     * @param startRow-开始行
     * @param startCol-开始列
     * @param vals-写入值
     */
    public static void writeExcel(String modelFilePath,String sheetName,String descPath,Integer startRow,Integer startCol,List<List<Object>> vals) throws Exception{
        //检查文件路径不为空
        Assert.notNull(modelFilePath,"模板路径不能为空");
        //检查工作空间名称不为空
        Assert.notNull(sheetName,"sheetName工作空间名称不能为空");
        //检查文件路径不为空
        Assert.notNull(descPath,"生成的文件路径不能为空");
        //建立输入流-模板路径
        FileInputStream in = new FileInputStream(modelFilePath);
        //保存为Excel文件
        File outFile = new File(descPath);
        if(!outFile.getParentFile().exists()){
            if(outFile.getParentFile().mkdirs()){
                outFile.getParentFile().mkdir();
            }
        }
        if(!outFile.exists()){
            outFile.createNewFile();
        }
        FileOutputStream out = new FileOutputStream(descPath);
        //根据模板写入Excel
        writeExcel(in,sheetName,out,startRow,startCol,vals);
        if(in != null){
            in.close();
        }
        if(out != null){
            out.close();
        }
    }

    /**
     * 根据模板写入Excel
     * @param modelFile-模板文件
     * @param sheetName-工作空间名称
     * @param descFile-生成的文件
     * @param startRow-开始行
     * @param startCol-开始列
     * @param vals-写入值
     */
    public static void writeExcel(File modelFile,String sheetName,File descFile,Integer startRow,Integer startCol,List<List<Object>> vals) throws Exception{
        //检查文件不为空
        Assert.notNull(modelFile,"模板文件不能为空");
        //检查工作空间名称不为空
        Assert.notNull(sheetName,"sheetName工作空间名称不能为空");
        //检查文件不为空
        Assert.notNull(descFile,"生成的文件不能为空");
        //建立输入流-模板路径
        FileInputStream in = new FileInputStream(modelFile);
        //保存为Excel文件
        if(!descFile.getParentFile().exists()){
            if(descFile.getParentFile().mkdirs()){
                descFile.getParentFile().mkdir();
            }
        }
        if(!descFile.exists()){
            descFile.createNewFile();
        }
        FileOutputStream out = new FileOutputStream(descFile);
        //根据模板写入Excel
        writeExcel(in,sheetName,out,startRow,startCol,vals);
        if(in != null){
            in.close();
        }
        if(out != null){
            out.close();
        }
    }

    /**
     * 根据模板写入Excel
     * @param sheetName-工作空间名称
     * @param descFilePath-生成的文件流
     * @param startRow-开始行
     * @param startCol-开始列
     * @param vals-写入值
     */
    public static void writeExcel(String sheetName,String descFilePath,Integer startRow,Integer startCol,List<List<Object>> vals) throws Exception{
        //检查文件流不为空
        Assert.notNull(descFilePath,"Excel生成的文件流不能为空");
        //默认值
        if(startRow == null){
            startRow = 0;
        }
        //默认值
        if(startCol == null){
            startCol = 0;
        }
        //文件类型
        String fileType = getFileType(descFilePath);
        //判断是Excel文件
        if("xls".equals(fileType) || "xlsx".equals(fileType)){
            //获取Excel文件Workbook对象
            Workbook wb = newWorkbook(fileType);
            //工作空间
            Sheet sheet = wb.getSheet(sheetName);
            //判断数据不为空执行
            if(vals != null && vals.size() > 0){
                for(int x = 0 ; x < vals.size() ; x ++){
                    //获取行
                    Row row = sheet.getRow(startRow+x);
                    if(row == null){
                        row = sheet.createRow(startRow+x);
                    }
                    //获取数据
                    List<Object> cellVals = vals.get(x);
                    //判断数据不为空执行
                    if(cellVals != null && cellVals.size() > 0){
                        //遍历单元格
                        for(int y = 0 ; y < cellVals.size() ; y ++){
                            //获取单元格
                            Cell cell = row.getCell((short)(startCol+y));
                            if(cell == null){
                                cell = row.createCell((short)(startCol+y));
                            }
                            Object val = null;
                            //判断数据类型是否是Map类型执行
                            if(cellVals.get(y) instanceof Map){
                                Map map = (Map) cellVals.get(y);
                                //判断不为空不根据键
                                if(map != null && map.isEmpty()){
                                    //获取数据
                                    if(map.containsKey("data")){
                                        val = map.get("data");
                                    }
                                    //获取数据
                                    if(map.containsKey("style")){
                                        CellStyle style = (CellStyle) map.get("style");
                                        if(style != null){
                                            cell.setCellStyle(style);
                                        }
                                    }
                                }
                            }else{
                                val = cellVals.get(y);
                            }
                            //判断不为空执行
                            if(val != null){
                                //设置单元格数据类型
                                if(val instanceof Short || val instanceof Integer
                                        || val instanceof Float || val instanceof Double){
                                    cell.setCellType(CellType.NUMERIC);
                                }else if(val instanceof Boolean){
                                    cell.setCellType(CellType.BOOLEAN);
                                }else{
                                    cell.setCellType(CellType.STRING);
                                }
                                //设置单元格值
                                setCellValue(cell,cell.getCellTypeEnum(),val);
                            }
                        }
                    }
                }
            }
            FileOutputStream out = new FileOutputStream(descFilePath);
            wb.write(out);
            //关闭流
            if(out != null){
                out.close();
            }
        }
    }

    /**
     * 根据模板写入Excel
     * @param modelFile-模板文件流
     * @param sheetName-工作空间名称
     * @param descFile-生成的文件流
     * @param startRow-开始行
     * @param startCol-开始列
     * @param vals-写入值
     */
    public static void writeExcel(FileInputStream modelFile,String sheetName,FileOutputStream descFile,Integer startRow,Integer startCol,List<List<Object>> vals) throws Exception{
        //检查文件流不为空
        Assert.notNull(modelFile,"Excel模板文件流不能为空");
        //检查文件流不为空
        Assert.notNull(descFile,"Excel生成的文件流不能为空");
        //默认值
        if(startRow == null){
            startRow = 0;
        }
        //默认值
        if(startCol == null){
            startCol = 0;
        }
        //获取Excel文件Workbook对象
        Workbook wb = WorkbookFactory.create(modelFile);
        //工作空间
        Sheet sheet = wb.getSheet(sheetName);
        //判断数据不为空执行
        if(vals != null && vals.size() > 0){
            for(int x = 0 ; x < vals.size() ; x ++){
                //获取行
                Row row = sheet.getRow(startRow+x);
                if(row == null){
                    row = sheet.createRow(startRow+x);
                }
                //获取数据
                List<Object> cellVals = vals.get(x);
                //判断数据不为空执行
                if(cellVals != null && cellVals.size() > 0){
                    //遍历单元格
                    for(int y = 0 ; y < cellVals.size() ; y ++){
                        //获取单元格
                        Cell cell = row.getCell((short)(startCol+y));
                        if(cell == null){
                            cell = row.createCell((short)(startCol+y));
                        }
                        Object val = null;
                        //判断数据类型是否是Map类型执行
                        if(cellVals.get(y) instanceof Map){
                            Map map = (Map) cellVals.get(y);
                            //判断不为空不根据键
                            if(map != null && map.isEmpty()){
                                //获取数据
                                if(map.containsKey("data")){
                                    val = map.get("data");
                                }
                                //获取数据
                                if(map.containsKey("style")){
                                    CellStyle style = (CellStyle) map.get("style");
                                    if(style != null){
                                        cell.setCellStyle(style);
                                    }
                                }
                            }
                        }else{
                            val = cellVals.get(y);
                        }
                        //判断不为空执行
                        if(val != null){
                            //设置单元格数据类型
                            if(val instanceof Short || val instanceof Integer
                                    || val instanceof Float || val instanceof Double){
                                cell.setCellType(CellType.NUMERIC);
                            }else if(val instanceof Boolean){
                                cell.setCellType(CellType.BOOLEAN);
                            }else{
                                cell.setCellType(CellType.STRING);
                            }
                            //设置单元格值
                            setCellValue(cell,cell.getCellTypeEnum(),val);
                        }
                    }
                }
            }
        }
        wb.write(descFile);
        //关闭流
        if(modelFile != null){
            modelFile.close();
        }
        if(descFile != null){
            descFile.close();
        }
    }

    /**
     * 将矩阵转置
     * @param temp
     */
    public static void reverse(Object temp [][]) {
        for (int i = 0; i < temp.length; i++) {
            for (int j = i; j < temp[i].length; j++) {
                Object obj = temp[i][j] ;
                temp[i][j] = temp[j][i] ;
                temp[j][i] = obj ;
            }
        }
    }

    /**
     * 将矩阵转置
     * @param datas 源数据
     */
    private static List<Object> reverse(List<Object> datas) {
        //结果数据
        List<Object> res = new ArrayList<Object>();
        //判断不为空执行
        if(datas != null && datas.size() > 0){
            //获取源数据行数
            Integer n = datas.size();
            //获取源数据列数
            Integer m = 0;
            //遍历获取最大列数
            for(Object obj : datas){
                //获取行数据
                List<Object> rows = (List<Object>) obj;
                if(rows != null && rows.size() > m){
                    m = rows.size();
                }
            }
            //遍历翻转
            for(int i = 0 ; i < m ; i ++){
                //翻转后数据集
                List<Object> rows = new ArrayList<Object>();
                //遍历
                for(int x = 0 ; x < n ; x ++){
                    //获取行数据
                    List<Object> rs = (List<Object>) datas.get(x);
                    Object row = null;
                    //判断不为空执行
                    if(rs != null && rs.size() > 0){
                        if(rs.size() > i){
                            row = rs.get(i);
                        }
                    }
                    rows.add(row);
                }
                res.add(rows);
            }
        }
        return res;
    }

    /**
     * 获取单元格的值
     * @param cell
     * @return
     */
    public static Object getCellValue(Cell cell) {
        if (cell == null) {return "";}
        Object obj = null;
        switch (cell.getCellTypeEnum()) {
            case BOOLEAN:
                obj = cell.getBooleanCellValue();
                break;
            case ERROR:
                obj = cell.getErrorCellValue();
                break;
            case NUMERIC:
                DataFormatter formatter = new DataFormatter();
                String retValue = formatter.formatCellValue(cell);
                //判断不为空执行
                if(!StringUtils.isEmpty(retValue)){
                    //判断是时间格式数据
                    if(DateUtil.isCellDateFormatted(cell) || DateUtil.isCellInternalDateFormatted(cell)){
                        obj = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cell.getDateCellValue());
                    }else if(isInteger(retValue) || isDouble(retValue)){
                        BigDecimal bd = new BigDecimal(cell.getNumericCellValue());
                        String value = String.valueOf(bd);
                        //小数点后值不都是零标识
                        boolean eqZore = true;
                        if(value.indexOf(".")>0){
                            String px = "0"+value.substring(value.indexOf("."));
                            BigDecimal pxbd = new BigDecimal(px);
                            if(pxbd.doubleValue() > 0){
                                eqZore = false;
                            }
                        }
                        if(eqZore){
                            if(value.indexOf(".")>0){value=value.substring(0,value.indexOf("."));}
                            if(value.length() > 9){
                                obj = Long.parseLong(value);
                            }else{
                                obj = Integer.parseInt(value);
                            }
                        }else{
                            obj = bd.doubleValue();
                        }
                    }else{
                        obj = retValue;
                    }
                }else{
                    obj = "";
                }
                break;
            case STRING:
                obj = String.valueOf(cell.getStringCellValue());
                break;
            default:
                try {
                    obj = String.valueOf(cell.getStringCellValue());
                } catch (IllegalStateException e) {
                    obj = "";
                }
                break;
        }
        return obj;
    }
    /**
     * 修改单元格的值
     * @param cell
     * @return
     */
    public static void setCellValue(Cell cell, CellType type, Object val) {
        switch (type) {
            case BOOLEAN:
                cell.setCellValue((Boolean) val);
                break;
            case NUMERIC:
                String value = String.valueOf(val);
                if(isInteger(value) || isDouble(value)){
                    //小数点后值不都是零标识
                    boolean eqZore = true;
                    if(value.indexOf(".")>0){
                        String px = "0"+value.substring(value.indexOf("."));
                        BigDecimal pxbd = new BigDecimal(px);
                        if(pxbd.doubleValue() > 0){
                            eqZore = false;
                        }
                    }
                    if(eqZore){
                        if(value.indexOf(".")>0){value=value.substring(0,value.indexOf("."));}
                        if(value.length() > 9){
                            cell.setCellValue(Long.parseLong(value));
                        }else{
                            cell.setCellValue(Integer.parseInt(value));
                        }
                    }else{
                        BigDecimal pxbd = new BigDecimal(value);
                        cell.setCellValue(pxbd.doubleValue());
                    }
                }else{
                    cell.setCellValue((String) val);
                }
                break;
            case STRING:
                cell.setCellValue((String) val);
                break;
            default:
                cell.setCellValue((String) "");
                break;
        }
    }

    /**
     * 根据类型创建Workbook
     * @param fileType
     * @return
     */
    public static Workbook newWorkbook(String fileType){
        //获取Excel文件Workbook对象
        Workbook wb = null;
        if("xls".equals(fileType)){
            wb = new HSSFWorkbook();
        }else if("xlsx".equals(fileType)){
            wb = new XSSFWorkbook();
        }
        return wb;
    }

    /**
     * 判断是integer类型
     * @param obj
     * @return
     */
    public static boolean isInteger(Object obj){
        if(obj instanceof Integer || obj instanceof Long || (String.valueOf(obj)).matches("[0-9]+")){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 判断字符串是否为数字
     * @param obj
     * @return
     */
    public static boolean isDouble(Object obj) {
        if (null == obj || "".equals(String.valueOf(obj))) {
            return false;
        }
        if(obj instanceof Double){
            return true;
        }else{
            String str = String.valueOf(obj);
            if(str.indexOf(".") > -1){
                String im = str.substring(0, str.indexOf("."));
                if(im.length() > 9){
                    return false;
                }
            }else if(str.length() > 9){
                return false;
            }
            Pattern pattern = Pattern.compile("^[-\\+]?[.\\d]*$");
            return pattern.matcher(str).matches();
        }
    }

    /**
     * @param obj 待校验的邮箱地址
     * @return
     */
    public static boolean isEmailValid(Object obj) {
        boolean flag = false;
        try {
            String email = (String) obj;
            String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
            Pattern regex = Pattern.compile(check);
            Matcher matcher = regex.matcher(email);
            flag = matcher.matches();
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }

    /**
     * 验证字符串为有效时间
     * @param obj
     * @return
     */
    public static boolean isValidDate(Object obj,String formatStr) {
        String str = String.valueOf(obj);
        boolean convertSuccess = true;
        // 指定日期格式为四位年/两位月份/两位日期，注意yyyy/MM/dd区分大小写；
        str = str.replaceAll("/", "-").replaceAll("[.]", "-").replaceAll("\\\\", "-");
        if(StringUtils.isEmpty(formatStr)){
            formatStr = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat format = new SimpleDateFormat(formatStr);
        try {
            // 设置lenient为false. 否则SimpleDateFormat会比较宽松地验证日期，比如2007/02/29会被接受，并转换成2007/03/01
            format.setLenient(false);
            format.parse(str);
            convertSuccess = true;
        } catch (ParseException e) {
            // 如果throw java.text.ParseException或者NullPointerException，就说明格式不对
            convertSuccess=false;
        }
        return convertSuccess;
    }
}
