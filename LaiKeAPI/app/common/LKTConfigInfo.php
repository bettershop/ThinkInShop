<?php

namespace app\common;
use think\facade\Db;

use app\admin\model\UploadSetModel;
use app\admin\model\ConfigModel;
/**
 * LKT 来客推配置信息获取类
 */
class LKTConfigInfo
{


    function __construct()
    {

    }

    /**
     * 阿里云OSS配置信息
     */
    public static function getOSSConfig($type = '')
    {//oss配置为所有商户使用，store_id为1

        //查询文件上传配置方式
        $upserver = Db::name('config')->where('store_id', 1)->value('upserver');
        //默认阿里云
        if(empty($upserver)) $upserver = 2;
        if($type) $upserver = $type;
        $res = UploadSetModel::where('upserver',$upserver)->field('attr,attrvalue')->select()->toArray();
        $oss = array();

        if ($res)
        {

            foreach ($res as $k => $v)
            {
                $attr = $v['attr'];

                $attrvalue = $v['attrvalue'];

                switch ($attr)
                {
                    case 'AccessKeySecret':
                        $oss['accessKeySecret'] = $attrvalue;
                        break;

                    case 'AccessKeyID':
                        $oss['accessKeyId'] = $attrvalue;
                        break;
                    case 'Endpoint':
                        $oss['endpoint'] = $attrvalue;
                        break;
                    case 'Bucket':
                        $oss['bucket'] = $attrvalue;
                        break;
                    case 'isopenzdy':
                        $oss['isopenzdy'] = $attrvalue;
                        break;
                    case 'MyEndpoint':
                        $oss['MyEndpoint'] = $attrvalue;
                        break;
                    case 'serveruri':
                        $oss['serveruri'] = $attrvalue;
                        break;
                }
            }
        }

        return $oss;
    }

    /**
     * 支付配置信息
     */
    public static function getPayConfig($store_id, $type)
    {
        $sql = "select a.config_data from lkt_payment_config a left join lkt_payment b on a.pid=b.id where a.store_id='$store_id' and a.status = 1 and b.class_name='$type'";
        $res = Db::query($sql);
        $config = array();
        if ($res)
        {
            if ($type == 'baidu_pay')
            {
                $list = unserialize($res[0]['config_data']);
            }
            else
            {
                $list = json_decode($res[0]['config_data']);
            }
            if (!empty($list) && count((array)$list) > 0)
            {
                $config = (array)$list;
            }
        }
        return $config;
    }

    /**
     * 系统配置信息
     */
    public static function getSysConfig($store_id)
    {

    }

    public static function getH5Domain($store_id)
    {
        $rr = ConfigModel::where('store_id',$store_id)->field('H5_domain')->select()->toArray();
        if (!empty($rr[0]->H5_domain))
        {
            return $rr[0]->H5_domain;
        }
        return "/#/";
    }

    /**
     * 获取拼团配信息
     */
    public static function getPTconfig()
    {

    }

    /**
     * 获取插件配置信息
     */
    public static function getPluginConfig()
    {

    }


}

?>