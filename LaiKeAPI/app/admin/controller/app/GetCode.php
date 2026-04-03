<?php
namespace app\admin\controller\app;

use app\BaseController;
use app\common;
use think\facade\Db;
use app\common\ServerPath;


require_once "../app/common/phpqrcode.php";
// require_once "../app/common/alipay/test.php";

use app\admin\model\ConfigModel;
use app\admin\model\CustomerModel;
use app\admin\model\PaymentConfigModel;
use app\admin\model\ThirdModel;
use app\admin\model\ProductConfigModel;
use app\admin\model\ProductListModel;
use app\admin\model\GroupProductModel;
use app\admin\model\MchModel;
use app\admin\model\UserCollectionModel;
use app\admin\model\PtGroupProductModel;
use app\admin\model\AuctionProductModel;
/**
 * 分享
 */
class GetCode extends BaseController
{
    public $appid; // 小程序唯一标识
    public $appsecret; // 小程序的 app secret


    /*
    * param ori_img 原图像的名称和路径
    * param new_img 生成图像的名称
    * param percent 表示按照原图的百分比进行缩略，此项为空时默认按50%
    * param width 指定缩略后的宽度
    * param height 指定缩略后的高度
    *
    * 注：当 percent width height 都传入值的时候，且percent>0时，优先按照百分比进行缩略
    * by：//www.jb51.net 更多源码与你分享
    * 温馨提示：使用此功能要在php.ini中开启 gd2
    *
    **/

    function makeThumb($ori_img, $new_img, $percent = 50, $width = 0, $height = 0)
    {

        $original = getimagesize($ori_img); //得到图片的信息，可以print_r($original)发现它就是一个数组
        //$original[2]是图片类型，其中1表示gif、2表示jpg、3表示png
        switch ($original[2])
        {
            case 1 :
                $s_original = imagecreatefromgif($ori_img);
                break;
            case 2 :
                $s_original = imagecreatefromjpeg($ori_img);
                break;
            case 3 :
                $s_original = imagecreatefrompng($ori_img);
                break;
        }

        if ($percent > 0)
        {
            $width = $original[0] * $percent / 100;
            $width = ($width > 0) ? $width : 1;
            $height = $original[1] * $percent / 100;
            $height = ($height > 0) ? $height : 1;
        }

        //创建一个真彩的画布
        $canvas = imagecreatetruecolor($width, $height);
        imagecopyresized($canvas, $s_original, 0, 0, 0, 0, $width, $height, $original[0], $original[1]);
        //header("Content-type:image/jpeg");
        //imagejpeg($canvas); //向浏览器输出图片
        $loop = imagejpeg($canvas, $new_img); //生成新的图片
        return $loop;
    }

    public function get_mini_code($url, $APPID, $APPSECRET, $code_width = 165)
    {
        header('content-type:text/html;charset=utf-8');
        //      获取access_token
        $access_token = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=$APPID&secret=$APPSECRET";
        //      缓存access_token
        $_SESSION['access_token'] = "";
        $_SESSION['expires_in'] = 0;
        $ACCESS_TOKEN = "";
        if (!isset($_SESSION['access_token']) || (isset($_SESSION['expires_in']) && time() > $_SESSION['expires_in']))
        {

            $json = $this->httpRequest($access_token);
            $json = json_decode($json, true);
            $_SESSION['access_token'] = $json['access_token'];
            $_SESSION['expires_in'] = time() + 7200;
            $ACCESS_TOKEN = $json["access_token"];
        }
        else
        {
            $ACCESS_TOKEN = $_SESSION["access_token"];
        }
        //      构建请求二维码参数
        //      path是扫描二维码跳转的小程序路径，可以带参数?id=xxx
        //      width是二维码宽度
        $qcode = "https://api.weixin.qq.com/wxa/getwxacode?access_token=$ACCESS_TOKEN";
        $param = json_encode(array("path" => $url, "width" => 50));

        //      POST参数
        $result = $this->httpRequest($qcode, $param, "POST");
        //      生成二维码
        $file_path = '../public/images/product_share_img/';
        $qrcode_name = md5(time() . rand(1000, 9999));
        $filename = $file_path . $qrcode_name . '.jpg';
        $qrcode_name = file_put_contents($filename, $result);

        $imgPath = "../public/images/product_share_img/" . uniqid() . mt_rand(1, 200) . ".jpg";

        list($width, $height) = getimagesize($filename);
        $per = round($code_width / $height, 3);
        $n_w = $width * $per;
        $n_h = $height * $per;

        $new = imagecreatetruecolor($n_w, $n_h);
        $img = imagecreatefromjpeg($filename);
        //copy部分图像并调整
        imagecopyresized($new, $img, 0, 0, 0, 0, $n_w, $n_h, $width, $height);
        //图像输出新图片、另存为
        imagejpeg($new, $imgPath);
        imagedestroy($new);
        imagedestroy($img);
        // unlink($filename);
        $filename = $imgPath;
        return $filename;
        //        $base64_image ="data:image/jpeg;base64,".base64_encode( $result );
        //        echo $base64_image;
    }

    public function upimg($filename, $code_width = 200)
    {      
        $type = explode('.', $filename);
        if ($type[1] == 'png' || $type[1] == 'PNG')
        {
            $imgPath = "../public/image/product_share_img/" . uniqid() . mt_rand(1, 200) . ".png";
        }
        else
        {
            $imgPath = "../public/image/product_share_img/" . uniqid() . mt_rand(1, 200) . ".jpg";
        }
        
        list($width, $height) = getimagesize($filename);
        $per = round($code_width / $height, 3);
        $n_w = $width * $per;
        $n_h = $height * $per; 
        $new = imagecreatetruecolor($n_w, $n_h);
        // if ($type[1] == 'png' || $type[1] == 'PNG')
        // {   
        //     $img = imagecreatefrompng($filename);
        // }
        // else
        // {
        //     $img = imagecreatefromjpeg($filename);
        // }
        $filename = base64_encode(file_get_contents($filename));
        $filename = base64_decode($filename);

        $img = imagecreatefromstring($filename);
        //copy部分图像并调整
        imagecopyresized($new, $img, 0, 0, 0, 0, $n_w, $n_h, $width, $height);
        //图像输出新图片、另存为
        imagejpeg($new, $imgPath);
        // var_dump($imgPath);die;
        imagedestroy($new);
        imagedestroy($img);
        // unlink($filename);
        $filename = $imgPath;
        return $filename;
    }
    //图片放大处理
    public function enlargeimg($filename,$code_width = 250)
    {   
        $type = substr($filename,strripos($filename,".")+1);
        if ($type == 'png' || $type == 'PNG')
        {
            $imgPath = MO_IMAGE_DIR."/product_share_img/" . uniqid() . mt_rand(1, 200) . ".png";
        }
        else
        {
            $imgPath = MO_IMAGE_DIR."/product_share_img/" . uniqid() . mt_rand(1, 200) . ".jpg";
        }
        list($width, $height)=getimagesize($filename);
        //缩放比例
        $per=round($code_width/$width,3);
        $n_w=$width*$per;
        $n_h=$height*$per;
        $new = imagecreatetruecolor($n_w, $n_h);
        if ($type == 'png' || $type == 'PNG')
        {   
            $img = imagecreatefrompng($filename);
        }
        else
        {   
            $img = imagecreatefromjpeg($filename);
        }
        //copy部分图像并调整
        imagecopyresized($new, $img, 0, 0, 0, 0, $n_w, $n_h, $width, $height);
        //图像输出新图片、另存为
        imagejpeg($new, $imgPath);
        imagedestroy($new);
        imagedestroy($img);
        unlink($filename);
        $filename = $imgPath;
        return $filename;
    }
    /**
     * 图片圆角处理函数
     *
     * @param source  $srcFile 本地图片路径
     * @param integer $radius 圆角大小 = 最短边的长度 / $radius
     */
    function roundImgs($srcFile, $radius=2) {
        //得到图片后缀
        $ext     = pathinfo($srcFile);
        //设定圆形图片名称为“当前图片名称_cir”,并组装成本地圆形图片路径
        $img_path = $ext['dirname']."/".$ext['filename']."_cir.".$ext['extension'];
        //检测圆形图片存在的时候直接当前图片返回
        if(is_file($img_path)) return $img_path;

        $data = getimagesize($srcFile);
        switch ($data['2']) {
            case 1:
                $im = imagecreatefromgif($srcFile);
                break;
            case 2:
                $im = imagecreatefromjpeg($srcFile);
                break;
            case 3:
                $im = imagecreatefrompng($srcFile);
                break;
            case 6:
                $im = imagecreatefromwbmp($srcFile);
                break;
        }
        $srcImg = $im;
        $w = imagesx($srcImg);
        $h = imagesy($srcImg);
        $radius = min($w, $h) / $radius;
        $img = imagecreatetruecolor($w, $h);
        imagesavealpha($img, true); // 设置透明通道
        $bg = imagecolorallocatealpha($img, 255, 255, 255, 127); // 拾取一个完全透明的颜色, 最后一个参数127为全透明
        imagefill($img, 0, 0, $bg);
        $r = $radius; // 圆 角半径
        for ($x = 0; $x < $w; $x++) {
            for ($y = 0; $y < $h; $y++) {
                $rgbColor = imagecolorat($srcImg, $x, $y);
                if (($x >= $radius && $x <= ($w - $radius)) || ($y >= $radius && $y <= ($h - $radius))) {
                    imagesetpixel($img, $x, $y, $rgbColor); // 不在四角的范围内,直接画
                } else { // 在四角的范围内选择画
                    // 上左
                    $yx = $r; // 圆心X坐标
                    $yy = $r; // 圆心Y坐标
                    if (((($x - $yx) * ($x - $yx) + ($y - $yy) * ($y - $yy)) <= ($r * $r))) {
                        imagesetpixel($img, $x, $y, $rgbColor);
                    }
                    // 上右
                    $yx = $w - $r; // 圆心X坐标
                    $yy = $r; // 圆心Y坐标
                    if (((($x - $yx) * ($x - $yx) + ($y - $yy) * ($y - $yy)) <= ($r * $r))) {
                        imagesetpixel($img, $x, $y, $rgbColor);
                    }
                    // 下左
                    $yx = $r; // 圆心X坐标
                    $yy = $h - $r; // 圆心Y坐标
                    if (((($x - $yx) * ($x - $yx) + ($y - $yy) * ($y - $yy)) <= ($r * $r))) {
                        imagesetpixel($img, $x, $y, $rgbColor);
                    }
                    // 下右
                    $yx = $w - $r; // 圆心X坐标
                    $yy = $h - $r; // 圆心Y坐标
                    if (((($x - $yx) * ($x - $yx) + ($y - $yy) * ($y - $yy)) <= ($r * $r))) {
                        imagesetpixel($img, $x, $y, $rgbColor);
                    }
                }
            }
        }

        switch ($data['2']) {
            case 1:
                imagegif($img,$img_path);
                break;
            case 2:
                imagejpeg($img,$img_path);
                break;
            case 3:
                imagepng($img,$img_path);
                break;
            case 6:
                image2wbmp($img,$img_path);
                break;
        }

        imagedestroy($img);
        return $img_path;
    }

    //把请求发送到微信服务器换取二维码
    function httpRequest($url, $data = '', $method = 'GET')
    {
        $curl = curl_init();
        curl_setopt($curl, CURLOPT_URL, $url);
        curl_setopt($curl, CURLOPT_SSL_VERIFYPEER, 0);
        curl_setopt($curl, CURLOPT_SSL_VERIFYHOST, 0);
        curl_setopt($curl, CURLOPT_USERAGENT, $_SERVER['HTTP_USER_AGENT']);
        curl_setopt($curl, CURLOPT_FOLLOWLOCATION, 1);
        curl_setopt($curl, CURLOPT_AUTOREFERER, 1);
        if ($method == 'POST')
        {
            curl_setopt($curl, CURLOPT_POST, 1);
            if ($data != '')
            {
                curl_setopt($curl, CURLOPT_POSTFIELDS, $data);
            }
        }

        curl_setopt($curl, CURLOPT_TIMEOUT, 30);
        curl_setopt($curl, CURLOPT_HEADER, 0);
        curl_setopt($curl, CURLOPT_RETURNTRANSFER, 1);
        $result = curl_exec($curl);
        curl_close($curl);
        return $result;
    }


    //创建图片 根据类型
    public function create_imagecreatefromjpeg($pic_path)
    {
        $arrContextOptions = array(
            "ssl" => array(
                "verify_peer" => false,
                "verify_peer_name" => false,
            ),
        );

        $pathInfo = pathinfo($pic_path); // 函数以数组的形式返回文件路径的信息。
        if (is_file($pathInfo['basename']))
        {
            if (array_key_exists('extension', $pathInfo))
            {
                switch (strtolower($pathInfo['extension']))
                {
                    case 'jpeg':
                        $imagecreatefromjpeg = 'imagecreatefromjpeg';
                        break;
                    case 'png':
                        $imagecreatefromjpeg = 'imagecreatefrompng';
                        break;
                    case 'gif':
                    default:
                        $imagecreatefromjpeg = 'imagecreatefromstring';
                        break;
                }
            }
            else
            {
                $imagecreatefromjpeg = 'imagecreatefromstring';
            }
            $pic_path = file_get_contents($pic_path, false, stream_context_create($arrContextOptions));
        }
        else
        {
            $imagecreatefromjpeg = 'imagecreatefromstring';
            // 把整个文件读入一个字符串中
            $pic_path = file_get_contents($pic_path, false, stream_context_create($arrContextOptions));
        }
        $im = $imagecreatefromjpeg($pic_path);

        return $im;
    }

    public function getRealData($data)
    {
        $data['left'] = intval(str_replace('px', '', $data['left'])) * 2;
        $data['top'] = intval(str_replace('px', '', $data['top'])) * 2;
        $data['width'] = intval(str_replace('px', '', $data['width'])) * 2;
        $data['height'] = intval(str_replace('px', '', $data['height'])) * 2;

        if ($data['size'])
        {
            $data['size'] = intval(str_replace('px', '', $data['size'])) * 2;
        }
        if ($data['src'])
        {
            $data['src'] = tomedia($data['src']);
        }

        return $data;
    }

    // 写入文件
    public function write_img($target, $data, $imgurl)
    {

        $img = $this->create_imagecreatefromjpeg($imgurl);

        $w = imagesx($img);
        $h = imagesy($img);

        imagecopyresized($target, $img, $data['left'], $data['top'], 0, 0, $data['width'], $data['height'], $w, $h); // 函数用于拷贝部分图像并调整大小。
        imagedestroy($img); // 函数用于销毁图像资源。

        return $target;
    }

    function autowrap($fontsize, $angle, $fontface, $string, $width)
    {
        // 参数分别是 字体大小, 角度, 字体名称, 字符串, 预设宽度
        $content = "";
        // 将字符串拆分成一个个单字 保存到数组 letter 中
        preg_match_all("/./u", $string, $arr);
        $letter = $arr[0];
        foreach ($letter as $l)
        {
            $teststr = $content . $l;
            $testbox = imagettfbbox($fontsize, $angle, $fontface, $teststr);
            if (($testbox[2] > $width) && ($content !== ""))
            {
                $content .= PHP_EOL;
            }
            $content .= $l;
        }
        return $content;
    }

    public function write_text($dest, $data, $string, $fontfile)
    {
        if (!empty($data['type']))
        {
            if ($data['type'] == 'title')
            {
                $width = imagesx($dest) - $data['left'] * 2;
            }
            else
            {
                $width = imagesx($dest) * 2;
            }
        }
        else
        {
            $width = imagesx($dest) * 2;
        }

        $font_file = $fontfile . 'simhei.ttf';


        $colors = $this->hex2rgb($data['color']);
        $color = imagecolorallocate($dest, $colors['red'], $colors['green'], $colors['blue']);//背景色
        $string = $this->autowrap($data['size'], 0, $font_file, $string, $width);

        $fontsize = $data['size'];

        imagettftext($dest, $fontsize, 0, $data['left'], $data['top'], $color, $font_file, $string);
        return $dest;
    }

    function hex2rgb($colour)
    {
        if ($colour[0] == '#')
        {
            $colour = substr($colour, 1);
        }
        if (strlen($colour) == 6)
        {
            list($r, $g, $b) = array(
                $colour[0] . $colour[1],
                $colour[2] . $colour[3],
                $colour[4] . $colour[5]
            );
        }
        elseif (strlen($colour) == 3)
        {
            list($r, $g, $b) = array(
                $colour[0] . $colour[0],
                $colour[1] . $colour[1],
                $colour[2] . $colour[2]
            );
        }
        else
        {
            return false;
        }
        $r = hexdec($r);
        $g = hexdec($g);
        $b = hexdec($b);
        return array(
            'red' => $r,
            'green' => $g,
            'blue' => $b
        );
    }

    //将颜色代码转rgb
    function wpjam_hex2rgb($hex)
    {
        $hex = str_replace("#", "", $hex);

        if (strlen($hex) == 3)
        {
            $r = hexdec(substr($hex, 0, 1) . substr($hex, 0, 1));
            $g = hexdec(substr($hex, 1, 1) . substr($hex, 1, 1));
            $b = hexdec(substr($hex, 2, 1) . substr($hex, 2, 1));
        }
        else
        {
            $r = hexdec(substr($hex, 0, 2));
            $g = hexdec(substr($hex, 2, 2));
            $b = hexdec(substr($hex, 4, 2));
        }

        return array($r, $g, $b);
    }

    // 获得二维码，并储存到本地
    public function get_qrcode($url, $uploadImg, $size = 5)
    {

        $value = $url;                  //二维码内容
        $errorCorrectionLevel = 'L';    //容错级别
        $matrixPointSize = $size;           //生成图片大小
        $filename = $uploadImg . md5(time() . mt_rand(0, 1000)) . '.png';

        app('QRcode')::png($value, $filename, $errorCorrectionLevel, $matrixPointSize, 2);

        return $filename;
    }




    function httpsRequest($url, $data = null)
    {
        // 1.初始化会话
        $ch = curl_init();
        // 2.设置参数: url + header + 选项
        // 设置请求的url
        curl_setopt($ch, CURLOPT_URL, $url);
        // 保证返回成功的结果是服务器的结果
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, FALSE);
        curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, FALSE);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        if (!empty($data))
        {
            // 发送post请求
            curl_setopt($ch, CURLOPT_POST, 1);
            // 设置发送post请求参数数据
            curl_setopt($ch, CURLOPT_POSTFIELDS, $data);
        }
        // 3.执行会话; $result是微信服务器返回的JSON字符串
        $result = curl_exec($ch);
        // 4.关闭会话
        curl_close($ch);
        return $result;
    }

    //生成红包文字
    function madeCode()
    {
        $db = DBAction::getInstance();
        $request = $this->getContext()->getRequest();
        $store_id = trim($this->request->param('store_id'));
        $language = trim($this->request->post('language')); // 来源
        $access_id = trim($this->request->post('access_id'));

        $id = trim($this->request->post('id'));
        $wx_id = $this->request->post('openid');
        // 查询公司名称
        $sql = "select * from lkt_config where store_id = '$store_id'";
        $r = $db->select($sql);
        $company = $r[0]->company;
        $message = Lang($db, $store_id,$access_id, $language,"I'll_give_you_a_red_envelope");
        $instring = $company . $message;

        $resArr = array('status' => 1, 'text' => $instring);
        $this->output(200, '',$resArr);
    }

    function getAccessToken($appID, $appSerect, $re = '')
    {
        $url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" . $appID . "&secret=" . $appSerect;
        // 时效性7200秒实现
        // 1.当前时间戳
        $currentTime = time();
        // 2.修改文件时间
        $fileName = $appID . "_accessToken";
        // 文件名
        if (is_file($fileName))
        {
            if ($re == '')
            {
                $modifyTime = filemtime($fileName);
                if (($currentTime - $modifyTime) < 7200)
                {
                    // 可用, 直接读取文件的内容
                    $accessToken = file_get_contents($fileName);
                    return $accessToken;
                }
            }

        }
        // 重新发送请求
        $result = $this->httpsRequest($url);
        $jsonArray = json_decode($result, true);
        // 写入文件
        $accessToken = $jsonArray['access_token'];
        file_put_contents($fileName, $accessToken);
        return $accessToken;
    }

    //获得二维码-微信小程序
    public function get_share_qrcode($path, $width, $id, $AccessToken)
    {
        // header('content-type:image/jpeg');  测试时可打开此项 直接显示图片
        $store_id = trim($this->request->param('store_id'));
        $language = trim($this->request->post('language')); // 来源


        // 查询系统参数
        $r_1 = ConfigModel::where('store_id',$store_id)->select()->toArray();
        $uploadImg_domain = $r_1[0]['uploadImg_domain']; // 图片上传域名
        $uploadImg = $r_1[0]['uploadImg']; // 图片上传位置
        if (strpos($uploadImg, '../') === false)
        { // 判断字符串是否存在 ../
            $img = $uploadImg_domain . $uploadImg; // 图片路径
        }
        else
        { // 不存在
            $img = $uploadImg_domain . substr($uploadImg, 2); // 图片路径
        }
        $pid = $this->request->post('pid');
        $path_name = str_replace('/', '_', $path);
        $filename = $path_name . '_share_' . $id . '_' . $pid . '.jpeg';///
        $imgDir = 'product_share_img/';
        $width = 430;
        //要生成的图片名字
        $newFilePath = $uploadImg . $imgDir . $filename;
        if (is_file($newFilePath))
        {
            return $newFilePath;
        }
        else
        {
            $scene = $this->request->post('scene');
            //获取三个重要参数 页面路径  图片宽度  文章ID
            //--B $arr = ["page"=> $path, "width"=>$width,'scene'=>$scene];
            //--A
            $arr = ["path" => $path . '?' . $scene, "width" => $width];
            $data = json_encode($arr);
            //把数据转化JSON 并发送
            // 接口A: 适用于需要的码数量较少的业务场景 接口地址：
            $interface_wx = 'https://api.weixin.qq.com/wxa/getwxacode?access_token=';
            $url = $interface_wx . $AccessToken;
            // 接口B：适用于需要的码数量极多的业务场景
            // $url = 'https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token=' . $AccessToken;
            // 接口C：适用于需要的码数量较少的业务场景
            // $url = 'https://api.weixin.qq.com/cgi-bin/wxaapp/createwxaqrcode?access_token=' . $AccessToken;
            //获取二维码API地址

            $da = $this->httpsRequest($url, $data);
            //发送post带参数请求

            //新增access_token过期验证
            $json = json_decode($da);
            if (isset($json->errcode))
            {
                $AccessToken = $this->getAccessToken($this->appid, $this->appsecret, 1);
                $url = $interface_wx . $AccessToken;
                $da = $this->httpsRequest($url, $data);
            }
            // var_dump($json,$da,isset($json->errcode),empty($json));exit;
            // header('content-type:image/jpeg');
            // echo $da;exit;
            $newFile = fopen($newFilePath, "w"); //打开文件准备写入
            fwrite($newFile, $da); //写入二进制流到文件
            fclose($newFile); //关闭文件
            return $newFilePath;
        }

    }


    public function product_share()
    {
        $type = $this->request->param('type');
        if (intval($type) == 4)
        {
            $data = $this->comm_share();
            $message = Lang('Success');
            return output(200,$message,$data);
        }


    }

    /**
     * 分销
     */
    public function comm_share()
    {
        $store_id = $this->request->param('store_id');
        $language = $this->request->param('language'); // 来源

        $store_type = $this->request->param('store_type'); // 来源
        $access_id = $this->request->param('access_id'); // 来源
        $path = $this->request->param('path');//分享地址

        $user_id = $this->user_list['user_id'];
        $user_name = $this->user_list['user_name'];
        $headimgurl = $this->user_list['headimgurl'] . "?x-oss-process=image/resize,m_fixed,h_250,w_250/circle,r_100/format,png";
        $r = CustomerModel::where('id',$store_id)->field('name')->select()->toArray();
        $store_name = $r[0]['name'];

        $url = $path;

        if ($store_type == 1)
        {
            //小程序
            $r = PaymentConfigModel::where(['pid'=>5,'store_id'=>$store_id])->select()->toArray();
            $value = $r ? $r[0] : [];
            if ($value)
            {
                $list = json_decode($value['config_data']);
                $appid = $list->appid;
                $appsecret = $list->appsecret;
            }

            $qrcode_path = $this->get_mini_code($url, $appid, $appsecret, 550);
        }
        else
        {
            $r = ConfigModel::where('store_id',$store_id)->select()->toArray();
            if (!empty($r[0]['H5_domain']))
            {
                $url = $r[0]['H5_domain'] . $url;
            }
            else
            {
                $message = Lang("getcode.0");

                $this->output(ERROR_CODE_PZBCZ, $message);
            }
            //生成二维码
            if ($store_type == 2 || $store_type == 7)
            {
                $qrcode_path = $this->get_qrcode1($url, '../public/image/product_share_img', 30);
                $qrcode_path = $this->upimg($qrcode_path, 800);
            }
            else
            {
                $url = "pages/tabBar/my";
                $query_param = "fatherId=" . $user_id;
                $qrcode_path = $this->get_qrcode2($store_id, $url, $query_param, '../public/image/product_share_img', 30);
                $qrcode_path = $this->upimg($qrcode_path, 990);
            }
        }
        // 二维码
        $path_2 = $qrcode_path;
        $path_2 = $this->upimg($path_2, 150);

        //获取根目录
        $end_res = ThirdModel::select()->toArray();
        if($end_res)
        {
            $endurl = $end_res[0]['endurl'];
        }
        else
        {
            $message = Lang('getcode.0');
            $this->output(ERROR_CODE_SPBCZ, $message);
        }
        $qrCode = $endurl.str_replace('../public/','',$path_2);
        unlink($qrcode_path);
        $resArr = array('qrCode' => $qrCode,'userHeadUrl'=>$headimgurl,'userName'=>$user_name);
        return $resArr;
    }

    /**
     * 店铺分享
     */
    public function share_shop()
    {
        $store_id = trim($this->request->param('store_id'));
        $language = trim($this->request->post('language')); // 来源

        $store_type = trim($this->request->param('store_type'));
        $shop_id = trim($this->request->post('shop_id'));
        $access_id = trim($this->request->post('access_id'));
        $type = trim($this->request->post('type'));

        $user_id = $this->user_list['user_id'];

        $r_con = ProductConfigModel::where('store_id',$store_id)->field('is_open')->select()->toArray();
        if ($r_con)
        {
            $is_open = $r_con[0]['is_open'];
        }
        else
        {
            $is_open = 0;
        }
        if ($is_open == 0)
        {
            $status = '2';
        }
        else
        {
            $status = '2,3';
        }
        //查询店铺信息
        $r0 = MchModel::where(['store_id'=>$store_id,'id'=>$shop_id])->select()->toArray();
        $mch_data = array();
        if ($r0)
        {
            $mch_data['logo'] = ServerPath::getimgpath($r0[0]['logo'], $store_id);
            $mch_data['name'] = $r0[0]['name'];
            //在售数量
            if ($is_open == 0)
            {
                $mch_data['quantity_on_sale'] = ProductListModel::where(['store_id'=>$store_id,'mch_id'=>$shop_id,'mch_status'=>2,'recycle'=>0,'active'=>1,'status'=>2])
                                    ->field('id')
                                    ->count();
            }
            else
            {   
                $mch_data['quantity_on_sale'] = ProductListModel::where(['store_id'=>$store_id,'mch_id'=>$shop_id,'mch_status'=>2,'recycle'=>0,'active'=>1])
                                    ->where('status','in','2,3')
                                    ->field('id')
                                    ->count();
            }
            $mch_data['quantity_sold'] = 0;
            $sql_3 = "select tt.* from (select c.img,a.id,a.product_title,a.subtitle,a.imgurl,a.volume,min(c.price) over (partition by c.pid) as price,c.yprice,row_number () over (partition by c.pid) as top from lkt_product_list AS a RIGHT JOIN lkt_configure AS c ON a.id = c.pid where a.store_id = '$store_id' and a.mch_id = '$shop_id' and a.mch_status = 2 and a.status in ($status) and a.recycle = 0 and c.recycle = 0 and a.active = 1) as tt where tt.top<2 ";
            $r3 = Db::query($sql_3);
            if ($r3)
            {
                foreach ($r3 as $k => $v)
                {
                    $mch_data['quantity_sold'] += $v['volume'];  // 已售数量
                }
            }
            if($mch_data['quantity_sold'] < 0)
            {
                $mch_data['quantity_sold'] = 0;
            }

            $mch_data['collection_num'] = UserCollectionModel::where(['store_id'=>$store_id,'mch_id'=>$shop_id])->field('id')->count();//收藏人数

        }
        else
        {
            $message = Lang('getcode.1');
            echo json_encode(array('code' => ERROR_CODE_CSCW, 'msg' => $message));
            exit;
        }
        $url = "pagesB/store/store?is_share=true&shop_id=" . $shop_id."&fatherId=".$user_id;

        if ($store_type == 1)
        {
            //小程序
            $r = PaymentConfigModel::where(['pid'=>5,'store_id'=>$store_id])->select()->toArray();
            $value = $r ? $r[0] : [];
            if ($value)
            {
                $list = json_decode($value['config_data']);
                $appid = $list->appid;
                $appsecret = $list->appsecret;
            }

            $qrcode_path = $this->get_mini_code($url, $appid, $appsecret, 350);
            $this->makeThumb($qrcode_path, $qrcode_path, 0, 350, 350);
        }
        else
        {
            $r = ConfigModel::where('store_id',$store_id)->select()->toArray();
            if (!empty($r[0]['H5_domain']))
            {
                $url = $r[0]['H5_domain'] . $url;
            }
            //生成二维码
            $qrcode_path = $this->get_qrcode1($url, '../public/image/product_share_img', 11);
        }


        $mch_data['logo'] = $mch_data['logo'] . "?x-oss-process=image/resize,m_fixed,h_100,w_100/format,png";

        //合成图片
        //案例二：将活动背景图片设置透明，然后和动态二维码图片合成一张图片
        // 图片一
        $path_1 = '../public/image/product_share_img/shop_bg.png';
        // 图片二
        $path_2 = $qrcode_path;

        $logo = $this->downImgUrl($mch_data['logo']);//下载店铺logo
        $logo = $this->upimg($logo,100);
        //创建图片对象
        $image_1 = imagecreatefrompng($path_1);
        $image_2 = $this->getImageType($path_2);
        $image_6 = $this->getImageType($logo);
        $image_3 = imageCreatetruecolor(imagesx($image_1), imagesy($image_1));
        $color = imagecolorallocate($image_3, 255, 255, 255);
        imagefill($image_3, 0, 0, $color);
        imagecopyresampled($image_3, $image_1, 0, 0, 0, 0, imagesx($image_1), imagesy($image_1), imagesx($image_1), imagesy($image_1));
        //与图片二合成
        //二维码
        if ($store_type != 1)
        {
            imagecopymerge($image_3, $image_2, 50, 350, 0, 0, imagesx($image_2), imagesy($image_2), 100);
        }
        else
        {
            imagecopymerge($image_3, $image_2, 105, 400, 0, 0, imagesx($image_2), imagesy($image_2), 100);
        }
        //店铺头像
        imagecopy($image_3, $image_6, 225, 60, 0, 0, imagesx($image_6), imagesy($image_6));
        //文字水印
        $font1 = '../public/image/product_share_img/PINGFANG BOLD.TTF';//字体,字体文件需保存到相应文件夹下
        $font2 = '../public/image/product_share_img/PINGFANG MEDIUM.TTF';//字体,字体文件需保存到相应文件夹下

        $black = imagecolorallocate($image_3, 0, 0, 0);//字体颜色 RGB
        $white = imagecolorallocate($image_3, 255, 255, 255);//字体颜色 RGB
        $hui = imagecolorallocate($image_3, 170, 170, 170);//字体颜色 RGB

        $circleSize = 0; //旋转角度

        $nameWidth = mb_strlen($mch_data['name']) * 24;
        $x = (550 - floatval($nameWidth)) / 2;

        imagefttext($image_3, 18, $circleSize, $x, 200, $white, $font2, $mch_data['name']);//店铺名称
        $width1 = $this->get_text_width($mch_data['quantity_on_sale'], $font1, 18);
        $x1 = (190 - $width1) / 2;
        imagefttext($image_3, 18, $circleSize, $x1, 270, $white, $font1, $mch_data['quantity_on_sale']);
        $message_0 = Lang('getcode.2');
        imagefttext($image_3, 14, $circleSize, 60, 300, $hui, $font1, $message_0);
        $width2 = $this->get_text_width($mch_data['quantity_sold'], $font1, 18);
        $x2 = (556 - $width2) / 2;
        imagefttext($image_3, 18, $circleSize, $x2, 270, $white, $font1, $mch_data['quantity_sold']);
        $message_1 = Lang('getcode.3');
        imagefttext($image_3, 14, $circleSize, 260, 300, $hui, $font1, $message_1);
        $width3 = $this->get_text_width($mch_data['collection_num'], $font1, 18);
        $x3 = (910 - $width3) / 2;
        imagefttext($image_3, 18, $circleSize, $x3, 270, $white, $font1, $mch_data['collection_num']);
        $message_2 = Lang('getcode.4');
        imagefttext($image_3, 14, $circleSize, 420, 300, $hui, $font1, $message_2);

        $message_3 = Lang('getcode.5');
        imagefttext($image_3, 18, $circleSize, 170, 860, $black, $font1, $message_3);

        // 输出合成图片
        $versin = 2;//版本号

        $share_img_name = time() . $shop_id . $versin . '_share.png';

        $share_img_path = '../public/image/product_share_img/' . $share_img_name;
        imagepng($image_3, $share_img_path);
        unlink($qrcode_path);
        unlink($logo);
        //获取根目录
        $end_res = ThirdModel::select()->toArray();
        if($end_res)
        {
            $endurl = $end_res[0]['endurl'];
        }
        else
        {
            $message = Lang('getcode.0');
            $this->output(ERROR_CODE_SPBCZ, $message);
        }
        $qrCode = $endurl.str_replace('../public/','',$share_img_path);
        $imgUrl = 'http://' . $_SERVER['HTTP_HOST'] . $_SERVER['REQUEST_URI'] . '&type=img';
        $resArr = array('code' => 200, 'imgUrl' => $qrCode);
        return output(200, '',$resArr);

    }

    public function get_text_width($str, $fontFamily, $fontSize)
    {

        // 计算总占宽
        $dimensions = imagettfbbox($fontSize, 0, $fontFamily, $str);
        $textWidth = abs($dimensions[4] - $dimensions[0]);

        return $textWidth;
    }

    /**
     * 图片分享
     */
    public function share()
    {
        $store_id = trim($this->request->param('store_id'));
        $language = trim($this->request->post('language')); // 来源

        $store_type = trim($this->request->param('store_type'));
        $proId = trim($this->request->post('proId'));
        $attr_id = trim($this->request->post('attr_id'));
        $order_no = trim($this->request->post('order_no'));
        $type = trim($this->request->post('type'));
        $access_id = trim($this->request->post('access_id'));
        $href = trim($this->request->post('href'));
        $path = trim($this->request->post('path'));
        $share_type = intval($this->request->post('share_type'));

        file_put_contents('../public/image/product_share_img/1log.txt', '开始', FILE_APPEND);

        $isfx = false;//是否为分销商品
        $goods_id = $proId;

        // 如果是积分商品
        if ($share_type == 2)
        {
            $sql = "select g.integral,g.money,a.product_title,a.id as goods_id,c.img as imgurl from lkt_integral_goods as g left join lkt_product_list as a on g.goods_id=a.id left join lkt_configure as c on g.attr_id = c.id  where g.id = '$proId'";
            $c_res = Db::query($sql);

            $goods_id = $c_res[0]['goods_id'];
        }


        $sql = "SELECT p.product_title, p.imgurl, p.separate_distribution,p.active, c.price
                FROM lkt_product_list AS p
                LEFT JOIN lkt_configure AS c ON p.id = c.pid
                where p.id = $goods_id
                ORDER BY c.price";
        $c_res = Db::query($sql);

        //计算价格
        if ($c_res)
        {
            $priceStr = $c_res[0]['price'];
            $pro_type_no = $c_res[0]['active']; //商品类型 1正常商品 2拼团商品 3砍价商品 4竞拍商品
            if ($pro_type_no == 2)
            {
                //如果是拼团商品 查询出参团的最低价格
                $activity_no = intval($this->request->post('activity_no'));

                if ($type == 'pt')
                {
                    $guigeres = GroupProductModel::where(['product_id'=>$proId,'store_id'=>$store_id,'activity_no'=>$activity_no])
                                ->field('group_level')
                                ->select()
                                ->toArray();
                }
                else
                {
                    $guigeres = GroupProductModel::where(['product_id'=>$proId,'store_id'=>$store_id,'is_delete'=>0])
                                ->where('g_status','<>',1)
                                ->field('group_level')
                                ->order('id','desc')
                                ->select()
                                ->toArray();
                }

                $level = $guigeres[0]['group_level'];
                //计算拼团的最低价 和 对应的参团人数hg
                $sel_attr_sql = "select c.price from lkt_configure as c left join lkt_product_list as p on c.pid=p.id where c.pid=$proId";
                $min_price = '';
                $sel_attr_Res = Db::query($sel_attr_sql);
                foreach ($sel_attr_Res as $k => $v3)
                {
                    if ($min_price == '')
                    {
                        $min_price = $v3['price'];
                    }
                    else if ($min_price > $v3['price'])
                    {
                        $min_price = $v3['price'];
                    }
                }

                //查询最低价格的和 拼团人数
                $kai_min_bili = $min_bili = 100;
                $kai_min_man = $min_man = 100;
                $level = unserialize($level);
                if ($type == 'pt')
                {
                    foreach ($level as $k => $v__)
                    {
                        $bili = explode('~', $v__);
                        if ($min_bili > $bili[0])
                        {
                            $min_bili = $bili[0];
                            $min_man = $k;
                        }
                    }
                }
                else
                {
                    $sNo = trim($this->request->post('sNo'));

                    $sql_grp = "SELECT goo.groupman FROM lkt_order as o left JOIN lkt_group_open as goo on o.ptcode = goo.ptcode where o.sNo ='$sNo' and o.store_id = $store_id ";
                    $grp_res = Db::query($sql_grp);
                    if (!empty($grp_res))
                    {
                        $groupman = $grp_res[0]['groupman'];
                        $bili = explode('~', $level[$groupman]);
                        $min_bili = $bili[0];
                    }
                }
                $min_price = floatval($min_price) * intval($min_bili) / 100;
                $priceStr = sprintf("%.2f", $min_price);
            }
            else if($type == 'pp')
            {
                //如果是拼团商品 查询出参团的最低价格
                $activity_no = intval($this->request->post('activity_no'));
                $guigeres = GroupProductModel::where(['product_id'=>$proId,'store_id'=>$store_id,'activity_no'=>$activity_no])
                                ->field('group_level')
                                ->select()
                                ->toArray();
                $level = $guigeres[0]['group_level'];
                //计算拼团的最低价 和 对应的参团人数hg
                $sel_attr_sql = "select c.price from lkt_configure as c left join lkt_product_list as p on c.pid=p.id where c.pid=$proId";
                $min_price = '';
                $sel_attr_Res = Db::query($sel_attr_sql);
                foreach ($sel_attr_Res as $k => $v3)
                {
                    if ($min_price == '')
                    {
                        $min_price = $v3['price'];
                    }
                    else if ($min_price > $v3['price'])
                    {
                        $min_price = $v3['price'];
                    }
                }

                //查询最低价格的和 拼团人数
                $kai_min_bili = $min_bili = 100;
                $kai_min_man = $min_man = 100;
                $level = unserialize($level);
                $sNo = trim($this->request->post('sNo'));
                if ($sNo)
                {
                    $sql_grp = "SELECT goo.groupman FROM lkt_order as o left JOIN lkt_group_open as goo on o.ptcode = goo.ptcode where o.sNo ='$sNo' and o.store_id = $store_id ";
                    $grp_res = Db::query($sql_grp);
                    if (!empty($grp_res))
                    {
                        $groupman = $grp_res[0]['groupman'];
                        $bili = explode('~', $level[$groupman]);
                        $min_bili = $bili[0];
                    }
                }
                else
                {
                    foreach ($level as $k => $v__)
                    {
                        $bili = explode('~', $v__);
                        if ($min_bili > $bili[0])
                        {
                            $min_bili = $bili[0];
                            $min_man = $k;
                        }
                    }   
                }
                $min_price = floatval($min_price) * intval($min_bili) / 100;
                $priceStr = sprintf("%.2f", $min_price);
            }
        }
        else
        {
            $message = Lang('getcode.6');
            $this->output(ERROR_CODE_SPBCZ, $message);
        }

        $isfx = true;
        $user_id = $this->user_list['user_id'];
        $pic_size = 3;
        $product_title = $c_res[0]['product_title'];
        $url = "pages/goods/goodsDetailed?productId=" . $proId . "&isfx=true&fatherId=" . $user_id;
        if($type == 'fx'){
            $fx_id = trim($this->request->post('fx_id'));//分销商品ID
            $url = "pages/goods/goodsDetailed?isDistribution=true&pro_id=" . $proId . "&fx_id=" . $fx_id . "&isfx=true&fatherId=" . $user_id;
        }
        elseif ($type == 'pt')
        {
            //如果是拼团
            $activity_no = trim($this->request->post('activity_no'));
            $url = "pagesA/group/groupDetailed?pro_id=" . $proId . "&activity_no=$activity_no&isfx=true&fatherId=". $user_id;
            $pic_size = 4;
        }
        else if ($type == 'pp')
        {
            //如果是拼团
            $activity_no = trim($this->request->post('activity_no'));
            //查询平台活动ID
            $res = PtGroupProductModel::where('activity_no',$activity_no)->field('platform_activities_id')->select()->toArray();
            if($res)
            {
                $platform_activities_id = $res[0]['platform_activities_id'];
            }
            else
            {
                $platform_activities_id = '';
            }
            $url = "pagesA/group/groupDetailed?pro_id=" . $proId .  "&a_type=1&activity_no=$activity_no&isfx=true&platform_activities_id=".$platform_activities_id."&fatherId=". $user_id;
            $pic_size = 4;
        }
        else if ($type == 'pt_end')
        {
            //如果是拼团结束页面
            $activity_no = trim($this->request->post('activity_no'));
            $url = "pagesA/group/group_end?sNo=$sNo&friend=true&activity_no=$activity_no&fatherId=". $user_id;
            $url_ = "pagesA/group/group_end";
            $query_param = "sNo=$sNo&friend=true";
            $pic_size = 4;
        }
        else if ($type == 'pp_end') 
        {
            //如果是拼团结束页面
            $activity_no = trim($this->request->post('activity_no'));
            $sNo = trim($this->request->post('sNo'));
            $url = "pagesA/group/group_end?sNo=$sNo&friend=true&a_type=1&activity_no=$activity_no&fatherId=". $user_id;
            $url_ = "pagesA/group/group_end";
            $query_param = "sNo=$sNo&friend=true";
            $pic_size = 4;
        }
        else if ($type == "JP")
        {
            //$url = "pagesA/bidding/bidding_detailed?biddingId=$proId&type=1";
            $bindding_id = trim($this->request->post('bindding_id'));//竞拍活动id
            $res = AuctionProductModel::where(['store_id'=>$store_id,'id'=>$bindding_id])->field('title,current_price')->select()->toArray();
            $product_title = $res[0]['title'];
            $priceStr = $res[0]['current_price'];
            $url = "pagesA/bidding/bidding_detailed?bindding_id=$bindding_id&type=1&isfx=true&fatherId=". $user_id;
            if ($path && !empty($path)) {
                $url = $path;
            }
        }
        
        // 如果是积分商品
        if ($share_type == 2)
        {
            $url = "pagesB/integral/integral_detail?pro_id=" . $proId . "&edition=" . $edition . "&isfx=true&fatherId=". $user_id;

            $sql = "select g.integral,g.money,a.product_title,c.img as imgurl from lkt_integral_goods as g left join lkt_product_list as a on g.goods_id=a.id left join lkt_configure as c on g.attr_id = c.id  where g.id = '$proId'";
            $c_res = Db::query($sql);
            $priceStr = $c_res[0]['integral'] . '积分';
            $product_title = $c_res[0]['product_title'];
            if (floatval($c_res[0]['money']) > 0)
            {
                $priceStr = '￥' . $c_res[0]['money'] . '+' . $c_res[0]['integral'] . '积分';
            }
            $pic_size = 4;
        }

        if ($store_type == 1)
        {
            //微信小程序
            $r = PaymentConfigModel::where(['pid'=>5,'store_id'=>$store_id])->select()->toArray();
            $value = $r ? $r[0] : [];
            if ($value)
            {
                $list = json_decode($value['config_data']);
                $appid = $list->appid;
                $appsecret = $list->appsecret;
            }
            $qrcode_path = $this->get_mini_code($url, $appid, $appsecret);
        }
        else if ($store_type == 3)
        {
            //支付宝小程序
            $url = $url_;
            $qrcode_path = $this->get_qrcode2($store_id, $url, $query_param, '../public/image/product_share_img', 5);

            $qrcode_path = $this->upimg($qrcode_path, 200);
        }
        else
        {
            $r = ConfigModel::where('store_id',$store_id)->select()->toArray();
            if (!empty($r[0]['H5_domain']))
            {
                $url = $r[0]['H5_domain'] . $url;
            }
            //生成二维码
            $qrcode_path = $this->get_qrcode1($url, '../public/image/product_share_img', $pic_size);
        }

        $user_name = $this->user_list['user_name'];
        $head_img_url = $this->user_list['headimgurl'] . "?x-oss-process=image/resize,m_fixed,h_50,w_50/circle,r_100/format,png";
        //合成图片

        // 底图
        $path_1 = '../public/image/product_share_img/bg.jpg';
        // 二维码
        $path_2 = $qrcode_path;
        $path_2 = $this->upimg($path_2, 150);
        //用户头像
        $head_img = $this->downImgUrl($head_img_url, 'jpg');//下载用户头像

        list($width_hh, $height_hh) = getimagesize($head_img);
        if ($width_hh > 52)
        {
            $head_img = $this->upimg($head_img, 50);

        }

        // 商品图
        $path_4 = ServerPath::getimgpath($c_res[0]['imgurl']);//获取图片地址
        $goods_img = $this->downImgUrl($path_4);//下载商品图片
        $goods_img_rand = rand(100000, 999999);

        $goods_img_obj = \think\Image::open($goods_img);
        $goods_img1 = "../public/image/product_share_img/goods_img_$goods_img_rand.jpg";
        list($width, $height) = getimagesize($goods_img);
        if ($width >= $height)
        {
            $goods_img_nw = 550 / $height * $width;
            $goods_img_nh = 550;
        }
        else
        {
            $goods_img_nh = 550 / $width * $height;
            $goods_img_nw = 550;
        }

        $goods_img_obj
            ->thumb($goods_img_nw, $goods_img_nh, \think\Image::THUMB_FIXED)
            ->save($goods_img1, 'jpg');
        unlink($goods_img);
        $goods_img = $goods_img1;
        list($width, $height) = getimagesize($goods_img);

        //查询logo
        $log_res = ConfigModel::where('store_id',$store_id)->field('logo')->select()->toArray();
        $image = \think\Image::open($path_1);
        //字体
        $font2 = '../public/image/product_share_img/PINGFANG MEDIUM.TTF';//字体,字体文件需保存到相应文件夹下
        $font1 = '../public/image/product_share_img/PINGFANG BOLD.TTF';//字体,字体文件需保存到相应文件夹下
        $goods_local = array(0, 0);
        $qrcode_local = array(360, 620);
        $logo_local = array(360, 620);
        $priceStr_local = array(50, 814);
        $text1_local = array(362, 814);
        $text2_local = array(180, 915);
        $text3_local = array(190, 945);
        $text4_local = array(100, 600);
        $red = "#ff0000";
        $hui = "#999999";
        $black = "#000000";

        $versin = 2;//版本号

        //判断是否为分销产品
        if ($isfx)
        {
            $share_img_name = time() . $proId . $versin . '_share.jpg';
        }
        else
        {
            $share_img_name = time() . $proId . $versin . '_share.jpg';
        }

        $share_img_path = 'image/product_share_img/' . $share_img_name;

        $share = Lang('getcode.7');
        $see = Lang('getcode.8');
        $company = Lang('getcode.9');

        $image
            ->water($goods_img, $goods_local)//商品图片
            ->water($path_2, $qrcode_local)//二维码
            ->water($head_img, array(30, 580))//头像
            ->text($priceStr, $font1, 26, $red, $priceStr_local)
            ->text($see, $font2, 17, $hui, $text1_local)
            ->text($company, $font1, 15, $hui, $text2_local)
            ->text("www.laiketui.com", $font2, 15, $hui, $text3_local)
            ->text($user_name . "  " . $share, $font2, 18, $black, $text4_local);

        //log
        if (!empty($log_res))
        {
            $logg = ServerPath::getimgpath($log_res[0]['logo'], $store_id);
            $loggg = $this->downImgUrl($logg);
            $log_type = $this->getImageType_text($loggg);
            $logo_name = "../public/image/product_share_img/" . uniqid() . mt_rand(1, 200) . ".$log_type";
            $logo_img = \think\Image::open($loggg);
            $logo_img->thumb(50, 50, \think\Image::THUMB_FIXED)->save($logo_name);
            $image->water($logo_name, array(15, 15));//logo
            unlink($logo_name);
        }

        $str_arr = $this->autoLineSplit($product_title, $font2, 20, 'utf8', 300);
        $font_y = 610;
        $time = 0;
        for ($i = 0; $i < count($str_arr); $i++)
        {
            $font_y = $font_y + 40;
            $image->text($str_arr[$time], $font2, 20, $black, array(30, $font_y));
            $time++;
        }
        $image->save($share_img_path, 'jpg');
        unlink($qrcode_path);
        unlink($goods_img);
        unlink($head_img);
        $resArr = array('code' => 200, 'imgUrl' => $share_img_path);
        return output(200, '',$resArr);
    }

    //new商品分享
    public function rqCodeInfo()
    {
        $store_id = trim($this->request->param('store_id'));
        $language = trim($this->request->post('language')); // 来源

        $store_type = trim($this->request->param('store_type'));
        $proId = trim($this->request->post('proId'));
        $attr_id = trim($this->request->post('attr_id'));
        $order_no = trim($this->request->post('order_no'));
        $type = trim($this->request->post('type'));
        $access_id = trim($this->request->post('access_id'));
        $href = trim($this->request->post('href'));
        $path = trim($this->request->post('path'));
        $share_type = intval($this->request->post('share_type'));

        file_put_contents('../public/image/product_share_img/1log.txt', '开始', FILE_APPEND);

        $url = $path;
        $pic_size = 3;
        $r = ConfigModel::where('store_id',$store_id)->select()->toArray();
        if ($r) 
        {
            if (!empty($r[0]['H5_domain']) && $store_type != 1 )
            {
                $url = $r[0]['H5_domain'] . $url;
                // URL 规范化处理
                $url = $this->normalizeUrl($url);
            }

            //生成二维码
            $qrcode_path = $this->get_qrcode1($url, '../public/image/product_share_img', $pic_size);
            $watermarkName = $r[0]['watermark_name']; //水印名称
            $watermarkUrl = $r[0]['watermark_url']; //水印网址
        }
        if ($store_type == 1)
        {
            //微信小程序
            $r = PaymentConfigModel::where(['pid'=>5,'store_id'=>$store_id])->select()->toArray();
            $value = $r ? $r[0] : [];
            if ($value)
            {
                $list = json_decode($value['config_data']);
                $appid = $list->appid;
                $appsecret = $list->appsecret;
            }
            $qrcode_path = $this->get_mini_code($url, $appid, $appsecret);
        }
        if ($store_type == 3)
        {
            //支付宝小程序
//            $url = $url_;
//            $qrcode_path = $this->get_qrcode2($store_id, $url, $query_param, '../public/image/product_share_img', 5);

//            $qrcode_path = $this->upimg($qrcode_path, 200);
        }

        $user_name = Db::name('user')->where(['store_id'=>$store_id,'access_id'=>$access_id])->value('user_name');//用户昵称 $this->user_list['user_name'];
        $head_img_url = Db::name('user')->where(['store_id'=>$store_id,'access_id'=>$access_id])->value('headimgurl');//用户昵称 $this->user_list['user_name'];
        // $head_img_url = $this->user_list['headimgurl'];// . "?x-oss-process=image/resize,m_fixed,h_50,w_50/circle,r_100/format,png";
        //合成图片

        $wer = $_SERVER['REQUEST_SCHEME'] . '://' . $_SERVER['HTTP_HOST'] . '/';

        // 底图
        $path_1 = '../public/image/product_share_img/bg.jpg';
        // 二维码
        $path_2 = $qrcode_path;
        $path_2 = $this->upimg($path_2, 150);

        // //获取根目录
        // $end_res = ThirdModel::select()->toArray();
        // if($end_res)
        // {
        //     $endurl = $end_res[0]['endurl'];
        // }
        // else
        // {
        //     $message = Lang('getcode.0');
        //     return output(ERROR_CODE_SPBCZ, $message);
        // }
        $qrCode = $wer.str_replace('../public/','',$path_2);
        unlink($qrcode_path);
        $resArr = array('qrCode' => $qrCode,'userHeadUrl'=>$head_img_url,'userName'=>$user_name,'watermarkName'=>$watermarkName,'watermarkUrl'=>$watermarkUrl);
        $message = Lang('Success');
        return output("200", $message,$resArr);
    }

    /**
     * URL 规范化处理
     */
    private function normalizeUrl($url)
    {
        // 处理 /#/ 多个斜杠的问题
        $url = preg_replace('/(\/#)\/+/', '$1/', $url);

        // 可选：处理其他可能的重复斜杠
        $url = preg_replace('/([^:])\/\/+/', '$1/', $url);

        return $url;
    }

    /**
     *
     * @param $picname
     * @return resource|null
     */
    function getImageType($picname)
    {
        $ename = getimagesize($picname);
        $ename = explode('/', $ename['mime']);
        $ext = $ename[1];
        $image = null;
        switch ($ext)
        {
            case "png":
                $image = imagecreatefrompng($picname);
                break;
            case "jpeg":
                $image = imagecreatefromjpeg($picname);
                break;
            case "jpg":
                $image = imagecreatefromjpeg($picname);
                break;
            case "gif":
                $image = imagecreatefromgif($picname);
                break;
        }
        return $image;
    }

    /**
     *
     * @param $picname
     * @return resource|null
     */
    function getImageType_text($picname)
    {
        $ename = getimagesize($picname);
        $ename = explode('/', $ename['mime']);
        $ext = $ename[1];
        $image = null;
        return $ext;
    }


    /**
     * 字符串换行
     * @param $str
     * @param $fontFamily
     * @param $fontSize
     * @param $charset
     * @param $width
     * @return array
     */
    function autoLineSplit($str, $fontFamily, $fontSize, $charset, $width)
    {
        $result = array();

        $len = (strlen($str) + mb_strlen($str, $charset)) / 2;
        // echo $len; exit;
        // 计算总占宽
        $dimensions = imagettfbbox($fontSize, 0, $fontFamily, $str);
        $textWidth = abs($dimensions[4] - $dimensions[0]);

        // 计算每个字符的长度
        $singleW = $textWidth / $len;
        // 计算每行最多容纳多少个字符
        $maxCount = floor($width / $singleW);

        while ($len > $maxCount)
        {
            // 成功取得一行
            $result[] = mb_strimwidth($str, 0, $maxCount, '', $charset);
            // 移除上一行的字符
            $str = str_replace($result[count($result) - 1], '', $str);
            // 重新计算长度
            $len = (strlen($str) + mb_strlen($str, $charset)) / 2;
        }
        // 最后一行在循环结束时执行
        $result[] = $str;

        return $result;
    }

    /**
     * 下载网络图片
     * @param $imgurl
     * @return string
     */
    function downImgUrl($imgurl, $type = "png")
    {

        // 获取头像
        $imgPath = "../public/image/product_share_img/" . uniqid() . mt_rand(1, 200) . ".$type";

        //合成头像
        $ch = curl_init($imgurl);
        curl_setopt($ch, CURLOPT_HEADER, 0);
        curl_setopt($ch, CURLOPT_REFERER, '');
        curl_setopt($ch, CURLOPT_NOBODY, 0);
        curl_setopt($ch, CURLOPT_TIMEOUT, 10);   //只需要设置一个秒的数量就可以
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
        curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, false);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
        $img = curl_exec($ch);
        curl_close($ch);
        file_put_contents($imgPath, $img);
        return $imgPath;
        exit;
    }

    function base64EncodeImage($image_file)
    {
        $base64_image = '';
        $image_info = getimagesize($image_file);
        $image_data = fread(fopen($image_file, 'r'), filesize($image_file));
        $base64_image = 'data:' . $image_info['mime'] . ';base64,' . chunk_split(base64_encode($image_data));
        return $base64_image;
    }

    // 获得二维码，并储存到本地
    public function get_qrcode1($url, $uploadImg, $size = 5)
    {
        $qrcode_name = md5(time() . rand(1000, 9999));
        $value = str_replace("&amp;", "&", $url);                  //二维码内容
        $errorCorrectionLevel = 'L';    //容错级别
        $matrixPointSize = $size;           //生成图片大小
        $filename = $uploadImg . '/' . $qrcode_name . '.png';

        app('QRcode')::png($value, $filename, $errorCorrectionLevel, $matrixPointSize, 2);

        return $filename;
    }

    // 获得二维码，并储存到本地
    public function get_qrcode2($store_id, $url, $query_param, $uploadImg, $size = 5)
    {
        $appid = $this->config_data->appid;
        $data['$store_id'] = $store_id;
        $data['$url'] = $url;
        $data['$query_param'] = $query_param;
        $data['$uploadImg'] = $uploadImg;
        // echo  json_encode($data);exit;
        $code_url = TestImage::getcode($url, $query_param, $uploadImg, $store_id, '分销推广二维码');

        $img = $this->downImgUrl($code_url . 'jpg');

        return $img;
    }

}