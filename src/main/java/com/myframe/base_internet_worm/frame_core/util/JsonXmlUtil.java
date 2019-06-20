package com.myframe.base_internet_worm.frame_core.util;


import com.alibaba.fastjson.JSON;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Json与XML工具类
 * @author chenqc
 * 
 */
public class JsonXmlUtil {
	
	/**
	 * 对象转json
	 * @param src
	 * @return
	 */
	public static String toJson(Object src) {  
       if (src == null) {
           return "{}";
       }  
       return JSON.toJSONString(src);
	}
	
	/***
     * JSON 转换为 List
     * @param jsonStr
     * @param objectClass 链表中元素类型
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> json2List(String jsonStr, Class<T> objectClass){
        if (!StringUtils.isEmpty(jsonStr)) {
            List<T> list = JSON.parseArray(jsonStr, objectClass);
            return list;
        }
        return null;
    }
    
    /***
     * JSON 转 Object
     * @param jsonStr
     * @param objectClass 目标类型
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T json2Ojbect(String jsonStr,Class<T> objectClass){
        if(null != jsonStr){
            String leftStr = jsonStr.substring(0,2);
            String rightStr = jsonStr.substring(jsonStr.length()-2,jsonStr.length());
            if(leftStr.equals("[{")){
                jsonStr = jsonStr.substring(1,jsonStr.length());
            }
            if(rightStr.equals("}]")){
                jsonStr = jsonStr.substring(0,jsonStr.length()-1);
            }
            return JSON.parseObject(jsonStr, objectClass);
        }
        return null;
    }
    
    /** 
    * 判断是否是json结构 
    */ 
    public static boolean isJson(String value) {
        boolean res = false;
	    try {
	        if(!StringUtils.isEmpty(value)){
                value = value.trim();
                if(value.startsWith("[")){
                    res = JSON.isValidArray(value);
                }else if(value.startsWith("{")){
                    res = JSON.isValidObject(value);
                }
            }
	    } catch (Exception e) {}
        return res;
    }
     
    /** 
    * 判断是否是xml结构 
    */ 
    public static boolean isXML(String value) { 
	    try { 
	    	DocumentHelper.parseText(value); 
	    } catch (DocumentException e) { 
	    	return false; 
	    } 
	    return true; 
    }
    
    /**
     * 将map参数对象转成xml格式
     * @param map
     * @param prefix
     * @param nameSpace
     * @param operation
     * @return
     */
    public static String map2SoapXml(Map<String,String> map,String prefix,String nameSpace,String operation){
    	StringBuffer xml = new StringBuffer();
    	//判断不为空执行
    	if(!StringUtils.isEmpty(operation)){
    		xml.append("<"+(!StringUtils.isEmpty(prefix)?(prefix+":"):"")+operation+"");
    		//判断不为空执行
    		if(!StringUtils.isEmpty(nameSpace)){
    			xml.append(" xmlns"+(!StringUtils.isEmpty(prefix)?(":"+prefix):"")+"=\""+nameSpace+"\"");
    		}
    		xml.append(">");
    	}
    	//判断map不为空执行
    	if(map != null){
    		//遍历map
    		for (Map.Entry<String,String> entry : map.entrySet()) {
    			//判断不为空执行
    			if(!StringUtils.isEmpty(entry.getKey())){
    				//判断包含.符合
    				if(entry.getKey().indexOf(".") > -1){
    					//割取.
    					String[] names = entry.getKey().split("[.]");
    					//判断不为空执行遍历
    					if(names != null && names.length > 0){
    						//遍历
    						for(int i = 0 ; i < names.length ; i ++){
    							xml.append("<"+(!StringUtils.isEmpty(prefix)?(prefix+":"):"")+names[i]+">");
    						}
    						//判断是否有特殊字符
    						if(hasXmlSymbol(entry.getValue())){
    							xml.append("<![CDATA["+entry.getValue()+"]]>");
    						}else{
    							xml.append(entry.getValue());
    						}
    						//遍历
    						for(int i = names.length - 1 ; i >= 0 ; i --){
    							xml.append("</"+(!StringUtils.isEmpty(prefix)?(prefix+":"):"")+names[i]+">");
    						}
    					}
    				}else{
    					xml.append("<"+(!StringUtils.isEmpty(prefix)?(prefix+":"):"")+entry.getKey()+">");
    					//判断不为空执行
    					if(!StringUtils.isEmpty(entry.getValue())){
    						//判断是否有特殊字符
    						if(hasXmlSymbol(entry.getValue())){
    							xml.append("<![CDATA["+entry.getValue()+"]]>");
    						}else{
    							xml.append(entry.getValue());
    						}
    					}
    					xml.append("</"+(!StringUtils.isEmpty(prefix)?(prefix+":"):"")+entry.getKey()+">");
    				}
    			}
		    }
    	}
    	//判断不为空执行
    	if(!StringUtils.isEmpty(operation)){
    		xml.append("</"+(!StringUtils.isEmpty(prefix)?(prefix+":"):"")+operation+">");
    	}
    	return xml.toString();
    }
    
    /**
     * 判断是否包含特殊xml符号
     * @param val
     * @return
     */
    public static boolean hasXmlSymbol(String val){
    	if(val.indexOf("&") > -1 ||
    			val.indexOf("<") > -1 ||
    			val.indexOf(">") > -1 ||
    			val.indexOf("'") > -1 ||
    			val.indexOf("\"") > -1){
    		return true;
		}else{
			return false;
		}
    }

    /**
     * 将Document对象转为Map（String→Document→Map）
     * @param doc
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static Map<String, Object> Dom2Map(Document doc){
        Map<String, Object> map = new HashMap<String, Object>();
        if(doc == null)
            return map;
        Element root = doc.getRootElement();
        for (Iterator iterator = root.elementIterator(); iterator.hasNext();) {
            Element e = (Element) iterator.next();
            List list = e.elements();
            if(list.size() > 0){
                map.put(e.getName(), Dom2Map(e));
            }else
                map.put(e.getName(), e.getText());
        }
        return map;
    }

    /**
     * 将Element对象转为Map（String→Document→Element→Map）
     * @param e
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Map Dom2Map(Element e){
        Map map = new HashMap();
        List list = e.elements();
        if(list.size() > 0){
            for (int i = 0;i < list.size(); i++) {
                Element iter = (Element) list.get(i);
                List mapList = new ArrayList();
                if(iter.elements().size() > 0){
                    Map m = Dom2Map(iter);
                    if(map.get(iter.getName()) != null){
                        Object obj = map.get(iter.getName());
                        if(!obj.getClass().getName().equals("java.util.ArrayList")){
                            mapList = new ArrayList();
                            mapList.add(obj);
                            mapList.add(m);
                        }
                        if(obj.getClass().getName().equals("java.util.ArrayList")){
                            mapList = (List) obj;
                            mapList.add(m);
                        }
                        map.put(iter.getName(), mapList);
                    }else
                        map.put(iter.getName(), m);
                }
                else{
                    if(map.get(iter.getName()) != null){
                        Object obj = map.get(iter.getName());
                        if(!obj.getClass().getName().equals("java.util.ArrayList")){
                            mapList = new ArrayList();
                            mapList.add(obj);
                            mapList.add(iter.getText());
                        }
                        if(obj.getClass().getName().equals("java.util.ArrayList")){
                            mapList = (List) obj;
                            mapList.add(iter.getText());
                        }
                        map.put(iter.getName(), mapList);
                    }else
                        map.put(iter.getName(), iter.getText());//公共map resultCode=0
                }
            }
        }else
            map.put(e.getName(), e.getText());
        return map;
    }

    /**
     * xml转换成map
     * @param str
     * @return
     */
    public static Map xmltoMap(String str){
        Map map = null;
        try {
            Document document = DocumentHelper.parseText(str);
            map = Dom2Map(document);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return map;
    }
    
    public static void main(String[] args) {
    	Map map = new HashMap();
    	map.put("text", "text");
    	String xml = map2SoapXml(map, "wed", "http://www.baidu.com", "getCity");
        System.out.println(xml);
        Map mp = xmltoMap(xml);
        System.out.println("map>>> " + toJson(mp));
	}
}
