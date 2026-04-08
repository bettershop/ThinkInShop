package com.laiketui.common.utils.tool;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.domain.SmsMessageModel;

import java.util.Map;
import java.util.regex.Pattern;


/**
 * 手机服务工具
 *
 * @author Trick
 * @date 2020/9/23 11:17
 */
public class MobileUtils
{

    /**
     * 超时时间
     */
    private static final String OUT_TIME          = "10000";
    /**
     * 短信API产品名称（短信产品名固定，无需修改）
     */
    private static final String PRODUCT           = "Dysmsapi";
    /**
     * 短信API产品域名（接口地址固定，无需修改）
     */
    private static final String DOMAIN            = "dysmsapi.aliyuncs.com";


    /**
     * 发送短信
     *
     * @param phone - 手机号 model - 短信模板 paramMap - 模板参数 - signName - 签名
     * @return SmsMessageModel -
     * @author Trick
     * @date 2020/9/23 15:50
     */
    public static SmsMessageModel sendSms(String phone, String model, String signName, String key, String secret, Map<String, String> paramMap)
    {
        SmsMessageModel mobileUtils;
        try
        {
            //设置请求超时时间
            System.setProperty("sun.net.client.defaultConnectTimeout", OUT_TIME);
            System.setProperty("sun.net.client.defaultReadTimeout", OUT_TIME);

            DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", key, secret);
            IAcsClient     client  = new DefaultAcsClient(profile);

            CommonRequest request = new CommonRequest();
            request.setSysMethod(MethodType.POST);
            request.setSysDomain(DOMAIN);
            request.setSysVersion("2017-05-25");
            request.setSysAction("SendSms");
            request.putQueryParameter("RegionId", "cn-hangzhou");
            request.putQueryParameter("PhoneNumbers", phone);
            request.putQueryParameter("SignName", signName);
            request.putQueryParameter("TemplateCode", model);
            request.putQueryParameter("SmsUpExtendCode", "123");
            request.putQueryParameter("TemplateParam", JSON.toJSONString(paramMap));
            CommonResponse response = client.getCommonResponse(request);
            System.out.println("短信方式返回数据：" + response.getData());
            System.out.println(JSON.toJSONString(request.getSysQueryParameters()));
            mobileUtils = JSONObject.parseObject(response.getData(), SmsMessageModel.class);
        }
        catch (ClientException e)
        {
            e.printStackTrace();
            throw new LaiKeAPIException(ErrorCode.SysErrorCode.SEND_SMS_FAIL, model + " 短信发送失败!", "sendSms");
        }

        return mobileUtils;
    }


    /**
     * 校验手机是否合规 2020年最全的国内手机号格式
     *
     * @date 2020/9/23 11:17
     */
    private static final String REGEX_MOBILE = "((\\+86|0086)?\\s*)((134[0-8]\\d{7})|(((13([0-3]|[5-9]))|(14[5-9])|15([0-3]|[5-9])|(16(2|[5-7]))|17([0-3]|[5-8])|18[0-9]|19(1|[8-9]))\\d{8})|(14(0|1|4)0\\d{7})|(1740([0-5]|[6-9]|[10-12])\\d{7}))";

    /**
     * (中国移动号码格式验证 手机段：134(0-8),135,136,137,138,139,147,148,150,151,152,157,158,159,172,178,182,183,184,187,188,195,197,198,1440,1703,1705,1706
     */
    private static final String CHINA_MOBILE_PATTERN = "(?:^(?:\\+86)?1(?:34[0-8]|3[5-9]|4[78]|5[0-27-9]|7[28]|8[2-478]|9[578])\\d{8}$)|(?:^(?:\\+86)?1440\\d{7}$)|(?:^(?:\\+86)?170[356]\\d{7}$)";

    /**
     * 中国联通号码格式验证 手机段：130,131,132,140,145,146,155,156,166,185,186,171,175,176,196,1704,1707,1708,1709
     */
    private static final String CHINA_UNICOM_PATTERN = "(?:^(?:\\+86)?1(?:3[0-2]|4[056]|5[56]|66|7[156]|8[56]|96)\\d{8}$)|(?:^(?:\\+86)?170[47-9]\\d{7}$)";

    /**
     * 中国电信号码格式验证 手机段：133,149,153,177,173,180,181,189,190,191,193,199,1349,1410,1700,1701,1702
     */
    private static final String CHINA_TELECOM_PATTERN = "(?:^(?:\\+86)?1(?:33|49|53|7[37]|8[019]|9[0139])\\d{8}$)|(?:^(?:\\+86)?1349\\d{7}$)|(?:^(?:\\+86)?1410\\d{7}$)|(?:^(?:\\+86)?170[0-2]\\d{7}$)";

    /**
     * 截止2022年2月,中国大陆四家运营商以及虚拟运营商手机号码正则验证
     */
    private static final String CHINA_PATTERN = "(?:^(?:\\+86)?1(?:3[0-9]|4[01456879]|5[0-35-9]|6[2567]|7[0-8]|8[0-9]|9[0-35-9])\\d{8}$)";


    /**
     * 校验手机号
     *
     * @param phone 手机号
     * @return boolean true:是  false:否
     */
    public static boolean isMobile(String phone)
    {
        if (StringUtils.isEmpty(phone))
        {
            return false;
        }
        if (!Pattern.matches(REGEX_MOBILE, phone))
        {
            if (!Pattern.matches(CHINA_MOBILE_PATTERN, phone))
            {
                if (!Pattern.matches(CHINA_UNICOM_PATTERN, phone))
                {
                    if (!Pattern.matches(CHINA_TELECOM_PATTERN, phone))
                    {
                        return Pattern.matches(CHINA_PATTERN, phone);
                    }
                }
            }
        }
        return true;
    }
}
