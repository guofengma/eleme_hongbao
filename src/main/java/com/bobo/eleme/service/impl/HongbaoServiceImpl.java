package com.bobo.eleme.service.impl;

import com.bobo.eleme.dao.CookiesDao;
import com.bobo.eleme.entity.Cookies;
import com.bobo.eleme.service.HongbaoService;
import com.bobo.eleme.utils.ResultUtils;
import com.bobo.eleme.utils.Tools;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class HongbaoServiceImpl implements HongbaoService {

    @Autowired
    private CookiesDao cookiesDao;

    @Override
    public Map<String, Object> hongBao(String url, Integer num) {
        try {

            List<Cookies> cookiesList = cookiesDao.selectCookies();

            Map<String, Object> snAndPlat = Tools.jiexiURL(url);

            for (int i = 1; i < num; i++) {
                String responseBody = Tools.elemeResultJson(cookiesList.get(i).getOpenId(),cookiesList.get(i).getElemeKey(),cookiesList.get(i).getSid(), snAndPlat.get("sn"), cookiesList.get(i).getPhone(), snAndPlat.get("plat"));
                //识别已领取红包数量
                int count = StringUtils.countMatches(responseBody, "\"sns_username\"");
                if (count == (num - 1)) {
                    return ResultUtils.resultMap(1, url, "拆包成功");
                }
            }

            return ResultUtils.resultMap(2, "拆包失败", "发生未知错误");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public List<Cookies> selectCookies() {
        return cookiesDao.selectCookies();
    }

}
