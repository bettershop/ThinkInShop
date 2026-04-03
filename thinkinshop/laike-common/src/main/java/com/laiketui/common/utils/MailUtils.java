package com.laiketui.common.utils;

import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

/**
 * @author liuao
 * @version 1.0
 * @description: 邮件发送工具类
 * @date 2025/2/24 10:49
 */

@Component
public class MailUtils
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private JavaMailSenderImpl mailSender;

    @Autowired
    private RedisUtil redisUtil;
    /**
     * 有效时间内最大发送次数
     */
    private static final int maxAttempts = 5;


    /**
     * @param host 邮箱服务器地址
     * @param username 发件人邮箱账号（必须和授权码对应）
     * @param password 邮箱授权码
     * @param sslEnable 是否启用SSL加密
     * @param targetEmail 接受验证码
     */
    public void sendEmailCode(String host, String username, String password, boolean sslEnable, String targetEmail,String store_name)
    {

        mailSender.setHost(host);
        mailSender.setUsername(username);
        mailSender.setPassword(password);
        mailSender.setDefaultEncoding("utf-8");
        mailSender.setPort(sslEnable ? 465 : 25);

        String code = generateCode(6);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", sslEnable ? "true" : "false");

        //连接服务器的超时时间5秒
        props.put("mail.smtp.timeout", "5000");

        try
        {
            doSendEmail(mailSender, username, targetEmail, code,store_name);
        }
        catch (Exception e)
        {
            logger.error("邮件发送失败，目标邮箱：{}，错误信息：{}", targetEmail, e.getMessage());
            throw new RuntimeException("邮件发送失败", e);
        }
    }


    /**
     * 公共发送邮件方法
     */
    private void doSendEmail(JavaMailSender mailSender,
                             String username,
                             String targetEmail,
                             String code,String store_name) throws MessagingException
    {
        String emailKey = GloabConst.RedisHeaderKey.MAIL_CODE_KEY + targetEmail;
        long outTime = 60;
        int count = 1;

        if (redisUtil.hasKey(emailKey))
        {
            count = (int) redisUtil.get(emailKey);
            outTime = redisUtil.getExpire(emailKey);
            if (count > maxAttempts)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_YXFSPLGG,"邮箱发送频率过高");
            }
            count += 1;
        }
        redisUtil.set(emailKey, count, outTime);

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        String time = DateUtil.dateFormate(new Date(), GloabConst.TimePattern.YMDHMS);

        logger.info("邮箱验证码：{}", code);

        // 邮件标题
        helper.setSubject(store_name + "邮箱安全码");

        // 邮件内容
        String htmlFragment = "<div style=\"text-align:left; margin:20px 0; font-size:24px; color:#000;\">\n" +
                "    您的" + store_name + "邮箱安全码：<br>\n" +
                "    <span style=\"font-size:36px; color:#e74c34;\">" + code + "</span>\n" +
                "</div>\n" +
                "<div style=\"color:#000; margin-bottom:15px;\">\n" +
                "    绝不要与任何人分享您的验证码——" + store_name + "客服绝不会要求您提供此验证码。\n" +
                "</div>\n" +
                "<div style=\"margin-top:30px; color:#000; font-size:12px;\">\n" +
                "    时间：<span style=\"font-weight:normal;\"></span>\n" + time +
                "</div>";

        helper.setText(htmlFragment, true);
        helper.setTo(targetEmail);
        helper.setFrom(username);
        mailSender.send(mimeMessage);

        String emailCodeKey = emailKey + code;
        redisUtil.set(emailCodeKey,code,outTime);
    }

/*
    public Session createSession()
    {
        try
        {
            Properties props = new Properties();
            props.put("smtp.qq.com", "smtp.163.com"); // SMTP服务器地址
            props.put("mail.smtp.port", "465"); // 端口号
            props.put("mail.smtp.auth", "true"); // 启用认证
            props.put("mail.smtp.starttls.enable", "true"); // 启用TLS加密（部分邮箱需要）

            return Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication("1664089464@qq.com", "wysgyvsviuwmfcfi");
                }
            });
        }catch (LaiKeAPIException e)
        {
            logger.error("获取session失败，错误信息：{}",e.getMessage());
        }
        return null;
    }
*/


    /**
     * 生成x位验证码
     *
     * @return string
     */
    public static String generateCode(Integer length)
    {
        return String.format("%0" + length + "d", new Random().nextInt(999999));
    }
}
