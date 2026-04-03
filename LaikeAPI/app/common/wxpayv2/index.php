<?php
namespace app\common\wxpayv2;

require_once "WxPay.Api.php";
require_once "WxPay.Data.php";
require_once "../app/common/phpqrcode.php";

use think\facade\Db;

use app\common\LKTConfigInfo;
use app\common\LaiKeLogUtils;


// 获取支付金额
class wxpay
{
	
	public static function payment_APP($out_trade_no,$total = 100,$title = '来客电商',$store_id,$type='app_wechat')
	{

        $log = new LaiKeLogUtils('wechat/app.log');

        $config = LKTConfigInfo::getPayConfig($store_id,$type);

        if (empty($config)) {

                $log -> customerLog($type."执行日期：".date('Y-m-d H:i:s')."\n支付暂未配置".$store_id."，无法调起支付！\r\n");

                return 'file';
        }

		$total = round($total*100); // 将元转成分
		$unifiedOrder = new WxPayUnifiedOrder();
        $unifiedOrder->config = $config;
		$unifiedOrder->SetBody($title);//商品或支付单简要描述
		$unifiedOrder->SetOut_trade_no($out_trade_no);
		$unifiedOrder->SetTotal_fee($total);
		$unifiedOrder->SetTrade_type("APP");
		// var_dump($title);
        WxPayApi::$config = $config;
		$result = WxPayApi::unifiedOrder($unifiedOrder);

        $log -> customerLog("【APP调用支付】执行日期：".date('Y-m-d H:i:s')."\n".json_encode($result)."\r\n");

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
        if(!$xml){
            echo "xml数据异常！";
        }
        //将XML转为array
        //禁止引用外部xml实体
        libxml_disable_entity_loader(true);
        $data = json_decode(json_encode(simplexml_load_string($xml, 'SimpleXMLElement', LIBXML_NOCDATA)), true);
        return $data;
    }
    
    public static function payment_H5($out_trade_no,$total = 100,$title = '来客电商',$store_id,$type)
    {

        $config = LKTConfigInfo::getPayConfig($store_id,$type);

        $log = new LaiKeLogUtils('wechat/h5.log');

        if (empty($config)) {

                $log -> customerLog($type."执行日期：".date('Y-m-d H:i:s')."\n支付暂未配置".$store_id."，无法调起支付！\r\n");

                return 'file';
        }

        $total_fee =    round($total*100); // 因为充值金额最小是1 而且单位为分 如果是充值1元所以这里需要*100
        $body =         mb_substr($title,0,8,'utf-8');
        $appid =        $config['appid']; // 如果是公众号 就是公众号的appid
        $mch_id =       $config['mch_id']; // 商户id
        $mch_key =      $config['mch_key']; // 商户key
        $nonce_str =    self::nonce_str(); // 随机字符串
        $notify_url =   $config['notify_url'];
        $spbill_create_ip = $config['spbill_create_ip']?$config['spbill_create_ip']:'120.76.189.152'; // ip地址
        $trade_type = 'MWEB'; // 交易类型 默认
        $scene_info ='{"h5_info":{"type":"Wap","wap_url":"https://xiaochengxu.laiketui.com","wap_name":"支付"}}';//场景信息 必要参数

        // 这里是按照顺序的 因为下面的签名是按照顺序 排序错误 肯定出错
        $post['appid'] = $appid; // 如果是公众号 就是公众号的appid
        $post['body'] = $body; // 公司名称
        $post['mch_id'] = $mch_id; // 商户id
        $post['nonce_str'] = $nonce_str;//随机字符串
        $post['notify_url'] = $notify_url;
        $post['out_trade_no'] = $out_trade_no; // 商户订单号
        $post['scene_info'] = $scene_info; //场景信息
        $post['spbill_create_ip'] = $spbill_create_ip; // 终端的ip
        $post['total_fee'] = $total_fee; // 交易金额
        $post['trade_type'] = $trade_type; // 交易类型
        $sign = self::sign($post,$mch_key);//签名

        $post_data="<xml><appid>$appid</appid><body>$body</body><mch_id>$mch_id</mch_id><nonce_str>$nonce_str</nonce_str><notify_url>$notify_url</notify_url><out_trade_no>$out_trade_no</out_trade_no><scene_info>$scene_info</scene_info><spbill_create_ip>$spbill_create_ip</spbill_create_ip><total_fee>$total_fee</total_fee><trade_type>$trade_type</trade_type><sign>$sign</sign></xml>";//拼接成XML 格式
        $log -> customerLog("【支付参数】执行日期：".date('Y-m-d H:i:s')."\n".$post_data."\r\n");

        $url = "https://api.mch.weixin.qq.com/pay/unifiedorder";//微信传参地址
        $xml = self::http_request($url,$post_data);
        $array = self::FromXml($xml);//全要大写

        $log -> customerLog("【h5调用支付】执行日期：".date('Y-m-d H:i:s')."\n".json_encode($array)."\r\n");

        $order_type = substr($out_trade_no, 0, 2);
        $ret = array();
        if($array['return_code'] == 'SUCCESS' && $array['result_code'] == 'SUCCESS'){
            $h5domain = LKTConfigInfo::getH5Domain($store_id)."pages/pay/payResult?sNo=".$out_trade_no."&payment_money=".$total."&order_type=".$order_type."&isH5=true";
            $mweb_url= $array['mweb_url']."&redirect_url=".urlencode($h5domain);
            $ret['mweb_url']= $mweb_url;
            $ret['prepayid']= $array['prepay_id'];
        }
        
        return $ret;
    }


    //提交支付 JSAPI
    public static function payment_JSAPI($out_trade_no,$payment_money,$openid,$title = '来客电商',$appid,$store_id,$type){

        $config = LKTConfigInfo::getPayConfig($store_id,$type);

        $log = new LaiKeLogUtils('wechat/'.$type.'_.log');

        if (empty($config)) {

                $log -> customerLog($type."执行日期：".date('Y-m-d H:i:s')."\n支付暂未配置".$store_id."，无法调起支付！\r\n");

                return 'file';
        }

        $body = $title;
        // $appid =        $appid; // 如果是公众号 就是公众号的appid
        $body =         $body; // 公司名称
        $mch_id =       $config['mch_id']; // 商户id
        $mch_key =      $config['mch_key']; // 商户key
        $nonce_str =    self::nonce_str(); // 随机字符串
        $notify_url =   $config['notify_url'];
        $spbill_create_ip = $config['spbill_create_ip']?$config['spbill_create_ip']:'120.76.189.152'; // ip地址
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

        }else{
            $data['state'] = $array;
        }

        $log -> customerLog("【".$type."调用支付】执行日期：".date('Y-m-d H:i:s')."\n".json_encode($data)."\r\n");

        return $data;
        
    }

    //二维码支付
    public function payment_PC($out_trade_no,$payment_money,$title = '来客电商',$appid,$store_id,$type){
        $config = LKTConfigInfo::getPayConfig($store_id,$type);

        $log = new LaiKeLogUtils('wechat/'.$type.'_.log');

        if (empty($config)) {

                $log -> customerLog($type."执行日期：".date('Y-m-d H:i:s')."\n支付暂未配置".$store_id."，无法调起支付！\r\n");

                return 'file';
        }

        $body = $title;
        // $appid =        $appid; // 如果是公众号 就是公众号的appid
        $body =         $body; // 公司名称
        $mch_id =       $config['mch_id']; // 商户id
        $mch_key =      $config['mch_key']; // 商户key
        $nonce_str =    self::nonce_str(); // 随机字符串
        $notify_url =   $config['notify_url'];
        $spbill_create_ip = $config['spbill_create_ip']?$config['spbill_create_ip']:'120.76.189.152'; // ip地址
        $total_fee =    $payment_money*100; // 因为充值金额最小是1 而且单位为分 如果是充值1元所以这里需要*100
        $trade_type = 'NATIVE'; // 交易类型 默认

        // 这里是按照顺序的 因为下面的签名是按照顺序 排序错误 肯定出错
        $post['appid'] = $appid; // 如果是公众号 就是公众号的appid
        $post['body'] = $body; // 公司名称
        $post['mch_id'] = $mch_id; // 商户id
        $post['nonce_str'] = $nonce_str;//随机字符串
        $post['notify_url'] = $notify_url;
        $post['out_trade_no'] = $out_trade_no; // 商户订单号
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
            $uploadImg = dirname(MO_WEBAPP_DIR) . '/code_img' ; // 图片上传位置
            if (is_dir($uploadImg) == '')
            { // 如果文件不存在
                mkdir($uploadImg); // 创建文件
            }

            $uploadImg = dirname(MO_WEBAPP_DIR) . '/code_img/'.date('Y-m-d') . '/' ; // 图片上传位置
            if (is_dir($uploadImg) == '')
            { // 如果文件不存在
                mkdir($uploadImg); // 创建文件
            }
            $code_url = $array['code_url'];
            $imgURL_name = time() . mt_rand(1, 1000) . '.png';
            $uploadImg1 = 'code_img/'.date('Y-m-d') . '/' . $imgURL_name;
            app('QRcode')::png($code_url,$uploadImg1);
            $QR = $uploadImg1; //已经生成的原始二维码图片文件
            imagepng($QR, 'qrcode.png');       
            imagedestroy($QR);
            $data['code_url'] = $QR;
        }else{
            $data['state'] = $array;
        }
              
        $log -> customerLog("【".$type."调用支付】执行日期：".date('Y-m-d H:i:s')."\n".json_encode($data)."\r\n");

        return $data;
    }

    //二维码支付
    public function payment_PC0($out_trade_no,$payment_money,$title = '来客电商',$appid,$store_id,$type){
        $config = LKTConfigInfo::getPayConfig($store_id,$type);

        $log = new LaiKeLogUtils('wechat/'.$type.'_.log');

        if (empty($config)) {

            $log -> customerLog($type."执行日期：".date('Y-m-d H:i:s')."\n支付暂未配置".$store_id."，无法调起支付！\r\n");

            return 'file';
        }

        $body = $title;
        // $appid =        $appid; // 如果是公众号 就是公众号的appid
        $body =         $body; // 公司名称
        $mch_id =       $config['mch_id']; // 商户id
        $mch_key =      $config['mch_key']; // 商户key
        $nonce_str =    self::nonce_str(); // 随机字符串
        $notify_url =   $config['notify_url'];
        $spbill_create_ip = $config['spbill_create_ip']?$config['spbill_create_ip']:'120.76.189.152'; // ip地址
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

        if($array['return_code'] == 'SUCCESS' && $array['result_code'] == 'SUCCESS'){
            $uploadImg = dirname(MO_WEBAPP_DIR) . '/code_img' ; // 图片上传位置
            if (is_dir($uploadImg) == '')
            { // 如果文件不存在
                mkdir($uploadImg); // 创建文件
            }

            $uploadImg = dirname(MO_WEBAPP_DIR) . '/code_img/'.date('Y-m-d') . '/' ; // 图片上传位置
            if (is_dir($uploadImg) == '')
            { // 如果文件不存在
                mkdir($uploadImg); // 创建文件
            }
            $code_url = $array['code_url'];
            $imgURL_name = time() . mt_rand(1, 1000) . '.png';
            $uploadImg1 = 'code_img/'.date('Y-m-d') . '/' . $imgURL_name;
            QRcode::png($code_url,$uploadImg1);
            $QR = $uploadImg1; //已经生成的原始二维码图片文件
            imagepng($QR, 'qrcode.png');
            imagedestroy($QR);
            $data['code_url'] = $QR;
        }else{
            $data['state'] = $array;
        }

        $log -> customerLog("【".$type."调用支付】执行日期：".date('Y-m-d H:i:s')."\n".json_encode($data)."\r\n");

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

        $log = new LaiKeLogUtils('wechat/return.log');

        $config = LKTConfigInfo::getPayConfig($store_id,$type);

        if (empty($config)) {

                $log -> customerLog($type."执行日期：".date('Y-m-d H:i:s')."\n支付暂未配置".$store_id."，无法退款！\r\n");

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

        $log -> customerLog("【".$type."退款】执行日期：".date('Y-m-d H:i:s')."\n".json_encode($result)."\r\n");

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


}


?>