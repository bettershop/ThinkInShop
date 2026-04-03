<?php
namespace app\common;

use think\facade\Db;
use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\Exception;

use app\admin\model\SessionIdModel;

class EmailController
{
    public function sendEmail($array)
    {
        $store_id = $array['store_id'];
        $email = $array['email'];
        $time = date('Y-m-d H:i:s'); // 当前时间
        $code = rand(100000, 999999);
        $Subject = '来客推邮件验证码';
        
        $Body = "<p style='font-size:30px'>您的来客推邮箱安全码：</p>";
        $Body .= "<br/>";
        $Body .= "<p>绝不要与任何人分享您的验证码——来客推客服绝不会要求您提供此验证码。</p>";
        $Body .= "<br/>";
        $Body .= "<p style='font-size:50px'>".$code."</p>";
        $Body .= "<p>时间：".$time."</p>";
        $AltBody = '';

        $smtp_host = "smtp.qq.com";
        $smtp_secure = "ssl";
        $smtp_username = "274817968@qq.com";
        $smtp_password = "ahsjeuarvgskbgga";
        $sql = "select mail_config from lkt_config where store_id = '$store_id' ";
        $r = Db::query($sql);
        if($r)
        {
            $mail_config = json_decode($r[0]['mail_config'],true);
            $smtp_host = $mail_config['host'];
            $smtp_username = $mail_config['username'];
            $smtp_password = $mail_config['password'];
            $sslEnable = $mail_config['sslEnable'];
            if($sslEnable == true)
            {
                $smtp_secure = 'ssl';
            }
        }

        // 加载邮件配置
        $config = array(
            'smtp_host' => $smtp_host, // SMTP 服务器地址，根据邮箱提供商修改
            'smtp_port' => 465, // SMTP 服务器端口号
            'smtp_secure' => $smtp_secure, // 加密方式，可选 'ssl' 或 'tls'
            'smtp_username' => $smtp_username, // 发件人邮箱用户名
            'smtp_password' => $smtp_password, // 发件人邮箱密码或授权码
            'from_email' => $smtp_username, // 发件人邮箱地址
            'from_name' => '发件人姓名', // 发件人姓名
        );

        // 创建 PHPMailer 实例
        $mail = new PHPMailer(true);

        try 
        {
            // 服务器配置
            $mail->SMTPDebug = 0; // 调试模式，0 表示关闭调试信息
            $mail->isSMTP(); // 使用 SMTP 协议
            $mail->Host = $config['smtp_host']; // SMTP 服务器地址
            $mail->SMTPAuth = true; // 开启 SMTP 认证
            $mail->Username = $config['smtp_username']; // 发件人邮箱用户名
            $mail->Password = $config['smtp_password']; // 发件人邮箱密码或授权码
            $mail->SMTPSecure = $config['smtp_secure']; // 加密方式
            $mail->Port = $config['smtp_port']; // SMTP 服务器端口号

            // 发件人信息
            $mail->setFrom($config['from_email'], $config['from_name']);

            // 收件人信息
            $mail->addAddress($email, '收件人姓名');

            // 邮件内容
            $mail->isHTML(true); // 设置邮件为 HTML 格式
            $mail->Subject = $Subject;
            $mail->Body = $Body;
            $mail->AltBody = $AltBody;

            // 发送邮件
            $mail->send();

            $TemplateParam = array('code' => $code); // 验证码
            $arr = array($email, $TemplateParam);
            $content1 = json_encode($arr); // 数组转json字符串

            $rew = 0; // 用来判断，是否有短信数据。0代表没有，1代表有
            $r1 = SessionIdModel::select()->toArray();
            if($r1)
            {
                foreach ($r1 as $k => $v)
                {
                    $content2 = json_decode($v['content']);
                    if (($email == $content2[0]))
                    {
                        $update = array('content' => $content1);
                        Db::name('session_id')->where('id', $v['id'])->update($update);
                        $rew = 1;
                    }
                }
            }
            if ($rew == 0)
            {
                $insert = array('content' => $content1, 'add_date' => $time);
                Db::name('session_id')->insert($insert);
            }

            $message = Lang('Success');
            echo json_encode(array('code' => "200",'data'=>true, 'message' => $message));
            exit();
            // return '邮件发送成功';
        } 
        catch (Exception $e) 
        {
            $message = Lang('login.19');
            echo json_encode(array('code' => 109, 'message' => $message,'res'=>$mail->ErrorInfo));
            exit();
            // return "邮件发送失败: {$mail->ErrorInfo}";
        }
    }
}