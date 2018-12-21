package com.bobo.eleme.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Peter
 */
public class ResultUtils {

    public static Map<String, Object> resultMap(Integer status, String msg, Object obj) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (status != null && status == 1) {
            map.put("status", "success");// 返回状态：成功
        } else if (status != null && status == 2) {
            map.put("status", "error");// 返回状态：失败
        } else if (status != null && status == 3) {
            map.put("status", "warning");// 返回状态：提示信息
        }
        map.put("msg", msg);// 返回信息
        map.put("result", obj);// 返回值
        return map;
    }

}
