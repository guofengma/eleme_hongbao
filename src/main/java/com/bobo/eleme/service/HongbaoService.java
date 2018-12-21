package com.bobo.eleme.service;

import com.bobo.eleme.entity.Cookies;

import java.util.List;
import java.util.Map;

/**
 * @author Peter
 * @date 2018-11-29
 */
public interface HongbaoService {

    /**
     * 饿了么红包拆单操作
     *
     * @param url
     * @param num
     * @return
     */
    Map<String, Object> hongBao(String url, Integer num);


    List<Cookies> selectCookies();

}
