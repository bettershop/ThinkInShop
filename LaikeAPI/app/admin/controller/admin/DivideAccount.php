<?php
namespace app\admin\controller\admin;

use app\BaseController;
use app\common;
use think\facade\Db;
use think\facade\Request;
use app\common\Tools;
use app\common\ServerPath;
use app\common\LaiKeLogUtils;
use app\common\Jurisdiction;
use app\common\LKTConfigInfo;


/**
 * 功能：分账设置
 * 修改人：DHB
 */
class DivideAccount extends BaseController
{   
    // 分账设置
    public function divideAccountInfo()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));

        $mch_id = addslashes(trim($this->request->param('mchId')));

        $accounts_set = '';
        $list = array();

        $sql0 = "select accounts_set from lkt_config where store_id = '$store_id' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $accounts_set = $r0[0]['accounts_set'];
        }

        $list_0 = array();
        $sql_0 = "select a.value,a.text from lkt_data_dictionary_list as a left join lkt_data_dictionary_name as b on a.sid = b.id where a.recycle = 0 and b.name = '分账接收方关系' and a.status = 1";
        $r_0 = Db::query($sql_0);
        if($r_0)
        {
            foreach($r_0 as $k_0 => $v_0)
            {
                $list_0[$v_0['value']] = $v_0['text'];
            }
        }

        $list_1 = array();
        $sql_1 = "select a.value,a.text from lkt_data_dictionary_list as a left join lkt_data_dictionary_name as b on a.sid = b.id where a.recycle = 0 and b.name = '分账接收方类型' and a.status = 1";
        $r_1 = Db::query($sql_1);
        if($r_1)
        {
            foreach($r_1 as $k_1 => $v_1)
            {
                $list_1[$v_1['value']] = $v_1['text'];
            }
        }
        
        $sql1 = "select id,mch_id,d_type,account,relationship,proportion,name,add_date from lkt_mch_distribution where mch_id = '$mch_id' ";
        $r1 = Db::query($sql1);
        if($r1)
        {
            foreach($r1 as $k1 => $v1)
            {
                $v1['type'] = $v1['d_type'];
                $v1['proportion'] = round($v1['proportion']);

                $v1['relationshipDesc'] = $list_0[$v1['relationship']];
                $v1['typeDesc'] = $list_1[$v1['d_type']];
                $list[] = $v1;
            }
        }

        $data = array('accounts_set'=>$accounts_set,'list'=>$list);
        $message = Lang("Success");
        return output(200,$message,$data);
    }

    // 设置分账设置
    public function saveDivideAccount()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = trim($this->request->param('accessId'));

        $mch_id = addslashes(trim($this->request->param('mchId')));
        $divideAccountInfo = trim($this->request->param('divideAccountInfo'));

        $list = json_decode($divideAccountInfo,true);

        $time = date("Y-m-d H:i:s");
        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        Db::startTrans();

        $sql1 = "select * from lkt_mch_distribution where mch_id = '$mch_id' ";
        $r1 = Db::query($sql1);
        if($r1)
        {
            foreach($r1 as $k1 => $v1)
            {
                $receiver1 = array('type'=>strtoupper($v1['d_type']),'account'=>$v1['account']);

                $return1 = $this->delDivideAccountsReceiver($store_id,json_encode($receiver1));
                if($return1 != '')
                {
                    Db::rollback();
                    return output(109,$return1);
                }
            }
            
            $sql2 = "delete from lkt_mch_distribution where mch_id = '$mch_id' ";
            $r2 = Db::execute($sql2);
        }

        $Remaining_proportion = 100; // 平台比例
        $receiver = array();
        if($list != array())
        {
            foreach($list as $k => $v)
            {
                $d_type = $v['type']; // 分账接收方类型
                $account = $v['account']; // 分账接收方账号
                $relationship = $v['relationship']; // 分账接收方关系
                $proportion = $v['proportion']; // 分账比例
                $name = '';
                if(isset($v['name']))
                {
                    $name = $v['name']; // 分账接收方全称
                }
                if(strtoupper($d_type) == 'MERCHANT_ID')
                {
                    $receiver[] = array('type'=>strtoupper($d_type),'account'=>$account,'name'=>$name,'relation_type'=>strtoupper($relationship));
                }
                else
                {
                    $receiver[] = array('type'=>strtoupper($d_type),'account'=>$account,'relation_type'=>strtoupper($relationship));
                }
                
                $Remaining_proportion = $Remaining_proportion - $proportion; // 平台比例 - 分账比例
                if($Remaining_proportion < 0)
                {
                    Db::rollback();
                    $Jurisdiction->admin_record($store_id, $operator, '修改了店铺ID为 '.$mch_id.' 的分账设置失败',2,1,0,$operator_id);
                    $message = Lang('divideAccount.3');
                    return output(109,$message);
                }

                $sql3 = "insert into lkt_mch_distribution(mch_id,d_type,account,name,relationship,proportion,add_date) value ('$mch_id','$d_type','$account','$name','$relationship','$proportion','$time') ";
                $r3 = Db::execute($sql3);
                if ($r3 < 1)
                {
                    Db::rollback();
                    $Jurisdiction->admin_record($store_id, $operator, '修改了店铺ID为 '.$mch_id.' 的分账设置失败',2,1,0,$operator_id);
                    $message = Lang('divideAccount.2');
                    return output(109,$message);
                }
            }
            
            $return = $this->addDivideAccountsReceiver($store_id,json_encode($receiver));
            if($return != '')
            {
                Db::rollback();
                return output(109,$return);
            }
        }

        Db::commit();
        $Jurisdiction->admin_record($store_id, $operator, '修改了店铺ID为 '.$mch_id.' 的分账设置',2,1,0,$operator_id);
        $message = Lang("Success");
        return output(200,$message);
    }

    // 申请分账账单
    public function applyBilling()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = trim($this->request->param('accessId'));

        $date = trim($this->request->param('date'));
        
        $lktlog = new LaiKeLogUtils();
        $Jurisdiction = new Jurisdiction();
        $operator_id = cache($access_id.'admin_id');
        $operator = cache($access_id.'admin_name');

        $data = array();

        $url = "https://api.mch.weixin.qq.com/v3/profitsharing/bills?sub_mchid=&bill_date=".$date."&tar_type=GZIP";

        $lktlog->log("admin/DivideAccount.log",__METHOD__ . ":" . __LINE__ . "分账传参：" . $url);
        
        $Tools = new Tools($store_id, 1);
        $data = $Tools->https_get($url, $type = 1);

        $lktlog->log("admin/DivideAccount.log",__METHOD__ . ":" . __LINE__ . "分账接口返回：" . $data);
        $data = json_decode($data,true);

        if(isset($data['download_url']))
        {
            $download_url = $data['download_url'];
            $data = array('download_url'=>$download_url);
            $Jurisdiction->admin_record($store_id, $operator, '下载分账账单',2,1,0,$operator_id);
            $message = Lang("Success");
            return output(200,$message,$data);
        }
        else
        {
            $message = Lang("Request error");
            return output(109,$message);
        }
    }

    // 下载账单
    public function downloadBilling()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = trim($this->request->param('accessId'));

        $url = trim($this->request->param('url'));
        
        $lktlog = new LaiKeLogUtils();

        $data = array();

        $lktlog->log("admin/DivideAccount.log",__METHOD__ . ":" . __LINE__ . "分账传参：" . $url);
        
        $Tools = new Tools($store_id, 1);
        $data = $Tools->https_get($url, $type = 1);

        $lktlog->log("admin/DivideAccount.log",__METHOD__ . ":" . __LINE__ . "分账接口返回：" . $data);
        $data = json_decode($data,true);

        if(isset($data['download_url']))
        {
            $download_url = $data['download_url'];
            $data = array('download_url'=>$download_url);
        }

        $message = Lang("Success");
        return output(200,$message,$data);
    }

    // 分账记录
    public function divideRecord()
    {
        $store_id = trim($this->request->param('storeId'));
        $store_type = trim($this->request->param('storeType'));
        $access_id = trim($this->request->param('accessId'));
        
        $condition = trim($this->request->param('condition'));
        $startDate = trim($this->request->param('startDate'));
        $endDate = trim($this->request->param('endDate'));
        $page = trim($this->request->param('pageNo'));
        $pagesize = trim($this->request->param('pageSize'));
        $page = $page ? $page : '1';
        $pagesize = $pagesize ? $pagesize : '10';

        $shop_id = cache($access_id.'_'.$store_type);

        $start = 0;
        if($page)
        {
            $start = ($page - 1) * $pagesize;
        }

        $con = " a.mch_id = '$shop_id' and b.mch_id = '$shop_id' ";
        if($condition != '')
        {
            $condition = Tools::FuzzyQueryConcatenation($condition);
            $con .= " and (a.order_no like $condition or b.account like $condition) ";
        }

        if($startDate != '')
        {
            $con .= " and a.add_date >= '$startDate' ";
        }

        if($endDate != '')
        {
            $con .= " and a.add_date <= '$endDate' ";
        }

        $total = 0;
        $list = array();

        $sql0 = "select count(a.id) as total from lkt_mch_distribution_record as a left join lkt_mch_distribution as b on a.sub_mch_id = b.id where $con ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $total = $r0[0]['total'];
        }

        $sql0 = "select a.*,b.account from lkt_mch_distribution_record as a left join lkt_mch_distribution as b on a.sub_mch_id = b.id where $con order by a.add_date desc limit $start,$pagesize ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $list = $r0;
        }

        $data = array('list'=>$list,'total'=>$total);
        $message = Lang("Success");
        return output(200,$message,$data);
    }

    // 添加微信分账接收方
    public function addDivideAccountsReceiver($store_id,$receiver)
    {
        $config = LKTConfigInfo::getPayConfig($store_id,'mini_wechat');
        $mch_id = $config['mch_id']; // 商户id
        $mch_key = $config['mch_key']; // 商户key
        $appid = $config['appid'];

        $receiver = json_decode($receiver,true);
        
        foreach($receiver as $k => $v)
        {
            $tmp_receive_data = [
                'mch_id' => $mch_id,
                'appid' => $appid,
                'nonce_str' => $this->getNonceStr(),
                'sign_type' => 'HMAC-SHA256',
                'receiver' => json_encode($v)
            ];
            $url = "https://api.mch.weixin.qq.com/pay/profitsharingaddreceiver";
            $tmp_receive_data['sign'] = $this->makeSign($tmp_receive_data, $mch_key);
            
            $xml = $this->arrayToXml($tmp_receive_data);

            $curl_arr = $this->postXmlCurl($xml, $url);

            $result = $this->xmlToArray($curl_arr);
            
            if($result['return_code'] == 'SUCCESS' )
            {
                if($result['result_code'] == 'SUCCESS')
                {
                    
                }
                else
                {
                    return $result['err_code_des'];
                }
            }
            else
            {
                return $result['return_msg'];
            }
        }
        return;
    }

    // 删除微信分账接收方
    public function delDivideAccountsReceiver($store_id,$receiver)
    {
        $config = LKTConfigInfo::getPayConfig($store_id,'mini_wechat');
        $mch_id = $config['mch_id']; // 商户id
        $mch_key = $config['mch_key']; // 商户key
        $appid = $config['appid'];

        $tmp_receive_data = [
            'mch_id' => $mch_id,
            'appid' => $appid,
            'nonce_str' => $this->getNonceStr(),
            'sign_type' => 'HMAC-SHA256',
            'receiver' => $receiver
        ];
        $url = "https://api.mch.weixin.qq.com/pay/profitsharingremovereceiver";
        $tmp_receive_data['sign'] = $this->makeSign($tmp_receive_data, $mch_key);

        $xml = $this->arrayToXml($tmp_receive_data);

        $curl_arr = $this->postXmlCurl($xml, $url);

        $result = $this->xmlToArray($curl_arr);

        if($result['return_code'] == 'SUCCESS' )
        {
            if($result['result_code'] == 'SUCCESS')
            {
                
            }
            else
            {
                return $result['err_code_des'];
            }
        }
        else
        {
            return $result['return_msg'];
        }
        
        return;
    }

    /**
     * Notes: 获取随机数
     * @param int $length
     * @return string
     */
    private function getNonceStr($length = 32)
    {
        $chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        $str = "";
        for ($i = 0; $i < $length; $i++) 
        {
            $str .= substr($chars, mt_rand(0, strlen($chars) - 1), 1);
        }
        return $str;
    }

    /**
     * Notes: 生成sign
     * @param $arr
     * @param $secret
     * @return string
     */
    private function makeSign($arr, $secret)
    {
        //签名步骤一：按字典序排序参数
        ksort($arr);
        $str = $this->toUrlParams($arr);
        //签名步骤二：在str后加入KEY
        $str = $str . "&key=" . $secret;
        //签名步骤三：HMAC-SHA256 类型  加密的字符串 key是商户秘钥
        $str = hash_hmac('sha256', $str, $this->mch_secrect);
        //签名步骤四：所有字符转为大写
        $result = strtoupper($str);
        return $result;
    }

    /**
     * Notes: 数组转字符串
     * @param $arr
     * @return string
     */
    private function toUrlParams($arr)
    {
        $str = "";
        foreach ($arr as $k => $v) 
        {
            if (!empty($v) && ($k != 'sign')) 
            {
                $str .= "$k" . "=" . $v . "&";
            }
        }
        $str = rtrim($str, "&");
        return $str;
    }

    /**
     * Notes: 数组转XML
     * @param $arr
     * @return string
     */
    private function arrayToXml($arr)
    {
        $xml = '<?xml version="1.0" encoding="UTF-8"?><xml>';
        foreach ($arr as $key => $val) 
        {
            $xml.="<".$key.">$val</".$key.">";
        }
        $xml.="</xml>";
        return $xml;
    }

    /**
     * Notes: POST 请求 此处不需要证书
     * @param $xml
     * @param $url
     * @param int $second
     * @return bool|string
     */
    private function postXmlCurl($xml, $url, $second = 30)
    {
        //初始化curl
        $ch = curl_init();
        //设置超时
        curl_setopt($ch, CURLOPT_TIMEOUT, $second);
        curl_setopt($ch,CURLOPT_URL, $url);
        curl_setopt($ch,CURLOPT_SSL_VERIFYPEER,FALSE);
        curl_setopt($ch,CURLOPT_SSL_VERIFYHOST,FALSE);
        //设置header
        curl_setopt($ch, CURLOPT_HEADER, FALSE);
        //要求结果为字符串且输出到屏幕上
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, TRUE);
        //post提交方式
        curl_setopt($ch, CURLOPT_POST, TRUE);
        curl_setopt($ch, CURLOPT_POSTFIELDS, $xml);
        //运行curl
        $data = curl_exec($ch);
        //curl_close($ch);
        //返回结果
        if($data)
        {
            curl_close($ch);
            return $data;
        }
        else
        {
            $error = curl_errno($ch);
            echo "curl出错，错误码:$error"."<br>";
            echo "<a href='http://curl.haxx.se/libcurl/c/libcurl-errors.html'>错误原因查询</a></br>";
            curl_close($ch);
            return false;
        }
    }

    /**
     * Notes: XML转数组
     * @param $xml
     * @return mixed
     */
    private function xmlToArray($xml)
    {
        libxml_disable_entity_loader(true);
        $arr = json_decode(json_encode(simplexml_load_string($xml, 'SimpleXMLElement', LIBXML_NOCDATA)), true);
        return $arr;
    }
}
