package com.weibo.system.utils.http;

import groovy.transform.CompileStatic;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import com.ttpod.rest.AppProperties;
import com.ttpod.rest.common.util.JSONUtil;
import com.ttpod.rest.common.util.WebUtils;
import com.ttpod.rest.common.util.http.HttpClientUtil;

@CompileStatic
public abstract class Web extends WebUtils{


    static final String API_DOMAIN = AppProperties.get("api.domain", "http://api.51weibo.com/");
    static final Charset UTF8= Charset.forName("utf8");
    public static Object api(String url) throws IOException{
        Object obj = null ;

        try
        {

            String sUrl = API_DOMAIN + url;

            String json =  HttpClientUtil.get(sUrl, null, UTF8);

            obj = JSONUtil.jsonToMap(json).get("data");
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }

        return obj ;

    }

    public static Object api(String url,Boolean bFlag) throws IOException
    {
        Map<String,Object> map = new HashMap<String,Object>();
        String json =  HttpClientUtil.get(API_DOMAIN + url, null, UTF8);
        Map content = JSONUtil.jsonToMap(json) ;
        map.put("data",content.get("data"));
        if(bFlag)
        {    map.put("count",content.get("count")) ;
        }

        return map ;

    }

}
