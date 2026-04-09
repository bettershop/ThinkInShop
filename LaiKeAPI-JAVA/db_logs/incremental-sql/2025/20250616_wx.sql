ALTER TABLE lkt_message
    MODIFY COLUMN `type1` int(2) NOT NULL DEFAULT 0 COMMENT '类别 1登录2验证码注册3修改手机号4修改登陆密码5修改支付密码6提现模板7通用模板8支付成功通知9退款' AFTER `type`;