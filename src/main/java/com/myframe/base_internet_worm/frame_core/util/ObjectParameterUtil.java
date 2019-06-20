package com.myframe.base_internet_worm.frame_core.util;

import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Object工具类
 */
public class ObjectParameterUtil {
    /**
     * 获取所有属性
     * @param object
     * @return
     */
    public static Field[] getAllFields(Object object){
        Class clazz = object.getClass();
        List<Field> fieldList = new ArrayList<Field>();
        while (clazz != null){
            fieldList.addAll(new ArrayList<Field>(Arrays.asList(clazz.getDeclaredFields())));
            clazz = clazz.getSuperclass();
        }
        Field[] fields = new Field[fieldList.size()];
        fieldList.toArray(fields);
        return fields;
    }
    /**
     * 获取属性值
     * @param ob 对象
     * @param fieldName 属性名
     */
    public static Object getFieldVal(Object ob,String fieldName){
        Object val = null;
        try {
            Field field = null;
            Field[] fields = getAllFields(ob);
            if(fields != null && fields.length > 0){
                for(int i = 0 ; i < fields.length ; i ++){
                    if(fieldName.toLowerCase().equals(fields[i].getName().toLowerCase())){
                        field = fields[i];
                        break;
                    }
                }
            }
            if(field != null){
                field.setAccessible(true); //设置些属性是可以访问的
                val = field.get(ob);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return val;
    }

    /**
     * 判断类是否含有该属性
     * @param obj
     * @param fieldname
     * @return
     */
    public static boolean hasField(Object obj,String fieldname){
        //含有标识
        boolean hased = false;
        //循环遍历所有的元素，检测有没有这个名字
        Field[] fields = getAllFields(obj);
        for (int i = 0; i < fields.length; i++) {
            if(fields[i].getName().equals(fieldname)){
                hased = true;
                break;
            }
        }
        return hased;
    }

    /**
     * 从obj中获取对象
     * @param obj
     * @param keyFields
     * @return
     */
    public static Object getValOnObj(Object obj,String keyFields){
        Object val = null;
        try {
            //判断不为空执行
            if (obj != null && (keyFields != null && keyFields.trim().length() > 0)) {
                keyFields = keyFields.trim();
                //下标组
                List<String> pIs = new ArrayList<String>();
                //判断是否多级
                if (keyFields.indexOf(".") > -1) {
                    //分割
                    String[] ks = keyFields.split("[.]");
                    //判断不为空执行
                    if(ks != null && ks.length > 0){
                        //临时对象
                        Object tmp = null;
                        //遍历
                        for(int i = 0 ; i < ks.length ; i ++){
                            if(i == 0){
                                tmp = getValOnObj(obj,ks[i]);
                            }else if(i > 0 && i < ks.length - 1){
                                tmp = getValOnObj(tmp,ks[i]);
                            }else{
                                val = getValOnObj(tmp,ks[i]);
                            }
                        }
                    }
                }else{
                    //指定下标标识
                    boolean pi = false;
                    //临时对象
                    Object tmp = null;
                    //判断是否包含
                    if(keyFields.trim().indexOf("[") > -1 && keyFields.trim().indexOf("]") > -1){
                        //临时key
                        String tmpKeyFields = keyFields.substring(0,keyFields.trim().indexOf("["));
                        //获取下标组
                        pIs = getValues(keyFields,"\\[","]");
                        //判断不为空执行
                        if(pIs != null && pIs.size() > 0){
                            //指定下标标识
                            pi = true;
                            //判断不为空执行
                            if(!StringUtils.isEmpty(tmpKeyFields)){
                                tmp = getValOnObj(obj,tmpKeyFields);
                            }else{
                                tmp = obj;
                            }
                        }
                    }else{
                        tmp = obj;
                    }
                    if(tmp instanceof List){
                        //原始数据转换
                        List base = (List)tmp;
                        //判断不为空执行
                        if(base != null && base.size() > 0){
                            //判断是否指定下标并是有效下标
                            if(pi){
                                //先取第一级
                                String key = pIs.get(0);
                                //判断是数字
                                if(isNumber(key)){
                                    //将字符串转换成下标
                                    Integer index = Integer.valueOf(key);
                                    //判断数据长度是否有效
                                    if(base.size() > index){
                                        //判断是多维数组
                                        if(pIs.size() > 1){
                                            //临时对象
                                            Object tp = base.get(index);
                                            //从第二级开始逐级取值
                                            for(int i = 1 ; i < pIs.size() ; i ++){
                                                if(i < pIs.size() - 1){
                                                    tp = getValOnObj(tp,"["+pIs.get(i)+"]");
                                                }else{
                                                    val = getValOnObj(tp,"["+pIs.get(i)+"]");
                                                }
                                            }
                                        }else{
                                            val = base.get(index);
                                        }
                                    }
                                }
                            }else{
                                List list = new ArrayList();
                                //遍历
                                for(Object o : base){
                                    Object tv = getValOnObj(o,keyFields);
                                    list.add(tv);
                                }
                                val = list;
                            }
                        }
                    }else if(obj instanceof Map){
                        val = ((Map) obj).get(keyFields);
                    }else if(hasField(obj,keyFields)){
                        val = getFieldVal(obj,keyFields);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return val;
    }

    /**
     * 判断是否为数字格式不限制位数
     * @param o 待校验参数
     * @return 如果全为数字，返回true；否则，返回false
     */
    public static boolean isNumber(Object o){
        return  (Pattern.compile("[0-9]*")).matcher(String.valueOf(o)).matches();
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

}
