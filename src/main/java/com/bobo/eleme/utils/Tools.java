package com.bobo.eleme.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpHost;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.params.ConnRoutePNames;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Peter
 * @date 2018-11-29
 * 工具类
 */
public class Tools {

    /**
     * cookie-Openid
     *
     * @param sn    拼手气红包id
     * @param phone 手机号码
     * @param plat  平台标识
     *              cookie-eleme_key
     * @return
     */
    public static String elemeResultJson(String openId,String elemeKey,String sid, Object sn, String phone, Object plat) {
        try {
            Thread.sleep(3000);
            HttpClient httpClient = new HttpClient();
            HttpHost proxy = new HttpHost("183.129.244.17", 23222); //添加代理
            httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
            PostMethod httpPost = new PostMethod(Constants.ELEME_URL+"/restapi/marketing/promotion/weixin/" + openId);
            NameValuePair[] data = {
                    new NameValuePair("method", Constants.METHOD),
                    new NameValuePair("group_sn", sn.toString()),
                    new NameValuePair("sign", elemeKey),
                    new NameValuePair("phone", phone),
                    new NameValuePair("platform", plat.toString()),
                    new NameValuePair("track_id", ""),
                    new NameValuePair("weixin_avatar", Constants.WEIXIN_AVATAR),
                    new NameValuePair("weixin_username", Constants.WEIXIN_USERNAME),
                    new NameValuePair("unionid", Constants.UNION_ID)
            };
            httpPost.setRequestBody(data);
            httpPost.setRequestHeader("cookie", "SID=" + sid);
            httpClient.executeMethod(httpPost);

            String result = new String(httpPost.getResponseBodyAsString().getBytes("UTF-8"), "UTF-8");

            JSONObject jsonObject = JSONObject.parseObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray("promotion_records");

            return jsonArray.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解析URL
     * 获取 sn and plat
     *
     * @param url
     * @return
     */
    public static Map<String, Object> jiexiURL(String url) {
        String codeUrl = url.replace("&amp;", "&");
        Map<String, Object> result = new HashMap<String, Object>();
        String sn = codeUrl; // 分析多个URL 个人的出 结论 这个参数是 该拼手气红包id
        String plat = codeUrl; // 字面意思是平台，猜测应该是 饿了么针对某些平台的标识符

        String regSn = "&sn=[0-9,a-z]+";
        Pattern pSn = Pattern.compile(regSn);
        Matcher mSn = pSn.matcher(sn);
        while (mSn.find()) {
            sn = (mSn.group());
        }
        sn = sn.substring(4, sn.length());

        String regPlat = "&platform=[0-9]+";
        Pattern pPlat = Pattern.compile(regPlat);
        Matcher mPlat = pPlat.matcher(plat);
        while (mPlat.find()) {
            plat = (mPlat.group());
        }
        plat = plat.substring(10, plat.length());

        result.put("sn", sn);
        result.put("plat", plat);

        return result;
    }

    /**
     * 解析qq登录饿了么cookies
     *
     * @param url
     * @return HashMap
     */
    public static Map<String, Object> replaceCookieUrlCode(String url) {
        String codeUrl = url.replace("&amp;", "&");
        Map<String, Object> result = new HashMap<String, Object>();
        String urls = codeUrl.replace("%22", "\"").replace("%3A", ":").replace("%2C", ",").replace("%2F", "/")
                .replace("%7B", "{").replace("%7D", "}").replace("[", "").replace("]", "").replace(";", ",");

        try {
            String[] param = urls.split(",");

            String jsonString = "{"+ param[5] + "," + param[18] + "}";

            JSONObject jsonObject = JSONObject.parseObject(jsonString);
            result.put("cookies",param[0]+ ";" +param[1].replace(",",";")+ ";" +param[2].replace(",",";")+ ";" );
            result.put("open_id", jsonObject.get("openid"));
            result.put("eleme_key", jsonObject.get("eleme_key"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 使用短信验证码登录，需要先调用 sendMobileCode
     * @param mobile 手机号码
     * @param validateCode 短信验证码
     * @param validate_token token
     */
    public static Map<String,Object> loginByMobile(String mobile,String validateCode,String validate_token){
        try {
            System.out.println("--------------->：饿了么登录！");
            Map<String,Object> param = new HashMap<String,Object>();
            HttpClient httpClient = new HttpClient();
            PostMethod httpPost = new PostMethod(Constants.ELEME_URL + "/restapi/eus/login/login_by_mobile");
            NameValuePair[] data = {
                    new NameValuePair("mobile", mobile),
                    new NameValuePair("validate_code", validateCode),
                    new NameValuePair("validate_token", validate_token)
            };
            httpPost.setRequestBody(data);
            httpClient.executeMethod(httpPost);
            String result = new String(httpPost.getResponseBodyAsString().getBytes("UTF-8"), "UTF-8");
            System.out.println(httpPost.getResponseHeader("set-cookie"));

            String cookie = httpPost.getResponseHeader("set-cookie").toString();

            param.put("SID",cookie.substring(cookie.indexOf("SID")).split(";")[0].replace("SID=",""));
            return param;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 发送短信验证码 需要先调用 captchas
     * @param  mobile 手机号码
     * @param  captcha_hash 图形验证码标识
     * @param  captcha_value 图形验证码内容
     */
    public static Map<String,Object> sendMobileCode(String mobile,String captcha_hash,String captcha_value){
        try {
            System.out.println("--------------->：发送短信！");
            Map<String,Object> param = new HashMap<String,Object>();
            // 请求十次，绕过图片验证码 验证。待优化
            for (int i = 0; i < 10; i++) {
                Thread.sleep(2000);
                HttpClient httpClient = new HttpClient();
                HttpHost proxy = new HttpHost("183.129.244.17", 23222); //添加代理
                httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
                PostMethod httpPost = new PostMethod(Constants.ELEME_URL + "/restapi/eus/login/mobile_send_code");
                NameValuePair[] data = {
                        new NameValuePair("mobile", mobile),
                        new NameValuePair("captcha_hash", captcha_hash),
                        new NameValuePair("captcha_value", captcha_value)
                };
                httpPost.setRequestBody(data);
                httpClient.executeMethod(httpPost);
                String result = new String(httpPost.getResponseBodyAsString().getBytes("UTF-8"), "UTF-8");
                JSONObject jsonObject = JSONObject.parseObject(result);

                if (jsonObject != null){
                    if (jsonObject.containsKey("name")){
                        if (StringUtils.equals("NEED_CAPTCHA",jsonObject.get("name").toString())){
                            /*param.put("error",jsonObject.get("name"));
                            return param;*/
                            continue;
                        }
                    }else if (jsonObject.containsKey("validate_token")){
                        param.put("validate_token",jsonObject.get("validate_token"));
                        return param;
                    }else if (jsonObject.containsKey("CAPTCHA_CODE_ERROR")){
                        /*param.put("error","验证码错误");
                        return param;*/
                        continue;
                    }
                }
            }
            param.put("error","NEED_CAPTCHA");
            return param;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取图形验证码
     * @param  captcha_str 手机号码
     */
    public static Map<String,Object> captchas(String captcha_str){
        try {
            System.out.println("--------------->：获取图形验证码！");
            Map<String,Object> param = new HashMap<String,Object>();
            HttpClient httpClient = new HttpClient();
            PostMethod httpPost = new PostMethod(Constants.ELEME_URL + "/restapi/eus/v3/captchas");
            NameValuePair[] data = {
                    new NameValuePair("captchas", captcha_str),
            };
            httpPost.setRequestBody(data);
            httpClient.executeMethod(httpPost);
            String result = new String(httpPost.getResponseBodyAsString().getBytes("UTF-8"), "UTF-8");
            JSONObject jsonObject = JSONObject.parseObject(result);

            if (jsonObject != null){
                param.put("captcha_hash",jsonObject.get("captcha_hash"));
                param.put("captcha_image",jsonObject.get("captcha_image"));
            }
            System.out.println("captchas："+param);
            return param;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取短信发送平台token
     * @return
     */
    public static String getSmsToken(){
        try {
            String param = "action=login&username=" + Constants.SMS_ACCOUNT + "&password=" + Constants.SMS_PASSWORD;
            String result = HttpRequest.sendGet(Constants.SMS_URL + "/UserInterface.aspx",param);
            String susccess = result.substring(result.indexOf("|") + 1,result.length());
            return susccess;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取手机号码接口
     * @return
     */
    public static String getSmsMobile(){
        try {
            System.out.println("--------------->：获取手机号码！");
            String param = "action=getmobile&token=" + Constants.SMS_TOKEN + "&itemid=352";
            String result = HttpRequest.sendGet(Constants.SMS_URL + "/UserInterface.aspx",param);
            String susccess = result.substring(result.indexOf("|") + 1,result.length());;
            return susccess;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 获取短信接口
     * @return
     */
    public static String getSmsCode(String mobile){
        try {
            System.out.println("--------------->：获取手机短信！");
            // 请求十次
            int cont = 10;
            String result = "";
            for (int i = 0; i < cont; i++){
                System.out.println("--------------->：第" + (i + 1) + "次");
                String param = "action=getsms&token=" + Constants.SMS_TOKEN + "&itemid=352&mobile=" + mobile;
                String msg = HttpRequest.sendGet(Constants.SMS_URL + "/UserInterface.aspx",param);
                if (StringUtils.equals("3001",msg)){
                    Thread.sleep(5000);
                    continue;
                }
                if (i == cont){
                    result = "尚未收到短信";
                    return result;
                }
                System.out.println("--------------->：获取短信验证码成功：" + msg);
                result = msg;
                break;
            }
            String regEx="[^0-9]";
            Pattern p = Pattern.compile(regEx);
            Matcher m = p.matcher(result);
            String susccess = m.replaceAll("").trim().substring(0,6);;
            return susccess;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        /*String url = "[\"perf_ssid=gn81x0m0sk3qjo6haypwyltybqvo3o6k_2018-12-04; ubt_ssid=940ec4vc4dg5cuanqbhjqxoslwmeegay_2018-12-04; _utrace=2813cd119b0598ae5913fa435f9f1674_2018-12-04; snsInfo[101204453]=%7B%22city%22%3A%22%22%2C%22constellation%22%3A%22%22%2C%22eleme_key%22%3A%22626efa615ff52e5af466c6efe8f7ccd3%22%2C%22figureurl%22%3A%22http%3A%2F%2Fqzapp.qlogo.cn%2Fqzapp%2F101204453%2F96ADD1425E68871250D91D782D46634F%2F30%22%2C%22figureurl_1%22%3A%22http%3A%2F%2Fqzapp.qlogo.cn%2Fqzapp%2F101204453%2F96ADD1425E68871250D91D782D46634F%2F50%22%2C%22figureurl_2%22%3A%22http%3A%2F%2Fqzapp.qlogo.cn%2Fqzapp%2F101204453%2F96ADD1425E68871250D91D782D46634F%2F100%22%2C%22figureurl_qq_1%22%3A%22http%3A%2F%2Fthirdqq.qlogo.cn%2Fqqapp%2F101204453%2F96ADD1425E68871250D91D782D46634F%2F40%22%2C%22figureurl_qq_2%22%3A%22http%3A%2F%2Fthirdqq.qlogo.cn%2Fqqapp%2F101204453%2F96ADD1425E68871250D91D782D46634F%2F100%22%2C%22gender%22%3A%22%E7%94%B7%22%2C%22is_lost%22%3A0%2C%22is_yellow_vip%22%3A%220%22%2C%22is_yellow_year_vip%22%3A%220%22%2C%22level%22%3A%220%22%2C%22msg%22%3A%22%22%2C%22nickname%22%3A%22%E5%96%B5%22%2C%22openid%22%3A%2296ADD1425E68871250D91D782D46634F%22%2C%22province%22%3A%22%22%2C%22ret%22%3A0%2C%22vip%22%3A%220%22%2C%22year%22%3A%221900%22%2C%22yellow_vip_level%22%3A%220%22%2C%22name%22%3A%22%E5%96%B5%22%2C%22avatar%22%3A%22http%3A%2F%2Fthirdqq.qlogo.cn%2Fqqapp%2F101204453%2F96ADD1425E68871250D91D782D46634F%2F40%22%7D\"]";
        System.out.println(sendMobileCode("17002860543","",""));*/
        Scanner scanner = new Scanner(System.in);
        // 1. 获取手机号码
        String phone = getSmsMobile();
        // 2. 发送短信
        Map<String,Object> code = sendMobileCode(phone,"","");
        if (code.containsKey("error")){
            // 3. 获取图形验证码
            Map<String,Object> code1 = captchas(phone);
            String varCode = scanner.next();
            // 验证码验证 继续发短信
            Map<String,Object> code2 = sendMobileCode(phone,code1.get("captcha_hash").toString(),varCode);

            if (code2.containsKey("error")){
                System.out.println(code2.get("error"));
                System.exit(0);
            }
            String validate_token = code2.get("validate_token").toString();
            // 4.接收验证码
            getSmsCode(phone);
            // 4 登录
            String varCode1 = scanner.next();
            loginByMobile(phone,varCode1,validate_token);
        }else{
            String validate_token = code.get("validate_token").toString();
            // 3.接收验证码
            String codeSms = getSmsCode(phone);
            System.out.println(loginByMobile(phone,codeSms,validate_token));
        }
        /*System.out.println(getSmsToken());*/
    }
}
