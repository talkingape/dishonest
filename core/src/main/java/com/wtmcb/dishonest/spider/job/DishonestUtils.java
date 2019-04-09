package com.wtmcb.dishonest.spider.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wtmcb.dishonest.entity.Dishonestor;
import com.wtmcb.dishonest.entity.exception.RemoteException;
import com.wtmcb.dishonest.utils.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by WangGang on 2019-04-04.
 * Email: 384967599@qq.com
 */
@Slf4j
public class DishonestUtils {

    private static final String ENCODING = "UTF-8";
    private static final String SPIDER_URL = "https://sp0.baidu.com/8aQDcjqpAAV3otqbppnN2DJv/api.php";

    private static final Integer LIMIT = 20;

    public static List<Dishonestor> fetchList(Integer page) throws Exception {
        Map<String, Object> queryParams = warpQueryParams(LIMIT, LIMIT * (page - 1));
        List<Dishonestor> dishonestorCustomerInfoList = null;
        String dishonestQueryUrl = getDishonestQueryUrl(queryParams);
        String response = HttpUtils.getResponse(dishonestQueryUrl);
        if (StringUtils.isNotEmpty(response)){
            dishonestorCustomerInfoList = wrapDishonest(response);
            log.info("获取第{}页共{}条失信人信息", page, dishonestorCustomerInfoList.size());
        }else {
            log.error("获取第{}页失信人信息为空，url为{}", page, dishonestQueryUrl);
        }
        return dishonestorCustomerInfoList;
    }

    private static String getDishonestQueryUrl(Map<String, Object> params) throws RemoteException {
        String fullUrl = null;
        // 构建请求参数
        StringBuilder buffer = new StringBuilder();
        if (params != null && params.size() > 0) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                buffer.append(entry.getKey());
                buffer.append("=");
                try {
                    buffer.append(URLEncoder.encode((String) entry.getValue(), ENCODING));
                } catch (UnsupportedEncodingException e) {
                    throw new RemoteException("失信人查询编码异常:"+e);
                }
                buffer.append("&");
            }
        }
        // 拼接查询URL
        if (buffer.length() > 0) {
            fullUrl = SPIDER_URL + "?" + buffer.substring(0, buffer.length() - 1);
        }
        // 请求拼接后的地址获取详细信息
        return fullUrl;
    }

    private static Map<String, Object> warpQueryParams(Integer rn, Integer pn){
        // 查询条件
        Map<String, Object> map = new HashMap<>();
        map.put("resource_id", "6899");
        map.put("query", "失信被执行人名单");
        map.put("cardNum", "");
        map.put("iname", "");
        map.put("areaName", "");
        map.put("rn", rn.toString());
        map.put("pn", pn.toString());
        map.put("ie", "utf-8");
        map.put("oe", "utf-8");
        map.put("format", "json");
        map.put("t", "1524537973200");
        map.put("cb", "jQuery110207319777455577083_1524537959352");
        map.put("_", "1524537959354");
        return map;
    }

    private static List<Dishonestor> wrapDishonest(String strResult) {
        // 查询结果
        strResult = strResult.substring(strResult.indexOf("(")+1,strResult.lastIndexOf(");"));
        // json封装
        JSONObject firstMap = JSONObject.parseObject(strResult);
        JSONArray secondMap = (JSONArray) firstMap.get("data");
        // 返回的结果初始化列表
        List<Dishonestor> infoList = new ArrayList<>();
        if (secondMap != null && secondMap.size() > 0) {
            JSONObject thirdMap = (JSONObject) secondMap.get(0);
            JSONArray forthMap = (JSONArray) thirdMap.get("result");
            for (int i = 0; i < forthMap.size(); i++) {
                JSONObject item = forthMap.getJSONObject(i);
                // 自定义类封装
                Dishonestor dishonestorCustomerInfo = JSON.parseObject(item.toJSONString(), Dishonestor.class);
                infoList.add(dishonestorCustomerInfo);
            }
        }
        return infoList;
    }
}
