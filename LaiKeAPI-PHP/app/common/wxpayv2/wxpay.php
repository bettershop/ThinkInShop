<?php
namespace app\common\wxpayv2;

require_once "WxPay.Api.php";
require_once "WxPay.Data.php";
require_once "../app/common/phpqrcode.php";

use think\facade\Db;
use think\facade\Request;

use app\common\LKTConfigInfo;
use app\common\LaiKeLogUtils;
use app\common\PC_Tools;

// 获取支付金额
class wxpay
{
	
	public static function payment_APP($out_trade_no,$store_id,$total = 100,$title = '来客电商',$type='app_wechat')
	{
        $log = new LaiKeLogUtils();
        $config = LKTConfigInfo::getPayConfig($store_id,$type);
        if (empty($config)) 
        {
            $log->log('wechat/payment_APP.log',$type."执行日期：".date('Y-m-d H:i:s')."支付暂未配置".$store_id."，无法调起支付！");
            return 'file';
        }
		$total = round($total*100); // 将元转成分
		$unifiedOrder = new WxPayUnifiedOrder();
		
        $unifiedOrder->config = $config;
		$unifiedOrder->SetBody($title);//商品或支付单简要描述
		$unifiedOrder->SetOut_trade_no($out_trade_no);
		$unifiedOrder->SetTotal_fee($total);
		$unifiedOrder->SetTrade_type("APP");
        WxPayApi::$config = $config;
		$result = WxPayApi::unifiedOrder($unifiedOrder);

        $log->log('wechat/app.log',"【APP调用支付】执行日期：".date('Y-m-d H:i:s').json_encode($result));

		return $result;
	}

	//传输给微信的参数要组装成xml格式发送,传如参数数组
    public static function ToXml($data=array())
    {
            if(!is_array($data) || count($data) <= 0)
            {
               return '数组异常';
            }

            $xml = "<xml>";
            foreach ($data as $key=>$val)
            {
                if (is_numeric($val)){
                    $xml.="<".$key.">".$val."</".$key.">";
                }else{
                    $xml.="<".$key."><![CDATA[".$val."]]></".$key.">";
                }
            }
            $xml.="</xml>";
            return $xml;
    }

    //生成签名
    public static function getSign($params) {
        ksort($params);        //将参数数组按照参数名ASCII码从小到大排序
        foreach ($params as $key => $item) {
            if (!empty($item)) {         //剔除参数值为空的参数
                $newArr[] = $key.'='.$item;     // 整合新的参数数组
            }
        }
        $stringA = implode("&", $newArr);         //使用 & 符号连接参数
        $stringSignTemp = $stringA."&key=".self::mch_key;        //拼接key
        // key是在商户平台API安全里自己设置的
        $stringSignTemp = MD5($stringSignTemp);       //将字符串进行MD5加密
        $sign = strtoupper($stringSignTemp);      //将所有字符转换为大写
        return $sign;
    }

    //将xml数据转换为数组,接收微信返回数据时用到
    public static function FromXml($xml)
    {
        if(!$xml)
        {
            echo "xml数据异常！";
        }
        //将XML转为array
        //禁止引用外部xml实体
        @libxml_disable_entity_loader(true);
        $data = json_decode(json_encode(simplexml_load_string($xml, 'SimpleXMLElement', LIBXML_NOCDATA)), true);
        return $data;
    }
    
    public static function payment_H5($out_trade_no,$store_id,$type,$total = 100,$title = '来客电商')
    {   
        
        $log = new LaiKeLogUtils();
        $config = LKTConfigInfo::getPayConfig($store_id,$type);

        // $log = new LaiKeLogUtils('wechat/h5.log');
        if (empty($config)) 
        {
            $log->log('wechat/h5.log',$type."执行日期：".date('Y-m-d H:i:s')."支付暂未配置".$store_id."，无法调起支付！");
            return 'file';
        }
        if(!isset($config['appid']) || !isset($config['mch_id']) || !isset($config['mch_key']) || !isset($config['notify_url']) || !isset($config['spbill_create_ip']) )
        {
            $message = Lang('pay.12');
            echo json_encode(array('code'=>109,'message'=>$message));
            exit();
        }
        
        $profit_sharing = PC_Tools::Obtain_the_splitting_status(array('store_id'=>$store_id));
        if($profit_sharing == 'Y')
        {
            cache('Divide_accounts_list_'.$out_trade_no, $profit_sharing);
        }

        $total_fee =    round($total*100); // 因为充值金额最小是1 而且单位为分 如果是充值1元所以这里需要*100
        $body =         mb_substr($title,0,8,'utf-8');
        $appid =        $config['appid']; // 如果是公众号 就是公众号的appid
        $mch_id =       $config['mch_id']; // 商户id
        $mch_key =      $config['mch_key']; // 商户key
        $nonce_str =    self::nonce_str(); // 随机字符串
        $notify_url =   $config['notify_url'];
        $spbill_create_ip = isset($config['spbill_create_ip'])?$config['spbill_create_ip']:'120.76.189.152'; // ip地址
        $trade_type = 'MWEB'; // 交易类型 默认
        $scene_info ='{"h5_info":{"type":"Wap","wap_url":"https://xiaochengxu.laiketui.com","wap_name":"支付"}}';//场景信息 必要参数

        // 这里是按照顺序的 因为下面的签名是按照顺序 排序错误 肯定出错
        $post['appid'] = $appid; // 如果是公众号 就是公众号的appid
        $post['body'] = $body; // 公司名称
        $post['mch_id'] = $mch_id; // 商户id
        $post['nonce_str'] = $nonce_str;//随机字符串
        $post['notify_url'] = $notify_url;
        $post['out_trade_no'] = $out_trade_no; // 商户订单号
        $post['profit_sharing'] = $profit_sharing; // Y-是，需要分账    N-否，不分账
        $post['scene_info'] = $scene_info; //场景信息
        $post['spbill_create_ip'] = $spbill_create_ip; // 终端的ip
        $post['total_fee'] = $total_fee; // 交易金额
        $post['trade_type'] = $trade_type; // 交易类型
        $sign = self::sign($post,$mch_key);//签名
    
        $post_data="<xml><appid>$appid</appid><body>$body</body><mch_id>$mch_id</mch_id><nonce_str>$nonce_str</nonce_str><notify_url>$notify_url</notify_url><out_trade_no>$out_trade_no</out_trade_no><profit_sharing>'.$profit_sharing.'</profit_sharing><scene_info>$scene_info</scene_info><spbill_create_ip>$spbill_create_ip</spbill_create_ip><total_fee>$total_fee</total_fee><trade_type>$trade_type</trade_type><sign>$sign</sign></xml>";//拼接成XML 格式
        $log->log('wechat/h5.log',"【支付参数】执行日期：".date('Y-m-d H:i:s'). $post_data);

        $url = "https://api.mch.weixin.qq.com/pay/unifiedorder";//微信传参地址
        $xml = self::http_request($url,$post_data);
        $array = self::FromXml($xml);//全要大写

        $log->log('wechat/h5.log',"【h5调用支付】执行日期：".date('Y-m-d H:i:s').json_encode($array));

        $order_type = substr($out_trade_no, 0, 2);
        $ret = array();
        if($array['return_code'] == 'SUCCESS' && $array['result_code'] == 'SUCCESS')
        {
            $h5domain = LKTConfigInfo::getH5Domain($store_id)."pages/pay/payResult?sNo=".$out_trade_no."&payment_money=".$total."&order_type=".$order_type."&isH5=true";
            $mweb_url= $array['mweb_url']."&redirect_url=".urlencode($h5domain);
            $ret['mweb_url']= $mweb_url;
            $ret['prepayid']= $array['prepay_id'];
        }
        
        return $ret;
    }


    //提交支付 JSAPI
    public static function payment_JSAPI($out_trade_no,$payment_money,$openid,$appid,$store_id,$type,$title = '来客电商')
    {   
        file_put_contents('cz.php',print_r($payment_money,true));
        $log = new LaiKeLogUtils();
        $config = LKTConfigInfo::getPayConfig($store_id,$type);
        if (empty($config)) 
        {
            $log->log('wechat/payment_JSAPI.log',$type."执行日期：".date('Y-m-d H:i:s')."支付暂未配置".$store_id."，无法调起支付！");
            return 'file';
        }
        
        $profit_sharing = PC_Tools::Obtain_the_splitting_status(array('store_id'=>$store_id));
        if($profit_sharing == 'Y')
        {
            cache('Divide_accounts_list_'.$out_trade_no, $profit_sharing);
        }

        $body = $title;
        // $appid =        $appid; // 如果是公众号 就是公众号的appid
        $body =         $body; // 公司名称
        $mch_id =       $config['mch_id']; // 商户id
        $mch_key =      $config['mch_key']; // 商户key
        $nonce_str =    self::nonce_str(); // 随机字符串
        $notify_url =   $config['notify_url'];
        $spbill_create_ip = isset($config['spbill_create_ip'])?$config['spbill_create_ip']:'120.76.189.152'; // ip地址
        $total_fee =    $payment_money*100; // 因为充值金额最小是1 而且单位为分 如果是充值1元所以这里需要*100
        $trade_type = 'JSAPI'; // 交易类型 默认
        // 这里是按照顺序的 因为下面的签名是按照顺序 排序错误 肯定出错
        $post['appid'] = $appid; // 如果是公众号 就是公众号的appid
        $post['body'] = $body; // 公司名称
        $post['mch_id'] = $mch_id; // 商户id
        $post['nonce_str'] = $nonce_str;//随机字符串
        $post['notify_url'] = $notify_url;
        $post['openid'] = $openid; // 微信id
        $post['out_trade_no'] = $out_trade_no; // 商户订单号
        $post['profit_sharing'] = $profit_sharing; // Y-是，需要分账    N-否，不分账
        $post['spbill_create_ip'] = $spbill_create_ip; // 终端的ip
        $post['total_fee'] = $total_fee; // 总金额 最低为一块钱 必须是整数
        $post['trade_type'] = $trade_type; // 交易类型
        
        $sign = self::sign($post,$mch_key);//签名
        $post_xml = '<xml>
               <appid>'.$appid.'</appid>
               <body>'.$body.'</body>
               <mch_id>'.$mch_id.'</mch_id>
               <nonce_str>'.$nonce_str.'</nonce_str>
               <notify_url>'.$notify_url.'</notify_url>
               <openid>'.$openid.'</openid>
               <out_trade_no>'.$out_trade_no.'</out_trade_no>
               <profit_sharing>'.$profit_sharing.'</profit_sharing>
               <spbill_create_ip>'.$spbill_create_ip.'</spbill_create_ip>
               <total_fee>'.$total_fee.'</total_fee>
               <trade_type>'.$trade_type.'</trade_type>
               <sign>'.$sign.'</sign>
            </xml> ';
        //统一接口prepay_id
        $url = 'https://api.mch.weixin.qq.com/pay/unifiedorder';
        $xml = self::http_request($url,$post_xml);
        $array = self::FromXml($xml);//全要大写

        if($array['return_code'] == 'SUCCESS' && $array['result_code'] == 'SUCCESS')
        {
            $time = time();
            $tmp=[];//临时数组用于签名
            $tmp['appId'] = $appid;
            $tmp['nonceStr'] = $nonce_str;
            $tmp['package'] = 'prepay_id='.$array['prepay_id'];
            $tmp['signType'] = 'MD5';
            $tmp['timeStamp'] = "$time";

            $data['state'] = 1;
            $data['timeStamp'] = "$time";//时间戳
            $data['appid'] = $appid;//时间戳
            $data['nonceStr'] = $nonce_str;//随机字符串
            $data['signType'] = 'MD5';//签名算法，暂支持 MD5
            $data['package'] = 'prepay_id='.$array['prepay_id'];//统一下单接口返回的 prepay_id 参数值，提交格式如：prepay_id=*
            $data['paySign'] = self::sign($tmp,$mch_key);//签名,具体签名方案参见微信公众号支付帮助文档;
            $data['out_trade_no'] = $out_trade_no;
        }
        else
        {
            $data['state'] = $array;
        }

        $log->log('wechat/payment_JSAPI.log',"【".$type."调用支付】执行日期：".date('Y-m-d H:i:s').json_encode($data));

        return $data;
        
    }

    //二维码支付
    public static function payment_PC($out_trade_no,$payment_money,$appid,$store_id,$type,$title = '来客电商'){
        $config = LKTConfigInfo::getPayConfig($store_id,$type);

        $log = new LaiKeLogUtils();

        if (empty($config)) 
        {
            $log -> log('wechat/'.$type.'_.log',$type."执行日期：".date('Y-m-d H:i:s')."\n支付暂未配置".$store_id."，无法调起支付！\r\n");
            return 'file';
        }

        $profit_sharing = PC_Tools::Obtain_the_splitting_status(array('store_id'=>$store_id));
        if($profit_sharing == 'Y')
        {
            cache('Divide_accounts_list_'.$out_trade_no, $profit_sharing);
        }

        $body = $title;
        // $appid =        $appid; // 如果是公众号 就是公众号的appid
        $body =         $body; // 公司名称
        $mch_id =       $config['mch_id']; // 商户id
        $mch_key =      $config['mch_key']; // 商户key
        $nonce_str =    self::nonce_str(); // 随机字符串
        $notify_url =   $config['notify_url'];
        $spbill_create_ip = isset($config['spbill_create_ip'])?$config['spbill_create_ip']:'120.76.189.152'; // ip地址
        $total_fee =    $payment_money*100; // 因为充值金额最小是1 而且单位为分 如果是充值1元所以这里需要*100
        $trade_type = 'NATIVE'; // 交易类型 默认

        // 这里是按照顺序的 因为下面的签名是按照顺序 排序错误 肯定出错
        $post['appid'] = $appid; // 如果是公众号 就是公众号的appid
        $post['body'] = $body; // 公司名称
        $post['mch_id'] = $mch_id; // 商户id
        $post['nonce_str'] = $nonce_str;//随机字符串
        $post['notify_url'] = $notify_url;
        $post['out_trade_no'] = $out_trade_no; // 商户订单号
        $post['profit_sharing'] = $profit_sharing; // Y-是，需要分账    N-否，不分账
        $post['spbill_create_ip'] = $spbill_create_ip; // 终端的ip
        $post['total_fee'] = $total_fee; // 总金额 最低为一块钱 必须是整数
        $post['trade_type'] = $trade_type; // 交易类型
        $sign = self::sign($post,$mch_key);//签名
        $post_xml = '<xml>
               <appid>'.$appid.'</appid>
               <body>'.$body.'</body>
               <mch_id>'.$mch_id.'</mch_id>
               <nonce_str>'.$nonce_str.'</nonce_str>
               <notify_url>'.$notify_url.'</notify_url>
               <out_trade_no>'.$out_trade_no.'</out_trade_no>
               <profit_sharing>'.$profit_sharing.'</profit_sharing>
               <spbill_create_ip>'.$spbill_create_ip.'</spbill_create_ip>
               <total_fee>'.$total_fee.'</total_fee>
               <trade_type>'.$trade_type.'</trade_type>
               <sign>'.$sign.'</sign>
            </xml> ';
        //统一接口prepay_id
        $url = 'https://api.mch.weixin.qq.com/pay/unifiedorder';
        $xml = self::http_request($url,$post_xml);
        $array = self::FromXml($xml);//全要大写

        if($array['return_code'] == 'SUCCESS' && $array['result_code'] == 'SUCCESS'){
            $uploadImg = dirname(MO_IMAGE_DIR) . '/code_img' ; // 图片上传位置
            if (is_dir($uploadImg) == '')
            { // 如果文件不存在
                mkdir($uploadImg); // 创建文件
            }

            $uploadImg = dirname(MO_IMAGE_DIR) . '/code_img/'.date('Y-m-d') . '/' ; // 图片上传位置
            if (is_dir($uploadImg) == '')
            { // 如果文件不存在
                mkdir($uploadImg); // 创建文件
            }
            $code_url = $array['code_url'];
            // $imgURL_name = time() . mt_rand(1, 1000) . '.png';
            // $uploadImg1 = 'code_img/'.date('Y-m-d') . '/' . $imgURL_name;
            // app('QRcode')::png($code_url,$uploadImg1);
            // $QR = $uploadImg1; //已经生成的原始二维码图片文件
            // ob_clean();
            // imagepng($QR, 'qrcode.png');       
            // imagedestroy($QR);
            $data['code_url'] = $code_url;
        }else{
            $data['state'] = $array;
        }
              
        $log -> log('wechat/'.$type.'_.log',"【".$type."调用支付】执行日期：".date('Y-m-d H:i:s')."\n".json_encode($data)."\r\n");

        return $data;
    }

    //二维码支付
    public function payment_PC0($out_trade_no,$payment_money,$appid,$store_id,$type,$title = '来客电商')
    {
        $config = LKTConfigInfo::getPayConfig($store_id,$type);

        $log = new LaiKeLogUtils();
        if (empty($config))
        {
            $log->log('wechat/'.$type.'_.log',$type."执行日期：".date('Y-m-d H:i:s')."支付暂未配置".$store_id."，无法调起支付！");
            return 'file';
        }

        $profit_sharing = PC_Tools::Obtain_the_splitting_status(array('store_id'=>$store_id));
        if($profit_sharing == 'Y')
        {
            cache('Divide_accounts_list_'.$out_trade_no, $profit_sharing);
        }

        $body = $title;
        // $appid =        $appid; // 如果是公众号 就是公众号的appid
        $body =         $body; // 公司名称
        $mch_id =       $config['mch_id']; // 商户id
        $mch_key =      $config['mch_key']; // 商户key
        $nonce_str =    self::nonce_str(); // 随机字符串
        $notify_url =   $config['notify_url'];
        if(isset($config['spbill_create_ip']))
        {
            $spbill_create_ip = $config['spbill_create_ip']; // ip地址
        }
        else
        {
            $spbill_create_ip = '120.76.189.152'; // ip地址
        }
        $total_fee =    $payment_money*100; // 因为充值金额最小是1 而且单位为分 如果是充值1元所以这里需要*100
        $trade_type = 'NATIVE'; // 交易类型 默认
        $time_expire = date("YmdHis",strtotime(date("Y-m-d H:i:s")." +1 minutes"));
        // 这里是按照顺序的 因为下面的签名是按照顺序 排序错误 肯定出错
        $post['appid'] = $appid; // 如果是公众号 就是公众号的appid
        $post['body'] = $body; // 公司名称
        $post['mch_id'] = $mch_id; // 商户id
        $post['nonce_str'] = $nonce_str;//随机字符串
        $post['notify_url'] = $notify_url;
        $post['out_trade_no'] = $out_trade_no; // 商户订单号
        $post['profit_sharing'] = $profit_sharing; // Y-是，需要分账    N-否，不分账
        $post['spbill_create_ip'] = $spbill_create_ip; // 终端的ip
        $post['time_expire'] = $time_expire;
        $post['total_fee'] = $total_fee; // 总金额 最低为一块钱 必须是整数
        $post['trade_type'] = $trade_type; // 交易类型
        $sign = self::sign($post,$mch_key);//签名

        $post_xml = '<xml>
                <appid>'.$appid.'</appid>
                <body>'.$body.'</body>
                <mch_id>'.$mch_id.'</mch_id>
                <nonce_str>'.$nonce_str.'</nonce_str>
                <notify_url>'.$notify_url.'</notify_url>
                <out_trade_no>'.$out_trade_no.'</out_trade_no>
                <profit_sharing>'.$profit_sharing.'</profit_sharing>
                <spbill_create_ip>'.$spbill_create_ip.'</spbill_create_ip>
                <time_expire>'.$time_expire.'</time_expire>
                <total_fee>'.$total_fee.'</total_fee>
                <trade_type>'.$trade_type.'</trade_type>
                <sign>'.$sign.'</sign>
            </xml> ';

        //统一接口prepay_id
        $url = 'https://api.mch.weixin.qq.com/pay/unifiedorder';
        $xml = self::http_request($url,$post_xml);
        $array = self::FromXml($xml);//全要大写

        if($array['return_code'] == 'SUCCESS' && $array['result_code'] == 'SUCCESS')
        {
            $uploadImg = '../public/image' ; // 图片上传位置
            if (is_dir($uploadImg) == '')
            { // 如果文件不存在
                mkdir($uploadImg); // 创建文件
            }

            $uploadImg = $uploadImg . '/code_img';
            if (is_dir($uploadImg) == '')
            { // 如果文件不存在
                mkdir($uploadImg); // 创建文件
            }

            $uploadImg = $uploadImg . '/' . date('Y-m-d');
            if (is_dir($uploadImg) == '')
            { // 如果文件不存在
                mkdir($uploadImg); // 创建文件
            }

            $code_url = $array['code_url'];
            $imgURL_name = time() . mt_rand(1, 1000) . '.png';
            $uploadImg1 = 'image/code_img/'.date('Y-m-d') . '/' . $imgURL_name;

            app('QRcode')::png($code_url,'../public/' . $uploadImg1);

            $QR = $uploadImg1; //已经生成的原始二维码图片文件
            // @imagepng($QR, 'qrcode.png');
            // @imagedestroy($QR);
            $data['code_url'] = $QR;
        }
        else
        {
            $data['state'] = $array;
        }

        $log->log('wechat/'.$type.'_.log',"【".$type."调用支付】执行日期：".date('Y-m-d H:i:s').json_encode($data));

        return $data;
    }

    // //随机32位字符串
    public static function nonce_str(){
        $result = '';
        $str = 'QWERTYUIOPASDFGHJKLZXVBNMqwertyuioplkjhgfdsamnbvcxz';
        for ($i=0;$i<32;$i++){
            $result .= $str[rand(0,48)];
        }
        return $result;
    }

    //签名 $data要先排好顺序
    public static function sign($data,$mch_key){
        $stringA = '';
        foreach ($data as $key=>$value){
            if(!$value) continue;
            if($stringA) $stringA .= '&'.$key."=".$value;
            else $stringA = $key."=".$value;
        }
        $wx_key = $mch_key;//申请支付后有给予一个商户账号和密码，登陆后自己设置key
        $stringSignTemp = $stringA.'&key='.$wx_key;//申请支付后有给予一个商户账号和密码，登陆后自己设置key    
        return strtoupper(md5($stringSignTemp));
    }

    //curl请求
    public static function http_request($url,$data = null,$headers=array())
    {
        $curl = curl_init();
        if( count($headers) >= 1 ){
            curl_setopt($curl, CURLOPT_HTTPHEADER, $headers);
        }
        curl_setopt($curl, CURLOPT_URL, $url);

        curl_setopt($curl, CURLOPT_SSL_VERIFYPEER, FALSE);
        curl_setopt($curl, CURLOPT_SSL_VERIFYHOST, FALSE);

        if (!empty($data)){
            curl_setopt($curl, CURLOPT_POST, 1);
            curl_setopt($curl, CURLOPT_POSTFIELDS, $data);
        }
        curl_setopt($curl, CURLOPT_RETURNTRANSFER, 1);
        $output = curl_exec($curl);
        curl_close($curl);
        return $output;
    }

    // //获取xml
    public static  function xml($xml){
        $p = xml_parser_create();
        xml_parse_into_struct($p, $xml, $vals, $index);
        xml_parser_free($p);
        $data = "";
        foreach ($index as $key=>$value) {
            if($key == 'xml' || $key == 'XML') continue;
            $tag = $vals[$value[0]]['tag'];
            $value = $vals[$value[0]]['value'];
            $data[$tag] = $value;
        }
        return $data;
    }


    public static function wxrefundapi($ordersNo, $refund, $total_fee, $price,$store_id,$type)
    {
        $log = new LaiKeLogUtils();

        $config = LKTConfigInfo::getPayConfig($store_id,$type);
        if (empty($config)) 
        {
            $log->log('wechat/wxrefundapi.log',$type."执行日期：".date('Y-m-d H:i:s')."支付暂未配置".$store_id."，无法退款！");
            return 'file';
        }
        
        //通过微信api进行退款流程
        $total = round($price*100); // 将元转成分
        $total_fee = round($total_fee*100); // 将元转成分
        $unifiedOrder = new WxPayRefund();
        // $unifiedOrder->SetBody($title);//商品或支付单简要描述
        $unifiedOrder->SetOut_trade_no($ordersNo);
        $unifiedOrder->SetOut_refund_no($refund);//退款单号
        $unifiedOrder->SetTotal_fee($total);
        $unifiedOrder->SetRefund_fee($total_fee);
        $unifiedOrder->SetOp_user_id($ordersNo);
        WxPayApi::$config = $config;
        $result = WxPayApi::refund($unifiedOrder);

        $log->log('wechat/wxrefundapi.log',"【".$type."退款】执行日期：".date('Y-m-d H:i:s')."\n".json_encode($result)."\r\n");

        return $result;
    }
    
    /**
     *  作用：格式化参数，签名过程需要使用
     */
    public static function formatBizQueryParaMap($paraMap, $urlencode)
    {
        $buff = "";
        ksort($paraMap);
        foreach ($paraMap as $k => $v)
        {
            if($urlencode)
            {
               $v = urlencode($v);
            }
            //$buff .= strtolower($k) . "=" . $v . "&";
            $buff .= $k . "=" . $v . "&";
        }
        $reqPar;
        if (strlen($buff) > 0) 
        {
            $reqPar = substr($buff, 0, strlen($buff)-1);
        }
        return $reqPar;
    }

    // 分账
    public static function Divide_accounts($array)
    {
        $store_id = $array['store_id'];
        $shop_id = $array['mch_id']; // 店铺ID
        $accounts_set = $array['accounts_set']; // 平台账号
        $transaction_id = $array['transaction_id']; // 微信返回支付单号唯一标识
        $out_order_no = $array['out_order_no'];  // 支付单号
        $receivers = $array['receivers']; // 分账接收方列表
        $With_proportion = $array['With_proportion']; // 店铺分账记录信息
        $pay = $array['pay'];
        $sNo = $array['sNo'];
        $total_amount = $array['money'];
        $time = date("Y-m-d H:i:s");

        // $pay = 'mini_wechat';
        $config = LKTConfigInfo::getPayConfig($store_id,$pay);
        $appid = $config['appid']; // appid
        $mch_id = $config['mch_id']; // 商户id
        $mch_key = $config['mch_key']; // 商户key

        // 分账API地址：https://pay.weixin.qq.com/wiki/doc/api/allocation.php?chapter=27_6&index=2
        $url = "https://api.mch.weixin.qq.com/secapi/pay/multiprofitsharing";

        $nonce_str = self::nonce_str();
        $sign_type = "HMAC-SHA256";
        $post = array(
            'appid' => $appid,
            'mch_id' => $mch_id,
            'nonce_str' => $nonce_str,
            "out_order_no" => $out_order_no,
            "receivers" => json_encode($receivers),
            'sign_type' => $sign_type,
            "transaction_id" => $transaction_id
        );

        $sign = self::SHA256_sign($post,$mch_key);//签名

        $post_xml = '<xml>
              <appid>'.$appid.'</appid>
              <mch_id>'.$mch_id.'</mch_id>
              <nonce_str>'.$nonce_str.'</nonce_str>
              <out_order_no>'.$out_order_no.'</out_order_no>
              <receivers>'.json_encode($receivers).'</receivers>
              <sign_type>'.$sign_type.'</sign_type>
              <transaction_id>'.$transaction_id.'</transaction_id>
              <sign>'.$sign.'</sign>
            </xml> ';

        $xml = self::postXmlCurl($post_xml,$url,$store_id,true,30);

        $data = self::FromXml($xml);//全要大写

        $log = new LaiKeLogUtils();
        $log->log('wechat/Divide_accounts.log',__METHOD__ . ":" . __LINE__ . "分账接口返回：" . json_encode($data));

        $zh_num = count($With_proportion) - 1;

        if(isset($data['result_code']) && $data['return_code'] == 'SUCCESS')
        {
            foreach($With_proportion as $k => $v)
            {
                $account = $v['account']; // 分账接收方账号
                $amount = $v['amount'] / 100; // 分账总金额
                $proportion = $v['proportion']; // 分账比例

                $is_platform_account = 0;
                if($zh_num == $k)
                {
                    $is_platform_account = 1;
                }
                $sql0 = "insert into lkt_mch_distribution_record(mch_id,sub_mch_id,account,order_no,wx_order_no,out_order_no,total_amount,amount,proportion,r_type,add_date,is_platform_account) value ('$shop_id','','$account','$sNo','$transaction_id','$out_order_no','$total_amount','$amount','$proportion',1,'$time','$is_platform_account')";
                $r0 = Db::execute($sql0);
            }
            return 1;
        }
        else
        {
            return 0;
        }
    }
    
    //签名 $data要先排好顺序
    public static function SHA256_sign($data,$mch_key){
        $stringA = '';
        foreach ($data as $key=>$value){
            if(!$value) continue;
            if($stringA) $stringA .= '&'.$key."=".$value;
            else $stringA = $key."=".$value;
        }
        $wx_key = $mch_key;//申请支付后有给予一个商户账号和密码，登陆后自己设置key
        $stringSignTemp = $stringA.'&key='.$wx_key;//申请支付后有给予一个商户账号和密码，登陆后自己设置key    

        return strtoupper(hash_hmac("sha256",$stringSignTemp,$wx_key));
    }

    /**
	 * 以post方式提交xml到对应的接口url
	 * 
	 * @param WxPayConfigInterface $config  配置对象
	 * @param string 	$xml  		需要post的xml数据
	 * @param string 	$url  		url
	 * @param bool 		$useCert 	是否需要证书，默认不需要
	 * @param int 		$second   	url执行超时时间，默认30s
	 */
	public static function postXmlCurl($xml, $url,$store_id, $useCert = false, $second = 30)
	{
        $config = LKTConfigInfo::getPayConfig($store_id,'mini_wechat');
        $appid = $config['appid']; // appid
        $mch_id = $config['mch_id']; // 商户id

		$ch 			= curl_init();
 
		//设置超时
		curl_setopt($ch, CURLOPT_TIMEOUT, $second);
 
		$proxyHost = "0.0.0.0";
		$proxyPort = 0;
 
		// 如果有配置代理这里就设置代理
		if ($proxyHost != "0.0.0.0" && $proxyPort != 0) 
        {
			curl_setopt($ch, CURLOPT_PROXY, $proxyHost);
			curl_setopt($ch, CURLOPT_PROXYPORT, $proxyPort);
		}
 
		curl_setopt($ch, CURLOPT_URL, $url);
		// curl_setopt($ch,CURLOPT_SSL_VERIFYPEER,TRUE);
		// curl_setopt($ch,CURLOPT_SSL_VERIFYHOST,2);//严格校验
		curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, FALSE);
		curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, FALSE); //严格校验
		// curl_setopt($ch, CURLOPT_USERAGENT, $ua);
		// 设置header
		curl_setopt($ch, CURLOPT_HEADER, FALSE);
		// 要求结果为字符串且输出到屏幕上
		curl_setopt($ch, CURLOPT_RETURNTRANSFER, TRUE);
 
		if ($useCert == true) 
        {
			// 设置证书
			// 使用证书：cert 与 key 分别属于两个.pem文件
			// 证书文件请放入服务器的非web目录下
            curl_setopt($ch,CURLOPT_SSLCERTTYPE,'PEM');
			curl_setopt($ch,CURLOPT_SSLCERT, $config['sslcert_path']);
			curl_setopt($ch,CURLOPT_SSLKEYTYPE,'PEM');
			curl_setopt($ch,CURLOPT_SSLKEY, $config['sslkey_path']);
		}
 
		// post提交方式
		curl_setopt($ch, CURLOPT_POST, TRUE);
		curl_setopt($ch, CURLOPT_POSTFIELDS, $xml);
 
		// 运行curl
		$data = curl_exec($ch);
 
		// 返回结果
		if ($data) 
        {
			curl_close($ch);
			return $data;
		} 
        else 
        {
			$error = curl_errno($ch);
			curl_close($ch);
			throw new WxPayException("curl出错，错误码:$error");
		}
	}
}


?>