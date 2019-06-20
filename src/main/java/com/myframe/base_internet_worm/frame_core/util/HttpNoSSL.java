package com.myframe.base_internet_worm.frame_core.util;

import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 绕过认证
 */
public class HttpNoSSL {

    /**
     * 获取ResponseHandler<String>
     * @return
     */
    private static ResponseHandler<String> getHandler(){
        // Create a custom response handler
        ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
            @Override
            public String handleResponse(
                    final HttpResponse response) throws ClientProtocolException, IOException {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            }
        };
        return responseHandler;
    }

    /**
     * 参数处理
     * @param params
     * @param map
     */
    private static void mapToNameValuePair(List<NameValuePair> params,Map<String,String> map){
        if(map != null){
            // 遍历方法二
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String key = entry.getKey().toString();
                String value = entry.getValue().toString();
                params.add(new BasicNameValuePair(key, value));
            }
        }
    }
    /**
     * 绕过验证
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    public static SSLContext createIgnoreVerifySSL() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sc = SSLContext.getInstance("SSLv3");
        // 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
        X509TrustManager trustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                    String paramString) throws CertificateException {
            }
            @Override
            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                    String paramString) throws CertificateException {
            }
            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
        sc.init(null, new TrustManager[] { trustManager }, null);
        return sc;
    }

    /**
     * get请求
     * @param url
     * @param headersMap - 请求头
     * @return
     * @throws IOException
     */
    public static String get(String url,Map<String,String> headersMap) throws IOException{
        String responseBody = "";
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpGet httpget = new HttpGet(url);
            //设置请求头
            httpget.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:6.0.2) Gecko/20100101 Firefox/6.0.2");
            httpget.setHeader("Referer", url);
            //判断不为空执行
            if(headersMap != null && !headersMap.isEmpty()){
                //遍历
                for (Map.Entry<String, String> entry : headersMap.entrySet()) {
                    //设置请求头
                    httpget.setHeader(entry.getKey(), entry.getValue());
                }
            }
            // Create a custom response handler
            ResponseHandler<String> responseHandler = getHandler();
            responseBody = httpclient.execute(httpget, responseHandler);
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            httpclient.close();
        }
        return responseBody;
    }

    /**
     * 请求下载文件
     * @param url
     * @param headersMap - 请求头
     * @return
     * @throws IOException
     */
    public static String downloadFile(String url,String fileDirOrPath,Map<String,String> headersMap) throws IOException{
        //文件路径
        String filePath = "";
        Assert.notNull(url,"url must is not null");
        Assert.notNull(fileDirOrPath,"fileDirOrPath must is not null");
        //请求客户端对象
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpGet httpget = new HttpGet(url);
            //设置请求头
            httpget.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:6.0.2) Gecko/20100101 Firefox/6.0.2");
            httpget.setHeader("Referer", url);
            //判断不为空执行
            if(headersMap != null && !headersMap.isEmpty()){
                //遍历
                for (Map.Entry<String, String> entry : headersMap.entrySet()) {
                    //设置请求头
                    httpget.setHeader(entry.getKey(), entry.getValue());
                }
            }
            HttpResponse response = httpclient.execute(httpget);
            filePath = readFile(url,fileDirOrPath,response);
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            httpclient.close();
        }
        return filePath;
    }

    //读取文件并保存
    public static String readFile(String url,String fileDirOrPath,HttpResponse response) throws IOException {
        //文件路径
        String filePath = "";
        HttpEntity entity = response.getEntity();
        InputStream is = entity.getContent();
        //输出流
        FileOutputStream fileout = null;
        //创建文件对象
        File file = new File(fileDirOrPath);
        if (!file.isFile()) {
            if(!file.exists()){
                file.mkdirs();
            }
            //获取文件名
            String fileName = getFileName(response);
            //判断为空执行
            if(StringUtils.isEmpty(fileName)){
                if(url.indexOf("/") > -1){
                    fileName = url.substring(url.lastIndexOf("/")+1);
                    if(fileName.indexOf("?") > -1){
                        fileName = fileName.substring(0,fileName.indexOf("?"));
                    }
                }
            }
            //判断为空执行
            if(StringUtils.isEmpty(fileName)){
                fileName = String.valueOf(System.currentTimeMillis());
            }
            filePath = fileDirOrPath+"/"+fileName;
            fileout = new FileOutputStream(filePath);
        }else{
            if(!file.getParentFile().exists()){
                file.getParentFile().mkdirs();
            }
            filePath = fileDirOrPath;
            fileout = new FileOutputStream(file);
        }
        //根据实际运行效果 设置缓冲区大小
        byte[] buffer = new byte[10 * 1024];
        int ch = 0;
        while ((ch = is.read(buffer)) != -1) {
            fileout.write(buffer,0,ch);
        }
        is.close();
        fileout.flush();
        fileout.close();
        return filePath;
    }

    /**
     * 获取response header中Content-Disposition中的filename值
     * @param response
     * @return
     */
    public static String getFileName(HttpResponse response) {
        Header contentHeader = response.getFirstHeader("Content-Disposition");
        String filename = null;
        if (contentHeader != null) {
            HeaderElement[] values = contentHeader.getElements();
            if (values.length == 1) {
                NameValuePair param = values[0].getParameterByName("filename");
                if (param != null) {
                    try {
                        filename = param.getValue();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return filename;
    }

    /**
     * post请求
     * @param url
     * @param headersMap - 请求头
     * @param paramsMap - 请求参数
     * @return
     * @throws IOException
     */
    public static String post(String url,Map<String,String> headersMap,Map<String,String> paramsMap) throws IOException{
        String responseBody = "";
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            //判断不为空执行
            if(paramsMap != null && paramsMap.size() > 0){
                //参数处理
                mapToNameValuePair(params,paramsMap);
            }
            HttpEntity reqEntity = new UrlEncodedFormEntity(params, "utf-8");
            //请求配置
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(5000)//一、连接超时：connectionTimeout-->指的是连接一个url的连接等待时间
                    .setSocketTimeout(5000)// 二、读取数据超时：SocketTimeout-->指的是连接上一个url，获取response的返回等待时间
                    .setConnectionRequestTimeout(5000)
                    .build();
            HttpPost post = new HttpPost(url);
            //设置请求头
            post.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:6.0.2) Gecko/20100101 Firefox/6.0.2");
            post.setHeader("Referer", url);
            //判断不为空执行
            if(headersMap != null && !headersMap.isEmpty()){
                //遍历
                for (Map.Entry<String, String> entry : headersMap.entrySet()) {
                    //设置请求头
                    post.setHeader(entry.getKey(), entry.getValue());
                }
            }
            post.setEntity(reqEntity);
            post.setConfig(requestConfig);
            // Create a custom response handler
            ResponseHandler<String> responseHandler = getHandler();
            responseBody = httpclient.execute(post, responseHandler);
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            httpclient.close();
        }
        return responseBody;
    }

    /**
     * post请求
     * @param url
     * @param headersMap - 请求头
     * @param paramsMap - 请求参数
     * @return
     * @throws IOException
     */
    public static String downloadFile(String url,String fileDirOrPath,Map<String,String> headersMap,Map<String,String> paramsMap) throws IOException{
        //文件路径
        String filePath = "";
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            //判断不为空执行
            if(paramsMap != null && paramsMap.size() > 0){
                //参数处理
                mapToNameValuePair(params,paramsMap);
            }
            HttpEntity reqEntity = new UrlEncodedFormEntity(params, "utf-8");
            //请求配置
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(5000)//一、连接超时：connectionTimeout-->指的是连接一个url的连接等待时间
                    .setSocketTimeout(5000)// 二、读取数据超时：SocketTimeout-->指的是连接上一个url，获取response的返回等待时间
                    .setConnectionRequestTimeout(5000)
                    .build();
            HttpPost post = new HttpPost(url);
            //设置请求头
            post.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:6.0.2) Gecko/20100101 Firefox/6.0.2");
            post.setHeader("Referer", url);
            //判断不为空执行
            if(headersMap != null && !headersMap.isEmpty()){
                //遍历
                for (Map.Entry<String, String> entry : headersMap.entrySet()) {
                    //设置请求头
                    post.setHeader(entry.getKey(), entry.getValue());
                }
            }
            post.setEntity(reqEntity);
            post.setConfig(requestConfig);
            HttpResponse response = httpclient.execute(post);
            filePath = readFile(url,fileDirOrPath,response);
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            httpclient.close();
        }
        return filePath;
    }

    /**
     * get请求-绕过认证
     * @param url
     * @param headersMap - 请求头
     * @return
     * @throws IOException
     */
    public static String getSSL(String url,Map<String,String> headersMap) throws IOException{
        String body = "";
        //创建自定义的httpclient对象
        CloseableHttpClient client = null;
        try{
            //采用绕过验证的方式处理https请求
            SSLContext sslcontext = createIgnoreVerifySSL();

            //设置协议http和https对应的处理socket链接工厂的对象
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.INSTANCE)
                    .register("https", new SSLConnectionSocketFactory(sslcontext))
                    .build();
            PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            HttpClients.custom().setConnectionManager(connManager);


            //创建自定义的httpclient对象
            client = HttpClients.custom().setConnectionManager(connManager).build();
            //CloseableHttpClient client = HttpClients.createDefault();
            //创建get方式请求对象
            HttpGet get = new HttpGet(url);

            //指定报文头Content-type、User-Agent
            get.setHeader("Content-type", "application/x-www-form-urlencoded");
            get.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:6.0.2) Gecko/20100101 Firefox/6.0.2");
            get.setHeader("Referer", url);
            //判断不为空执行
            if(headersMap != null && !headersMap.isEmpty()){
                //遍历
                for (Map.Entry<String, String> entry : headersMap.entrySet()) {
                    //设置请求头
                    get.setHeader(entry.getKey(), entry.getValue());
                }
            }

            //执行请求操作，并拿到结果（同步阻塞）
            CloseableHttpResponse response = client.execute(get);

            //获取结果实体
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                //按指定编码转换结果实体为String类型
                body = EntityUtils.toString(entity, "UTF-8");
            }

            EntityUtils.consume(entity);
            //释放链接
            response.close();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if(client != null){
                client.close();
            }
        }
        return body;
    }

    /**
     * 下载文件-绕过认证
     * @param url
     * @param headersMap - 请求头
     * @return
     * @throws IOException
     */
    public static String downloadFileSSL(String url,String fileDirOrPath,Map<String,String> headersMap) throws IOException{
        //文件路径
        String filePath = "";
        //创建自定义的httpclient对象
        CloseableHttpClient client = null;
        try{
            //采用绕过验证的方式处理https请求
            SSLContext sslcontext = createIgnoreVerifySSL();

            //设置协议http和https对应的处理socket链接工厂的对象
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.INSTANCE)
                    .register("https", new SSLConnectionSocketFactory(sslcontext))
                    .build();
            PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            HttpClients.custom().setConnectionManager(connManager);


            //创建自定义的httpclient对象
            client = HttpClients.custom().setConnectionManager(connManager).build();
            //CloseableHttpClient client = HttpClients.createDefault();
            //创建get方式请求对象
            HttpGet get = new HttpGet(url);

            //指定报文头Content-type、User-Agent
            get.setHeader("Content-type", "application/x-www-form-urlencoded");
            get.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:6.0.2) Gecko/20100101 Firefox/6.0.2");
            get.setHeader("Referer", url);
            //判断不为空执行
            if(headersMap != null && !headersMap.isEmpty()){
                //遍历
                for (Map.Entry<String, String> entry : headersMap.entrySet()) {
                    //设置请求头
                    get.setHeader(entry.getKey(), entry.getValue());
                }
            }

            //执行请求操作，并拿到结果（同步阻塞）
            CloseableHttpResponse response = client.execute(get);
            //保存文件
            filePath = readFile(url,fileDirOrPath,response);
            //释放链接
            response.close();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if(client != null){
                client.close();
            }
        }
        return filePath;
    }

    /**
     * post请求-绕过认证
     * @param url
     * @param headersMap - 请求头
     * @param paramsMap - 请求参数
     * @return
     * @throws IOException
     */
    public static String postSSL(String url,Map<String,String> headersMap,Map<String,String> paramsMap) throws IOException{
        String body = "";
        //创建自定义的httpclient对象
        CloseableHttpClient client = null;
        try{
            //采用绕过验证的方式处理https请求
            SSLContext sslcontext = createIgnoreVerifySSL();
            //设置协议http和https对应的处理socket链接工厂的对象
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.INSTANCE)
                    .register("https", new SSLConnectionSocketFactory(sslcontext))
                    .build();
            PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            HttpClients.custom().setConnectionManager(connManager);
            //创建自定义的httpclient对象
            client = HttpClients.custom().setConnectionManager(connManager).build();
            //CloseableHttpClient client = HttpClients.createDefault();

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            //判断不为空执行
            if(paramsMap != null && !paramsMap.isEmpty()){
                //参数处理
                mapToNameValuePair(params,paramsMap);
            }

            //请求实体
            HttpEntity reqEntity = new UrlEncodedFormEntity(params, "utf-8");

            //请求配置
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(5000)//一、连接超时：connectionTimeout-->指的是连接一个url的连接等待时间
                    .setSocketTimeout(5000)// 二、读取数据超时：SocketTimeout-->指的是连接上一个url，获取response的返回等待时间
                    .setConnectionRequestTimeout(5000)
                    .build();
            //创建post方式请求对象
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(reqEntity);
            httpPost.setConfig(requestConfig);
            //指定报文头Content-type、User-Agent
            httpPost.setHeader("Content-type", "application/x-www-form-urlencoded");
            httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:6.0.2) Gecko/20100101 Firefox/6.0.2");
            httpPost.setHeader("Referer", url);
            //判断不为空执行
            if(headersMap != null && !headersMap.isEmpty()){
                //遍历
                for (Map.Entry<String, String> entry : headersMap.entrySet()) {
                    //设置请求头
                    httpPost.setHeader(entry.getKey(), entry.getValue());
                }
            }

            //执行请求操作，并拿到结果（同步阻塞）
            CloseableHttpResponse response = client.execute(httpPost);
            //获取结果实体
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                //按指定编码转换结果实体为String类型
                body = EntityUtils.toString(entity, "UTF-8");
            }
            EntityUtils.consume(entity);
            //释放链接
            response.close();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if(client != null){
                client.close();
            }
        }
        return body;
    }
    /**
     * 下载文件-绕过认证
     * @param url
     * @param headersMap - 请求头
     * @param paramsMap - 请求参数
     * @return
     * @throws IOException
     */
    public static String downloadFileSSL(String url,String fileDirOrPath,Map<String,String> headersMap,Map<String,String> paramsMap) throws IOException{
        //文件路径
        String filePath = "";
        //创建自定义的httpclient对象
        CloseableHttpClient client = null;
        try{
            //采用绕过验证的方式处理https请求
            SSLContext sslcontext = createIgnoreVerifySSL();
            //设置协议http和https对应的处理socket链接工厂的对象
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.INSTANCE)
                    .register("https", new SSLConnectionSocketFactory(sslcontext))
                    .build();
            PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            HttpClients.custom().setConnectionManager(connManager);
            //创建自定义的httpclient对象
            client = HttpClients.custom().setConnectionManager(connManager).build();
            //CloseableHttpClient client = HttpClients.createDefault();

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            //判断不为空执行
            if(paramsMap != null && !paramsMap.isEmpty()){
                //参数处理
                mapToNameValuePair(params,paramsMap);
            }
            //请求实体
            HttpEntity reqEntity = new UrlEncodedFormEntity(params, "utf-8");

            //请求配置
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(5000)//一、连接超时：connectionTimeout-->指的是连接一个url的连接等待时间
                    .setSocketTimeout(5000)// 二、读取数据超时：SocketTimeout-->指的是连接上一个url，获取response的返回等待时间
                    .setConnectionRequestTimeout(5000)
                    .build();
            //创建post方式请求对象
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(reqEntity);
            httpPost.setConfig(requestConfig);
            //指定报文头Content-type、User-Agent
            httpPost.setHeader("Content-type", "application/x-www-form-urlencoded");
            httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:6.0.2) Gecko/20100101 Firefox/6.0.2");
            httpPost.setHeader("Referer", url);
            //判断不为空执行
            if(headersMap != null && !headersMap.isEmpty()){
                //遍历
                for (Map.Entry<String, String> entry : headersMap.entrySet()) {
                    //设置请求头
                    httpPost.setHeader(entry.getKey(), entry.getValue());
                }
            }

            //执行请求操作，并拿到结果（同步阻塞）
            CloseableHttpResponse response = client.execute(httpPost);
            //保存文件
            filePath = readFile(url,fileDirOrPath,response);
            //释放链接
            response.close();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if(client != null){
                client.close();
            }
        }
        return filePath;
    }
}
