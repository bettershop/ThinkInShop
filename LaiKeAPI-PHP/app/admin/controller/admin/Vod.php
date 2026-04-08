<?php
namespace app\admin\controller\admin;
session_name("money_mojavi");
session_start();
date_default_timezone_set('Asia/Chongqing');
set_time_limit(0);

use app\common;
use think\facade\Db;
use app\common\LaiKeLogUtils;

header("Content-type:text/html;charset=utf-8");

class Vod
{
    public function apply()
    {
        $lktlog = new LaiKeLogUtils();
        $log_name = 'common/Vod.log';
        $lktlog->log($log_name, __LINE__ . ":" . json_encode(file_get_contents('php://input')));

        $str = file_get_contents('php://input');
        $list = json_decode($str, true);

        $EventType = $list['EventType']; // 事件类型
        $lktlog->log($log_name, __LINE__ . ":" . $EventType);

        if($EventType == 'NewFileUpload')
        { // 视频上传完成
            $file_id = $list['FileUploadEvent']['FileId']; // 文件ID
            $url = $list['FileUploadEvent']['MediaBasicInfo']['MediaUrl']; // 文件路径

            $sql0 = "select * from lkt_bbs_video where file_id = '$file_id' ";
            $r0 = Db::query($sql0);
            if($r0)
            {
                $store_id = $r0[0]['store_id'];

                $response_msg = $this->media_processing($store_id,$file_id); // 媒体处理

                $sql1 = "update lkt_bbs_video set url = '$url',status = 1,response_msg = '$response_msg' where file_id = '$file_id' ";
                $r1 = Db::execute($sql1);
            }
        }
        else if($EventType == 'ProcedureStateChanged')
        { // 任务流状态变更
            $file_id = $list['ProcedureStateChangeEvent']['FileId']; // 文件ID
            $MediaProcessResultSet = $list['ProcedureStateChangeEvent']['MediaProcessResultSet']; // 媒体处理结果集

            $con = "status = 2";
            $url = ""; // 播放地址
            $cover_img = ""; // 封面图
            if($MediaProcessResultSet != array())
            {
                foreach($MediaProcessResultSet as $k => $v)
                {
                    if($v['Type'] == 'Transcode')
                    { // 转码
                        $url = $v['TranscodeTask']['Output']['Url'];
                        $con .= ",url = '$url'";
                    }
                    else if($v['Type'] == 'CoverBySnapshot')
                    { // 封面图
                        $cover_img = $v['CoverBySnapshotTask']['Output']['CoverUrl'];
                        $con .= ",cover_img = '$cover_img'";
                    }
                }
            }

            $sql1 = "update lkt_bbs_video set $con where file_id = '$file_id' ";
            $r1 = Db::execute($sql1);
        }

        return;
    }

    // 媒体处理
    public function media_processing($store_id,$file_id)
    {
        $lktlog = new LaiKeLogUtils();
        $log_name = 'common/Vod.log';

        $secret_id = "";
        $secret_key = "";
        $token = "";

        $sql0 = "select * from lkt_bbs_config where store_id = '$store_id' ";
        $r0 = Db::query($sql0);
        if($r0)
        {
            $appid = $r0[0]['appid'];
            $secret_id = $r0[0]['secret_id'];
            $secret_key = $r0[0]['secret_key'];
            $definition_template_id = $r0[0]['definition_template_id']; // 视频转码模板id
            $sample_template_id = $r0[0]['sample_template_id']; // 视频采样截图模板id
        }

        $service = "vod";
        $host = "vod.tencentcloudapi.com";
        $req_region = "";
        $version = "2018-07-17";
        $action = "ProcessMedia";

        $payload = "{\"FileId\":\"$file_id\",\"SubAppId\":$appid,\"MediaProcessTask\":{\"TranscodeTaskSet\":[{\"Definition\":$definition_template_id}],\"CoverBySnapshotTaskSet\":[{\"Definition\":$sample_template_id,\"PositionType\":\"Percent\",\"PositionValue\":10}]}}";
        $params = json_decode($payload);
        $endpoint = "https://vod.tencentcloudapi.com";
        $algorithm = "TC3-HMAC-SHA256";
        $timestamp = time();
        $date = gmdate("Y-m-d", $timestamp);

        // ************* 步骤 1：拼接规范请求串 *************
        $http_request_method = "POST";
        $canonical_uri = "/";
        $canonical_querystring = "";
        $ct = "application/json; charset=utf-8";
        $canonical_headers = "content-type:".$ct."\nhost:".$host."\nx-tc-action:".strtolower($action)."\n";
        $signed_headers = "content-type;host;x-tc-action";
        $hashed_request_payload = hash("sha256", $payload);
        $canonical_request = "$http_request_method\n$canonical_uri\n$canonical_querystring\n$canonical_headers\n$signed_headers\n$hashed_request_payload";

        // ************* 步骤 2：拼接待签名字符串 *************
        $credential_scope = "$date/$service/tc3_request";
        $hashed_canonical_request = hash("sha256", $canonical_request);
        $string_to_sign = "$algorithm\n$timestamp\n$credential_scope\n$hashed_canonical_request";

        // ************* 步骤 3：计算签名 *************
        $secret_date = $this->sign("TC3".$secret_key, $date);
        $secret_service = $this->sign($secret_date, $service);
        $secret_signing = $this->sign($secret_service, "tc3_request");
        $signature = hash_hmac("sha256", $string_to_sign, $secret_signing);

        // ************* 步骤 4：拼接 Authorization *************
        $authorization = "$algorithm Credential=$secret_id/$credential_scope, SignedHeaders=$signed_headers, Signature=$signature";

        // ************* 步骤 5：构造并发起请求 *************
        $headers = [
            "Authorization" => $authorization,
            "Content-Type" => "application/json; charset=utf-8",
            "Host" => $host,
            "X-TC-Action" => $action,
            "X-TC-Timestamp" => $timestamp,
            "X-TC-Version" => $version
        ];
        if ($req_region) 
        {
            $headers["X-TC-Region"] = $req_region;
        }
        if ($token) 
        {
            $headers["X-TC-Token"] = $token;
        }

        try {
            $ch = curl_init();
            curl_setopt($ch, CURLOPT_URL, $endpoint);
            curl_setopt($ch, CURLOPT_POST, true);
            curl_setopt($ch, CURLOPT_POSTFIELDS, $payload);
            curl_setopt($ch, CURLOPT_HTTPHEADER, array_map(function ($k, $v) { return "$k: $v"; }, array_keys($headers), $headers));
            curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
            $response = curl_exec($ch);
            curl_close($ch);

            $lktlog->log($log_name, __LINE__ . ":" . $response);
            // echo $response;
            return $response;
        } 
        catch (Exception $err) 
        {
            // echo $err->getMessage();
            $lktlog->log($log_name, __LINE__ . ":" . $err->getMessage());
        }
    }

    public function sign($key, $msg) 
    {
        return hash_hmac("sha256", $msg, $key, true);
    }
}
    
?> 