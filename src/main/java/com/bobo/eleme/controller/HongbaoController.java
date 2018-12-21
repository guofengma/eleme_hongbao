package com.bobo.eleme.controller;

import com.bobo.eleme.entity.Cookies;
import com.bobo.eleme.service.HongbaoService;
import com.bobo.eleme.service.impl.HongbaoServiceImpl;
import com.bobo.eleme.utils.ResultUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HongbaoController {

    @Autowired
    private HongbaoService hongbaoService;

    @ResponseBody
    @RequestMapping(value = "/hongbao", method = RequestMethod.POST)
    public Map<String, Object> hongbao(HttpServletRequest request) {

        String url = request.getParameter("url");
        String num = request.getParameter("num");

        Map<String, Object> result = hongbaoService.hongBao(url, Integer.parseInt(num));
        if (StringUtils.equals("success", result.get("status").toString())) {
            return result;

        }
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/index", method = RequestMethod.POST)
    public Map<String, Object> index(HttpServletRequest request) {
        List<Cookies> cookiesList = hongbaoService.selectCookies();
        Map<String,Object> result = new HashMap<>();
        for(Cookies cookies : cookiesList){
            result.put("id",cookies.getId());
            result.put("openId",cookies.getOpenId());
            result.put("elemeKey",cookies.getElemeKey());
            result.put("sid",cookies.getSid());
        }

        return ResultUtils.resultMap(1, result.toString(), "ssss");
    }

}
