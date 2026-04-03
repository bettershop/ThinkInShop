<?php
namespace app\common;
use think\facade\Db;

use app\common\LaiKeLogUtils;
use OSS\OssClient;
/**
 * 阿里云OSS帮助类
 * Class OSSCommon
 */
class OSSCommon
{

    /**
     * 获取OSS 配置信息
     * @return array
     */
    public static function getOSSConfig()
    {
        return LKTConfigInfo::getOSSConfig();
    }

    /**
     * 获取OSS 客户端
     * @return OssClient
     * @throws Exception
     */
    public static function getOSSClient()
    {
        $ossconfig = OSSCommon::getOSSConfig();
        if (empty($ossconfig))
        {
            throw new Exception("获取OSS客户端失败!");
        }
        $accessKeyId = $ossconfig['accessKeyId'];
        $accessKeySecret = $ossconfig['accessKeySecret'];
        $endpoint = $ossconfig['endpoint'];
        $isopenzdy = $ossconfig['isopenzdy'];
        $MyEndpoint = isset($ossconfig['MyEndpoint'])?$ossconfig['MyEndpoint']:"";
        try
        {
            $ossClient = new OssClient($accessKeyId, $accessKeySecret, $endpoint);
            if( $isopenzdy == 1 ){
                $ossClient->addBucketCname($ossconfig['bucket'], $MyEndpoint);
            }
            return $ossClient;
        } catch (OssException $e)
        {
            throw new Exception($e->getMessage());
        }
    }
}